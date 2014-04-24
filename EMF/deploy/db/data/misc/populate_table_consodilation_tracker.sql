

CREATE OR REPLACE FUNCTION public.populate_table_consolidation_tracker2() RETURNS void AS $$
DECLARE
	table_consolidation_tracker_record RECORD;
	has_table_consolidation_tracker_table boolean := false;
	cnt integer := 0;
	cnter integer := 0;
BEGIN

	raise notice '%', 'Check for emf.table_consolidation_tracker table';
	
	-- check if the table exists
	select true
	from pg_class
	where relname = 'table_consolidation_tracker2'
	into has_table_consolidation_tracker_table;


	IF has_table_consolidation_tracker_table THEN
		raise notice '%', 'Truncate emf.table_consolidation_tracker table records';
		truncate emf.table_consolidation_tracker2;
		
	ELSE
		raise notice '%', 'Create emf.table_consolidation_tracker table';
		-- create table
		create table emf.table_consolidation_tracker2 (
			dataset_id int4, 
			dataset_name character varying(255) NOT NULL,
			dataset_old_table character varying(64) NOT NULL DEFAULT '',
			dataset_new_table character varying(64) NOT NULL DEFAULT '',
			dataset_type_id int4 NOT NULL, 
			dataset_type character varying(255) NOT NULL,
			column_name_list character varying(4000) NOT NULL DEFAULT '', 
			column_data_type_list character varying(4000) NOT NULL DEFAULT '',
			record_count int4 NOT NULL DEFAULT 0,
			is_consolidated boolean NOT NULL DEFAULT false,
			is_deleted boolean NOT NULL DEFAULT false);
		
	END IF;

	raise notice '%', 'Populate emf.table_consolidation_tracker table';
	-- populate table
	insert into emf.table_consolidation_tracker2 (
			dataset_id, 
			dataset_name,
			dataset_type_id, 
			dataset_type,
			dataset_old_table,
			column_name_list, 
			column_data_type_list)
	-- get datasets to worry about
	select d.id, 
		d.name as dataset, 
		d.dataset_type as dataset_type_id, 
		dt.name as dataset_type, 
		i.table_name as dataset_table, 
		column_name_list, 
		column_data_type_list
	from emf.datasets d

		inner join emf.internal_sources i
		on i.dataset_id = d.id
		
		inner join emf.dataset_types dt
		on dt.id = d.dataset_type
		
		inner join pg_class c
		on c.relname = lower(i.table_name)

		inner join (
			select table_name, public.concatenate_with_ampersand(column_name) as column_name_list, public.concatenate_with_ampersand(column_type) as column_data_type_list
			from (
				SELECT c.relname as table_name,
					a.attname AS column_name, 
					case when t.typname = 'varchar' then t.typname || '(' || a.atttypmod - 4 || ')' else t.typname end as column_type
				FROM pg_class c
					inner join pg_attribute a
					on a.attrelid = c.oid
					inner join pg_type t
					on t.oid = a.atttypid
				WHERE a.attnum > 0
				order by c.relname, a.attnum
			) tbl
			group by table_name
		) table_metadata
		on table_metadata.table_name = lower(i.table_name)

	where d.status <> 'Deleted'
		-- target only CSV, Line-Based, and SMOKEReport importers...
		and d.dataset_type in (
			select id
			from emf.dataset_types
			where importer_classname in ('gov.epa.emissions.commons.io.csv.CSVImporter','gov.epa.emissions.commons.io.generic.LineImporter','gov.epa.emissions.commons.io.other.SMKReportImporter')
		)
		-- don't include tables already consolidated...
		and lower(i.table_name) not in (
			select output_table
			from emf.table_consolidations
		);


	-- get rid of tables that are already unique...
	raise notice '%', 'get rid of tables that are already unique';
	delete from emf.table_consolidation_tracker2 
	where dataset_id in (
		select dataset_id
		from emf.table_consolidation_tracker2 tct
			inner join emf.dataset_types dt
			on dt.id = tct.dataset_type_id

			inner join (
				select case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end as group1, column_name_list, column_data_type_list
				from emf.table_consolidation_tracker2
					inner join emf.dataset_types dt
					on dt.id = emf.table_consolidation_tracker2.dataset_type_id

				group by case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end, column_name_list, column_data_type_list
				having count(1) = 1
			) tbl
			on tbl.column_name_list = tct.column_name_list
			and tbl.column_data_type_list = tct.column_data_type_list
			and tbl.group1 = case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end
		);

	-- update new table name
	raise notice '%', 'update new table name';
	update emf.table_consolidation_tracker2 tct
	set dataset_new_table = tct2.dataset_new_table_name
	from (
		select tct.dataset_id, tbl.dataset_new_table_name
		from emf.table_consolidation_tracker2 tct
			inner join emf.dataset_types dt
			on dt.id = tct.dataset_type_id

			inner join (
				select distinct on (case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end, column_name_list, column_data_type_list)
					case 
						when length('ds_consol_' || dataset_name || round(EXTRACT(EPOCH FROM now()))) <= 63 then 
							'ds_consol_' || dataset_name || round(EXTRACT(EPOCH FROM now()))
						else 
							substring('ds_consol_' || dataset_name, 1, 63 - length(round(EXTRACT(EPOCH FROM now())) || '')) || round(EXTRACT(EPOCH FROM now()))
						end as dataset_new_table_name, case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end as group1, column_name_list, column_data_type_list
				from emf.table_consolidation_tracker2
					inner join emf.dataset_types dt
					on dt.id = emf.table_consolidation_tracker2.dataset_type_id
			) tbl
			on tbl.column_name_list = tct.column_name_list
			and tbl.column_data_type_list = tct.column_data_type_list
			and tbl.group1 = case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end
	) tct2
	where tct.dataset_id = tct2.dataset_id;

	select count(1)
	from emf.table_consolidation_tracker2
	into cnt;

	raise notice '%', 'Total records in emf.table_consolidation_tracker2 = ' || cnter;

	-- update record counts on dataset tables...
	FOR table_consolidation_tracker_record IN EXECUTE 
		'select dataset_id, dataset_old_table 
		from emf.table_consolidation_tracker2
		order by dataset_id'
	LOOP
		cnter := cnter + 1;
		raise notice '%', 'Update emf.table_consolidation_tracker2 ' || table_consolidation_tracker_record.dataset_old_table || ' table record count, ' || cnter;

		EXECUTE 'update emf.table_consolidation_tracker2
		set record_count = (SELECT count(1) from emissions.' || table_consolidation_tracker_record.dataset_old_table || ')
		where dataset_id = ' || table_consolidation_tracker_record.dataset_id || ';';
	END LOOP;

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.populate_table_consolidation_tracker2();

-- view table_consolidation_tracker table contents
select * 
from emf.table_consolidation_tracker2
order by column_name_list, column_data_type_list;

/*
select * 
from emf.table_consolidation_tracker
order by column_name_list, column_data_type_list;

-- get tables that are already unique
select *
from emf.table_consolidation_tracker tct
	inner join emf.dataset_types dt
	on dt.id = tct.dataset_type_id

	inner join (
		select case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end as group1, column_name_list, column_data_type_list
		from emf.table_consolidation_tracker
			inner join emf.dataset_types dt
			on dt.id = table_consolidation_tracker.dataset_type_id

		group by case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end, column_name_list, column_data_type_list
		having count(1) = 1
	) tbl
	on tbl.column_name_list = tct.column_name_list
	and tbl.column_data_type_list = tct.column_data_type_list
	and tbl.group1 = case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end;

-- get new table names
select tct.*, tbl.dataset_new_table_name
from emf.table_consolidation_tracker tct
	inner join emf.dataset_types dt
	on dt.id = tct.dataset_type_id

	inner join (
		select distinct on (case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end, column_name_list, column_data_type_list)
			case 
				when length('ds_consol_' || dataset_name || round(EXTRACT(EPOCH FROM now()))) <= 63 then 
					'ds_consol_' || dataset_name || round(EXTRACT(EPOCH FROM now()))
				else 
					substring('ds_consol_' || dataset_name, 1, 63 - length(round(EXTRACT(EPOCH FROM now())) || '')) || round(EXTRACT(EPOCH FROM now()))
				end as dataset_new_table_name, case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end as group1, column_name_list, column_data_type_list
		from emf.table_consolidation_tracker
			inner join emf.dataset_types dt
			on dt.id = table_consolidation_tracker.dataset_type_id
	) tbl
	on tbl.column_name_list = tct.column_name_list
	and tbl.column_data_type_list = tct.column_data_type_list
	and tbl.group1 = case when dt.importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end
order by tct.column_name_list, tct.column_data_type_list, tct.dataset_name;

-- tables that need to be consolidated
select case when importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end as group1, column_name_list, column_data_type_list, count(1)
from emf.table_consolidation_tracker
	inner join emf.dataset_types dt
	on dt.id = table_consolidation_tracker.dataset_type_id
group by case when importer_classname = 'gov.epa.emissions.commons.io.generic.LineImporter' then dt.name else '' end, column_name_list, column_data_type_list
having count(1) > 1;
*/
