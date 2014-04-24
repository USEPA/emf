CREATE OR REPLACE FUNCTION public.alias_filter(filter text, table_name character varying(64), table_alias character varying(64))
  RETURNS text AS
$BODY$
DECLARE
	table_column record;
	aliased_filter text := filter;
	pattern text;
	rep text;
	flag text;
BEGIN

		aliased_filter := ' ' || aliased_filter || ' ';
		flag := 'gi';
		FOR table_column IN EXECUTE 
			'SELECT a.attname as column_name
			FROM pg_class c
				inner join pg_attribute a
				on a.attrelid = c.oid
				inner join pg_type t
				on t.oid = a.atttypid
			WHERE c.relname = ' || lower(quote_literal(table_name)) || '
				AND a.attnum > 0
			order by a.attname desc'	
-- NOTE keep sort order this make sure we don't want to double alias columns with similar names (i.e., scc and scc_code)
		LOOP
			pattern := E'([[:space:],;=(])(' || table_column.column_name || E'[[:space:],;=)])';
			rep := E'\\1' || table_alias || E'.\\2';
			--raise NOTICE 'column=%', table_column.column_name;
			--raise NOTICE 'pattern=%', pattern;
			--raise NOTICE 'rep=%', rep;
			--raise NOTICE 'flag=%', flag;
			aliased_filter := regexp_replace(aliased_filter, pattern, rep, flag);
			--raise NOTICE 'filtered=%', aliased_filter;
		END LOOP;
	aliased_filter := trim(aliased_filter);
	return aliased_filter;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 100;
/*
CREATE OR REPLACE FUNCTION public.alias_filter(filter text, table_name character varying(64), table_alias character varying(64))
  RETURNS text AS
$BODY$
DECLARE
	table_column record;
	aliased_filter text := filter;
BEGIN

		
		FOR table_column IN EXECUTE 
			'SELECT a.attname as column_name
			FROM pg_class c
				inner join pg_attribute a
				on a.attrelid = c.oid
				inner join pg_type t
				on t.oid = a.atttypid
			WHERE c.relname = ' || lower(quote_literal(table_name)) || '
				AND a.attnum > 0
			order by a.attname desc'	
-- NOTE keep sort order this make sure we don't want to double alias columns with similar names (i.e., scc and scc_code)
		LOOP
--		raise notice '%', aliased_filter;
--		raise notice '%', '(^(?![...]))' || table_column.column_name || '[=,[:space:]]';
			aliased_filter := regexp_replace(aliased_filter, table_column.column_name || '[=,[:space:]]', table_alias || E'.\\&', 'gi');
--			aliased_filter := regexp_replace(aliased_filter, '(^(?![...]))' || table_column.column_name || '[=,[:space:]]', table_alias || '.\\&' || table_column.column_name, 'gi');
		END LOOP;


	return aliased_filter;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE STRICT
  COST 100;
*/
ALTER FUNCTION public.alias_filter(text, character varying(64), character varying(64)) OWNER TO emf;

--select public.alias_filter('version='' and version_fdsasdsa and fdsasdsa_version and compliance_date_foo = '''' and compliance_date = '''' and compliance_date = ''''', 'ds_control_ptnonipm_2020ce_test_14422549', 'cp');
--select public.alias_filter('scc='''' and scc_l1 and scc_l2 and compliance_date_foo = '''' and compliance_date = '''' and compliance_date = ''''', 'scc_codes', 'cp');

--select public.alias_filter('record_id as packet_record_id, compliance_date_foo = '''' and compliance_date = '''' and compliance_date = ''''', 'ds_control_ptnonipm_2020ce_test_14422549', 'cp');
--select regexp_replace('scc '''' and version, cp.scc, scc, cp.scc_code, cp.scc_level, version as asdsd_verson, version = '' , version= '' and version_fdsasdsa and compliance_date_foo = '''' and compliance_date = '''' and compliance_date = ''''', '((^(?![...]))scc[=,[:space:]])', 'cp.\\&', 'gi'); 