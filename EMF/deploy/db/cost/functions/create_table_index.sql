CREATE OR REPLACE FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(64) := '';
	has_columns boolean := false;
	already_has_index boolean := false;
BEGIN

	table_name := lower(table_name);
	table_col_list := lower(table_col_list);

	-- first lets see if there is already an index on the table with the same columns
	-- specified...
	execute '
	SELECT true
	FROM pg_index c
		inner JOIN pg_class t
		ON c.indrelid  = t.oid
		inner JOIN pg_attribute a
		ON a.attrelid = t.oid
		AND a.attnum = ANY(indkey)
		inner join pg_class i
		on c.indexrelid = i.oid
	where t.relname = ' || quote_literal(table_name) || '
		and a.attname in (' || '''' || regexp_replace(table_col_list, ',', ''',''', 'gi') || '''' || ')
		and array_upper(indkey, 1) + 1 = ' || array_upper(string_to_array(table_col_list, ','), 1)
	into already_has_index;
	already_has_index := coalesce(already_has_index, false);

	-- see if there are columns to be indexed
	has_columns := public.check_table_for_columns(table_name, table_col_list, ',');

	-- Create Indexes....

	IF has_columns and not already_has_index THEN
	raise notice '%', 'create index';
	
	
		-- create record_id btree index
		IF length(index_name_prefix || '_' || table_name) >= 63 - length(index_name_prefix) THEN
			index_name := index_name_prefix || '_' || substr(table_name, length(index_name_prefix) + 2, 63);
		ELSE
			index_name := index_name_prefix || '_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(' || table_col_list || ')';

	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying) OWNER TO emf;


--select public.create_table_index('cslcm__20080320225457strat_lccmws_20080320225457', 'fips', 'index_name_prefix');

CREATE OR REPLACE FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying, clustered boolean)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(64) := '';
	has_columns boolean := false;
	already_has_index boolean := false;
BEGIN
	table_name := lower(table_name);
	table_col_list := lower(table_col_list);

	-- first lets see if there is already an index on the table with the same columns
	-- specified...
	execute '
	SELECT true
	FROM pg_index c
		inner JOIN pg_class t
		ON c.indrelid  = t.oid
		inner JOIN pg_attribute a
		ON a.attrelid = t.oid
		AND a.attnum = ANY(indkey)
		inner join pg_class i
		on c.indexrelid = i.oid
	where t.relname = ' || quote_literal(table_name) || '
		and c.indisunique != ''t''
		and c.indisprimary != ''t''
		and a.attname in (' || '''' || regexp_replace(table_col_list, ',', ''',''', 'gi') || '''' || ')
		and array_upper(indkey, 1) + 1 = ' || array_upper(string_to_array(table_col_list, ','), 1)
	into already_has_index;
--return;
	already_has_index := coalesce(already_has_index, false);
	

	-- see if there are columns to be indexed
	has_columns := public.check_table_for_columns(table_name, table_col_list, ',');

	-- Create Indexes....

	IF has_columns and not already_has_index THEN
	raise notice '%', 'create index';

		-- create record_id btree index
		IF length(index_name_prefix || '_' || table_name) >= 63 - length(index_name_prefix) THEN
			index_name := index_name_prefix || '_' || substr(table_name, length(index_name_prefix) + 2, 63);
		ELSE
			index_name := index_name_prefix || '_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(' || table_col_list || ')';
		IF clustered THEN
			execute 'ALTER TABLE emissions.' || table_name || ' CLUSTER ON ' || index_name || ';';
		END IF;
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying, clustered boolean) OWNER TO emf;

--SELECT public.create_table_index('DS_ptinv_ptnonipm_2020cc_1068478967','record_id','idx_94cccddf2c524dd39e0374e4698ebf9f',true::boolean);
