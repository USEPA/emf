CREATE OR REPLACE FUNCTION public.populate_current_state_of_db(tmpSQLFile varchar) RETURNS void AS $$
DECLARE
	table_record RECORD;
BEGIN
	raise notice '%', 'start ' || clock_timestamp();
	CREATE TEMP TABLE current_state_of_db_sql(sql text) on commit drop;

	--create table to store results
        BEGIN
		CREATE TABLE public.current_state_of_db(table_schema varchar(64), table_name varchar(64), table_record_count bigint);

        EXCEPTION WHEN duplicate_table THEN
		--TO '/usr1/proj/bray/sql/a_list_countries.copy'
		-- get rid of old data...
		truncate public.current_state_of_db;
        END;

	insert into current_state_of_db_sql(sql)
	select 'select ''start '' || clock_timestamp();';

	--loop t
	FOR table_record IN EXECUTE 
		'SELECT table_schema, table_name
		FROM information_schema.tables
		WHERE table_type = ''BASE TABLE''
			AND table_schema NOT IN
			(''pg_catalog'', ''information_schema'')
		order by table_schema, table_name
		;
		'
	LOOP
		raise notice '%', 
		--execute
		'insert into public.current_state_of_db(table_schema, table_name, table_record_count) select ' || quote_literal(table_record.table_schema) || ' as table_schema, ' || quote_literal(table_record.table_name) || ' as table_name, (select count(1) from ' || table_record.table_schema || '.' || quote_ident(table_record.table_name) || ') as table_record_count;';
 
		insert into current_state_of_db_sql(sql)
		select ' begin;insert into public.current_state_of_db(table_schema, table_name, table_record_count) select ' || quote_literal(table_record.table_schema) || ' as table_schema, ' || quote_literal(table_record.table_name) || ' as table_name, (select count(1) from ' || table_record.table_schema || '.' || quote_ident(table_record.table_name) || ') as table_record_count;commit;';

	END LOOP;
	raise notice '%', 'done ' || clock_timestamp();

	insert into current_state_of_db_sql(sql)
	select 'select ''end '' || clock_timestamp();';

	execute
		'COPY current_state_of_db_sql TO ' || quote_literal(tmpSQLFile) || ' WITH DELIMITER ''!''';

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.populate_current_state_of_db('/largefs/emf_tmp/testsql.sql');--'/largefs/emf_tmp/testsql.sql'c:\\temp\\testsql.sql
