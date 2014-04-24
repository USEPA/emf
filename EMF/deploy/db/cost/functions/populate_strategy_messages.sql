CREATE OR REPLACE FUNCTION public.populate_strategy_messages(
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
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_mact_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_plant_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	has_control_ids_column boolean := false;
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
	inv_pct_red_expression character varying(256);
	inv_ceff_expression character varying(64) := 'ceff';
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
		inv_ceff_expression := 'ann_pct_red';
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
		inv_ceff_expression := 'ceff';
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

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_filter(cs.filter, inv_table_name, 'a') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version
	FROM emf.control_strategies cs
	where cs.id = intControlStrategyId
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version;

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

	-- see if there is a control_ids column in the inventory
	has_control_ids_column := public.check_table_for_columns(inv_table_name, 'control_ids', ',');

	-- see if there is plant column in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if there is control_measures column in the inventory
	has_control_measures_column := public.check_table_for_columns(inv_table_name, 'control_measures', ',');

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and a.fips in (SELECT fips
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
		and dr.control_eff > 0.0
		and ' || public.build_version_where_filter(strategy_detailed_result_dataset_id, 0, 'dr'::character varying(64));

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
		fips,
		scc,
		' || case when is_point_table then '
		plantid, 
		pointid, 
		stackid, 
		segment, 
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
				' || case when is_point_table = false then '' else '' || plantid_expression || ' as plantid,' || pointid_expression || ' as pointid,' || stackid_expression || ' as stackid,' || segment_expression || ' as segment,' end || '
				poll,
				scc,
				' || fips_expression || ' as fips,
				' || inv_ceff_expression || ' as ceff,
				lag(a.' || inv_ceff_expression || ', 1 , a.' || inv_ceff_expression || ') over source_window as lagging_ceff, 
				lead(a.' || inv_ceff_expression || ', 1 , a.' || inv_ceff_expression || ') over source_window as leading_ceff,
				sum(case when coalesce(a.' || inv_ceff_expression || ',0.0) = 0.0 then 1 else null end) over source_window as missing_ceff_count,
				sum(1) over source_window as partition_record_count

			from emissions.' || inv_table_name || ' a

			where ' || inv_filter || '
				and a.poll in (''PM10'',''PM2_5'')

			WINDOW source_window AS (PARTITION BY ' || fips_expression || ' ,scc' || case when is_point_table = false then '' else ',' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '' end || ' order by ' || fips_expression || ',scc' || case when is_point_table = false then '' else ',' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '' end || ')
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
		a.' || fips_expression || ' as fips,
		a.scc,
		' || case when is_point_table then '
		a.' || plantid_expression || ' as plantid, 
		a.' || pointid_expression || ' as pointid, 
		a.' || stackid_expression || ' as stackid, 
		a.' || segment_expression || ' as segment, 
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
				and coalesce(' || inv_ceff_expression || ',0.0) = 0.0 '
			else ''
			end || '
			' || case when has_primary_device_type_code_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and length(coalesce(primary_device_type_code,'''')) > 0
				and coalesce(' || inv_ceff_expression || ',0.0) = 0.0 '
			else ''
			end || '
			' || case when has_cpri_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and coalesce(cpri,0) <> 0
				and coalesce(' || inv_ceff_expression || ',0.0) = 0.0 '
			else ''
			end || '
			' || case when has_control_ids_column then '
			union all
			select record_id 
			FROM emissions.' || inv_table_name || ' a
			where ' || inv_filter || ' 
				and length(coalesce(control_ids,'''')) > 0
				and coalesce(' || inv_ceff_expression || ',0.0) = 0.0 '
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


	
	-- look for -Type ? equations in detailed result and add information on what fields might be missing.
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
		''Source is missing engineering cost equations, '' || et.name || '' ['' || et.description || ''], inventory inputs; '' || et.inventory_fields || ''.  The default cost per ton approach was used instead for costing.'' as "comment"
	from emissions.' || strategy_detailed_result_table_name || ' dr
		inner join emf.equation_types et
		on et.name = regexp_replace(dr.equation_type, ''(-)Type'', ''Type'')
	where dr.equation_type ~* ''(-)Type''
		and ' || public.build_version_where_filter(strategy_detailed_result_dataset_id, 0, 'dr'::character varying(64));


END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

