drop FUNCTION public.populate_least_cost_strategy_detailed_result(integer, 
	integer, 
	integer, 
	integer,
	double precision);
	
CREATE OR REPLACE FUNCTION public.populate_least_cost_strategy_detailed_result(int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer,
	domain_wide_emis_reduction double precision) RETURNS integer AS $$
DECLARE
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	worksheet_dataset_id integer := null;
	worksheet_table_name varchar(64) := '';
	costcurve_dataset_id integer := null;
	costcurve_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant varchar;
	county_dataset_filter_sql text := '';
	inventory_year integer := null;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	gimme_count integer := 0;
	marginal double precision;
	emis_reduction double precision;
	record_id double precision;
	apply_order integer;
	counter integer := 0;
	record_count integer := 0;
	deleted_record_count integer := 0;
	increasing_trend boolean := false;
	prev_apply_order integer;
	uncontrolled_emis double precision;
	emis_sql character varying;
--	random_number double precision := random();

	--support for flat file ds types...
	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'fips';
	is_flat_file_inventory boolean := false;
	is_flat_file_point_inventory boolean := false;
BEGIN
--	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';
--	SET enable_nestloop TO 'on';

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	--get dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = input_dataset_id
	into dataset_type_name;

	--if Flat File 2010 Types then change primary key field expression variables...
	IF dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		fips_expression := 'region_cd';
		is_flat_file_inventory := true;
		IF dataset_type_name = 'Flat File 2010 Point' THEN
			is_flat_file_point_inventory := true;
		END IF;
	ELSE
		fips_expression := 'fips';
	END If;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

	-- get worksheet table info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
		inner join emf.strategy_result_types srt
		on srt.id = sr.strategy_result_type_id
	where sr.control_strategy_id = int_control_strategy_id 
		and srt.name = 'Least Cost Control Measure Worksheet'
	into worksheet_dataset_id,
		worksheet_table_name;

	-- get cost curve summary table info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
		inner join emf.strategy_result_types srt
		on srt.id = sr.strategy_result_type_id
	where sr.control_strategy_id = int_control_strategy_id 
		and srt.name = 'Least Cost Curve Summary'
	into costcurve_dataset_id,
		costcurve_table_name;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.name,
		cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO strategy_name,
		target_pollutant_id,
		inv_filter,
		inventory_year,
		county_dataset_id,
		county_dataset_version;

	-- get target pollutant name
	select name
	from emf.pollutants
	where id = target_pollutant_id
	into target_pollutant;

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
	into no_days_in_month;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.' || fips_expression || ' in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	IF NOT is_flat_file_inventory THEN
		emis_sql := public.get_ann_emis_expression('inv', no_days_in_month);
	ELSE
		emis_sql := 'inv.ann_value';
	END IF;

	-- get the target pollutant uncontrolled emission 
	execute 'select sum(' || emis_sql || ') 
		FROM emissions.' || inv_table_name || ' as inv
		where ' || inv_filter || county_dataset_filter_sql || '
			and poll = ' || quote_literal(target_pollutant)
	into uncontrolled_emis;

	-- 
	execute 'update emissions.' || worksheet_table_name || '
				set status = null::integer
				where status = 1';

	execute 'select count(1) from emissions.' || worksheet_table_name || ' where status is null and poll = ' || quote_literal(target_pollutant)
	into record_count;
	record_count := coalesce(record_count, 0);
	raise notice '%', 'emissions.' || worksheet_table_name || ' count = ' || record_count || ' - ' || clock_timestamp();

	-- lets figure out what maximum emission reduction, is possible
	select public.get_least_cost_worksheet_emis_reduction(worksheet_table_name, target_pollutant, record_count)
	into emis_reduction;
/*	execute 'SELECT sum(emis_reduction)
		from (
			SELECT distinct on (source, original_dataset_id, source_id) 
				emis_reduction
			FROM emissions.' || worksheet_table_name || '
			where status is null 
				and poll = ' || quote_literal(target_pollutant) || '
			ORDER BY source, original_dataset_id, source_id, emis_reduction desc
		) tbl'
	into emis_reduction;*/
	get diagnostics gimme_count = row_count;
	raise notice '%', ' figure out what maximum emission reduction, is possible, emis_reduction = ' || emis_reduction || ' - ' || clock_timestamp();

	IF domain_wide_emis_reduction < emis_reduction THEN

		apply_order := public.narrow_in_on_least_cost_target(worksheet_table_name, 
			target_pollutant, 
			domain_wide_emis_reduction);

	ELSE
		raise notice '%', 'the reduction could not be met. return all controllled sources. row_count = ' || record_count || ', emis_reduction = ' || emis_reduction || ' - ' || clock_timestamp();
		apply_order := record_count;
	END IF;

	raise notice '%', 'narrow in on least cost target. apply_order = ' || apply_order || ', emis_reduction = ' || emis_reduction || ' - ' || clock_timestamp();

	execute 'update emissions.' || worksheet_table_name || '
		set status = 1
		where status is null 
		and record_id not in 
		(
			-- get sources measure for target and cobenefit pollutants
			SELECT ap.record_id
			FROM emissions.' || worksheet_table_name || ' ap
				inner join (
					-- get sources measure for tp
					SELECT distinct on (source, original_dataset_id, source_id) source, original_dataset_id, cm_id
					from (
						SELECT emis_reduction, marginal, original_dataset_id, record_id, source, source_id, source_poll_cnt, cm_id
						FROM emissions.' || worksheet_table_name || '
						where status is null 
							and poll = ' || quote_literal(target_pollutant) || '
						ORDER BY marginal, emis_reduction desc, source_poll_cnt desc, record_id
--						limit ' || (apply_order) || '
						limit ' || (apply_order + 1) || '
					) tbl
					ORDER BY source, original_dataset_id, source_id, marginal, emis_reduction desc, source_poll_cnt desc, record_id
--					source, marginal, emis_reduction desc, source_poll_cnt desc, record_id
				) tp
				on tp.source = ap.source
				and coalesce(tp.original_dataset_id,0) = coalesce(ap.original_dataset_id,0)
				and tp.cm_id = ap.cm_id
		)';
--		and apply_order <= ' || (apply_order + 1);
	get diagnostics deleted_record_count = row_count;
	raise notice '%', ' get rid of lesser effective controls, can only have one per source - count = ' || deleted_record_count || ' - ' || clock_timestamp();

	execute 'update emissions.' || worksheet_table_name || ' as detailed_result
	set apply_order = tbl.apply_order,
		cum_emis_reduction = tbl.cum_emis_reduction,
		cum_annual_cost = tbl.cum_annual_cost
	from (
		SELECT record_id, 
			apply_order, 
			cum_emis_reduction, 
			cum_annual_cost

		from (
			SELECT marginal, emis_reduction, record_id, source_annual_cost, source_poll_cnt, 1::integer as cnt --sum(emis_reduction)
			,	sum(1) over w as apply_order, 
				sum(emis_reduction) over w as cum_emis_reduction, 
				sum(source_annual_cost) over w as cum_annual_cost

			from emissions.' || worksheet_table_name || '
			where status is null 
				and poll = ' || quote_literal(target_pollutant) || '

			WINDOW w AS (ORDER BY marginal, emis_reduction desc, source_poll_cnt desc, record_id)

			order by marginal, emis_reduction desc, source_poll_cnt desc, record_id
--			limit ' || (apply_order) || '
			limit ' || (apply_order + 1) || '
		) tbl order by marginal, emis_reduction desc, source_poll_cnt desc, record_id
	) tbl
	where tbl.record_id = detailed_result.record_id';

	raise notice '%', 'update apply order, cum cost and red ' || coalesce(apply_order,0)  || ' - ' || clock_timestamp();

	execute  '/*truncate emissions.'|| detailed_result_table_name || ';*/
		insert into emissions.'|| detailed_result_table_name || '
		(
		dataset_id,
		cm_abbrev,
		poll,
		scc,
		fips,
		plantid, pointid, stackid, segment, 
		annual_oper_maint_cost,
		annual_variable_oper_maint_cost,
		annual_fixed_oper_maint_cost,
		annualized_capital_cost,
		total_capital_cost,
		annual_cost,
		ann_cost_per_ton,
		annualized_capital_cost_3pct,
		annual_cost_3pct,
		ann_cost_per_ton_3pct,
		control_eff,
		rule_pen,
		rule_eff,
		percent_reduction,
		final_emissions,
		control_ids,
		Inv_Ctrl_Eff,
		Inv_Rule_Pen,
		Inv_Rule_Eff,
		emis_reduction,
		inv_emissions,
		apply_order,
		input_emis,
		output_emis,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		input_ds_id,
		cs_id,
		cm_id,
		original_dataset_id,
		equation_type,
		sector,
		xloc,
		yloc,
		plant,
		REPLACEMENT_ADDON,
		EXISTING_MEASURE_ABBREVIATION,
		EXISTING_PRIMARY_DEVICE_TYPE_CODE,
		strategy_name,
		control_technology,
		source_group,
		county_name,
		state_name,
		scc_l1,
		scc_l2,
		scc_l3,
		scc_l4
		) 
	select 	
		' || detailed_result_dataset_id || '::integer,
		cm_abbrev,
		poll,
		scc,
		fips,
		plantid, pointid, stackid, segment,
		annual_oper_maint_cost as operation_maintenance_cost,
		annual_variable_oper_maint_cost,
		annual_fixed_oper_maint_cost,
		annualized_capital_cost as annualized_capital_cost,
		total_capital_cost as capital_cost,
		annual_cost as ann_cost,
		ann_cost_per_ton as computed_cost_per_ton,
		annualized_capital_cost_3pct,
		annual_cost_3pct,
		ann_cost_per_ton_3pct,
		control_eff as efficiency,
		rule_pen as rule_pen,
		rule_eff as rule_eff,
		percent_reduction as percent_reduction,
		final_emissions,
		control_ids,
		Inv_Ctrl_Eff,
		Inv_Rule_Pen,
		Inv_Rule_Eff,
		emis_reduction,
		inv_emissions,
		apply_order,
		input_emis,
		output_emis,
		substr(fips, 1, 2),
		substr(fips, 3, 3),
		sic,
		naics,
		source_id,
		' || input_dataset_id || '::integer,
		' || int_control_strategy_id || '::integer,
		cm_id,
		original_dataset_id,
		equation_type,
		sector,
		xloc,
		yloc,
		plant,
		REPLACEMENT_ADDON,
		EXISTING_MEASURE_ABBREVIATION,
		EXISTING_PRIMARY_DEVICE_TYPE_CODE,
		' || quote_literal(strategy_name) || ' as strategy_name,
		tbl.control_technology,
		tbl.source_group,
		county_name,
		state_name,
		scc_l1,
		scc_l2,
		scc_l3,
		scc_l4

	from (


				--make sure and keep target and cobenefit pollutant records...
				SELECT 
					ap.*
				FROM emissions.' || worksheet_table_name || ' tp
					inner join emissions.' || worksheet_table_name || ' ap
					on ap.source = tp.source
					and ap.cm_id = tp.cm_id
				where tp.status is null 
					and ap.status is null 
--					and tp.apply_order <= ' || coalesce(apply_order, record_count) || '
					and tp.apply_order <= ' || coalesce(apply_order + 1, record_count) || '
					and tp.poll = ' || quote_literal(target_pollutant) || '
				ORDER BY ap.source, ap.poll
	) as tbl
	
	inner join emf.control_measures cm
	on cm.id = tbl.cm_id
	
	order by apply_order';
	raise notice '%', 'populate the detailed result with the relevant results - ' || clock_timestamp();
--order by marginal, emis_reduction desc, record_id
--coalesce(marginal,0) <= ' || marginal || '
--		and (coalesce(marginal,0) < ' || marginal || ' or (coalesce(marginal,0) = ' || marginal || ' and (emis_reduction < ' || emis_reduction || ' or (emis_reduction = ' || emis_reduction || ' and record_id >= ' || record_id || '))))

	IF coalesce(costcurve_table_name, '') <> '' THEN
		execute 'insert into emissions.'|| costcurve_table_name || ' 
				(dataset_id,
				poll, 
				total_annual_cost, 
				average_ann_cost_per_ton, 
				Total_Annual_Oper_Maint_Cost,
				Total_Annualized_Capital_Cost,
				Total_Capital_Cost,
				Target_Percent_Reduction,
				Actual_Percent_Reduction,
				Total_Emis_Reduction,
				Uncontrolled_Emis
				) 
			select ' || costcurve_dataset_id || ',
				dr.poll, 
				sum(dr.annual_cost), 
				sum(dr.annual_cost) / sum(dr.emis_reduction), 
				sum(dr.annual_oper_maint_cost), 
				sum(dr.annualized_capital_cost), 
				sum(dr.total_capital_cost), 
				case when dr.poll = ' || quote_literal(target_pollutant) || ' then ' || domain_wide_emis_reduction || ' / ' || uncontrolled_emis || ' * 100 else null::double precision end, 
				case when dr.poll = ' || quote_literal(target_pollutant) || ' then sum(dr.emis_reduction) / ' || uncontrolled_emis || ' * 100 else null::double precision end, 
				sum(dr.emis_reduction), 
				case when dr.poll = ' || quote_literal(target_pollutant) || ' then ' || uncontrolled_emis || ' else null::double precision end
			from emissions.'|| detailed_result_table_name || ' dr
			where dr.poll = ' || quote_literal(target_pollutant) || '
			group by dr.poll';
	--			group by ' || costcurve_dataset_id || ', poll, ' || domain_wide_emis_reduction || ' / ' || uncontrolled_emis

		raise notice '%', 'populate the cost curve with the relevant results - ' || clock_timestamp();
	END IF;
	
	execute 'select count(1) from emissions.' || worksheet_table_name || ' where status is null'
	into gimme_count;
	raise notice '%', 'emissions.' || worksheet_table_name || ' table = ' || gimme_count || ' - ' || clock_timestamp();
	raise notice '%', 'emissions.' || detailed_result_table_name || ' - ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;

--SELECT * from public.populate_least_cost_strategy_detailed_result(65, 1395, 0, 1944, 3396.958456::double precision);	

