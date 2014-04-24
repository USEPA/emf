/*
-- orl example
select public.build_project_future_year_inventory_matching_hierarchy_sql(
	5315, --inv_dataset_id integer, 
	1, --inv_dataset_version integer, 
	4120, --control_program_dataset_id integer, 
	0, --control_program_dataset_version integer, 
	'fips,plantid,pointid,stackid,segment,scc,poll', --select_columns varchar, 
	'substring(fips,1,2) = ''37''',--null'substring(fips,1,2) = ''37''', --inv_filter text,
	null, --1279 county_dataset_id integer,
	null, --county_dataset_version integer,
	'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''07/01/2020''::timestamp without time zone	', --control_program_dataset_filter text,
	1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
	);

-- flat file point example
select public.build_project_future_year_inventory_matching_hierarchy_sql(
	8292, --inv_dataset_id integer, 
	1, --inv_dataset_version integer, 
	4120, --control_program_dataset_id integer, 
	0, --control_program_dataset_version integer, 
	'fips,plantid,pointid,stackid,segment,scc,poll', --select_columns varchar, 
	'substring(fips,1,2) = ''37''',--null'substring(fips,1,2) = ''37''', --inv_filter text,
	null, --1279 county_dataset_id integer,
	null, --county_dataset_version integer,
	'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''07/01/2020''::timestamp without time zone	', --control_program_dataset_filter text,
	1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
	);

-- flat file nonpoint example
select public.build_project_future_year_inventory_matching_hierarchy_sql(
	8307, --inv_dataset_id integer, 
	0, --inv_dataset_version integer, 
	4120, --control_program_dataset_id integer, 
	0, --control_program_dataset_version integer, 
	'fips,plantid,pointid,stackid,segment,scc,poll', --select_columns varchar, 
	'substring(fips,1,2) = ''37''',--null'substring(fips,1,2) = ''37''', --inv_filter text,
	null, --1279 county_dataset_id integer,
	null, --county_dataset_version integer,
	'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''07/01/2020''::timestamp without time zone	', --control_program_dataset_filter text,
	1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
	);

	' || case when control_program.type <> 'Projection' then '
							-- make the compliance date has been met
							and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
							' else '' end || '
*/

DROP FUNCTION build_project_future_year_inventory_matching_hierarchy_sql(integer, integer, integer, integer, character varying, text, integer, integer, text, integer);

CREATE OR REPLACE FUNCTION public.build_project_future_year_inventory_matching_hierarchy_sql(
	inv_dataset_id integer, 
	inv_dataset_version integer, 
	control_program_dataset_id integer, 
	control_program_dataset_version integer, 
	select_columns varchar, 
	inv_filter text,
	county_dataset_id integer,
	county_dataset_version integer,
	inventory_to_control_program_join_constraints text,
	match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = include only Matched Sources, quick version (just return one of each matching type)
	) RETURNS text AS
$BODY$
DECLARE
	inv_is_point_table boolean := false;
	inv_has_sic_column boolean := false; 
	inv_has_naics_column boolean := false;
	inv_has_mact_column boolean := false;
	control_packet_has_mact_column boolean := false;
	control_packet_has_naics_column boolean := false;
	sql text := '';

	control_program_table_name varchar(64) = ''; 
	inv_table_name varchar(64) = '';
	join_type varchar(10) := 'inner';
	control_program_dataset_version_filter_sql text := '';
	inv_dataset_filter_sql text := '';
	aliased_select_columns text := '';
	
	county_dataset_filter_sql text := '';

	inv_dataset_type_name character varying(255) := '';
	cp_dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'inv.fips';
	plantid_expression character varying(64) := 'inv.plantid';
	pointid_expression character varying(64) := 'inv.pointid';
	stackid_expression character varying(64) := 'inv.stackid';
	segment_expression character varying(64) := 'inv.segment';
	mact_expression character varying(64) := 'inv.mact';

	cp_fips_expression character varying(64) := 'cp.fips';
	cp_plantid_expression character varying(64) := 'cp.plantid';
	cp_pointid_expression character varying(64) := 'cp.pointid';
	cp_stackid_expression character varying(64) := 'cp.stackid';
	cp_segment_expression character varying(64) := 'cp.segment';
	cp_mact_expression character varying(64) := 'cp.mact';

BEGIN
	--get inventory dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = inv_dataset_id
	into inv_dataset_type_name;

	--get control packet dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = control_program_dataset_id
	into cp_dataset_type_name;

	--if Flat File 2010 Types then change primary key field expression variables...
	IF inv_dataset_type_name = 'Flat File 2010 Point' or  inv_dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		fips_expression := 'inv.region_cd';
		plantid_expression := 'inv.facility_id';
		pointid_expression := 'inv.unit_id';
		stackid_expression := 'inv.rel_point_id';
		segment_expression := 'inv.process_id';
		mact_expression := 'inv.reg_codes';
	END If;

	--if control packet is in the Extended Pack Format, then change the column names to the newer format
	IF strpos(cp_dataset_type_name, 'Extended') > 0 THEN
		cp_fips_expression := 'cp.region_cd';
		cp_plantid_expression := 'cp.facility_id';
		cp_pointid_expression := 'cp.unit_id';
		cp_stackid_expression := 'cp.rel_point_id';
		cp_segment_expression := 'cp.process_id';
		cp_mact_expression := 'cp.reg_code';
	END If;

	join_type := case when coalesce(match_type, 1) = 2 then 'left outer' else 'inner' end;

	-- get the contol program dataset table name
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = control_program_dataset_id
	into control_program_table_name;

	-- get the input dataset table name
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = inv_dataset_id
	into inv_table_name;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and ' || fips_expression || ' in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;

	--store control dataset version filter in variable
	select public.build_version_where_filter(control_program_dataset_id, control_program_dataset_version, 'cp')
	into control_program_dataset_version_filter_sql;

	-- alias the inv to control program join constraint
	inventory_to_control_program_join_constraints := coalesce(' and ' || '(' ||  public.alias_filter(public.alias_filter(inventory_to_control_program_join_constraints, control_program_table_name, 'cp'), inv_table_name, 'inv') || ')', '');

	--store control dataset version filter in variable
	select public.build_version_where_filter(inv_dataset_id, inv_dataset_version, 'inv') || coalesce(' and ' || '(' || public.alias_filter(inv_filter, inv_table_name, 'inv') || ')', '') || coalesce(county_dataset_filter_sql, '')
	into inv_dataset_filter_sql;

	select public.alias_filter(select_columns, control_program_table_name, 'cp')
	into select_columns;

	-- see if there are point specific columns in the inventory (make sure and look at new flat file type also)
	inv_is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',')
		or public.check_table_for_columns(inv_table_name, 'facility_id,unit_id,rel_point_id,process_id', ',');

	-- see if there is a mact column in the inventory
	inv_has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',')
		or public.check_table_for_columns(inv_table_name, 'reg_codes', ',');

	-- see if there is a mact column in the control packet
	control_packet_has_mact_column := public.check_table_for_columns(control_program_table_name, 'mact', ',');

	-- see if there is a naics column in the control packet
	control_packet_has_naics_column := public.check_table_for_columns(control_program_table_name, 'naics', ',');

	-- see if there is a sic column in the inventory
	inv_has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	inv_has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	sql :=
		'
		WITH inv AS (
		select * 
		from emissions.' || inv_table_name || ' inv
		where ' || inv_dataset_filter_sql || '
		)
		--NA - NEEDED To help with not having to worry about when a union might end up at the top of the sql statement
		select 
			null::integer as record_id,' || coalesce(select_columns || ',','') || '
			null::double precision as ranking
		FROM emissions.' || control_program_table_name || ' cp

		where 1 = 0

		' || case when inv_is_point_table or match_type = 2 then '
		--1 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) and (' || cp_segment_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 1::double precision --1
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '
			and ' || cp_segment_expression || ' = ' || segment_expression || '
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			
			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is not null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--2 - Country/State/County code, plant ID, point ID, stack ID, segment, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) and (' || cp_segment_expression || ' is not null) and (cp.poll is not null) then 2::double precision --2
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '
			and ' || cp_segment_expression || ' = ' || segment_expression || '
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is not null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--3 - Country/State/County code, plant ID, point ID, stack ID, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) and (cp.poll is not null) then 3::double precision --3
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--3.5 - Country/State/County code, plant ID, point ID, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 3.5::double precision --3.5
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		--4 - Country/State/County code, plant ID, point ID, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (cp.poll is not null) then 4::double precision --4
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--5 - Country/State/County code, plant ID, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 5::double precision --5
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		' || case when control_packet_has_mact_column then '
		--5.5 - Country/State/County code, plant ID, MACT code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_mact_expression || ' is not null) and (cp.poll is not null) then 5.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--6 - Country/State/County code, plant ID, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (cp.poll is not null) then 6::double precision --6
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--7 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) and (' || cp_segment_expression || ' is not null) and (cp.scc is not null) then 7::double precision --7
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '
			and ' || cp_segment_expression || ' = ' || segment_expression || '
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is not null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--8- Country/State/County code, plant ID, point ID, stack ID, segment
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) and (' || cp_segment_expression || ' is not null) then 8::double precision --8
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '
			and ' || cp_segment_expression || ' = ' || segment_expression || '

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is not null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--9 - Country/State/County code, plant ID, point ID, stack ID
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) and (' || cp_stackid_expression || ' is not null) then 9::double precision --9
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '
			and ' || cp_stackid_expression || ' = ' || stackid_expression || '

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is not null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--10 - Country/State/County code, plant ID, point id
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_pointid_expression || ' is not null) then 10::double precision --10
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and ' || cp_pointid_expression || ' = ' || pointid_expression || '

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is not null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--11 - Country/State/County code, plant ID, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (cp.scc is not null) then 11::double precision --11
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		' || case when control_packet_has_mact_column then '
		--12 - Country/State/County code, plant ID, MACT code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) and (' || cp_mact_expression || ' is not null) then 12::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--13 - Country/State/County code, plant ID
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_plantid_expression || ' is not null) then 13::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_is_point_table then '

			' || join_type || ' join inv
					
			on ' || cp_fips_expression || ' = ' || fips_expression || '
			
			and ' || cp_plantid_expression || ' = ' || plantid_expression || '

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is not null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_is_point_table then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		' || case when control_packet_has_mact_column and (inv_has_mact_column or match_type = 2) then '
		--14,16 - Country/State/County code or Country/State code, MACT code, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_mact_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 14::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (' || cp_mact_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 16::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--15,17 - Country/State/County code or Country/State code, MACT code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_mact_expression || ' is not null) and (cp.poll is not null) then 15::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (' || cp_mact_expression || ' is not null) and (cp.poll is not null) then 17::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--18 - MACT code, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_mact_expression || ' is not null) and (cp.scc is not null) and (cp.poll is not null) then 18::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--19 - MACT code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_mact_expression || ' is not null) and (cp.poll is not null) then 19::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--20,22 - Country/State/County code or Country/State code, 8-digit SCC code, MACT code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_mact_expression || ' is not null) and (cp.scc is not null) then 20::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (' || cp_mact_expression || ' is not null) and (cp.scc is not null) then 22::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--21,23 - Country/State/County code or Country/State code, MACT code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (' || cp_mact_expression || ' is not null) then 21::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (' || cp_mact_expression || ' is not null) then 23::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--24 - MACT code, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_mact_expression || ' is not null) and (cp.scc is not null) then 24::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25 - MACT code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_mact_expression || ' is not null) then 25::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_mact_column then '

			' || join_type || ' join inv
					
			on strpos(''&'' || ' || mact_expression || ' || ''&'',''&'' || ' || cp_mact_expression || ' || ''&'') > 0

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || cp_mact_expression || ' is not null

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_mact_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end



/*
25.01	Country/State/County code, NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.02	Country/State/County code, NAICS code, pollutant	point, nonpoint	control, cpection
25.03	Country/State code, NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.04	Country/State code, NAICS code, pollutant	point, nonpoint	control, cpection
25.05	NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.06	NAICS code, pollutant	point, nonpoint	control, cpection
25.07	Country/State/County code, NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.08	Country/State/County code, NAICS code	point, nonpoint	control, cpection
25.09	Country/State code, NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.10	Country/State code, NAICS code	point, nonpoint	control, cpection
25.11	NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.12	NAICS code	point, nonpoint	control, cpection
*/



		|| case when control_packet_has_naics_column and (inv_has_naics_column or match_type = 2) then '
		--25.01,25.03 - Country/State/County code or Country/State code, NAICS code, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.01::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.03::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.naics = inv.naics
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.02,25.04 - Country/State/County code or Country/State code, NAICS code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.naics is not null) and (cp.poll is not null) then 25.02::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.naics is not null) and (cp.poll is not null) then 25.04::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.naics = inv.naics
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.05 - NAICS code, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.05::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.06 - NAICS code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.poll is not null) then 25.06::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.07,25.09 - Country/State/County code or Country/State code, NAICS code, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.naics is not null) and (cp.scc is not null) then 25.07::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.naics is not null) and (cp.scc is not null) then 25.09::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.naics = inv.naics
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.08,25.10 - Country/State/County code or Country/State code, NAICS code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.naics is not null) then 25.08::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.naics is not null) then 25.10::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.naics = inv.naics

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.11 - NAICS code, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.scc is not null) then 25.11::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.12 - NAICS code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) then 25.12::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_naics_column then '

			' || join_type || ' join inv
					
			on cp.naics = inv.naics

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_naics_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '






		' || case when inv_has_sic_column then '
		--25.5,27.5 - Country/State/County code or Country/State code, 8-digit SCC code, 4-digit SIC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.5::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 27.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.sic = inv.sic
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--26,28 - Country/State/County code or Country/State code, 4-digit SIC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.sic is not null) and (cp.poll is not null) then 26::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.sic is not null) and (cp.poll is not null) then 28::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.sic = inv.sic
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--29.5 - 4-digit SIC code, SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 29.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--30 - 4-digit SIC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.poll is not null) then 30::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--31.5,33.5 - Country/State/County code or Country/State code, 4-digit SIC code, SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.sic is not null) and (cp.scc is not null) then 31.5::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.sic is not null) and (cp.scc is not null) then 33.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.sic = inv.sic
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--32,34 - Country/State/County code or Country/State code, 4-digit SIC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.sic is not null) then 32::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.sic is not null) then 34::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.sic = inv.sic

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--35.5 - 4-digit SIC code, SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.scc is not null) then 35.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--36 - 4-digit SIC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) then 36::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp

			' || case when inv_has_sic_column then '

			' || join_type || ' join inv
					
			on cp.sic = inv.sic

			' || inventory_to_control_program_join_constraints || '

			' else '' end || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when inv_has_sic_column then case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end else '' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--38,42 - Country/State/County code or Country/State code, 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.scc is not null) and (cp.poll is not null) then 38::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.scc is not null) and (cp.poll is not null) then 42::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--46 - 8-digit SCC code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.scc is not null) and (cp.poll is not null) then 46::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.scc = inv.scc
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--50,54 - Country/State/County code or Country/State code, 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.scc is not null) then 50::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.scc is not null) then 54::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--58 - 8-digit SCC code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.scc is not null) then 58::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.scc = inv.scc

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--62,64 - Country/State/County code or Country/State code, pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) and (cp.poll is not null) then 62::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) and (cp.poll is not null) then 64::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))
			and cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--63,65 - Country/State/County code or Country/State code
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 5 or length(' || cp_fips_expression || ') = 6) then 63::double precision
				when (' || cp_fips_expression || ' is not null) and (length(' || cp_fips_expression || ') = 2 or length(' || cp_fips_expression || ') = 3) then 65::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (' || cp_fips_expression || ' = ' || fips_expression || ' or ' || cp_fips_expression || ' = substr(' || fips_expression || ', 1, 2) or (substr(' || cp_fips_expression || ',3,3) = ''000'' and substr(' || cp_fips_expression || ', 1, 2) = substr(' || fips_expression || ', 1, 2)))

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is not null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--66 - Pollutant
		union all
		' || -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
		(case when match_type = 3 then '(' else '' end) 
		|| 'select 
			' || (case when match_type = 2 then 'null::integer as record_id' else 'inv.record_id' end) || ',' || coalesce(select_columns || ',','') || '
			case 
				when (cp.poll is not null) then 66::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.poll = inv.poll

			' || inventory_to_control_program_join_constraints || '

		where ' || cp_fips_expression || ' is null 
			and ' || cp_plantid_expression || ' is null 
			and ' || cp_pointid_expression || ' is null 
			and ' || cp_stackid_expression || ' is null 
			and ' || cp_segment_expression || ' is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and ' || cp_mact_expression || ' is null' else '' end || '

			' -- inlcude packets that didnt match anything (keep in WHERE Clause)
			|| case when join_type <> 'left outer' then '' else 'and inv.record_id is null' end || '

			and ' -- inlcude packets versioning filter
			|| control_program_dataset_version_filter_sql || '

			' -- add paranthesis so we can limit to only one record, cant use limit 1 with UNIONs
			|| (case when match_type = 3 then ' limit 1)' else '' end) || ' 
			';
	return sql;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;
