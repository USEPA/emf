DROP FUNCTION run_annotate_inventory(integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.run_annotate_inventory(
	int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer
	) RETURNS void AS
$BODY$
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
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	has_constraints boolean := null;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	annualized_emis_sql character varying;
	annual_emis_sql character varying;
	percent_reduction_sql character varying;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	creator_user_id integer := 0;
	is_cost_su boolean := false; 
BEGIN
--	SET work_mem TO '256MB';

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

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count, 
		measure_with_region_count;

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

	-- see if there is a cpri column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is a primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');


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

	-- if strategy have measures, then store these in a temp table for later use...
	IF measure_with_region_count > 0 THEN
		EXECUTE '
			CREATE TEMP TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL) ON COMMIT DROP;
			CREATE TEMP TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL) ON COMMIT DROP;

--			CREATE TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL);
--			CREATE TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL);

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

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'start ' || clock_timestamp();

--	annual_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end;
--	annualized_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end;
	annual_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end;
	annualized_emis_sql := 
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
			' || annual_emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(chained_gdp as double precision), 
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

	-- build insert column list and select column list
	FOR region IN EXECUTE 
		'SELECT a.attname
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = ' || quote_literal(inv_table_name) || '
		AND a.attnum > 0'
	LOOP
		column_name := region.attname;
		
		IF column_name = 'record_id' THEN
--			select_column_list_sql := select_column_list_sql || 'inv.record_id';
--			insert_column_list_sql := insert_column_list_sql || column_name;
		ELSIF column_name = 'dataset_id' THEN
			select_column_list_sql := select_column_list_sql || '' || detailed_result_dataset_id || ' as dataset_id';
			insert_column_list_sql := insert_column_list_sql || '' || column_name;
		ELSIF column_name = 'delete_versions' THEN
			select_column_list_sql := select_column_list_sql || ','''' as delete_versions';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'version' THEN
			select_column_list_sql := select_column_list_sql || ',0 as version';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'control_measures' THEN
			select_column_list_sql := select_column_list_sql || ', case when control_measures is null or length(control_measures) = 0 then abbreviation else control_measures || ''&'' || abbreviation end as control_measures';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
			has_control_measures_col := true;
		ELSIF column_name = 'pct_reduction' THEN
			select_column_list_sql := select_column_list_sql || ', case when pct_reduction is null or length(pct_reduction) = 0 then (inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ' * 100)::text else pct_reduction || ''&'' || (inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ' * 100)::text end as pct_reduction';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
			has_pct_reduction_col := true;
		ELSE
			select_column_list_sql := select_column_list_sql || ',' || column_name;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		END IF;
	
	END LOOP;

	IF has_control_measures_col = false or has_pct_reduction_col = false THEN 
		IF has_control_measures_col = false THEN
			select_column_list_sql := select_column_list_sql || ', abbreviation as control_measures';
			insert_column_list_sql := insert_column_list_sql || ',control_measures';
		END IF;
		IF has_pct_reduction_col = false THEN
			select_column_list_sql := select_column_list_sql || ', (inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ' * 100)::text as pct_reduction';
			insert_column_list_sql := insert_column_list_sql || ',pct_reduction';
		END IF;
	END IF;


	execute 'insert into emissions.' || detailed_result_table_name || ' 
		(
		' || insert_column_list_sql || ' 
		)
	select 
		' || select_column_list_sql || ' 
	FROM emissions.' || inv_table_name || ' inv

		left outer join (
			-- second pass, gets rid of ties for a paticular source
			select DISTINCT ON (inv.record_id) 
				inv.record_id,
				m.abbreviation,
				er.efficiency

			FROM emissions.' || inv_table_name || ' inv

				inner join emf.pollutants p
				on p.name = inv.poll
				
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

				left outer join emf.control_measure_nei_devices cmnd
				on cmnd.control_measure_id = m.id
				'  || 
				case 
					when has_cpri_column or has_primary_device_type_code_column then 
						case 
							when has_cpri_column then 'and cmnd.nei_device_code = inv.cpri::integer'
							else 'and cmnd.nei_device_code = inv.primary_device_type_code::integer'
						end
					else ''
				end || '
				
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
				and p.id = ' ||  target_pollutant_id || '
				and inv.ceff > 0.0
				-- measure region filter
				' || case when measure_with_region_count > 0 then '
				and 
				(
					csm.region_dataset_id is null 
					or
					(
						csm.region_dataset_id is not null 
						and exists (
							select 1
							from measures mr
								inner join measure_regions r
								on r.region_id = mr.region_id
								and r.region_version = mr.region_version
							where mr.control_measure_id = m.id
								and r.fips = inv.fips
						)
						and exists (
							select 1 
							from measures mr
							where mr.control_measure_id = m.id
						)
					)
				)					
				' else '' end || '
				-- constraints filter
				' || case when has_constraints then '
				and (
						' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
						' || coalesce(' and ' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' >= ' || min_emis_reduction_constraint, '')  || '
						' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || '
							* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
						' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' * ' || chained_gdp_adjustment_factor || '
							* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
				)' else '' end || '

			order by inv.record_id,
				case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
				case when cmnd.id is not null then 0 else 1 end,
				abs(inv.ceff - er.efficiency)
		) tbl
		on tbl.record_id = inv.record_id
		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			and inv.ceff > 0.0
		order by inv.record_id';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
