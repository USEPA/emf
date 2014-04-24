DROP FUNCTION public.run_apply_measures_in_series_strategy(integer, integer, integer, integer);
DROP FUNCTION public.run_apply_measures_in_series_strategy_finalize(integer, integer, integer, integer);


CREATE OR REPLACE FUNCTION public.run_apply_measures_in_series_strategy(int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id int) RETURNS integer AS $$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	measures_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_rpen_column boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	run_status character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	annualized_emis_sql character varying;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	inventory_sectors character varying := '';
	creator_user_id integer := 0;
	is_cost_su boolean := false; 
BEGIN
--	SET work_mem TO '512MB';
--	SET enable_seqscan TO 'off';

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = input_dataset_id
	into inventory_sectors;

	-- see if control strategy has only certain measures specified
	SELECT count(id)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = int_control_strategy_id
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100,
		cs.creator_id
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		creator_user_id;

	-- see if strategyt creator is a CoST SU
	SELECT 
		case 
			when 
				strpos('|' 
				|| (select p.value from emf.properties p where p.name = 'COST_SU') 
				|| '|', '|' || u.username || '|') > 0 then true 
			else false 
		end
	FROM emf.users u
	where u.id = creator_user_id
	INTO is_cost_su;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is lat & long columns in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'start call_batch_state ' || clock_timestamp();
	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	IF dataset_month = 1 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 2 THEN
		no_days_in_month := 29;
	ELSIF dataset_month = 3 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 4 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 5 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 6 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 7 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 8 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 9 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 10 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 11 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 12 THEN
		no_days_in_month := 31;
	END IF;

	raise notice '%', 'start ' || clock_timestamp();

	-- get gdp chained values
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = cost_year
	INTO cost_year_chained_gdp;
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = ref_cost_year
	INTO ref_cost_year_chained_gdp;

	chained_gdp_adjustment_factor := cost_year_chained_gdp / ref_cost_year_chained_gdp;

	uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end;
	emis_sql := 
			case 
				when dataset_month != 0 then 
					'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' 
				else 
					'inv.ann_emis' 
			end;
	annualized_emis_sql := 
			case 
				when dataset_month != 0 then 
					'coalesce(inv.avd_emis * 365, inv.ann_emis)'
				else 
					'inv.ann_emis'
			end;
	annualized_uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;
	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.control_measures_id, 
			abbreviation, ' || discount_rate|| ', 
			m.equipment_life, er.cap_ann_ratio, 
			er.cap_rec_factor, er.ref_yr_cost_per_ton, 
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision), 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null' else 'inv.stkflow * 60.0' end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity' end end || ', 
			' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_numerator' end end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_denominator' end end 
			else
			'null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null'
			end
			|| ',inv.ceff, ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton))';
	get_strategt_cost_inner_sql := replace(get_strategt_cost_sql,'m.control_measures_id','m.id');


--	EXECUTE 
	EXECUTE 
	--raise notice '%',
	'insert into emissions.' || detailed_result_table_name || ' 
		(
		dataset_id,
		cm_abbrev,
		poll,
		scc,
		fips,
		' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '

		annual_oper_maint_cost,
		annualized_capital_cost,
		total_capital_cost,
		annual_cost,
		ann_cost_per_ton,
		control_eff,
		rule_pen,
		rule_eff,
		percent_reduction,
		inv_ctrl_eff,
		inv_rule_pen,
		inv_rule_eff,

		final_emissions,
		emis_reduction,
		inv_emissions,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		input_ds_id,
		cs_id,
		cm_id,
		xloc,
		yloc,
		plant,
		sector,
		equation_type
		)
	select 	' || detailed_result_dataset_id || '::integer,
		abbreviation,
		poll,
		scc,
		fips,
		' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '

		operation_maintenance_cost,
		annualized_capital_cost,
		capital_cost,
		ann_cost,
		computed_cost_per_ton,
		efficiency,
		rule_pen,
		rule_eff,
		percent_reduction,
		ceff,
		rpen,
		reff,

		final_emissions,
		emis_reduction,
		inv_emissions,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		' || input_dataset_id || '::integer,
		' || int_control_strategy_id || '::integer,
		cm_id,
		xloc,
		yloc,
		plant,
		' || quote_literal(inventory_sectors) || ' as sector,
		equation_type
	from (
		select DISTINCT ON (inv.scc, inv.fips, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id, er.control_measures_id) 
			m.abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '

			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.operation_maintenance_cost as operation_maintenance_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.annualized_capital_cost as annualized_capital_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.capital_cost as capital_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.annual_cost as ann_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton as computed_cost_per_ton,
			er.efficiency as efficiency,
			' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
			' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
			' || percent_reduction_sql || ' as percent_reduction,
			inv.ceff,
			' || case when is_point_table = false then 'inv.rpen' else '100' end || ' as rpen,
			inv.reff,

			' || emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100 as emis_reduction,
			' || emis_sql || ' as inv_emissions,

			substr(inv.fips, 1, 2) as fipsst,
			substr(inv.fips, 3, 3) as fipscty,
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ' as sic,
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ' as naics,
			inv.record_id::integer as source_id,
			er.control_measures_id as cm_id,
			' || get_strategt_cost_inner_sql || '.actual_equation_type as equation_type,
			' || case when measures_count > 0 then 'csm.apply_order ' else '1.0' end || ' as apply_order,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || '
		FROM emissions.' || inv_table_name || ' inv

			inner join emf.pollutants p
			on p.name = inv.poll

			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

			inner join emf.control_measure_sccs scc
			on scc.name = inv.scc

			' || case when measures_count > 0 then '
			inner join emf.control_strategy_measures csm
			on csm.control_measure_id = scc.control_measures_id
			and csm.control_strategy_id = ' || int_control_strategy_id || '
			' else '' end || '

			inner join emf.control_measures m
			on m.id = scc.control_measures_id

-- for Non CoST SUs, make sure they only see their temporary measures
			' || case when not is_cost_su then '

			inner join emf.control_measure_classes cmc
			on cmc.id = m.cm_class_id
			and (
				(cmc.name = ''Temporary'' and m.creator = ' || creator_user_id || ')
				or (cmc.name <> ''Temporary'')
			)
			' else '' end || '

			inner join emf.control_measure_months ms
			on ms.control_measure_id = m.id
			and ms.month in (0' || case when dataset_month != 0 then ',' || dataset_month else '' end || ')

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

			inner join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = scc.control_measures_id
			-- pollutant filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and ' || annualized_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)

			left outer join reference.gdplev gdplev_incr
			on gdplev_incr.annual = er.cost_year

			' || case when measures_count = 0 and measure_classes_count > 0 then '
			inner join emf.control_strategy_classes csc
			on csc.control_measure_class_id = m.cm_class_id
			and csc.control_strategy_id = ' || int_control_strategy_id || '
			' else '' end || '

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

		order by inv.scc, inv.fips, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id, 
			er.control_measures_id, case when length(locale) = 5 then 0 when length(locale) = 2 then 1 else 2 end, coalesce(' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, 0), ' || percent_reduction_sql || ' desc
		) as tbl
	order by scc, fips, ' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || 'poll, apply_order, coalesce(computed_cost_per_ton, 0), percent_reduction desc';
	


	raise notice '%', 'end call_batch_state ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION public.run_apply_measures_in_series_strategy_finalize(int_control_strategy_id integer, input_dataset_id integer, 
	input_dataset_version integer, strategy_result_id int) RETURNS integer AS $$
DECLARE
	inv_table_name varchar(64) := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant varchar(255) := '';
	measure_with_region_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	has_constraints boolean := null;
	cost_year integer := null;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	is_point_table boolean := false;
	gimme_count integer := 0;
BEGIN
--	SET work_mem TO '512MB';
--	SET enable_seqscan TO 'off';

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the detailed result dataset info
	select lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_table_name;

	-- see if control strategy has only certain measures specified
	SELECT count(1)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
		and control_strategy_measures.region_dataset_id is not null
	INTO measure_with_region_count;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		cs.cost_year
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO target_pollutant_id,
		cost_year;
	
	select p.name
	FROM emf.pollutants p
	where p.id = target_pollutant_id
	INTO target_pollutant;

	-- get gdp chained values
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = cost_year
	INTO cost_year_chained_gdp;
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = ref_cost_year
	INTO ref_cost_year_chained_gdp;

	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = int_control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint;

	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

	-- see if there are point specific columns to be indexed
	SELECT count(1) = 4
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname in ('plantid','pointid','stackid','segment')
		AND a.attnum > 0
	into is_point_table;

	-- if strategy have measures, then store these in a temp table for later use...
	IF measure_with_region_count > 0 THEN
		EXECUTE '
			CREATE TEMP TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL) ON COMMIT DROP;
			CREATE TEMP TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL) ON COMMIT DROP;

			CREATE INDEX measure_regions_measure_id ON measures USING btree (control_measure_id);
			CREATE INDEX measure_regions_region ON measures USING btree (region_id, region_version);
			CREATE INDEX regions_region ON measure_regions USING btree (region_id, region_version);
			CREATE INDEX regions_fips ON measure_regions USING btree (fips);';

		FOR region IN EXECUTE 
			'SELECT m.control_measure_id, i.table_name, m.region_dataset_id, m.region_dataset_version
			FROM emf.control_strategy_measures m
				inner join emf.internal_sources i
				on m.region_dataset_id = i.dataset_id
			where m.control_strategy_id = ' || int_control_strategy_id || '
				and m.region_dataset_id is not null'
		LOOP
			EXECUTE 'insert into measures (control_measure_id, region_id, region_version)
			SELECT ' || region.control_measure_id || ', ' || region.region_dataset_id || ', ' || region.region_dataset_version || ';';

			EXECUTE 'select count(1)
			from measure_regions
			where region_id = ' || region.region_dataset_id || '
				and region_version = ' || region.region_dataset_version || ''
			into gimme_count;

			IF gimme_count = 0 THEN
				EXECUTE 'insert into measure_regions (region_id, region_version, fips)
				SELECT ' || region.region_dataset_id || ', ' || region.region_dataset_version || ', fips
				FROM emissions.' || region.table_name || '
				where ' || public.build_version_where_filter(region.region_dataset_id, region.region_dataset_version);
			END IF;
		END LOOP;
	END IF;

	raise notice '%', 'start call_batch_state ' || clock_timestamp();

	-- get rid of sources that use measures not in there county, this is post step becuase using a join during the measure selection process seem to be slower.
	IF measure_with_region_count > 0 THEN
--		execute 'raise notice ''%'', ''get rid of sources that use measures not in there county - before count '' || (select count(1) from emissions.' || detailed_result_table_name || ') || '' '' || clock_timestamp();';
--		raise notice '%', 'get rid of sources that use measures not in there county - before count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();


		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv2
			where	not exists (
					select 1
					from measures mr
						inner join measure_regions r
						on r.region_id = mr.region_id
						and r.region_version = mr.region_version
					where mr.control_measure_id = inv2.cm_id
						and r.fips = inv2.fips
					)
				and exists (
					select 1 
					from measures m
					where m.control_measure_id = inv2.cm_id
					)';

/*
--different approaches that are slower...
		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv
			where not exists (
			select 1
			from emissions.' || detailed_result_table_name || ' inv2
				inner join measures mr
				on mr.control_measure_id = inv2.cm_id
				inner join measure_regions r
				on r.region_id = mr.region_id
				and r.region_version = mr.region_version
				and r.fips = inv2.fips
			where 	inv2.record_id = inv.record_id
			)
			and inv.cm_id in (select control_measure_id from measures)';

		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv
			where not exists (
			select 1
			from emissions.' || detailed_result_table_name || ' inv2
				inner join measure_region mr
				on mr.control_measure_id = inv2.cm_id
				and mr.fips = inv2.fips
			where 	inv2.record_id = inv.record_id
			)
			and inv.cm_id in (select distinct control_measure_id from measure_region)';


		EXECUTE	'delete from only emissions.' || detailed_result_table_name || '
				where record_id in (
					select record_id
					from emissions.' || detailed_result_table_name || ' as inv
						inner join emf.control_strategy_measures csm
						on csm.control_measure_id = inv.cm_id
						and csm.region_dataset_id is not null
						left outer join measure_region mr
						on mr.control_measure_id = inv.cm_id
						and mr.fips = inv.fips
					where mr.fips is null
						and csm.control_strategy_id = ' || int_control_strategy_id || ')
		';

		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv2
			where	not exists (
					select 1
					from measures mr
						inner join measure_regions r
						on r.region_id = mr.region_id
						and r.region_version = mr.region_version
					where mr.control_measure_id = inv2.cm_id
						and r.fips = inv2.fips
					)
				and exists (
					select 1 
					from measures m
					where m.control_measure_id = inv2.cm_id
					)';
*/

--		raise notice '%', 'after count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();
	END IF;

--	SET enable_seqscan TO 'on';
--	execute 'raise notice ''%'', ''readjust detailed result - count '' || (select count(1) from emissions.' || detailed_result_table_name || ') || '' '' || clock_timestamp();';

	-- update the detailed result
/*raise notice '%', 
	'update emissions.' || detailed_result_table_name || ' as inv
	set 	inv_emissions = case when (select min(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions else null end,
		final_emissions = case when (select max(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions * (select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) else null end,
		emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';
*/

	-- NOTE:   currently, the assumption is cost equations don't need to be used...and that the computed cpt calculated early should be sufficienct.
	-- discuss with alison...
	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = ' || cost_year_chained_gdp || ' / ' || ref_cost_year_chained_gdp || ' * inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id),
		input_emis = inv.inv_emissions * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		output_emis = inv.inv_emissions * (1 - inv.percent_reduction / 100) * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		final_emissions = null,
		ann_cost_per_ton = ' || cost_year_chained_gdp || ' / ' || ref_cost_year_chained_gdp || ' * ann_cost_per_ton';

	-- make sure we meet the constraints, if not get rid of the applicable measures...
	IF has_constraints THEN
--		raise notice '%', 'get rid of sources that use measures not in there county - before count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();
/*		execute 'create table emissions.noconstraints as 
			select scc, fips, cm_id, inv2.percent_reduction, inv2.poll, inv2.emis_reduction, inv2.ann_cost_per_ton, inv2.annual_cost
			from emissions.' || detailed_result_table_name || ' as inv2
			where inv2.poll = ' || quote_literal(target_pollutant) || '';
		execute 'create table emissions.constraints as 
			select scc, fips, cm_id, inv2.percent_reduction, inv2.poll, inv2.emis_reduction, inv2.ann_cost_per_ton, inv2.annual_cost
			from emissions.' || detailed_result_table_name || ' as inv2
			where inv2.poll = ' || quote_literal(target_pollutant) || '
				and (
					inv2.percent_reduction < ' || coalesce(min_control_efficiency_constraint, -100.0) || '
					' || coalesce(' or inv2.emis_reduction < ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' or inv2.ann_cost_per_ton > ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' or inv2.annual_cost > ' || max_ann_cost_constraint, '')  || '
				)';
*/
		execute 'delete from emissions.' || detailed_result_table_name || ' as inv
			using emissions.' || detailed_result_table_name || ' as inv2
			where not exists (select 1 
				from emissions.' || detailed_result_table_name || ' as inv2
				where inv2.scc = inv.scc
				and inv2.fips = inv.fips
				' || case when is_point_table = false then '' else '
				and inv2.plantid = inv.plantid
				and inv2.pointid = inv.pointid
				and inv2.stackid = inv.stackid
				and inv2.segment = inv.segment' end || '
				and inv2.cm_id = inv.cm_id
				and inv2.poll = ' || quote_literal(target_pollutant) || '
				and (
					inv2.percent_reduction >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
					' || coalesce(' and inv2.emis_reduction >= ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' and coalesce(inv2.ann_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' and coalesce(inv2.annual_cost, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
				))
				and exists (select 1 
					from emissions.' || detailed_result_table_name || ' as inv3
					where inv3.fips = inv.fips
						and inv3.scc = inv.scc
						' || case when is_point_table = false then '' else '
						and inv3.plantid = inv.plantid
						and inv3.pointid = inv.pointid
						and inv3.stackid = inv.stackid
						and inv3.segment = inv.segment' end || '
						and inv3.poll = ' || quote_literal(target_pollutant) || '
					)
				';
		-- update the apply order again, there are bound to be gaps...
		EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
		set 	apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';

	END IF;

/*	'inv_emissions = case when (select min(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions else null end,
		final_emissions = case when (select max(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions * (select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) else null end,
		emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';
*/
	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	inv_emissions = null 
	where apply_order > 1';

	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	final_emissions = (select min(output_emis) from emissions.' || detailed_result_table_name || ' where source_id = inv.source_id)
	where apply_order = 1 ';

	raise notice '%', 'end call_batch_state ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;

