CREATE OR REPLACE FUNCTION public.check_table_for_columns(fully_qualified_table_name character varying, column_list character varying, column_list_delimiter character varying)
  RETURNS boolean AS
$BODY$
DECLARE
	has_columns boolean := false;
BEGIN

	execute 'SELECT count(1) = ' || array_upper(string_to_array(column_list, column_list_delimiter), 1) || '
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = ' || lower(quote_literal(fully_qualified_table_name)) || '
		and a.attname in (''' || array_to_string(string_to_array(column_list, column_list_delimiter), ''',''') || ''')
		AND a.attnum > 0'
	into has_columns;

	return has_columns;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.check_table_for_columns(character varying, character varying, character varying) OWNER TO emf;

--select public.check_table_for_columns('alm_cap2002v3', 'fips,scc,poll,record_id', ',');

