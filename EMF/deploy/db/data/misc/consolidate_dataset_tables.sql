/*select table_name, public.concatenate_with_ampersand(column_name) as column_name_list, public.concatenate_with_ampersand(column_type) as column_data_type_list
from (
	SELECT c.relname as table_name,
		a.attnum AS ordinal_position, 
		a.attname AS column_name, 
		t.typname AS data_type, 
		a.attlen AS character_maximum_length, 
		case when t.typname = 'varchar' then t.typname || '(' || a.atttypmod - 4 || ')' else t.typname end as column_type,
		a.atttypmod AS modifier, 
		a.attnotnull AS notnull, 
		a.atthasdef AS hasdefault
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE 
--		c.relname = lower('DS_rep_point_FAA_NextGen_base05a_proj_county_txt_1576663929')
--		and a.attname in (''' || array_to_string(string_to_array(column_list, column_list_delimiter), ''',''') || ''')
--		AND 
		a.attnum > 0
	order by a.attnum
) tbl
group by table_name
*/

-- get datasets to worry about
select d.id, d.name as dataset, i.table_name as dataset_table, d.dataset_type, column_name_list, column_data_type_list
from emf.datasets d

	inner join emf.internal_sources i
	on i.dataset_id = d.id
	
	inner join pg_class c
	on c.relname = lower(i.table_name)

	inner join (
	select table_name, public.concatenate_with_ampersand(column_name) as column_name_list, public.concatenate_with_ampersand(column_type) as column_data_type_list
from (
	SELECT c.relname as table_name,
		a.attnum AS ordinal_position, 
		a.attname AS column_name, 
		t.typname AS data_type, 
		a.attlen AS character_maximum_length, 
		case when t.typname = 'varchar' then t.typname || '(' || a.atttypmod - 4 || ')' else t.typname end as column_type,
		a.atttypmod AS modifier, 
		a.attnotnull AS notnull, 
		a.atthasdef AS hasdefault
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE 
--		c.relname = lower('DS_rep_point_FAA_NextGen_base05a_proj_county_txt_1576663929')
--		and a.attname in (''' || array_to_string(string_to_array(column_list, column_list_delimiter), ''',''') || ''')
--		AND 
		a.attnum > 0
	order by c.relname, a.attnum
) tbl
group by table_name) table_metadata
on table_metadata.table_name = lower(i.table_name)

where 
	-- target only CSV, Line-Based, and SMOKEReport importers...
	d.dataset_type in (
		select id
		from emf.dataset_types
		where importer_classname in ('gov.epa.emissions.commons.io.csv.CSVImporter','gov.epa.emissions.commons.io.generic.LineImporter','gov.epa.emissions.commons.io.other.SMKReportImporter')
	)
	-- don't include tables already consolidated...
	and lower(i.table_name) not in (
		select output_table
		from emf.table_consolidations
	)