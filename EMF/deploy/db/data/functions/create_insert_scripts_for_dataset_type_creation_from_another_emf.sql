--

CREATE OR REPLACE FUNCTION public.create_insert_script_for_dataset_type(dataset_type_name character varying)
  RETURNS text AS
$BODY$
DECLARE
	file_format_sql text := '';
	file_format_columns_sql text := '';
	dataset_type_sql text := '';
	dataset_type_keywords_sql text := '';
	dataset_type_qa_step_templates_sql text := '';
BEGIN

	-- create file format INSERT script
	select 'INSERT INTO emf.file_formats(
		"name", description, 
		"delimiter", fixed_format, date_added, 
		last_modified_date, creator)
	select ' || quote_literal(dataset_type_name) || ' as "name", ' || quote_literal(description) || ' as description, 
		' || quote_literal("delimiter") || ' as "delimiter", ' || fixed_format || ' as fixed_format, ' || quote_literal(now()) || ' as date_added, 
		' || quote_literal(now()) || ' as last_modified_date, (select id from emf.users where username=''admin'') as creator;' || E'\n'
	from emf.file_formats
	where id = (select file_format from emf.dataset_types where name = dataset_type_name)
	into file_format_sql;


	-- create file format INSERT script
	select string_agg('INSERT INTO emf.fileformat_columns(
		    file_format_id, list_index, "name", 
		    "type", default_value, description, 
		    formatter, "constraints", mandatory, 
		    width, spaces, fix_format_start, 
		    fix_format_end)
	select (select id from emf.file_formats where name=' || quote_literal(dataset_type_name) || ') as file_format_id, ' || list_index || ' as list_index, ' || quote_literal("name") || ' as "name", 
		' || quote_literal(type) || ' as type, ' || quote_literal(default_value) || ' as default_value, ' || quote_literal(description) || ' as description, 
		' || quote_literal(formatter) || ' as formatter, ' || quote_literal("constraints") || ' as "constraints", ' || mandatory || ' as mandatory, 
		' || width || ' as width, ' || spaces || ' as spaces, ' || fix_format_start || ' as fix_format_start, 
		' || fix_format_end || ' as fix_format_end;' || E'\n','' order by list_index)
	from emf.fileformat_columns
	where file_format_id = (select file_format from emf.dataset_types where name = dataset_type_name)
	group by file_format_id
	into file_format_columns_sql;


	-- create dataset_type INSERT script
	select 'INSERT INTO emf.dataset_types(
	"name", description, min_files, max_files, "external", default_sortorder, 
	importer_classname, exporter_classname, lock_owner, lock_date, 
	table_per_dataset, creation_date, last_mod_date, creator, file_format) 
	select ' || quote_literal("name") || ' as "name", ' || quote_literal(description) || ' as description, ' || min_files || ' as min_files, ' || max_files || ' as max_files, ' || "external" || ' as "external", ' || quote_literal(default_sortorder) || ' as default_sortorder, 
	' || quote_literal(importer_classname) || ' as importer_classname, ' || quote_literal(exporter_classname) || ' as exporter_classname, null as lock_owner, null as lock_date, 
	1 as table_per_dataset, ' || quote_literal(now()) || ' as creation_date, ' || quote_literal(now()) || ' as last_mod_date, (select id from emf.users where username=''admin'') as creator, (select id from emf.file_formats where name=' || quote_literal(dataset_types.name) || ') as file_format;' || E'\n'
	from emf.dataset_types
	where name = dataset_type_name
	into dataset_type_sql;

	-- create file format INSERT script
	select string_agg('INSERT INTO emf.dataset_types_keywords(
		    dataset_type_id, list_index, keyword_id, 
		    "value", kwname)
	select (select id from emf.dataset_types where name = ' || quote_literal(dataset_type_name) || ') as dataset_type_id, ' || list_index || ' as list_index, (select id from emf.keywords where name = ' || quote_literal(keywords.name) || ') as keyword_id, 
		' || quote_literal("value") || ' as "value", ' || quote_literal(kwname) || ' as kwname;' || E'\n','' order by list_index)
	from emf.dataset_types_keywords
		inner join emf.keywords
		on keywords.id = dataset_types_keywords.keyword_id
	where dataset_type_id = (select id from emf.dataset_types where name = dataset_type_name)
	group by dataset_type_id
	into dataset_type_keywords_sql;

	-- create file format INSERT script
	select string_agg('INSERT INTO emf.dataset_types_qa_step_templates(
		    dataset_type_id, list_index, "name", 
		    qa_program_id, program_arguments, required, 
		    order_no, description)
	select (select id from emf.dataset_types where name = ' || quote_literal(dataset_type_name) || ') as dataset_type_id, ' || list_index || ' as list_index, ' || quote_literal(dataset_types_qa_step_templates."name") || ' as "name",
		(select id from emf.qa_programs where name = ' || quote_literal(qa_programs.name) || ') as qa_program_id, ' || quote_literal(program_arguments) || ' as program_arguments, ' || required || ' as required, 
		' || order_no || ' as order_no, ' || quote_literal(description) || ' as description;' || E'\n','' order by list_index)
	from emf.dataset_types_qa_step_templates
		inner join emf.qa_programs
		on qa_programs.id = dataset_types_qa_step_templates.qa_program_id
	where dataset_type_id = (select id from emf.dataset_types where name = dataset_type_name)
	group by dataset_type_id
	into dataset_type_qa_step_templates_sql;

	return coalesce(file_format_sql,'') || coalesce(file_format_columns_sql,'') || dataset_type_sql || coalesce(dataset_type_keywords_sql,'') || coalesce(dataset_type_qa_step_templates_sql,'');
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;

select public.create_insert_script_for_dataset_type('Flat File 2010 Point');
