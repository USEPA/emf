CREATE OR REPLACE FUNCTION public.get_external_dataset(external_dataset_access_id integer)
  RETURNS SETOF orl_point AS
$BODY$
DECLARE
	dataset_table_name character varying(64);
	sql text;
	r record;
BEGIN

	-- get new dataset table name
	select lower(i.table_name)
	from emf.external_dataset_access e
		inner join emf.internal_sources i
		on i.dataset_id = e.dataset_id
	where e.id = external_dataset_access_id
	into dataset_table_name;

	sql := 'SELECT * FROM emissions.' || dataset_table_name || ' limit 100;';
	
	FOR r IN EXECUTE sql LOOP
	  RETURN NEXT r;
	END LOOP;

	return;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE
  COST 100;
ALTER FUNCTION public.get_external_dataset(integer) OWNER TO emf;