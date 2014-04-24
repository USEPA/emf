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

CREATE OR REPLACE FUNCTION public.drop_table_consolidation_tracker_tables() RETURNS void AS $$
DECLARE
	table_consolidation_tracker_record RECORD;
	has_table_consolidation_tracker_table boolean := false;
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
		where is_consolidated = true 
			and is_deleted = false
		order by dataset_new_table, dataset_old_table'
	LOOP
		raise notice '%', '
begin;
drop table emissions.' || table_consolidation_tracker_record.dataset_old_table || ';
update emf.table_consolidation_tracker
set is_deleted = true
where dataset_id = ' || table_consolidation_tracker_record.dataset_id || ';
commit;';
	END LOOP;

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.drop_table_consolidation_tracker_tables();
