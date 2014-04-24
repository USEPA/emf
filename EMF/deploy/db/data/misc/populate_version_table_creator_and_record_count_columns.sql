CREATE OR REPLACE FUNCTION public.populate_version_table_creator_and_record_count_columns() RETURNS void AS $$
DECLARE
	version_record RECORD;
BEGIN

  
	FOR version_record IN EXECUTE 
		'select versions.id,
			versions.dataset_id,
			versions."version",
			internal_sources.table_name
		from emf.versions
			inner join emf.internal_sources
			on internal_sources.dataset_id = versions.dataset_id
			-- make sure the table exists
			inner join pg_class
			on lower(relname) = lower(internal_sources.table_name)'
	LOOP
		--raise notice '%', 
		execute 
		'update emf.versions
		set number_records = coalesce((SELECT count(1) from emissions.' || version_record.table_name || ' where ' || public.build_version_where_filter(version_record.dataset_id, version_record."version") || '),0),
			creator = coalesce((SELECT users.id from emf.datasets inner join emf.users on users.username = datasets.creator where datasets.id = ' || version_record.dataset_id || '),2)
		where id = ' || version_record.id || ';';

	END LOOP;

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.populate_version_table_creator_and_record_count_columns();
