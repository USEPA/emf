
--select public.create_projected_future_year_inventory(81, 399, 0, 2750, 2822)
CREATE OR REPLACE FUNCTION public.create_projected_future_year_inventory(
	int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer, 
	output_dataset_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	cont_inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	control_program RECORD;
	target_pollutant_id integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	has_current_cost_col boolean := false;
	has_cumulative_cost_col boolean := false;

	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'inv.fips';
	plantid_expression character varying(64) := 'inv.plantid';
	pointid_expression character varying(64) := 'inv.pointid';
	stackid_expression character varying(64) := 'inv.stackid';
	segment_expression character varying(64) := 'inv.segment';
	mact_expression character varying(64) := 'inv.mact';
	is_flat_file_inventory boolean := false;
	is_monthly_source_sql character varying := 'coalesce(jan_value,feb_value,mar_value,apr_value,may_value,jun_value,jul_value,aug_value,sep_value,oct_value,nov_value,dec_value) is not null';
BEGIN
	--get dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = input_dataset_id
	into dataset_type_name;

	--if Flat File 2010 Types then change primary key field expression variables...
	IF dataset_type_name = 'Flat File 2010 Point' or  dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		fips_expression := 'inv.region_cd';
		plantid_expression := 'inv.facility_id';
		pointid_expression := 'inv.unit_id';
		stackid_expression := 'inv.rel_point_id';
		segment_expression := 'inv.process_id';
		mact_expression := 'inv.reg_codes';
		is_flat_file_inventory := true;
	END If;

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the cont inv dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = output_dataset_id
	into cont_inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is a cpri column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is a primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is a pct_reduction column in the inventory
	has_pct_reduction_col := public.check_table_for_columns(inv_table_name, 'pct_reduction', ',');

	-- see if there is a control_measures column in the inventory
	has_control_measures_col := public.check_table_for_columns(inv_table_name, 'control_measures', ',');

	-- see if there is a current_cost column in the inventory
	has_current_cost_col := public.check_table_for_columns(inv_table_name, 'current_cost', ',');

	-- see if there is a cumulative_cost column in the inventory
	has_cumulative_cost_col := public.check_table_for_columns(inv_table_name, 'cumulative_cost', ',');

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
	into no_days_in_month;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'start ' || clock_timestamp();

	-- build insert column list and select column list
	FOR region IN EXECUTE 
		'SELECT a.attname
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = ' || quote_literal(cont_inv_table_name) || '
		AND a.attnum > 0'
	LOOP
		column_name := region.attname;
		
		IF column_name = 'record_id' THEN
--			select_column_list_sql := select_column_list_sql || 'inv.record_id';
--			insert_column_list_sql := insert_column_list_sql || column_name;
		ELSIF column_name = 'dataset_id' THEN
			select_column_list_sql := select_column_list_sql || '' || output_dataset_id || ' as dataset_id';
			insert_column_list_sql := insert_column_list_sql || '' || column_name;
		ELSIF column_name = 'delete_versions' THEN
			select_column_list_sql := select_column_list_sql || ','''' as delete_versions';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'version' THEN
			select_column_list_sql := select_column_list_sql || ',0 as version';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'inv.design_capacity' else 'null::double precision as design_capacity' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity_unit_numerator' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'inv.design_capacity_unit_numerator' else 'null::character varying(10) as design_capacity_unit_numerator' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity_unit_denominator' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'inv.design_capacity_unit_denominator' else 'null::character varying(10) as design_capacity_unit_denominator' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'current_cost' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_current_cost_col then 'inv.current_cost' else 'null::double precision as current_cost' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'cumulative_cost' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_cumulative_cost_col then 'inv.cumulative_cost' else 'null::double precision as cumulative_cost' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'control_measures' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_control_measures_col then '
			case 
				when control_measures is null or length(control_measures) = 0 then 
					case 
						when cr.cm_abbrev is not null then cr.cm_abbrev
						when proj.cm_abbrev is not null and cont.cm_abbrev is not null then proj.cm_abbrev || ''&'' || cont.cm_abbrev 
						when proj.cm_abbrev is null and cont.cm_abbrev is not null then cont.cm_abbrev 
						when proj.cm_abbrev is not null and cont.cm_abbrev is null then proj.cm_abbrev 
						else null::character varying 
					end 
				else control_measures || coalesce(''&'' || cr.cm_abbrev, '''')  || coalesce(''&'' || proj.cm_abbrev, '''') || coalesce(''&'' || cont.cm_abbrev, '''') 
			end' else '
			case 
				when cr.cm_abbrev is not null then cr.cm_abbrev
				when proj.cm_abbrev is not null and cont.cm_abbrev is not null then proj.cm_abbrev || ''&'' || cont.cm_abbrev 
				when proj.cm_abbrev is null and cont.cm_abbrev is not null then cont.cm_abbrev 
				when proj.cm_abbrev is not null and cont.cm_abbrev is null then proj.cm_abbrev 
				else null::character varying 
			end ' end || ' as control_measures';
--			select_column_list_sql := select_column_list_sql || ', case when control_measures is null or length(control_measures) = 0 then abbreviation else control_measures || ''&'' || abbreviation end as control_measures';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'pct_reduction' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_pct_reduction_col then '
			case 
				when pct_reduction is null or length(pct_reduction) = 0 then 
					case 
						when cr.adj_factor is not null then cr.adj_factor::character varying
						when proj.adj_factor is not null and cont.percent_reduction is not null then proj.adj_factor || ''&'' || cont.percent_reduction 
						when proj.adj_factor is null and cont.percent_reduction is not null then cont.percent_reduction::character varying
						when proj.adj_factor is not null and cont.percent_reduction is null then proj.adj_factor::character varying
						else ''''::character varying 
					end 
				else pct_reduction || coalesce(''&'' || proj.adj_factor, '''') || coalesce(''&'' || cont.percent_reduction, '''') 
			end' else '
			case 
				when cr.adj_factor is not null then cr.adj_factor::character varying
				when proj.adj_factor is not null and cont.percent_reduction is not null then proj.adj_factor || ''&'' || cont.percent_reduction 
				when proj.adj_factor is null and cont.percent_reduction is not null then cont.percent_reduction::character varying
				when proj.adj_factor is not null and cont.percent_reduction is null then proj.adj_factor::character varying
				else ''''::character varying 
			end ' end || ' as pct_reduction';
--			select_column_list_sql := select_column_list_sql || ', case when pct_reduction is null or length(pct_reduction) = 0 then efficiency::text else pct_reduction || ''&'' || efficiency::text end as pct_reduction';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'avd_emis' THEN
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is not null then cr.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				when cont.source_id is not null then cont.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				when proj.source_id is not null then proj.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				else inv.avd_emis 
			end as avd_emis';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'ann_emis' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			case 
				when dataset_month != 0 then 
					'null::double precision as ann_emis'
				else 
					'case 
						when cr.source_id is not null then cr.final_emissions 
						when cont.source_id is not null then cont.final_emissions 
						when proj.source_id is not null then proj.final_emissions 
						else inv.ann_emis 
					end as ann_emis'
			end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;

		--this column is only relevant to the FF10 inventories
		ELSIF column_name = 'ann_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.final_emissions 
				when cont.source_id is not null then cont.final_emissions 
				when proj.source_id is not null then proj.final_emissions 
				else inv.ann_value 
			end as ann_value';

/*
			case 
				--annual no monthly emission numbers
				when ' || is_monthly_source_sql || ' then

					coalesce(cr.jan_final_emissions,cont.jan_final_emissions,proj.jan_final_emissions,inv.jan_value,0.0)
					+coalesce(cr.feb_final_emissions,cont.feb_final_emissions,proj.feb_final_emissions,inv.feb_value,0.0)
					+coalesce(cr.mar_final_emissions,cont.mar_final_emissions,proj.mar_final_emissions,inv.mar_value,0.0)
					+coalesce(cr.apr_final_emissions,cont.apr_final_emissions,proj.apr_final_emissions,inv.apr_value,0.0)
					+coalesce(cr.may_final_emissions,cont.may_final_emissions,proj.may_final_emissions,inv.may_value,0.0)
					+coalesce(cr.jun_final_emissions,cont.jun_final_emissions,proj.jun_final_emissions,inv.jun_value,0.0)
					+coalesce(cr.jul_final_emissions,cont.jul_final_emissions,proj.jul_final_emissions,inv.jul_value,0.0)
					+coalesce(cr.aug_final_emissions,cont.aug_final_emissions,proj.aug_final_emissions,inv.aug_value,0.0)
					+coalesce(cr.sep_final_emissions,cont.sep_final_emissions,proj.sep_final_emissions,inv.sep_value,0.0)
					+coalesce(cr.oct_final_emissions,cont.oct_final_emissions,proj.oct_final_emissions,inv.oct_value,0.0)
					+coalesce(cr.nov_final_emissions,cont.nov_final_emissions,proj.nov_final_emissions,inv.nov_value,0.0)
					+coalesce(cr.dec_final_emissions,cont.dec_final_emissions,proj.dec_final_emissions,inv.dec_value,0.0)
				else
					coalesce(cr.final_emissions,cont.final_emissions,proj.final_emissions,inv.ann_value)
			end
*/
			
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jan_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.jan_final_emissions 
				when cont.source_id is not null then cont.jan_final_emissions 
				when proj.source_id is not null then proj.jan_final_emissions 
				else inv.jan_value 
			end as jan_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'feb_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.feb_final_emissions 
				when cont.source_id is not null then cont.feb_final_emissions 
				when proj.source_id is not null then proj.feb_final_emissions 
				else inv.feb_value 
			end as feb_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'mar_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.mar_final_emissions 
				when cont.source_id is not null then cont.mar_final_emissions 
				when proj.source_id is not null then proj.mar_final_emissions 
				else inv.mar_value 
			end as mar_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'apr_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.apr_final_emissions 
				when cont.source_id is not null then cont.apr_final_emissions 
				when proj.source_id is not null then proj.apr_final_emissions 
				else inv.apr_value 
			end as apr_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'may_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.may_final_emissions 
				when cont.source_id is not null then cont.may_final_emissions 
				when proj.source_id is not null then proj.may_final_emissions 
				else inv.may_value 
			end as may_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jun_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.jun_final_emissions 
				when cont.source_id is not null then cont.jun_final_emissions 
				when proj.source_id is not null then proj.jun_final_emissions 
				else inv.jun_value 
			end as jun_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jul_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.jul_final_emissions 
				when cont.source_id is not null then cont.jul_final_emissions 
				when proj.source_id is not null then proj.jul_final_emissions 
				else inv.jul_value 
			end as jul_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'aug_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.aug_final_emissions 
				when cont.source_id is not null then cont.aug_final_emissions 
				when proj.source_id is not null then proj.aug_final_emissions 
				else inv.aug_value 
			end as aug_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'sep_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.sep_final_emissions 
				when cont.source_id is not null then cont.sep_final_emissions 
				when proj.source_id is not null then proj.sep_final_emissions 
				else inv.sep_value 
			end as sep_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'oct_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.oct_final_emissions 
				when cont.source_id is not null then cont.oct_final_emissions 
				when proj.source_id is not null then proj.oct_final_emissions 
				else inv.oct_value 
			end as oct_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'nov_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.nov_final_emissions 
				when cont.source_id is not null then cont.nov_final_emissions 
				when proj.source_id is not null then proj.nov_final_emissions 
				else inv.nov_value 
			end as nov_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'dec_value' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'case 
				when cr.source_id is not null then cr.dec_final_emissions 
				when cont.source_id is not null then cont.dec_final_emissions 
				when proj.source_id is not null then proj.dec_final_emissions 
				else inv.dec_value 
			end as dec_value';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'projection_factor' THEN
			select_column_list_sql := select_column_list_sql || ', ' ||
			'coalesce(cr.adj_factor, proj.adj_factor) as projection_factor';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'ann_pct_red' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.percent_reduction
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.percent_reduction) * (1.0 - coalesce(inv.ann_pct_red,0.0))
						else inv.ann_pct_red 
					end
				when cr.source_id is not null then null::double precision 
				else inv.ann_pct_red 
			end as ann_pct_red';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jan_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.jan_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.percent_reduction
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.jan_pct_red) * (1.0 - coalesce(inv.jan_pctred,0.0))
						else inv.jan_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.jan_pctred 
			end as jan_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'feb_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.feb_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.feb_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.feb_pct_red) * (1.0 - coalesce(inv.feb_pctred,0.0))
						else inv.feb_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.feb_pctred 
			end as feb_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'mar_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.mar_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.mar_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.mar_pct_red) * (1.0 - coalesce(inv.mar_pctred,0.0))
						else inv.mar_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.mar_pctred 
			end as mar_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'apr_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.apr_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.apr_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.apr_pct_red) * (1.0 - coalesce(inv.apr_pctred,0.0))
						else inv.apr_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.apr_pctred 
			end as apr_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'may_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.may_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.may_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.may_pct_red) * (1.0 - coalesce(inv.may_pctred,0.0))
						else inv.may_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.may_pctred 
			end as may_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jun_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.jun_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.jun_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.jun_pct_red) * (1.0 - coalesce(inv.jun_pctred,0.0))
						else inv.jun_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.jun_pctred 
			end as jun_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'jul_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.jul_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.jul_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.jul_pct_red) * (1.0 - coalesce(inv.jul_pctred,0.0))
						else inv.jul_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.jul_pctred 
			end as feb_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'aug_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.aug_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.aug_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.aug_pct_red) * (1.0 - coalesce(inv.aug_pctred,0.0))
						else inv.aug_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.aug_pctred 
			end as aug_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'sep_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.sep_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.sep_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.sep_pct_red) * (1.0 - coalesce(inv.sep_pctred,0.0))
						else inv.sep_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.sep_pctred 
			end as sep_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'oct_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.oct_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.oct_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.oct_pct_red) * (1.0 - coalesce(inv.oct_pctred,0.0))
						else inv.oct_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.oct_pctred 
			end as oct_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'nov_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.nov_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.nov_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.nov_pct_red) * (1.0 - coalesce(inv.nov_pctred,0.0))
						else inv.nov_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.nov_pctred 
			end as nov_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'dec_pctred' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.dec_pct_red <> 0.0 then 
					case 
						when cont.replacement_addon = ''R'' then 
							cont.dec_pct_red
						when cont.replacement_addon = ''A'' then 
							1.0 - (1.0 - cont.dec_pct_red) * (1.0 - coalesce(inv.dec_pctred,0.0))
						else inv.dec_pctred 
					end 
				when cr.source_id is not null then null::double precision 
				else inv.dec_pctred 
			end as dec_pctred';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;



		ELSIF column_name = 'ceff' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then cont.percent_reduction
				when cr.source_id is not null then null::double precision 
				else inv.ceff 
			end as ceff';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;


		ELSIF column_name = 'reff' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then 100.0::double precision 
				when cr.source_id is not null then null::double precision 
				else inv.reff 
			end as reff';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'rpen' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then 100.0::double precision 
				when cr.source_id is not null then null::double precision 
				else inv.rpen 
			end as rpen';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSE
			select_column_list_sql := select_column_list_sql || ',inv.' || column_name;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		END IF;
--            } else if (columnName.equalsIgnoreCase("avd_emis")) {
--                sql += ", case when b.source_id is not null then b.final_emissions / " + (month != -1 ? noOfDaysInMonth : "365") + " else avd_emis end as avd_emis";
--                columnList += "," + columnName;
--            } else if (columnName.equalsIgnoreCase("ann_emis")) {
--                sql += ", case when b.source_id is not null then b.final_emissions else ann_emis end as ann_emis";
--                columnList += "," + columnName;
	END LOOP;

/*	select_column_list_sql := replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
		select_column_list_sql, 
	'scc', 'inv.scc'), 
	'fips', 'inv.fips'), 
	'comments', 'inv.comments'),  
	'pointid', 'inv.pointid'), 
	'stackid', 'inv.stackid'), 
	'segment', 'inv.segment'), 
	'poll', 'inv.poll'), 
	'naics', 'inv.naics'), 
	'sic', 'inv.sic'), 
	'plant', 'inv.plant');
*/

	-- populate the new inventory...work off of new data





	execute 
	--raise notice '%', 
	'insert into emissions.' || cont_inv_table_name|| ' 
		(
		' || insert_column_list_sql || ' 
		)
	select 
		' || select_column_list_sql || ' 
	from emissions.' || inv_table_name || ' inv

		-- deal with projections, an apply order of 1 delineates a projection...
		left outer join (

			SELECT source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				adj_factor,
				jan_final_emissions,
				feb_final_emissions,
				mar_final_emissions,
				apr_final_emissions,
				may_final_emissions,
				jun_final_emissions,
				jul_final_emissions,
				aug_final_emissions,
				sep_final_emissions,
				oct_final_emissions,
				nov_final_emissions,
				dec_final_emissions
			FROM emissions.' || detailed_result_table_name || '
			where apply_order = 1

		) proj
		on inv.record_id = proj.source_id

		-- deal with controls, an apply order of 2 delineates a control...
		left outer join (

			SELECT source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				percent_reduction,
				replacement_addon,
				jan_final_emissions,
				feb_final_emissions,
				mar_final_emissions,
				apr_final_emissions,
				may_final_emissions,
				jun_final_emissions,
				jul_final_emissions,
				aug_final_emissions,
				sep_final_emissions,
				oct_final_emissions,
				nov_final_emissions,
				dec_final_emissions,
				jan_pct_red,
				feb_pct_red,
				mar_pct_red,
				apr_pct_red,
				may_pct_red,
				jun_pct_red,
				jul_pct_red,
				aug_pct_red,
				sep_pct_red,
				oct_pct_red,
				nov_pct_red,
				dec_pct_red
			FROM emissions.' || detailed_result_table_name || '
			where apply_order = 2

		) cont
		on inv.record_id = cont.source_id

		-- deal with caps and replacements, an apply order of 3 and 4 delineates a control, only get the cap if there are both a cap and replacement...
		-- the distinct on source id and order by source_id, apply_order makes sure the cap is the first one to use 
		left outer join (

			SELECT distinct on (source_id) source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				adj_factor,
				jan_final_emissions,
				feb_final_emissions,
				mar_final_emissions,
				apr_final_emissions,
				may_final_emissions,
				jun_final_emissions,
				jul_final_emissions,
				aug_final_emissions,
				sep_final_emissions,
				oct_final_emissions,
				nov_final_emissions,
				dec_final_emissions
			FROM emissions.' || detailed_result_table_name || '
			where apply_order in (3,4)
			order by source_id, apply_order

		) cr
		on inv.record_id = cr.source_id

	where ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

		-- get rid of plant closures records
		and not exists (
			select 1 
			from emissions.' || detailed_result_table_name || ' dr
			where inv.record_id = dr.source_id
				and apply_order = 0)';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;