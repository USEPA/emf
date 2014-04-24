



CREATE OR REPLACE FUNCTION public.find_emissions_schema_tables_with_missing_dataset() RETURNS void AS $$
DECLARE
	emission_table_record RECORD;
BEGIN


	truncate public.tables_with_missing_dataset;

	-- only look at tables with the 
	FOR emission_table_record IN 
		select t.tablename 
		from pg_tables t
			inner join pg_class c
			on t.tablename = c.relname
			inner join pg_attribute a
			on a.attrelid = c.oid
		WHERE a.attnum > 0
			and lower(a.attname) = 'dataset_id'
			and lower(t.schemaname) = 'emissions'
			and t.tablename not in ('versions')
--		limit 10
	LOOP
		--raise notice '%', 
		execute 
		'insert into public.tables_with_missing_dataset (table_name, dataset_id) select distinct ' || quote_literal(emission_table_record.tablename) || ', ds.dataset_id from emissions.' || emission_table_record.tablename || ' ds where not exists (select 1 from emf.datasets where datasets.id = ds.dataset_id);';

	END LOOP;

	return;
END;
$$ LANGUAGE plpgsql;

--create table public.tables_with_missing_dataset (table_name varchar(64), dataset_id integer);

-- run procedure
select public.find_emissions_schema_tables_with_missing_dataset();

/*
select * from public.tables_with_missing_dataset mds
inner join emf.datasets ds
on mds.dataset_id = ds.id;

*/
--drop table public.tables_with_missing_dataset;

