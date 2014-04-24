/*
select sum(record_count)
from emf.table_consolidation_tracker
where record_count < 100000

select * --sum(record_count)
from emf.table_consolidation_tracker
where record_count <= 100000
	and dataset_type = 'Log summary'
	and dataset_new_table = 'ds_consol_DS_ogs_ptnonipm_2002ce_04bv3_4_12EUS1_oneti1237902632'
	and is_consolidated = false;
--1893 datasets

select count(1) from emissions.ds_consol_DS_ogs_ptnonipm_2002ce_04bv3_4_12EUS1_oneti1237902632;
*/

CREATE OR REPLACE FUNCTION public.populate_table_consolidation_tracker_tables() RETURNS void AS $$
DECLARE
	table_consolidation_tracker_record RECORD;
	has_table_consolidation_tracker_table boolean := false;
	has_table boolean := false;
	column_name_list text := '';
	column_datatype_list text := '';
	dataset_id_list text := '';
	column_count integer := 0;
	column_counter integer := 0;
	counter integer := 0;
	column_names varchar(64)[];
	column_datatypes varchar(64)[];
	sql text := '';
	subsql text := '';
	select_columns_sql text := '';
	create_columns_sql text := '';
	new_table varchar(64) := '';
	prev_new_table varchar(64) := '';
	old_table varchar(64) := '';
BEGIN

	raise notice '%', 'Check for emf.table_consolidation_tracker table';
	
	-- check if the table exists
	select true
	from pg_class
	where relname = 'table_consolidation_tracker'
	into has_table_consolidation_tracker_table;

	IF not has_table_consolidation_tracker_table THEN
		raise exception '%', 'emf.table_consolidation_tracker doesnt exists, create it by running select.populate_table_consolidation_tracker();';
	END IF;

	-- process each dataset to consolidate, only insert when a new dataset_new_table is encountered...
	FOR table_consolidation_tracker_record IN EXECUTE 
		'select dataset_id, dataset_old_table, dataset_new_table, column_name_list, column_data_type_list 
		from emf.table_consolidation_tracker
		where is_consolidated = false
			and record_count <= 100000
--			and dataset_type = ''Log summary''
--			and dataset_new_table = ''ds_consol_DS_ogs_ptnonipm_2002ce_04bv3_4_12EUS1_oneti1237902632''
			
		order by dataset_new_table, dataset_old_table'
	LOOP
		counter := counter + 1;



		column_count := array_upper(string_to_array(table_consolidation_tracker_record.column_name_list, '&'), 1);
		column_names := string_to_array(table_consolidation_tracker_record.column_name_list, '&');
		column_datatypes := string_to_array(table_consolidation_tracker_record.column_data_type_list, '&');
		new_table := table_consolidation_tracker_record.dataset_new_table;
		old_table := table_consolidation_tracker_record.dataset_old_table;
		IF counter > 1 THEN
		IF new_table <> prev_new_table THEN
			-- check if the new table exists
			select true
			from pg_class
			where relname = lower(prev_new_table)
			into has_table;

			IF has_table THEN
				raise notice '%', 'truncate emissions.' || prev_new_table || ';';
			ELSE
				raise notice '%', 'create table emissions.' || prev_new_table || ' (' || create_columns_sql || ');';
			END IF;

			raise notice '%', 'begin;insert into emissions.' || prev_new_table || ' (' || select_columns_sql || ') ' || sql || ';';

			-- update the datasets to use the new table
			raise notice '%', 'update emf.internal_sources 
			set table_name = ' || quote_literal(prev_new_table) || '
			where dataset_id in (' || dataset_id_list || ');';

			-- update the table_consolidation_tracker dataset to say its been consolidated
			raise notice '%', 'update emf.table_consolidation_tracker 
			set is_consolidated = true
			where dataset_id in (' || dataset_id_list || ');
			commit;
			begin;
			select public.create_table_index(''' || prev_new_table || ''', ''record_id'', ''record_id'');
			select public.create_table_index(''' || prev_new_table || ''', ''dataset_id'', ''dataset_id'');
			select public.create_table_index(''' || prev_new_table || ''', ''version,delete_versions'', ''versions'');
			commit;
			';

			
		END IF;
		END IF;

		FOR column_counter IN 1..column_count LOOP
			IF column_counter > 1 THEN
				create_columns_sql := create_columns_sql || ',' || column_names[column_counter] || ' ' || column_datatypes[column_counter];
				IF column_counter > 2 THEN
					select_columns_sql := select_columns_sql || ',' || column_names[column_counter];
				ELSE
					select_columns_sql := column_names[column_counter];
				END IF;
			ELSE
				create_columns_sql := 'record_id serial NOT NULL PRIMARY KEY';
			END IF;
--			execute  'dataset_id=' || table_consolidation_tracker_record.dataset_id || ', dataset_old_table=' || table_consolidation_tracker_record.dataset_old_table || ', dataset_new_table=' || table_consolidation_tracker_record.dataset_new_table || ', column_name=' ||  column_names[column_counter] || ', column_data_type=' ||  column_datatypes[column_counter] || ' ';
		END LOOP;

		subsql := 'select ' || select_columns_sql || ' from emissions.' || old_table;

		IF new_table <> prev_new_table THEN
			dataset_id_list := table_consolidation_tracker_record.dataset_id;
			sql := subsql;
		ELSE
			dataset_id_list := dataset_id_list || ',' || table_consolidation_tracker_record.dataset_id;
			sql := sql || E'\nunion all ' || subsql;
		END IF;

		prev_new_table := new_table;
	
--		EXECUTE 'update emf.table_consolidation_tracker
--		set record_count = (SELECT count(1) from emissions.' || table_consolidation_tracker_record.dataset_old_table || ')
--		where dataset_id = ' || table_consolidation_tracker_record.dataset_id || ';';
	END LOOP;

	IF LENGTH(prev_new_table) > 0 THEN
		-- check if the new table exists
		select true
		from pg_class
		where relname = lower(prev_new_table)
		into has_table;

		IF has_table THEN
			raise notice '%', 'truncate emissions.' || prev_new_table || ';';
		ELSE
			raise notice '%', 'create table emissions.' || prev_new_table || ' (' || create_columns_sql || ');';
		END IF;

		
		raise notice '%', 'begin;insert into emissions.' || prev_new_table || ' (' || select_columns_sql || ') ' || sql || ';';
		-- update the datasets to use the new table
		raise notice '%', 'update emf.internal_sources 
		set table_name = ' || quote_literal(prev_new_table) || '
		where dataset_id in (' || dataset_id_list || ');';

		-- update the table_consolidation_tracker dataset to say its been consolidated
		raise notice '%', 'update emf.table_consolidation_tracker 
		set is_consolidated = true
		where dataset_id in (' || dataset_id_list || ');
		commit;
		begin;
		select public.create_table_index(''' || prev_new_table || ''', ''record_id'', ''record_id'');
		select public.create_table_index(''' || prev_new_table || ''', ''dataset_id'', ''dataset_id'');
		select public.create_table_index(''' || prev_new_table || ''', ''version,delete_versions'', ''versions'');
		commit;
		';
	END IF;

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.populate_table_consolidation_tracker_tables();

/*
select * 
from emf.table_consolidation_tracker
order by dataset_new_table, dataset_old_table
--order by column_name_list, column_data_type_list;

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
