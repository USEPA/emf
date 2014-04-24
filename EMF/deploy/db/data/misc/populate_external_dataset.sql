CREATE OR REPLACE FUNCTION public.populate_external_dataset(external_dataset_access_id integer, new_dataset_id integer)
  RETURNS void AS
$BODY$
DECLARE
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_definition_list character varying := '';
	column_name character varying;
	dataset_table_name character varying(64);
	colRecord RECORD;
BEGIN

	-- get new dataset table name
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = new_dataset_id
	into dataset_table_name;

	-- build insert column list and select column list
	FOR colRecord IN EXECUTE 
		'SELECT a.attname, 
			case when t.typname = ''varchar'' then t.typname || ''('' || a.atttypmod - 4 || '')'' else t.typname end as column_type
		FROM pg_class c
			inner join pg_attribute a
			on a.attrelid = c.oid
			inner join pg_type t
			on t.oid = a.atttypid
		WHERE lower(c.relname) = lower(' || quote_literal(dataset_table_name) || ')
			AND a.attnum > 0
		order by a.attnum'
	LOOP
		column_name := colRecord.attname;
		
		-- ignore record_id, this will be auto-populated since its a serial/identity column.
		IF column_name = 'record_id' THEN
--			select_column_list_sql := select_column_list_sql || 'inv.record_id';
--			insert_column_list_sql := insert_column_list_sql || column_name;
		ELSIF column_name = 'dataset_id' THEN
			select_column_list_sql := select_column_list_sql || '' || new_dataset_id || ' as dataset_id';
			insert_column_list_sql := insert_column_list_sql || '' || column_name;
			column_definition_list := column_definition_list || '' || column_name || ' ' || colRecord.column_type;
		ELSIF column_name = 'delete_versions' THEN
			select_column_list_sql := select_column_list_sql || ','''' as delete_versions';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
			column_definition_list := column_definition_list || ',' || column_name || ' ' || colRecord.column_type;
		ELSIF column_name = 'version' THEN
			select_column_list_sql := select_column_list_sql || ',0 as version';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
			column_definition_list := column_definition_list || ',' || column_name || ' ' || colRecord.column_type;
		ELSE
			select_column_list_sql := select_column_list_sql || ',' || column_name;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
			column_definition_list := column_definition_list || ',' || column_name || ' ' || colRecord.column_type;
		END IF;
	END LOOP;


	execute 'insert into emissions.' || dataset_table_name || ' (' || insert_column_list_sql || ')
	SELECT ' || select_column_list_sql || ' 
	FROM dblink(''host=treehug1.unc.edu
		dbname=EMF
		user=emf
		password=emf'',
	''select ' || replace(select_column_list_sql, '''', '''''') || ' from public.get_external_dataset(' || external_dataset_access_id || '::integer)'')
	AS remote_entries(' || column_definition_list || ')';

--	public.get_external_dataset(external_dataset_access_id integer)
	
	RETURN;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;


/*
select public.populate_external_dataset(0::integer, 4910);

		record_id int,
		dataset_id bigint,
		"version" integer,
		delete_versions text,
		fips character varying(6),
		plantid character varying(15),
		pointid character varying(15),
		stackid character varying(15),
		segment character varying(15),
		plant character varying(40),
		scc character varying(10),
		erptype character varying(2),
		srctype character varying(2),
		stkhgt double precision,
		stkdiam double precision,
		stktemp double precision,
		stkflow double precision,
		stkvel double precision,
		sic character varying(4),
		mact character varying(6),
		naics character varying(6),
		ctype character varying(1),
		xloc double precision,
		yloc double precision,
		utmz smallint,
		poll character varying(16),
		ann_emis double precision,
		avd_emis double precision,
		ceff double precision,
		reff double precision,
		cpri integer,
		csec integer,
		nei_unique_id character varying(20),
		oris_facility_code character varying(6),
		oris_boiler_id character varying(6),
		ipm_yn character varying(1),
		data_source character varying(10),
		stack_default_flag character varying(10),
		location_default_flag character varying(10),
		"year" character varying(4),
		tribal_code character varying(3),
		horizontal_area_fugitive double precision,
		release_height_fugitive double precision,
		zipcode character varying(14),
		naics_flag character varying(3),
		sic_flag character varying(3),
		mact_flag character varying(15),
		process_mact_compliance_status character varying(6),
		ipm_facility character varying(3),
		ipm_unit character varying(3),
		bart_source character varying(10),
		bart_unit character varying(10),
		control_status character varying(12),
		start_date character varying(10),
		end_date character varying(10),
		winter_throughput_pct double precision,
		spring_throughput_pct double precision,
		summer_throughput_pct double precision,
		fall_throughput_pct double precision,
		annual_avg_days_per_week double precision,
		annual_avg_weeks_per_year double precision,
		annual_avg_hours_per_day double precision,
		annual_avg_hours_per_year double precision,
		period_days_per_week double precision,
		period_weeks_per_period double precision,
		period_hours_per_day double precision,
		period_hours_per_period double precision,
		design_capacity double precision,
		design_capacity_unit_numerator character varying(10),
		design_capacity_unit_denominator character varying(10),
		control_measures text,
		pct_reduction text,
		current_cost double precision,
		cumulative_cost double precision,
		comments character varying(256)
*/