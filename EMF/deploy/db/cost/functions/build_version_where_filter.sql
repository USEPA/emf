CREATE OR REPLACE FUNCTION public.build_version_where_filter(dataset_id integer, "version" integer)
  RETURNS text AS
$BODY$
DECLARE
	path text := '';
	versions_path text := '';
	where_filter text := '';
	delete_filter text := '';
	counter integer := 1;
	version_part text := '';
BEGIN

	--get version path
	select versions.path
	from emf.versions
	where versions.dataset_id = $1
		and versions.version = $2
	into path;

	-- build path into where clause
	IF path is null or length(path) = 0 THEN
		versions_path := $2;
	ELSE
		versions_path := path || ',' || $2;
	END IF;
	where_filter := 'version IN (' || versions_path || ') and dataset_id = ' || $1;

	version_part := split_part(versions_path, ',', counter);
	WHILE length(version_part) > 0 LOOP
		IF version_part != '0' THEN
			IF length(delete_filter) > 0 THEN
				delete_filter := delete_filter || ' and delete_versions NOT SIMILAR TO ''' || '(' || version_part || '|' || version_part || ',%|%,' || version_part || ',%|%,' || version_part || ')''';
			ELSE
				delete_filter := delete_filter || 'delete_versions NOT SIMILAR TO ''' || '(' || version_part || '|' || version_part || ',%|%,' || version_part || ',%|%,' || version_part || ')''';
			END IF;
		END IF;
		counter := counter + 1;
		version_part := split_part(versions_path, ',', counter);
	END LOOP;
	IF length(delete_filter) > 0 THEN
		where_filter := where_filter || ' and ' || delete_filter;
	END IF;

	RETURN where_filter;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.build_version_where_filter(integer, integer) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.build_version_where_filter(dataset_id integer, "version" integer, table_alias character varying(64))
  RETURNS text AS
$BODY$
DECLARE
	path text := '';
	versions_path text := '';
	where_filter text := '';
	delete_filter text := '';
	counter integer := 1;
	version_part text := '';
BEGIN

	--get version path
	select versions.path
	from emf.versions
	where versions.dataset_id = $1
		and versions.version = $2
	into path;

	-- build path into where clause
	IF path is null or length(path) = 0 THEN
		versions_path := $2;
	ELSE
		versions_path := path || ',' || $2;
	END IF;
	where_filter := '' || table_alias || '.version IN (' || versions_path || ') and ' || table_alias || '.dataset_id = ' || $1;

	version_part := split_part(versions_path, ',', counter);
	WHILE length(version_part) > 0 LOOP
		IF version_part != '0' THEN
			IF length(delete_filter) > 0 THEN
				delete_filter := delete_filter || ' and ' || table_alias || '.delete_versions NOT SIMILAR TO ''' || '(' || version_part || '|' || version_part || ',%|%,' || version_part || ',%|%,' || version_part || ')''';
			ELSE
				delete_filter := delete_filter || '' || table_alias || '.delete_versions NOT SIMILAR TO ''' || '(' || version_part || '|' || version_part || ',%|%,' || version_part || ',%|%,' || version_part || ')''';
			END IF;
		END IF;
		counter := counter + 1;
		version_part := split_part(versions_path, ',', counter);
	END LOOP;
	IF length(delete_filter) > 0 THEN
		where_filter := where_filter || ' and ' || delete_filter;
	END IF;

	RETURN where_filter;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.build_version_where_filter(integer, integer, character varying(64)) OWNER TO emf;
