DROP FUNCTION populate_max_emis_red_strategy_messages(integer, integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.populate_max_emis_red_strategy_messages(
	intControlStrategyId integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	message_strategy_result_id integer, 
	detailed_strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';

	strategy_detailed_result_dataset_id integer := null;
	strategy_detailed_result_table_name varchar(64) := '';

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
	has_mact_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_plant_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	column_name character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	sql character varying := '';
	has_control_measures_column boolean := false; 

	--support for flat file ds types...
	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'fips';
	plantid_expression character varying(64) := 'plantid';
	pointid_expression character varying(64) := 'pointid';
	stackid_expression character varying(64) := 'stackid';
	segment_expression character varying(64) := 'segment';
	is_flat_file_inventory boolean := false;
	is_flat_file_point_inventory boolean := false;
BEGIN

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
		plantid_expression := 'facility_id';
		pointid_expression := 'unit_id';
		stackid_expression := 'rel_point_id';
		segment_expression := 'process_id';
		is_flat_file_inventory := true;
		IF dataset_type_name = 'Flat File 2010 Point' THEN
			is_flat_file_point_inventory := true;
		END IF;
	ELSE
		fips_expression := 'fips';
		plantid_expression := 'plantid';
		pointid_expression := 'pointid';
		stackid_expression := 'stackid';
		segment_expression := 'segment';
	END If;

	-- get the strategy messages result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = message_strategy_result_id
	into strategy_messages_dataset_id,
		strategy_messages_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = detailed_strategy_result_id
	into strategy_detailed_result_dataset_id,
		strategy_detailed_result_table_name;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = intControlStrategyId 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = intControlStrategyId
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'a') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = intControlStrategyId
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');
	
	-- see if there is a mact column in the inventory
	has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',');

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

	-- see if there is plant column in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if there is control_measures column in the inventory
	has_control_measures_column := public.check_table_for_columns(inv_table_name, 'control_measures', ',');

	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = intControlStrategyId
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

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and a.' || fips_expression || ' in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'a'::character varying(64)) || ')' || coalesce(' and ' || inv_filter, '');


	-- look for negative emissions in detailed result...
	execute
	--raise notice '%',
	 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		fips, 
		scc, 
		plantid, 
		pointid, 
		stackid, 
		segment, 
		poll, 
		status,
		control_program,
		message
		)
	select 
		' || strategy_messages_dataset_id || '::integer,
		dr.fips,
		dr.scc,
		dr.plantid, 
		dr.pointid, 
		dr.stackid, 
		dr.segment, 
		dr.poll,
		''Warning''::character varying(11) as status,
		null::character varying(255) as control_program,
		''Emission reduction is negative, '' || emis_reduction || ''.'' as "comment"
	from emissions.' || strategy_detailed_result_table_name || ' dr
	where dr.emis_reduction < 0.0 
		and dr.control_eff > 0.0';

	-- if PM target pollutant run, then see if any of the CEFF are missing from the PM 10 vs 2.5 pollutant sources
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		fips, 
		scc, 
		' || case when is_point_table then '
		plantid, 
		pointid, 
		stackid, 
		segment, 
		' else '' end || ' 
		poll, 
		status,
		control_program,
		message
		)
		select 
		' || strategy_messages_dataset_id || '::integer,
		' || fips_expression || ',
		scc,
		' || case when is_point_table then '
		' || plantid_expression || ', 
		' || pointid_expression || ', 
		' || stackid_expression || ', 
		' || segment_expression || ', 
		' else '' end || ' 
		poll,
		''Warning''::character varying(11) as status,
		null::character varying(255) as control_program,
		poll || '' source record is missing CEFF, will use '' || 
		(case when poll = ''PM10'' then ''PM2_5'' else ''PM10'' end) || '' CEFF,'' || 
		coalesce(
			case 
				when coalesce(lagging_ceff,0.0) <> 0.0 then lagging_ceff 
				else null 
			end, 
			case 
				when coalesce(leading_ceff,0.0) <> 0.0 then leading_ceff 
				else null 
			end) 
			|| ''%.'' as "comment"


		from (
			select record_id,
				' || case when is_point_table = false then '' else '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || ',' end || '
				poll,
				scc,
				' || fips_expression || ',
				ceff,
				lag(a.ceff, 1 , a.ceff) over source_window as lagging_ceff, 
				lead(a.ceff, 1 , a.ceff) over source_window as leading_ceff,
				sum(case when coalesce(a.ceff,0.0) = 0.0 then 1 else null end) over source_window as missing_ceff_count,
				sum(1) over source_window as partition_record_count

			from emissions.' || inv_table_name || ' a

			where ' || inv_filter || '
				and a.poll in (''PM10'',''PM2_5'')

			WINDOW source_window AS (PARTITION BY ' || fips_expression || ',scc' || case when is_point_table = false then '' else ',' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '' end || ' order by ' || fips_expression || ',scc' || case when is_point_table = false then '' else ',' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '' end || ')
		) foo
		where missing_ceff_count <> partition_record_count
			and coalesce(ceff,0.0) = 0.0;';
			
	-- check to see if any of the sources that have an existing control (cpri/primary_device_type_code or control_measures columns are populated)
	-- are missing the ceff value
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		fips, 
		scc, 
		' || case when is_point_table then '
		plantid, 
		pointid, 
		stackid, 
		segment, 
		' else '' end || ' 
		poll, 
		status,
		control_program,
		message
		)
	select 
		' || strategy_messages_dataset_id || '::integer,
		a.' || fips_expression || ',
		a.scc,
		' || case when is_point_table then '
		a.' || plantid_expression || ', 
		a.' || pointid_expression || ', 
		a.' || stackid_expression || ', 
		a.' || segment_expression || ', 
		' else '' end || ' 
		a.poll,
		''Warning''::character varying(11) as status,
		null::character varying(255) as control_program,
		''Source has existing control but is missing ceff.'' as "comment"
	FROM emissions.' || inv_table_name || ' a

	where record_id in (
			select null::integer as record_id 
			where 1 = 0
			
			' || case when has_control_measures_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and length(coalesce(control_measures,'''')) > 0 
				and coalesce(ceff,0.0) = 0.0 '
			else ''
			end || '
			' || case when has_primary_device_type_code_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and length(coalesce(primary_device_type_code,'''')) > 0
				and coalesce(ceff,0.0) = 0.0 '
			else ''
			end || '
			' || case when has_cpri_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and coalesce(cpri,0) <> 0
				and coalesce(ceff,0.0) = 0.0 '
			else ''
			end || '
		)
	order by a.' || fips_expression || ',
		a.scc, 
		' || case when is_point_table then '
		a.' || plantid_expression || ', 
		a.' || pointid_expression || ', 
		a.' || stackid_expression || ', 
		a.' || segment_expression || ', 
		' else '' end || ' 
		a.poll';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

