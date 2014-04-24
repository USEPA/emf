/*
select public.run_project_future_year_inventory(
	153, --int_control_strategy_id, 
	349, --input_dataset_id, 
	1, --input_dataset_version, 
	6117 --strategy_result_id
	);
SELECT public.run_project_future_year_inventory(137, 5048, 0, 6147);
SELECT public.run_project_future_year_inventory(137, 8292, 0, 6469);

select * from emf.control_strategies where name = 'new projection test2';
select * from emf.strategy_results where control_strategy_id = 162;

SELECT public.run_project_future_year_inventory(162, 8292, 0, 6517);
SELECT public.clean_project_future_year_inventory_control_programs(162);


*/

DROP FUNCTION IF EXISTS public.run_project_future_year_inventory(integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.run_project_future_year_inventory(
	int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer,
	run_packet_type varchar(64) = 'all'
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	control_program RECORD;
	target_pollutant_id integer := 0;
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	control_program_technologies_count integer := 0;
	control_program_measures_count integer := 0;
	county_dataset_filter_sql text := '';
	control_program_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year smallint := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	control_program_measure_min_pct_red_diff_constraint double precision := null;
	has_constraints boolean := null;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_mact_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	has_facility_name_column boolean := false;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	cont_packet_percent_reduction_sql character varying;
	inv_percent_reduction_sql character varying;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	sql character varying := '';
	compliance_date_cutoff_daymonth varchar(256) := '';
	effective_date_cutoff_daymonth varchar(256) := '';

	--support for flat file ds types...
	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'fips';
	plantid_expression character varying(64) := 'plantid';
	pointid_expression character varying(64) := 'pointid';
	stackid_expression character varying(64) := 'stackid';
	segment_expression character varying(64) := 'segment';
	is_flat_file_inventory boolean := false;
	is_flat_file_point_inventory boolean := false;
	jan_annualized_uncontrolled_emis_sql character varying;
	feb_annualized_uncontrolled_emis_sql character varying;
	mar_annualized_uncontrolled_emis_sql character varying;
	apr_annualized_uncontrolled_emis_sql character varying;
	may_annualized_uncontrolled_emis_sql character varying;
	jun_annualized_uncontrolled_emis_sql character varying;
	jul_annualized_uncontrolled_emis_sql character varying;
	aug_annualized_uncontrolled_emis_sql character varying;
	sep_annualized_uncontrolled_emis_sql character varying;
	oct_annualized_uncontrolled_emis_sql character varying;
	nov_annualized_uncontrolled_emis_sql character varying;
	dec_annualized_uncontrolled_emis_sql character varying;
	jan_uncontrolled_emis_sql character varying;
	feb_uncontrolled_emis_sql character varying;
	mar_uncontrolled_emis_sql character varying;
	apr_uncontrolled_emis_sql character varying;
	may_uncontrolled_emis_sql character varying;
	jun_uncontrolled_emis_sql character varying;
	jul_uncontrolled_emis_sql character varying;
	aug_uncontrolled_emis_sql character varying;
	sep_uncontrolled_emis_sql character varying;
	oct_uncontrolled_emis_sql character varying;
	nov_uncontrolled_emis_sql character varying;
	dec_uncontrolled_emis_sql character varying;
	jan_emis_sql character varying;
	feb_emis_sql character varying;
	mar_emis_sql character varying;
	apr_emis_sql character varying;
	may_emis_sql character varying;
	jun_emis_sql character varying;
	jul_emis_sql character varying;
	aug_emis_sql character varying;
	sep_emis_sql character varying;
	oct_emis_sql character varying;
	nov_emis_sql character varying;
	dec_emis_sql character varying;

	jan_inv_percent_reduction_sql character varying;
	feb_inv_percent_reduction_sql character varying;
	mar_inv_percent_reduction_sql character varying;
	apr_inv_percent_reduction_sql character varying;
	may_inv_percent_reduction_sql character varying;
	jun_inv_percent_reduction_sql character varying;
	jul_inv_percent_reduction_sql character varying;
	aug_inv_percent_reduction_sql character varying;
	sep_inv_percent_reduction_sql character varying;
	oct_inv_percent_reduction_sql character varying;
	nov_inv_percent_reduction_sql character varying;
	dec_inv_percent_reduction_sql character varying;
	
	jan_cont_packet_percent_reduction_sql character varying;
	feb_cont_packet_percent_reduction_sql character varying;
	mar_cont_packet_percent_reduction_sql character varying;
	apr_cont_packet_percent_reduction_sql character varying;
	may_cont_packet_percent_reduction_sql character varying;
	jun_cont_packet_percent_reduction_sql character varying;
	jul_cont_packet_percent_reduction_sql character varying;
	aug_cont_packet_percent_reduction_sql character varying;
	sep_cont_packet_percent_reduction_sql character varying;
	oct_cont_packet_percent_reduction_sql character varying;
	nov_cont_packet_percent_reduction_sql character varying;
	dec_cont_packet_percent_reduction_sql character varying;


	is_monthly_source_sql character varying := 'coalesce(jan_value,feb_value,mar_value,apr_value,may_value,jun_value,jul_value,aug_value,sep_value,oct_value,nov_value,dec_value) is not null';
	is_annual_source_sql character varying := 'coalesce(jan_value,feb_value,mar_value,apr_value,may_value,jun_value,jul_value,aug_value,sep_value,oct_value,nov_value,dec_value) is null and ann_value is not null';
	is_control_packet_extended_format boolean := false;
	projection_column_list varchar := 'ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor';
	number_of_days_in_year smallint;
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	--get dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = input_dataset_id
	into dataset_type_name;

	--if Flat File 2010 Types then change primary key field expression variables...
	IF dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		fips_expression := 'region_cd';
		plantid_expression := 'facility_id';
		pointid_expression := 'unit_id';
		stackid_expression := 'rel_point_id';
		segment_expression := 'process_id';
		is_flat_file_inventory := true;
		IF dataset_type_name = 'Flat File 2010 Point' THEN
			is_flat_file_point_inventory := true;
		END IF;
	ELSE
		fips_expression := 'fips';
		plantid_expression := 'plantid';
		pointid_expression := 'pointid';
		stackid_expression := 'stackid';
		segment_expression := 'segment';
	END If;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = int_control_strategy_id
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || cs.filter || ')' else null end,
--		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
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

	select date_part('doy', (('01/01/' || (inventory_year + 1))::date - '1 day'::interval))
	into number_of_days_in_year;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');

	-- see if there is a mact column in the inventory
	has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',');

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

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',')
		or public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_units', ',');

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is plant column in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');
	has_facility_name_column := public.check_table_for_columns(inv_table_name, 'facility_name', ',');

	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost,
		replacement_control_min_eff_diff,
		control_program_measure_min_pct_red_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = int_control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		replacement_control_min_eff_diff_constraint,
		control_program_measure_min_pct_red_diff_constraint;

	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

	--Get month and no of days in month for ONLY ORL inventories, Flat File have monthly and annual data defined in the table structure
	IF  Not (dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint') THEN
		-- get month of the dataset, 0 (Zero) indicates an annual inventory
		select public.get_dataset_month(input_dataset_id)
		into dataset_month;

		select public.get_days_in_month(dataset_month, inventory_year)
		into no_days_in_month;
	END IF;

	-- get gdp chained values
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = cost_year
	INTO cost_year_chained_gdp;
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = ref_cost_year
	INTO ref_cost_year_chained_gdp;

	chained_gdp_adjustment_factor := cost_year_chained_gdp / ref_cost_year_chained_gdp;

	-- load the Compliance and Effective Date Cutoff Day/Month (Stored as properties)
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_COMPLIANCE_DATE_CUTOFF_MONTHDAY'
	into compliance_date_cutoff_daymonth;
	compliance_date_cutoff_daymonth := coalesce(compliance_date_cutoff_daymonth, '07/01');	--default just in case
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_EFFECTIVE_DATE_CUTOFF_MONTHDAY'
	into effective_date_cutoff_daymonth;
	effective_date_cutoff_daymonth := coalesce(effective_date_cutoff_daymonth, '07/01');	--default just in case
	
	uncontrolled_emis_sql := 
		case 
			when is_flat_file_inventory then 
				'case when (1 - coalesce(case when inv.ann_pct_red = 100.0 and inv.ann_value > 0.0 then 0.0 else inv.ann_pct_red end / 100 , 0)) != 0 then inv.ann_value / (1 - coalesce(case when inv.ann_pct_red = 100.0 and inv.ann_value > 0.0 then 0.0 else inv.ann_pct_red end / 100 , 0)) else 0.0::double precision end' 
			when dataset_month != 0 then 
				'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			else 
				'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
		end;
	jan_uncontrolled_emis_sql := 'case when (1 - coalesce(case when inv.jan_pctred = 100.0 and inv.jan_value > 0.0 then 0.0 else inv.jan_pctred end / 100 , 0)) != 0 then inv.jan_value / (1 - coalesce(case when inv.jan_pctred = 100.0 and inv.jan_value > 0.0 then 0.0 else inv.jan_pctred end / 100 , 0)) else 0.0::double precision end';
	feb_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'feb_');
	mar_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'mar_');
	apr_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'apr_');
	may_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'may_');
	apr_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'apr_');
	jun_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'jun_');
	jul_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'jul_');
	aug_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'aug_');
	sep_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'sep_');
	oct_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'oct_');
	nov_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'nov_');
	dec_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'dec_');
	emis_sql := 
		case 
			when is_flat_file_inventory then 
				'inv.ann_value' 
			when dataset_month != 0 then 
				'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' 
			else 
				'inv.ann_emis' 
		end;
	jan_emis_sql := 'inv.jan_value';
	feb_emis_sql := 'inv.feb_value';
	mar_emis_sql := 'inv.mar_value';
	apr_emis_sql := 'inv.apr_value';
	may_emis_sql := 'inv.may_value';
	jun_emis_sql := 'inv.jun_value';
	jul_emis_sql := 'inv.jul_value';
	aug_emis_sql := 'inv.aug_value';
	sep_emis_sql := 'inv.sep_value';
	oct_emis_sql := 'inv.oct_value';
	nov_emis_sql := 'inv.nov_value';
	dec_emis_sql := 'inv.dec_value';
	annualized_uncontrolled_emis_sql := 
		case 
			when is_flat_file_inventory then 
				'case when (1 - coalesce(case when inv.ann_pct_red = 100.0 and inv.ann_value > 0.0 then 0.0 else inv.ann_pct_red end / 100 , 0)) != 0 then inv.ann_value / (1 - coalesce(case when inv.ann_pct_red = 100.0 and inv.ann_value > 0.0 then 0.0 else inv.ann_pct_red end / 100 , 0)) else 0.0::double precision end' 
			when dataset_month != 0 then 
				'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || number_of_days_in_year || ', inv.ann_emis) / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			else 
				'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
		end;
	jan_annualized_uncontrolled_emis_sql := 'case when (1 - coalesce(case when inv.jan_pctred = 100.0 and inv.jan_value > 0.0 then 0.0 else inv.jan_pctred end / 100 , 0)) != 0 then inv.jan_value / (1 - coalesce(case when inv.jan_pctred = 100.0 and inv.jan_value > 0.0 then 0.0 else inv.jan_pctred end / 100 , 0)) else 0.0::double precision end';
	feb_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'feb_');
	mar_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'mar_');
	apr_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'apr_');
	may_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'may_');
	apr_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'apr_');
	jun_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'jun_');
	jul_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'jul_');
	aug_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'aug_');
	sep_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'sep_');
	oct_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'oct_');
	nov_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'nov_');
	dec_annualized_uncontrolled_emis_sql := replace(jan_annualized_uncontrolled_emis_sql, 'jan_', 'dec_');






	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.' || fips_expression || ' in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
--	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');


--	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'first lets do plant closures ' || clock_timestamp();


	-- need to process based on processing_order of the program, i.e., first do plant closures, next do growth, then apply controls
	-- to various sources

	-- first lets do plant closures.
	IF run_packet_type = 'all' OR run_packet_type = 'closure' THEN
  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id
		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Plant Closure''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- apply plant closure control program
		IF control_program.type = 'Plant Closure' THEN

			execute
			--raise notice '%',
			 'insert into emissions.' || detailed_result_table_name || ' 
				(
				dataset_id,
				cm_abbrev,
				poll,
				scc,
				fips,
				' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
				annual_oper_maint_cost,
				annualized_capital_cost,
				total_capital_cost,
				annual_cost,
				ann_cost_per_ton,
				control_eff,
				rule_pen,
				rule_eff,
				percent_reduction,
				inv_ctrl_eff,
				inv_rule_pen,
				inv_rule_eff,
				final_emissions,
				emis_reduction,
				inv_emissions,
--				input_emis,
--				output_emis,
				apply_order,
				fipsst,
				fipscty,
				sic,
				naics,
				source_id,
				input_ds_id,
				cs_id,
				cm_id,
				equation_type,
				control_program,
				xloc,
				yloc,
				plant,
				"comment"
				)
			-- add distinct to limit to only one closure record, that is all that is needed...
			select distinct on (inv.record_id)
				' || detailed_result_dataset_id || '::integer,
				''PLTCLOSURE'' as abbreviation,
				inv.poll,
				inv.scc,
				inv.' || fips_expression || ',
				' || case when is_point_table = false then '' else 'inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || ', ' end || '
				null::double precision as operation_maintenance_cost,
				null::double precision as annualized_capital_cost,
				null::double precision as capital_cost,
				null::double precision as ann_cost,
				null::double precision as computed_cost_per_ton,
				null::double precision as control_eff,
				null::double precision as rule_pen,
				null::double precision as rule_eff,
				null::double precision as percent_reduction,
				' || case when is_flat_file_inventory then 'inv.ann_pct_red' else 'inv.ceff' end || ',
				' || case when is_flat_file_inventory then '100.0' when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ',
				' || case when is_flat_file_inventory then '100.0' else 'case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end' end || ',
				0.0 as final_emissions,
				' || emis_sql || ' as emis_reduction,
				' || emis_sql || ' as inv_emissions,
--				' || emis_sql || ' as input_emis,
--				0.0 as output_emis,
				0,
				substr(inv.' || fips_expression || ', 1, 2),
				substr(inv.' || fips_expression || ', 3, 3),
				' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
				' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
				inv.record_id::integer as source_id,
				' || input_dataset_id || '::integer,
				' || int_control_strategy_id || '::integer,
				null::integer as control_measures_id,
				null::varchar(255) as equation_type,
				' || quote_literal(control_program.control_program_name) || ',
				' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' when is_flat_file_point_inventory then 'inv.longitude,inv.latitude,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
				' || case when has_plant_column then 'inv.plant' when has_facility_name_column then 'inv.facility_name' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
				''''
			FROM emissions.' || inv_table_name || ' inv

				inner join emissions.' || control_program.table_name || ' pc
				on pc.fips = inv.' || fips_expression || '
				' || case when is_point_table then '
				and pc.plantid = inv.' || plantid_expression || '
				and coalesce(pc.pointid, inv.' || pointid_expression || ') = inv.' || pointid_expression || '
				and coalesce(pc.stackid, inv.' || stackid_expression || ') = inv.' || stackid_expression || '
				and coalesce(pc.segment, inv.' || segment_expression || ') = inv.' || segment_expression || '
				' else '' end || '

				-- only keep if before cutoff date
				and coalesce(pc.effective_date::timestamp without time zone, ''1/1/1900''::timestamp without time zone) < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone

				and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '

				' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
				on fipscode.state_county_fips = inv.' || fips_expression || '
				and fipscode.country_num = ''0''' else '' end || '

			where 	' || '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || public.alias_filter(inv_filter, inv_table_name, 'inv'::character varying(64)), '') || coalesce(county_dataset_filter_sql, '') || '
				' || case when not is_point_table then '
				and pc.plantid is null
				and pc.pointid is null
				and pc.stackid is null
				and pc.segment is null
				' else '
				' end || '';

		END IF;

	END LOOP;
	END IF;
	
	raise notice '%', 'next lets process projections ' || clock_timestamp();

	-- next lets process projections, need to union the the various program tables together, so we can make sure and get the most source specific projection
	IF run_packet_type = 'all' OR run_packet_type = 'projection' THEN
  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id

			inner join emf.datasets d
			on d.id = i.dataset_id

			inner join emf.dataset_types dt
			on dt.id = d.dataset_type
		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Projection''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- see if control packet is in the extended format
		is_control_packet_extended_format := public.check_table_for_columns(control_program.table_name, 'region_cd', ',');
		IF is_control_packet_extended_format THEN
			projection_column_list := 'ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor';
		ELSE
			projection_column_list := 'proj_factor as ann_proj_factor,null::double precision as jan_proj_factor,null::double precision as feb_proj_factor,null::double precision as mar_proj_factor,null::double precision as apr_proj_factor,null::double precision as may_proj_factor,null::double precision as jun_proj_factor,null::double precision as jul_proj_factor,null::double precision as aug_proj_factor,null::double precision as sep_proj_factor,null::double precision as oct_proj_factor,null::double precision as nov_proj_factor,null::double precision as dec_proj_factor';
		END IF;

		-- make sure the dataset type is right...
		IF strpos(control_program.dataset_type, 'Projection Packet') > 0 THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;



					
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select 
				/*distinct on (record_id)*/
				record_id,ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, ranking
			from (

				' || public.build_project_future_year_inventory_matching_hierarchy_sql(
				input_dataset_id, --inv_dataset_id integer, 
				input_dataset_version, --inv_dataset_version integer, 
				control_program.dataset_id, --control_program_dataset_id integer, 
				control_program.dataset_version, --control_program_dataset_version integer, 
				'' || projection_column_list ||'', --select_columns varchar, 
				inv_filter, --inv_filter text, --not aliased
				county_dataset_id, --1279 county_dataset_id integer,
				county_dataset_version, --county_dataset_version integer,
				case 
					when is_flat_file_inventory and is_control_packet_extended_format then --include monthly packets
						'(
							(' || is_annual_source_sql || ' and ann_proj_factor is not null)
							or 
							(' || is_monthly_source_sql || ' and coalesce(ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor) is not null)

						)'
					when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
						'ann_proj_factor is not null'
					else --dont include monthly packets
						'proj_factor is not null'
				end,
				1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
				) || '
/*				order by record_id, ranking, 
					case 
						when ' || is_monthly_source_sql || ' and coalesce(jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor) is not null then 
							2 --give highest priority
						when ' || is_monthly_source_sql || ' and ann_proj_factor is not null then 
							1 --give second highest priority
						else
							0
*/
			) tbl';

/*
			sql := sql || '
				select fips, 
					scc, 
					poll, 
					PROJ_FACTOR, 
					sic, 
					mact, 
					plantid, 
					pointid, 
					stackid, 
					segment,
					' || quote_literal(control_program.control_program_name) || ' as control_program_name
				from emissions.' || control_program.table_name || ' p
				where ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'p') || '';
*/
		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		sql := 'select distinct on (tbl.record_id)
				tbl.record_id,ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor,
				control_program_name, ranking
			from (' || sql;
		sql := sql || ') tbl 
				inner join emissions.' || inv_table_name || ' inv
				on inv.record_id = tbl.record_id
			
			order by tbl.record_id, tbl.ranking ' 
				||
				case 
					when is_flat_file_inventory then 
						',case 
							when ' || is_monthly_source_sql || ' and coalesce(jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor) is not null then 
								2 --give highest priority
							when ' || is_monthly_source_sql || ' and ann_proj_factor is not null then 
								1 --give second highest priority
							else
								0
						end desc'
					else
						''
				end || '
		';

		-- make sure the apply order is 1, this should be the first thing happening to a source....this is important when the controlled inventpory is created.
		execute 
		--raise notice '%', 
			'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			adj_factor,
			inv_ctrl_eff,
			inv_rule_pen,
			inv_rule_eff,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
			apply_order,
			fipsst,
			fipscty,
			sic,
			naics,
			source_id,
			input_ds_id,
			cs_id,
			cm_id,
			equation_type,
			control_program,
			xloc,
			yloc,
			plant,
			"comment",
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
			)
		select 
			' || detailed_result_dataset_id || '::integer,
			''PROJECTION'' as abbreviation,
			inv.poll,
			inv.scc,
			inv.' || fips_expression || ',
			' || case when is_point_table = false then '' else 'inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || ', ' end || '
			null::double precision as operation_maintenance_cost,
			null::double precision as annualized_capital_cost,
			null::double precision as capital_cost,
			null::double precision as ann_cost,
			null::double precision as computed_cost_per_ton,
			null::double precision as control_eff,
			null::double precision as rule_pen,
			null::double precision as rule_eff,
			null::double precision as percent_reduction,
/*			proj.ann_PROJ_FACTOR as adj_factor,*/

			case when coalesce((' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || '),0.0) <> 0.0 then
				(
				' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						'' || emis_sql || ' * proj.ann_proj_factor'
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce((' || jan_emis_sql || ') * coalesce(proj.jan_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || feb_emis_sql || ') * coalesce(proj.feb_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || mar_emis_sql || ') * coalesce(proj.mar_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || apr_emis_sql || ') * coalesce(proj.apr_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || may_emis_sql || ') * coalesce(proj.may_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || jun_emis_sql || ') * coalesce(proj.jun_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || jul_emis_sql || ') * coalesce(proj.jul_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || aug_emis_sql || ') * coalesce(proj.aug_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || sep_emis_sql || ') * coalesce(proj.sep_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || oct_emis_sql || ') * coalesce(proj.oct_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || nov_emis_sql || ') * coalesce(proj.nov_proj_factor, proj.ann_proj_factor), 0.0) 
								+ coalesce((' || dec_emis_sql || ') * coalesce(proj.dec_proj_factor, proj.ann_proj_factor), 0.0)


							else -- no monthly values to worry about
								' || emis_sql || ' * proj.ann_proj_factor
						end'
				end || '
				) 
				/ (' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						emis_sql
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(' || jan_emis_sql || ', 0.0)
								+ coalesce(' || feb_emis_sql || ', 0.0)
								+ coalesce(' || mar_emis_sql || ', 0.0)
								+ coalesce(' || apr_emis_sql || ', 0.0)
								+ coalesce(' || may_emis_sql || ', 0.0)
								+ coalesce(' || jun_emis_sql || ', 0.0)
								+ coalesce(' || jul_emis_sql || ', 0.0)
								+ coalesce(' || aug_emis_sql || ', 0.0)
								+ coalesce(' || sep_emis_sql || ', 0.0)
								+ coalesce(' || oct_emis_sql || ', 0.0)
								+ coalesce(' || nov_emis_sql || ', 0.0)
								+ coalesce(' || dec_emis_sql || ', 0.0)

							else -- no monthly values to worry about
								' || emis_sql || '
						end'
				end || ')
			else
				0.0
			end as adj_factor,


			' || case when is_flat_file_inventory then 'inv.ann_pct_red' else 'inv.ceff' end || ',
			' || case when is_flat_file_inventory then '100.0' when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ',
			' || case when is_flat_file_inventory then '100.0' else 'case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end' end || ',


--currently, not storing monthly numbers summed up here, will do this is in controlled inventory creation process




/*			' || emis_sql || ' * proj.ann_proj_factor as final_emissions,*/

			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'' || emis_sql || ' * proj.ann_proj_factor'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce((' || jan_emis_sql || ') * coalesce(proj.jan_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || feb_emis_sql || ') * coalesce(proj.feb_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || mar_emis_sql || ') * coalesce(proj.mar_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || apr_emis_sql || ') * coalesce(proj.apr_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || may_emis_sql || ') * coalesce(proj.may_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || jun_emis_sql || ') * coalesce(proj.jun_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || jul_emis_sql || ') * coalesce(proj.jul_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || aug_emis_sql || ') * coalesce(proj.aug_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || sep_emis_sql || ') * coalesce(proj.sep_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || oct_emis_sql || ') * coalesce(proj.oct_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || nov_emis_sql || ') * coalesce(proj.nov_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || dec_emis_sql || ') * coalesce(proj.dec_proj_factor, proj.ann_proj_factor), 0.0)


						else -- no monthly values to worry about
							' || emis_sql || ' * proj.ann_proj_factor
					end'
			end || ' as final_emissions,


/*			' || emis_sql || ' - ' || emis_sql || ' * proj.ann_proj_factor as emis_reduction,*/
			(' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || ') 
			- (
			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'' || emis_sql || ' * proj.ann_proj_factor'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce((' || jan_emis_sql || ') * coalesce(proj.jan_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || feb_emis_sql || ') * coalesce(proj.feb_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || mar_emis_sql || ') * coalesce(proj.mar_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || apr_emis_sql || ') * coalesce(proj.apr_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || may_emis_sql || ') * coalesce(proj.may_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || jun_emis_sql || ') * coalesce(proj.jun_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || jul_emis_sql || ') * coalesce(proj.jul_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || aug_emis_sql || ') * coalesce(proj.aug_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || sep_emis_sql || ') * coalesce(proj.sep_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || oct_emis_sql || ') * coalesce(proj.oct_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || nov_emis_sql || ') * coalesce(proj.nov_proj_factor, proj.ann_proj_factor), 0.0) 
							+ coalesce((' || dec_emis_sql || ') * coalesce(proj.dec_proj_factor, proj.ann_proj_factor), 0.0)


						else -- no monthly values to worry about
							' || emis_sql || ' * proj.ann_proj_factor
					end'
			end || '
			) as emis_reduction,

/*			' || emis_sql || ' as inv_emissions,*/
			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || ' as inv_emissions,

	--				' || emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			1,


			substr(inv.' || fips_expression || ', 1, 2),
			substr(inv.' || fips_expression || ', 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || int_control_strategy_id || '::integer,
			null::integer as control_measures_id,
			null::varchar(255) as equation_type,
			proj.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' when is_flat_file_point_inventory then 'inv.longitude,inv.latitude,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when has_facility_name_column then 'inv.facility_name' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			'''',
			' || case when is_flat_file_inventory then 
				jan_emis_sql || ' * coalesce(proj.jan_proj_factor, proj.ann_proj_factor) as jan_final_emissions,'
				 || feb_emis_sql || ' * coalesce(proj.feb_proj_factor, proj.ann_proj_factor) as feb_final_emissions,'
				 || mar_emis_sql || ' * coalesce(proj.mar_proj_factor, proj.ann_proj_factor) as mar_final_emissions,'
				 || apr_emis_sql || ' * coalesce(proj.apr_proj_factor, proj.ann_proj_factor) as apr_final_emissions,'
				 || may_emis_sql || ' * coalesce(proj.may_proj_factor, proj.ann_proj_factor) as may_final_emissions,'
				 || jun_emis_sql || ' * coalesce(proj.jun_proj_factor, proj.ann_proj_factor) as jun_final_emissions,'
				 || jul_emis_sql || ' * coalesce(proj.jul_proj_factor, proj.ann_proj_factor) as jul_final_emissions,'
				 || aug_emis_sql || ' * coalesce(proj.aug_proj_factor, proj.ann_proj_factor) as aug_final_emissions,'
				 || sep_emis_sql || ' * coalesce(proj.sep_proj_factor, proj.ann_proj_factor) as sep_final_emissions,'
				 || oct_emis_sql || ' * coalesce(proj.oct_proj_factor, proj.ann_proj_factor) as oct_final_emissions,'
				 || nov_emis_sql || ' * coalesce(proj.nov_proj_factor, proj.ann_proj_factor) as nov_final_emissions,'
				 || dec_emis_sql || ' * coalesce(proj.dec_proj_factor, proj.ann_proj_factor) as dec_final_emissions'
				 
			else 
				'null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision'
			end || '
		FROM emissions.' || inv_table_name || ' inv

			inner join (' || sql || ') proj
			on proj.record_id = inv.record_id

			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.' || fips_expression || '
			and fipscode.country_num = ''0''' else '' end || '

		where 	

			--remove plant closures from consideration
			inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)';
	END IF;
	END IF;

	-- next lets process controls, need to union the the various program tables together, so we can make sure and get the most source specific controls
	sql := ''; --reset
	IF run_packet_type = 'all' OR run_packet_type = 'control' THEN
	-- see if control strategy program have specific measures or technologies specified
	select count(cptech.id),
		count(cpm.id)
	 from emf.control_strategy_programs csp
		inner join emf.control_programs cp
		on cp.id = csp.control_program_id

		inner join emf.control_program_types cpt
		on cpt.id = cp.control_program_type_id

		left outer join emf.control_program_technologies cptech
		on cptech.control_program_id = cp.id
		
		left outer join emf.control_program_measures cpm
		on cpm.control_program_id = cp.id
		
	where csp.control_strategy_id = int_control_strategy_id
		and cpt."name" = 'Control'
	into control_program_technologies_count, 
		control_program_measures_count;
  	FOR control_program IN EXECUTE 
		'select cp.id as control_program_id, cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type,
			(select count(*) from emf.control_program_technologies cptech where cptech.control_program_id = cp.id) as control_program_technologies_count,
			(select count(*) from emf.control_program_measures cpm where cpm.control_program_id = cp.id) as control_program_measures_count
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id

			inner join emf.datasets d
			on d.id = i.dataset_id

			inner join emf.dataset_types dt
			on dt.id = d.dataset_type

		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Control''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- see if control packet is in the extended format
		is_control_packet_extended_format := public.check_table_for_columns(control_program.table_name, 'region_cd', ',');

		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Control Packet' or control_program.dataset_type = 'Control Packet Extended' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select 
				--distinct on (record_id)
				record_id,jan_pctred,feb_pctred,mar_pctred,apr_pctred,may_pctred,jun_pctred,jul_pctred,aug_pctred,sep_pctred,oct_pctred,nov_pctred,dec_pctred,ann_pctred,pri_cm_abbrev,replacement,compliance_date,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, ' || control_program.control_program_id || ' as control_program_id,
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (

				' || 
		public.build_project_future_year_inventory_matching_hierarchy_sql(
			input_dataset_id, --inv_dataset_id integer, 
			input_dataset_version, --inv_dataset_version integer, 
			control_program.dataset_id, --control_program_dataset_id integer, 
			control_program.dataset_version, --control_program_dataset_version integer, 
			case when not is_control_packet_extended_format then
				'null::double precision as jan_pctred,null::double precision as feb_pctred,null::double precision as mar_pctred,null::double precision as apr_pctred,null::double precision as may_pctred,null::double precision as jun_pctred,null::double precision as jul_pctred,null::double precision as aug_pctred,null::double precision as sep_pctred,null::double precision as oct_pctred,null::double precision as nov_pctred,null::double precision as dec_pctred,(ceff * coalesce(reff, 100) / 100 * coalesce(rpen, 100) / 100) as ann_pctred,pri_cm_abbrev,replacement,compliance_date'
			else 
				'jan_pctred,feb_pctred,mar_pctred,apr_pctred,may_pctred,jun_pctred,jul_pctred,aug_pctred,sep_pctred,oct_pctred,nov_pctred,dec_pctred,ann_pctred,pri_cm_abbrev,replacement,compliance_date'
			end 
			, --select_columns varchar, 
			inv_filter, --inv_filter text,
			county_dataset_id, --1279 county_dataset_id integer,
			county_dataset_version, --county_dataset_version integer,
			'application_control = ''Y'' and coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone'

			|| case 
				when is_flat_file_inventory and is_control_packet_extended_format then --include monthly packets
					' and (
						(' || is_annual_source_sql || ' and ann_pctred is not null)
						or 
						(' || is_monthly_source_sql || ' and coalesce(ann_pctred,jan_pctred,feb_pctred,mar_pctred,apr_pctred,may_pctred,jun_pctred,jul_pctred,aug_pctred,sep_pctred,oct_pctred,nov_pctred,dec_pctred) is not null)

					)'
				when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
					' and ann_pctred is not null'
				else --dont include monthly packets
					' and ceff is not null'
			end
			,
			1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
			)


				|| '

				--order by record_id, replacement, ranking, coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc
			 ) tbl';
		END IF;

	END LOOP;
--		raise notice '%', sql;



	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do controls ' || clock_timestamp();

		-- break ties on ranking, addon first then replacement, monthly control factor takes precedence over yearly if applicable, then compliance date breaks final tie
		sql := 'select distinct on (tbl.record_id)
				tbl.record_id,tbl.jan_pctred,tbl.feb_pctred,tbl.mar_pctred,tbl.apr_pctred,tbl.may_pctred,tbl.jun_pctred,tbl.jul_pctred,tbl.aug_pctred,tbl.sep_pctred,tbl.oct_pctred,tbl.nov_pctred,tbl.dec_pctred,tbl.ann_pctred,tbl.pri_cm_abbrev,replacement,compliance_date,
				control_program_name, control_program_id,
				control_program_technologies_count, control_program_measures_count, 
				ranking
			from (' || sql;
		sql := sql || ') tbl 
				inner join emissions.' || inv_table_name || ' inv
				on inv.record_id = tbl.record_id
			order by tbl.record_id, ranking, replacement, ' 
				||
				case 
					when is_flat_file_inventory then 
						'case 
							when ' || is_monthly_source_sql || ' and coalesce(tbl.jan_pctred,tbl.feb_pctred,tbl.mar_pctred,tbl.apr_pctred,tbl.may_pctred,tbl.jun_pctred,tbl.jul_pctred,tbl.aug_pctred,tbl.sep_pctred,tbl.oct_pctred,tbl.nov_pctred,tbl.dec_pctred) is not null then 
								2 --give highest priority
							when ' || is_monthly_source_sql || ' and tbl.ann_pctred is not null then 
								1 --give second highest priority
							else
								0
						end desc,'
					else
						''
				end || 'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc';

		inv_percent_reduction_sql := 
			case 
				when is_flat_file_inventory then 
					'(coalesce(case when inv.ann_pct_red = 100.0 and inv.ann_value > 0.0 then 0.0 else inv.ann_pct_red end, 0.0))'
				else 
					'(coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end, 0.0) * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end, 100) / 100 ' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end, 100.0) / 100.0 ' else '' end || ')'
			end;
		jan_inv_percent_reduction_sql := 
			case 
				when is_flat_file_inventory then 
					'(coalesce(case when inv.jan_pctred = 100.0 and inv.jan_value > 0.0 then 0.0 else inv.jan_pctred end,' || inv_percent_reduction_sql || ', 0.0))'
				else 
					'null::double precision'
			end;
		feb_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'feb_');
		mar_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'mar_');
		apr_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'apr_');
		may_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'may_');
		apr_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'apr_');
		jun_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'jun_');
		jul_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'jul_');
		aug_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'aug_');
		sep_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'sep_');
		oct_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'oct_');
		nov_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'nov_');
		dec_inv_percent_reduction_sql := replace(jan_inv_percent_reduction_sql, 'jan_', 'dec_');

		cont_packet_percent_reduction_sql := '(cont.ann_pctred)';
		jan_cont_packet_percent_reduction_sql := 'coalesce(cont.jan_pctred,cont.ann_pctred)';
		feb_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'feb_');
		mar_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'mar_');
		apr_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'apr_');
		may_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'may_');
		jun_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'jun_');
		jul_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'jul_');
		aug_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'aug_');
		sep_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'sep_');
		oct_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'oct_');
		nov_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'nov_');
		dec_cont_packet_percent_reduction_sql := replace(jan_cont_packet_percent_reduction_sql, 'jan_', 'dec_');

		uncontrolled_emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when (1 - ' || inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_value end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
				when dataset_month != 0 then 
					'case when (1 - ' || inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
				else 
					'case when (1 - ' || inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
			end;
		jan_uncontrolled_emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when (1 - ' || jan_inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then coalesce(dr.jan_final_emissions,inv.jan_value) else inv.jan_value end / (1 - coalesce(' || jan_inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
				else 
					'null::double precision' 
			end;
		feb_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'feb_');
		mar_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'mar_');
		apr_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'apr_');
		may_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'may_');
		apr_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'apr_');
		jun_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'jun_');
		jul_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'jul_');
		aug_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'aug_');
		sep_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'sep_');
		oct_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'oct_');
		nov_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'nov_');
		dec_uncontrolled_emis_sql := replace(jan_uncontrolled_emis_sql, 'jan_', 'dec_');


		emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when dr.record_id is not null then dr.final_emissions else inv.ann_value end' 
				when dataset_month != 0 then 
					'case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end' 
				else 
					'case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end' 
			end;
		jan_emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when dr.record_id is not null then coalesce(dr.jan_final_emissions,inv.jan_value) else inv.jan_value end' 
				else 
					'null::double precision' 
			end;
		feb_emis_sql := replace(jan_emis_sql, 'jan_', 'feb_');
		mar_emis_sql := replace(jan_emis_sql, 'jan_', 'mar_');
		apr_emis_sql := replace(jan_emis_sql, 'jan_', 'apr_');
		may_emis_sql := replace(jan_emis_sql, 'jan_', 'may_');
		apr_emis_sql := replace(jan_emis_sql, 'jan_', 'apr_');
		jun_emis_sql := replace(jan_emis_sql, 'jan_', 'jun_');
		jul_emis_sql := replace(jan_emis_sql, 'jan_', 'jul_');
		aug_emis_sql := replace(jan_emis_sql, 'jan_', 'aug_');
		sep_emis_sql := replace(jan_emis_sql, 'jan_', 'sep_');
		oct_emis_sql := replace(jan_emis_sql, 'jan_', 'oct_');
		nov_emis_sql := replace(jan_emis_sql, 'jan_', 'nov_');
		dec_emis_sql := replace(jan_emis_sql, 'jan_', 'dec_');

		annualized_uncontrolled_emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when (1 - coalesce(' || inv_percent_reduction_sql || ' / 100, 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_value end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end'
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || inv_percent_reduction_sql || ' / 100, 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || number_of_days_in_year || ', inv.ann_emis) end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || inv_percent_reduction_sql || ' / 100, 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end'
			end;



		get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
				abbreviation, ' || discount_rate|| ', 
				m.equipment_life, er.cap_ann_ratio, 
				er.cap_rec_factor, er.ref_yr_cost_per_ton, 
				case 
					when cont.replacement = ''R'' then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100.0
					when cont.replacement = ''A'' then ' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100.0
				end
				, ' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision), 
				' || case when use_cost_equations then 
				'et.name, 
				eq.value1, eq.value2, 
				eq.value3, eq.value4, 
				eq.value5, eq.value6, 
				eq.value7, eq.value8, 
				eq.value9, eq.value10, 
				' || case when not is_point_table then 'null' else 'inv.stkflow' end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity' end end || ', 
				' || case when not is_point_table then 'null' when is_flat_file_point_inventory then '(string_to_array(inv.design_capacity_units, ''/''))[1]' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_numerator' end end || ', ' || case when not is_point_table then 'null' when is_flat_file_point_inventory then '(string_to_array(inv.design_capacity_units, ''/''))[2]' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_denominator' end end 
				else
				'null, 
				null, null, 
				null, null, 
				null, null, 
				null, null, 
				null, null, 
				null, null, 
				null, null'
				end
				|| ',' || case when is_flat_file_inventory then 'inv.ann_pct_red' else 'inv.ceff' end || ', ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton))';


		-- make sure the apply order is 1, this should be the first thing happening to a source....this is important when the controlled inventpory is created.
		-- the apply order will dictate how the 
		execute 
		--raise notice '%', 
		'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			inv_ctrl_eff,
			inv_rule_pen,
			inv_rule_eff,
			adj_factor,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
			apply_order,
			fipsst,
			fipscty,
			sic,
			naics,
			source_id,
			input_ds_id,
			cs_id,
			cm_id,
			equation_type,
			control_program,
			xloc,
			yloc,
			plant,
			REPLACEMENT_ADDON,
			"comment",
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
			)
		select distinct on (inv.record_id)
			' || detailed_result_dataset_id || '::integer,
			coalesce(cont.pri_cm_abbrev, case when cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
			inv.poll,
			inv.scc,
			inv.' || fips_expression || ',
			' || case when is_point_table = false then '' else 'inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || ', ' end || '
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost else null::double precision end as operation_maintenance_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost else null::double precision end as annualized_capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost else null::double precision end as capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost else null::double precision end as ann_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton else null::double precision end as computed_cost_per_ton,
			case 
				when coalesce((' 
				|| case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					uncontrolled_emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || feb_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || mar_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || apr_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || may_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || jun_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || jul_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || aug_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || sep_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || oct_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || nov_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || dec_uncontrolled_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || uncontrolled_emis_sql || '
					end'
			end || '),0.0) <> 0.0 then
				(' || 
				case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						'case 
							when cont.replacement = ''R'' then 
								case 
									when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
									else 0.0 
								end 
							when cont.replacement = ''A'' then 
								' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
						end'
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jan_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jan_uncontrolled_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jan_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || feb_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || feb_uncontrolled_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || feb_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || mar_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || mar_uncontrolled_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || mar_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || apr_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || apr_uncontrolled_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || apr_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || may_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || may_uncontrolled_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || may_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jun_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jun_uncontrolled_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jun_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jul_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jul_uncontrolled_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jul_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || aug_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || aug_uncontrolled_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || aug_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || sep_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || sep_uncontrolled_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || sep_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || oct_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || oct_uncontrolled_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || oct_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || nov_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || nov_uncontrolled_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || nov_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || dec_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || dec_uncontrolled_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || dec_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)

							else -- no monthly values to worry about
								case 
									when cont.replacement = ''R'' then 
										case 
											when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
											else 0.0 
										end 
									when cont.replacement = ''A'' then 
										' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
								end
						end'
				end || ') 
				/ (' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						uncontrolled_emis_sql
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(' || jan_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || feb_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || mar_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || apr_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || may_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || jun_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || jul_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || aug_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || sep_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || oct_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || nov_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || dec_uncontrolled_emis_sql || ', 0.0)

							else -- no monthly values to worry about
								' || uncontrolled_emis_sql || '
						end'
				end || ') * 100.0 
			else
				0.0
			end as control_eff,
/*			case 
				when cont.replacement = ''R'' then cont.ann_pctred 
				when cont.replacement = ''A'' then ' || inv_percent_reduction_sql || ' + ' || cont_packet_percent_reduction_sql || ' * ( 100.0 - ' || inv_percent_reduction_sql || ') / 100.0
			end as control_eff,*/
			100.0 as rule_pen,
			100.0 as rule_eff,
			case 
				when coalesce((' 
				|| case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					uncontrolled_emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || feb_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || mar_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || apr_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || may_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || jun_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || jul_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || aug_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || sep_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || oct_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || nov_uncontrolled_emis_sql || ', 0.0)
							+ coalesce(' || dec_uncontrolled_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || uncontrolled_emis_sql || '
					end'
			end || '),0.0) <> 0.0 then
				(' || 
				case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						'case 
							when cont.replacement = ''R'' then 
								case 
									when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
									else 0.0 
								end 
							when cont.replacement = ''A'' then 
								' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
						end'
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jan_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jan_uncontrolled_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jan_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || feb_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || feb_uncontrolled_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || feb_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || mar_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || mar_uncontrolled_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || mar_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || apr_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || apr_uncontrolled_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || apr_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || may_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || may_uncontrolled_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || may_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jun_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jun_uncontrolled_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jun_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || jul_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || jul_uncontrolled_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || jul_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || aug_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || aug_uncontrolled_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || aug_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || sep_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || sep_uncontrolled_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || sep_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || oct_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || oct_uncontrolled_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || oct_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || nov_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || nov_uncontrolled_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || nov_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)
								+ coalesce(case 
									when cont.replacement = ''R'' then 
										case 
											when coalesce(' || dec_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
											' || dec_uncontrolled_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
											else 0.0
										end 
									when cont.replacement = ''A'' then ' || dec_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
								end, 0.0)

							else -- no monthly values to worry about
								case 
									when cont.replacement = ''R'' then 
										case 
											when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
											else 0.0 
										end 
									when cont.replacement = ''A'' then 
										' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
								end
						end'
				end || ') 
				/ (' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						uncontrolled_emis_sql
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(' || jan_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || feb_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || mar_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || apr_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || may_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || jun_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || jul_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || aug_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || sep_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || oct_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || nov_uncontrolled_emis_sql || ', 0.0)
								+ coalesce(' || dec_uncontrolled_emis_sql || ', 0.0)

							else -- no monthly values to worry about
								' || uncontrolled_emis_sql || '
						end'
				end || ') * 100.0 
			else
				0.0
			end as percent_reduction,

/*			case 
				when cont.replacement = ''R'' then ' || cont_packet_percent_reduction_sql || '
				when cont.replacement = ''A'' then ' || inv_percent_reduction_sql || ' + ' || cont_packet_percent_reduction_sql || ' * ( 100.0 - ' || inv_percent_reduction_sql || ') / 100.0
			end as percent_reduction,*/
			' || case when is_flat_file_inventory then 'inv.ann_pct_red' else 'inv.ceff' end || ' as inv_ceff,
			' || case when is_flat_file_inventory then '100.0' when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ' as inv_rpen,
			' || case when is_flat_file_inventory then '100.0' else 'case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end' end || ' as inv_reff,
			null::double precision as adj_factor,

			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'case 
						when cont.replacement = ''R'' then 
							case 
								when ' || cont_packet_percent_reduction_sql || ' <> 0 then 
								' || uncontrolled_emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100) 
								else ' || emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100)
					end'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || jan_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || jan_uncontrolled_emis_sql || ' * (1 - ' || jan_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || jan_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || jan_emis_sql || ' * (1 - ' || jan_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || feb_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || feb_uncontrolled_emis_sql || ' * (1 - ' || feb_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || feb_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || feb_emis_sql || ' * (1 - ' || feb_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || mar_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || mar_uncontrolled_emis_sql || ' * (1 - ' || mar_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || mar_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || mar_emis_sql || ' * (1 - ' || mar_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || apr_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || apr_uncontrolled_emis_sql || ' * (1 - ' || apr_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || apr_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || apr_emis_sql || ' * (1 - ' || apr_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || may_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || may_uncontrolled_emis_sql || ' * (1 - ' || may_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || may_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || may_emis_sql || ' * (1 - ' || may_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || jun_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || jun_uncontrolled_emis_sql || ' * (1 - ' || jun_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || jun_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || jun_emis_sql || ' * (1 - ' || jun_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || jul_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || jul_uncontrolled_emis_sql || ' * (1 - ' || jul_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || jul_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || jul_emis_sql || ' * (1 - ' || jul_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || aug_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || aug_uncontrolled_emis_sql || ' * (1 - ' || aug_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || aug_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || aug_emis_sql || ' * (1 - ' || aug_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || sep_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || sep_uncontrolled_emis_sql || ' * (1 - ' || sep_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || sep_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || sep_emis_sql || ' * (1 - ' || sep_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || oct_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || oct_uncontrolled_emis_sql || ' * (1 - ' || oct_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || oct_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || oct_emis_sql || ' * (1 - ' || oct_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || nov_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || nov_uncontrolled_emis_sql || ' * (1 - ' || nov_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || nov_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || nov_emis_sql || ' * (1 - ' || nov_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when ' || dec_cont_packet_percent_reduction_sql || ' <> 0 then 
										' || dec_uncontrolled_emis_sql || ' * (1 - ' || dec_cont_packet_percent_reduction_sql || ' / 100) 
										else ' || dec_emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || dec_emis_sql || ' * (1 - ' || dec_cont_packet_percent_reduction_sql || ' / 100)
							end, 0.0)

						else -- no monthly values to worry about
							case 
								when cont.replacement = ''R'' then 
									case 
										when ' || cont_packet_percent_reduction_sql || ' <> 0 then 
										' || uncontrolled_emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100) 
										else ' || emis_sql || ' 
									end 
								when cont.replacement = ''A'' then ' || emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100)
							end
					end'
			end || ' as final_emissions,

			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'case 
						when cont.replacement = ''R'' then 
							case 
								when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
								else 0.0 
							end 
						when cont.replacement = ''A'' then 
							' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
					end'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || jan_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || jan_uncontrolled_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || jan_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || feb_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || feb_uncontrolled_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || feb_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || mar_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || mar_uncontrolled_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || mar_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || apr_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || apr_uncontrolled_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || apr_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || may_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || may_uncontrolled_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || may_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || jun_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || jun_uncontrolled_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || jun_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || jul_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || jul_uncontrolled_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || jul_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || aug_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || aug_uncontrolled_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || aug_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || sep_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || sep_uncontrolled_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || sep_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || oct_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || oct_uncontrolled_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || oct_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || nov_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || nov_uncontrolled_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || nov_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)
							+ coalesce(case 
								when cont.replacement = ''R'' then 
									case 
										when coalesce(' || dec_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
										' || dec_uncontrolled_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
										else 0.0
									end 
								when cont.replacement = ''A'' then ' || dec_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
							end, 0.0)

						else -- no monthly values to worry about
							case 
								when cont.replacement = ''R'' then 
									case 
										when ' || cont_packet_percent_reduction_sql || ' <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
										else 0.0 
									end 
								when cont.replacement = ''A'' then 
									' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
							end
					end'
			end || ' as emis_reduction,
			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || ' as inv_emissions,
--				' || uncontrolled_emis_sql || ' as input_emis,
--				0.0 as output_emis,

			2,
			substr(inv.' || fips_expression || ', 1, 2),
			substr(inv.' || fips_expression || ', 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || int_control_strategy_id || '::integer,
			null::integer as control_measures_id,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ann_pctred <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0  then ' || get_strategt_cost_sql || '.actual_equation_type else null::varchar(255) end as equation_type,
			cont.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' when is_flat_file_point_inventory then 'inv.longitude,inv.latitude,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when has_facility_name_column then 'inv.facility_name' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			cont.replacement as REPLACEMENT_ADDON,
			'''',
			' || case 
				when is_flat_file_inventory then 
					'case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || jan_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || jan_uncontrolled_emis_sql || ' * (1 - ' || jan_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || jan_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || jan_emis_sql || ' * (1 - ' || jan_cont_packet_percent_reduction_sql || ' / 100)
					end as jan_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || feb_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || feb_uncontrolled_emis_sql || ' * (1 - ' || feb_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || feb_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || feb_emis_sql || ' * (1 - ' || feb_cont_packet_percent_reduction_sql || ' / 100)
					end as feb_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || mar_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || mar_uncontrolled_emis_sql || ' * (1 - ' || mar_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || mar_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || mar_emis_sql || ' * (1 - ' || mar_cont_packet_percent_reduction_sql || ' / 100)
					end as mar_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || apr_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || apr_uncontrolled_emis_sql || ' * (1 - ' || apr_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || apr_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || apr_emis_sql || ' * (1 - ' || apr_cont_packet_percent_reduction_sql || ' / 100)
					end as apr_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || may_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || may_uncontrolled_emis_sql || ' * (1 - ' || may_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || may_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || may_emis_sql || ' * (1 - ' || may_cont_packet_percent_reduction_sql || ' / 100)
					end as may_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || jun_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || jun_uncontrolled_emis_sql || ' * (1 - ' || jun_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || jun_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || jun_emis_sql || ' * (1 - ' || jun_cont_packet_percent_reduction_sql || ' / 100)
					end as jun_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || jul_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || jul_uncontrolled_emis_sql || ' * (1 - ' || jul_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || jul_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || jul_emis_sql || ' * (1 - ' || jul_cont_packet_percent_reduction_sql || ' / 100)
					end as jul_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || aug_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || aug_uncontrolled_emis_sql || ' * (1 - ' || aug_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || aug_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || aug_emis_sql || ' * (1 - ' || aug_cont_packet_percent_reduction_sql || ' / 100)
					end as aug_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || sep_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || sep_uncontrolled_emis_sql || ' * (1 - ' || sep_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || sep_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || sep_emis_sql || ' * (1 - ' || sep_cont_packet_percent_reduction_sql || ' / 100)
					end as sep_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || oct_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || oct_uncontrolled_emis_sql || ' * (1 - ' || oct_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || oct_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || oct_emis_sql || ' * (1 - ' || oct_cont_packet_percent_reduction_sql || ' / 100)
					end as oct_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || nov_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || nov_uncontrolled_emis_sql || ' * (1 - ' || nov_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || nov_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || nov_emis_sql || ' * (1 - ' || nov_cont_packet_percent_reduction_sql || ' / 100)
					end as nov_final_emissions,
					case 
						when cont.replacement = ''R'' then 
							case 
								when coalesce(' || dec_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then 
								' || dec_uncontrolled_emis_sql || ' * (1 - ' || dec_cont_packet_percent_reduction_sql || ' / 100) 
								else ' || dec_emis_sql || ' 
							end 
						when cont.replacement = ''A'' then ' || dec_emis_sql || ' * (1 - ' || dec_cont_packet_percent_reduction_sql || ' / 100)
					end as dec_final_emissions,
					case 
						when coalesce(' || jan_emis_sql || ', 0.0) <> 0.0 and coalesce(' || jan_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || jan_uncontrolled_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || jan_emis_sql || ' * ' || jan_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || jan_emis_sql || ') * 100.0 
					else
						null::double precision
					end as jan_pct_red,
					case 
						when coalesce(' || feb_emis_sql || ', 0.0) <> 0.0 and coalesce(' || feb_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || feb_uncontrolled_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || feb_emis_sql || ' * ' || feb_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || feb_emis_sql || ') * 100.0 
						else
							null::double precision
					end as feb_pct_red,
					case 
						when coalesce(' || mar_emis_sql || ', 0.0) <> 0.0 and coalesce(' || mar_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || mar_uncontrolled_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || mar_emis_sql || ' * ' || mar_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || mar_emis_sql || ') * 100.0 
					else
						null::double precision
					end as mar_pct_red,
					case 
						when coalesce(' || apr_emis_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || apr_uncontrolled_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || apr_emis_sql || ' * ' || apr_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || apr_emis_sql || ') * 100.0 
					else
						null::double precision
					end as apr_pct_red,
					case 
						when coalesce(' || may_emis_sql || ', 0.0) <> 0.0 and coalesce(' || may_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || may_uncontrolled_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || may_emis_sql || ' * ' || may_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || may_emis_sql || ') * 100.0 
					else
						null::double precision
					end as may_pct_red,
					case 
						when coalesce(' || jun_emis_sql || ', 0.0) <> 0.0 and coalesce(' || jun_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || jun_uncontrolled_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || jun_emis_sql || ' * ' || jun_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || jun_emis_sql || ') * 100.0 
					else
						null::double precision
					end as jun_pct_red,
					case 
						when coalesce(' || jul_emis_sql || ', 0.0) <> 0.0 and coalesce(' || jul_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || jul_uncontrolled_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || jul_emis_sql || ' * ' || jul_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || jul_emis_sql || ') * 100.0 
					else
						null::double precision
					end as jul_pct_red,
					case 
						when coalesce(' || aug_emis_sql || ', 0.0) <> 0.0 and coalesce(' || aug_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || aug_uncontrolled_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || aug_emis_sql || ' * ' || aug_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || aug_emis_sql || ') * 100.0 
					else
						null::double precision
					end as aug_pct_red,
					case 
						when coalesce(' || sep_emis_sql || ', 0.0) <> 0.0 and coalesce(' || sep_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || sep_uncontrolled_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || sep_emis_sql || ' * ' || sep_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || sep_emis_sql || ') * 100.0 
					else
						null::double precision
					end as sep_pct_red,
					case 
						when coalesce(' || oct_emis_sql || ', 0.0) <> 0.0 and coalesce(' || oct_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || oct_uncontrolled_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || oct_emis_sql || ' * ' || oct_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || oct_emis_sql || ') * 100.0 
					else
						null::double precision
					end as oct_pct_red,
					case 
						when coalesce(' || nov_emis_sql || ', 0.0) <> 0.0 and coalesce(' || nov_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || nov_uncontrolled_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || nov_emis_sql || ' * ' || nov_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || nov_emis_sql || ') * 100.0 
					else
						null::double precision
					end as nov_pct_red,
					case 
						when coalesce(' || dec_emis_sql || ', 0.0) <> 0.0 and coalesce(' || dec_cont_packet_percent_reduction_sql || ', 0.0) <> 0.0 then
							(
								case 
									when cont.replacement = ''R'' then 
										' || dec_uncontrolled_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
									when cont.replacement = ''A'' then 
										' || dec_emis_sql || ' * ' || dec_cont_packet_percent_reduction_sql || ' / 100
								end
							) 
							/ (' || dec_emis_sql || ') * 100.0 
					else
						null::double precision
					end as dec_pct_red'			
				else
					'null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,
					null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision'
			end || '
			
		FROM emissions.' || inv_table_name || ' inv

			-- see if the source was projected, if so, use the projected values
			left outer join emissions.' || detailed_result_table_name || ' dr
			on dr.source_id = inv.record_id
			and dr.input_ds_id = inv.dataset_id
			and dr.apply_order = 1

			left outer join emf.pollutants p
			on p.name = inv.poll
			
			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.' || fips_expression || '
			and fipscode.country_num = ''0''' else '' end || '

			inner join (' || sql || ') cont
			on cont.record_id = inv.record_id

			-- tables for predicting measure
			left outer join emf.control_measure_sccs sccs
			-- scc filter
			on sccs."name" = inv.scc

			left outer join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = sccs.control_measures_id
			-- poll filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.' || fips_expression || ' or er.locale = substr(inv.' || fips_expression || ', 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::smallint >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::smallint)		
			and abs(er.efficiency - cont.ann_pctred) <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision
			and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0
--			and ' || cont_packet_percent_reduction_sql || ' <> 0.0
			
			left outer join reference.gdplev gdplev_incr
			on gdplev_incr.annual = er.cost_year

			left outer join emf.control_measures m
			on m.id = er.control_measures_id
			-- control program measure and technology filter
			' || case when control_program_measures_count > 0 and control_program_technologies_count > 0  then '
			and (
				(cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id))
				or 
				(cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology))
			)
			' when control_program_measures_count > 0 then '
			and cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id)
			' when control_program_technologies_count > 0 then '
			and cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology)
			' else '' end || '
			

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	(
				(cont.replacement = ''R'' 
					and (
						--annual pct red must exceed preexisting control via inventory pct red
						(
							(' || cont_packet_percent_reduction_sql || ' >= ' || inv_percent_reduction_sql || ')
							or ' || cont_packet_percent_reduction_sql || ' = 0.0
						)


						--make sure AT LEAST ONE monthly packet specific pct reduction exceeds the existing control via the inventory pct red OR is zero which allows it to be used 
						--as a pass through
						or (
							(
								(' || jan_cont_packet_percent_reduction_sql || ' >= ' || jan_inv_percent_reduction_sql || ')
								or ' || jan_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || feb_cont_packet_percent_reduction_sql || ' >= ' || feb_inv_percent_reduction_sql || ')
								or ' || feb_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || mar_cont_packet_percent_reduction_sql || ' >= ' || mar_inv_percent_reduction_sql || ')
								or ' || mar_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || apr_cont_packet_percent_reduction_sql || ' >= ' || apr_inv_percent_reduction_sql || ')
								or ' || apr_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || may_cont_packet_percent_reduction_sql || ' >= ' || may_inv_percent_reduction_sql || ')
								or ' || may_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || jun_cont_packet_percent_reduction_sql || ' >= ' || jun_inv_percent_reduction_sql || ')
								or ' || jun_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || jul_cont_packet_percent_reduction_sql || ' >= ' || jul_inv_percent_reduction_sql || ')
								or ' || jul_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || aug_cont_packet_percent_reduction_sql || ' >= ' || aug_inv_percent_reduction_sql || ')
								or ' || aug_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || sep_cont_packet_percent_reduction_sql || ' >= ' || sep_inv_percent_reduction_sql || ')
								or ' || sep_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || oct_cont_packet_percent_reduction_sql || ' >= ' || oct_inv_percent_reduction_sql || ')
								or ' || oct_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || nov_cont_packet_percent_reduction_sql || ' >= ' || nov_inv_percent_reduction_sql || ')
								or ' || nov_cont_packet_percent_reduction_sql || ' = 0.0
							)
							or (
								(' || dec_cont_packet_percent_reduction_sql || ' >= ' || dec_inv_percent_reduction_sql || ')
								or ' || dec_cont_packet_percent_reduction_sql || ' = 0.0
							)
						)

					)
				)
				or (cont.replacement = ''A'')
			)
 
			--remove plant closures from consideration
			and inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)
		order by inv.record_id, 
			--makes sure we get the highest ranking control packet record
			cont.ranking,
			--makes sure replacements trump add on controls
			replacement, 
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			cont.ann_pctred - er.efficiency,
			' || cont_packet_percent_reduction_sql || ' desc, 
			' || get_strategt_cost_sql || '.computed_cost_per_ton';
	END IF;
	END IF;












	-- ALLOWABLE PACKET

	-- next lets caps/replacements, need to union the the various program tables together, so we can make sure and get the most source specific controls
	sql := ''; --reset
	IF run_packet_type = 'all' OR run_packet_type = 'allowable' THEN
	-- see if control strategy program have specific measures or technologies specified
	select count(cptech.id),
		count(cpm.id)
	 from emf.control_strategy_programs csp
		inner join emf.control_programs cp
		on cp.id = csp.control_program_id

		inner join emf.control_program_types cpt
		on cpt.id = cp.control_program_type_id

		left outer join emf.control_program_technologies cptech
		on cptech.control_program_id = cp.id
		
		left outer join emf.control_program_measures cpm
		on cpm.control_program_id = cp.id
		
	where csp.control_strategy_id = int_control_strategy_id
		and cpt."name" = 'Allowable'
	into control_program_technologies_count, 
		control_program_measures_count;
  	FOR control_program IN EXECUTE 
		'select cp.id as control_program_id, cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type,
			(select count(*) from emf.control_program_technologies cptech where cptech.control_program_id = cp.id) as control_program_technologies_count,
			(select count(*) from emf.control_program_measures cpm where cpm.control_program_id = cp.id) as control_program_measures_count
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id

			inner join emf.datasets d
			on d.id = i.dataset_id

			inner join emf.dataset_types dt
			on dt.id = d.dataset_type

		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Allowable''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP

		-- see if control packet is in the extended format
		is_control_packet_extended_format := public.check_table_for_columns(control_program.table_name, 'region_cd', ',');

		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Allowable Packet' or control_program.dataset_type = 'Allowable Packet Extended' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select 
				--distinct on (record_id, allowable_type)
				record_id,ann_cap,jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,ann_replacement,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement,allowable_type,compliance_date,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, 
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (
				' || public.build_project_future_year_inventory_matching_hierarchy_sql(
				input_dataset_id, --inv_dataset_id integer, 
				input_dataset_version, --inv_dataset_version integer, 
				control_program.dataset_id, --control_program_dataset_id integer, 
				control_program.dataset_version, --control_program_dataset_version integer, 
				case when not is_control_packet_extended_format then
					'null::double precision as jan_cap,null::double precision as feb_cap,null::double precision as mar_cap,null::double precision as apr_cap,null::double precision as may_cap,null::double precision as jun_cap,null::double precision as jul_cap,null::double precision as aug_cap,null::double precision as sep_cap,null::double precision as oct_cap,null::double precision as nov_cap,null::double precision as dec_cap,cap as ann_cap,null::double precision as jan_replacement,null::double precision as feb_replacement,null::double precision as mar_replacement,null::double precision as apr_replacement,null::double precision as may_replacement,null::double precision as jun_replacement,null::double precision as jul_replacement,null::double precision as aug_replacement,null::double precision as sep_replacement,null::double precision as oct_replacement,null::double precision as nov_replacement,null::double precision as dec_replacement,replacement as ann_replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,compliance_date'
				else 
					'jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,ann_cap,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement,ann_replacement,case when coalesce(jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement,ann_replacement) is not null then ''R'' when coalesce(jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,ann_cap) is not null then ''C'' end as allowable_type,compliance_date'
				end , --select_columns varchar, 
				inv_filter, --inv_filter text, --not aliased
				county_dataset_id, --1279 county_dataset_id integer,
				county_dataset_version, --county_dataset_version integer,
				'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone' 
				|| case 
					when is_flat_file_inventory and is_control_packet_extended_format then --include monthly packets
						' and (
							(' || is_annual_source_sql || ' and coalesce(ann_cap,ann_replacement) is not null)
							or 
							(' || is_monthly_source_sql || ' and coalesce(ann_cap,jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement) is not null)

						)'
					when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
						' and coalesce(ann_cap,ann_replacement) is not null'
					else --dont include monthly packets
						' and coalesce(cap,replacement) is not null'
				end,
				1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
				) || '


				--order by record_id, allowable_type, ranking, compliance_date desc
			) tbl';


		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do caps and replacemnts ' || clock_timestamp();

		sql := 'select distinct on (record_id, allowable_type)
				record_id,ann_cap,jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,ann_replacement,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement,allowable_type,
				control_program_name, 
				control_program_technologies_count, control_program_measures_count, 
				ranking
			from (' || sql;
		sql := sql || ') tbl order by record_id, allowable_type, ranking, coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc';

		emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_value end' 
				when dataset_month != 0 then 
					'case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end' 
				else 
					'case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end' 
			end;
		jan_emis_sql := 
			case 
				when is_flat_file_inventory then 
					'case when cdr.record_id is not null then coalesce(cdr.jan_final_emissions,pdr.jan_final_emissions,inv.jan_value) when pdr.record_id is not null then coalesce(pdr.jan_final_emissions,inv.jan_value) else inv.jan_value end' 
				else 
					'null::double precision' 
			end;
		feb_emis_sql := replace(jan_emis_sql, 'jan_', 'feb_');
		mar_emis_sql := replace(jan_emis_sql, 'jan_', 'mar_');
		apr_emis_sql := replace(jan_emis_sql, 'jan_', 'apr_');
		may_emis_sql := replace(jan_emis_sql, 'jan_', 'may_');
		jun_emis_sql := replace(jan_emis_sql, 'jan_', 'jun_');
		jul_emis_sql := replace(jan_emis_sql, 'jan_', 'jul_');
		aug_emis_sql := replace(jan_emis_sql, 'jan_', 'aug_');
		sep_emis_sql := replace(jan_emis_sql, 'jan_', 'sep_');
		oct_emis_sql := replace(jan_emis_sql, 'jan_', 'oct_');
		nov_emis_sql := replace(jan_emis_sql, 'jan_', 'nov_');
		dec_emis_sql := replace(jan_emis_sql, 'jan_', 'dec_');


		-- make sure the apply order is 4,....this is important when the controlled inventpory is created.
		-- the apply order will dictate how the controlled inventpory is created
		execute 
		--raise notice '%', 
		'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			inv_ctrl_eff,
			inv_rule_pen,
			inv_rule_eff,
			adj_factor,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
			apply_order,
			fipsst,
			fipscty,
			sic,
			naics,
			source_id,
			input_ds_id,
			cs_id,
			cm_id,
			equation_type,
			control_program,
			xloc,
			yloc,
			plant,
			"comment",
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
			)
		select distinct on (inv.record_id, cont.allowable_type)

			' || detailed_result_dataset_id || '::integer,
			coalesce(case when case when ' || emis_sql || ' <> 0 then coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' else 0.0::double precision end <> 0 and abs(case when ' || emis_sql || ' <> 0 then coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' else null::double precision end * 100.0 - er.efficiency) / case when ' || emis_sql || ' <> 0 then coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' else null::double precision end * 100 * 100 <= ' || control_program_measure_min_pct_red_diff_constraint || ' then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
--			case when allowable_type = ''C'' then ''C'' || substring(m.abbreviation,2,length(m.abbreviation)) when allowable_type = ''R'' then ''R'' || substring(m.abbreviation,2,length(m.abbreviation)) end as abbreviation,
			inv.poll,
			inv.scc,
			inv.' || fips_expression || ',
			' || case when is_point_table = false then '' else 'inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || ', ' end || '
			null::double precision as operation_maintenance_cost,
			null::double precision as annualized_capital_cost,
			null::double precision as capital_cost,
			null::double precision as ann_cost,
			null::double precision as computed_cost_per_ton,
			null::double precision as control_eff,
			null::double precision as rule_pen,
			null::double precision as rule_eff,
			null::double precision as percent_reduction,
			' || case when is_flat_file_inventory then 'inv.ann_pct_red' else 'inv.ceff' end || ' as inv_ceff,
			' || case when is_flat_file_inventory then '100.0' when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ' as inv_rpen,
			' || case when is_flat_file_inventory then '100.0' else 'case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end' end || ' as inv_reff,
/*			case 
				when ' || emis_sql || ' <> 0 then 
					coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' 
				else 
					null::double precision 
			end as adj_factor,*/

			case 
				when coalesce((' 
				|| case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || '),0.0) <> 0.0 then
				(' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						'case 
							when allowable_type = ''C'' then 
								cont.ann_cap * ' || number_of_days_in_year || ' 
							when allowable_type = ''R'' then 
								cont.ann_replacement * ' || number_of_days_in_year || ' 
						end'
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.jan_cap,cont.ann_cap) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.jan_replacement,cont.ann_replacement) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.feb_cap,cont.ann_cap) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.feb_replacement,cont.ann_replacement) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.mar_cap,cont.ann_cap) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.mar_replacement,cont.ann_replacement) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.apr_cap,cont.ann_cap) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.apr_replacement,cont.ann_replacement) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.may_cap,cont.ann_cap) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.may_replacement,cont.ann_replacement) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.jun_cap,cont.ann_cap) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.jun_replacement,cont.ann_replacement) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.jul_cap,cont.ann_cap) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.jul_replacement,cont.ann_replacement) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.aug_cap,cont.ann_cap) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.aug_replacement,cont.ann_replacement) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.sep_cap,cont.ann_cap) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.sep_replacement,cont.ann_replacement) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.oct_cap,cont.ann_cap) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.oct_replacement,cont.ann_replacement) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.nov_cap,cont.ann_cap) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.nov_replacement,cont.ann_replacement) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
									end, 0.0)
								+ coalesce(case 
										when allowable_type = ''C'' then 
											coalesce(cont.dec_cap,cont.ann_cap) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
										when allowable_type = ''R'' then 
											coalesce(cont.dec_replacement,cont.ann_replacement) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
									end, 0.0)

							else -- no monthly values to worry about
								case 
									when allowable_type = ''C'' then 
										cont.ann_cap * ' || number_of_days_in_year || ' 
									when allowable_type = ''R'' then 
										cont.ann_replacement * ' || number_of_days_in_year || ' 
								end
						end'
				end || ') 
				/ (' || case 
					when not is_flat_file_inventory  then -- no monthly values to worry about
						emis_sql
					else
						'case 
							when ' || is_monthly_source_sql || ' then
								coalesce(' || jan_emis_sql || ', 0.0)
								+ coalesce(' || feb_emis_sql || ', 0.0)
								+ coalesce(' || mar_emis_sql || ', 0.0)
								+ coalesce(' || apr_emis_sql || ', 0.0)
								+ coalesce(' || may_emis_sql || ', 0.0)
								+ coalesce(' || jun_emis_sql || ', 0.0)
								+ coalesce(' || jul_emis_sql || ', 0.0)
								+ coalesce(' || aug_emis_sql || ', 0.0)
								+ coalesce(' || sep_emis_sql || ', 0.0)
								+ coalesce(' || oct_emis_sql || ', 0.0)
								+ coalesce(' || nov_emis_sql || ', 0.0)
								+ coalesce(' || dec_emis_sql || ', 0.0)

							else -- no monthly values to worry about
								' || emis_sql || '
						end'
				end || ')
			else
				0.0
			end as adj_factor,
			
/*			case when allowable_type = ''C'' then cont.ann_cap * ' || number_of_days_in_year || ' when allowable_type = ''R'' then cont.ann_replacement * ' || number_of_days_in_year || ' end as final_emissions,*/
			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'case 
						when allowable_type = ''C'' then 
							cont.ann_cap * ' || number_of_days_in_year || ' 
						when allowable_type = ''R'' then 
							cont.ann_replacement * ' || number_of_days_in_year || ' 
					end'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jan_cap,cont.ann_cap) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jan_replacement,cont.ann_replacement) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.feb_cap,cont.ann_cap) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.feb_replacement,cont.ann_replacement) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.mar_cap,cont.ann_cap) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.mar_replacement,cont.ann_replacement) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.apr_cap,cont.ann_cap) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.apr_replacement,cont.ann_replacement) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.may_cap,cont.ann_cap) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.may_replacement,cont.ann_replacement) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jun_cap,cont.ann_cap) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jun_replacement,cont.ann_replacement) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jul_cap,cont.ann_cap) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jul_replacement,cont.ann_replacement) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.aug_cap,cont.ann_cap) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.aug_replacement,cont.ann_replacement) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.sep_cap,cont.ann_cap) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.sep_replacement,cont.ann_replacement) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.oct_cap,cont.ann_cap) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.oct_replacement,cont.ann_replacement) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.nov_cap,cont.ann_cap) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.nov_replacement,cont.ann_replacement) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
								end, 0.0)
							+ coalesce(case 
									when allowable_type = ''C'' then 
										coalesce(cont.dec_cap,cont.ann_cap) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.dec_replacement,cont.ann_replacement) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
								end, 0.0)

						else -- no monthly values to worry about
							case 
								when allowable_type = ''C'' then 
									cont.ann_cap * ' || number_of_days_in_year || ' 
								when allowable_type = ''R'' then 
									cont.ann_replacement * ' || number_of_days_in_year || ' 
							end
					end'
			end || ' as final_emissions,



/*			' || emis_sql || ' - (case when allowable_type = ''C'' then cont.ann_cap * ' || number_of_days_in_year || ' when allowable_type = ''R'' then cont.ann_replacement * ' || number_of_days_in_year || ' end) as emis_reduction,*/
			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					'coalesce(' || emis_sql || ',0.0) 
					- 
					(case 
						when allowable_type = ''C'' then 
							cont.ann_cap * ' || number_of_days_in_year || ' 
						when allowable_type = ''R'' then 
							cont.ann_replacement * ' || number_of_days_in_year || ' 
					end)'
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(coalesce(' || jan_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jan_cap,cont.ann_cap) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jan_replacement,cont.ann_replacement) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || feb_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.feb_cap,cont.ann_cap) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.feb_replacement,cont.ann_replacement) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || mar_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.mar_cap,cont.ann_cap) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.mar_replacement,cont.ann_replacement) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || apr_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.apr_cap,cont.ann_cap) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.apr_replacement,cont.ann_replacement) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || may_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.may_cap,cont.ann_cap) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.may_replacement,cont.ann_replacement) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || jun_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jun_cap,cont.ann_cap) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jun_replacement,cont.ann_replacement) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || jul_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.jul_cap,cont.ann_cap) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.jul_replacement,cont.ann_replacement) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || aug_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.aug_cap,cont.ann_cap) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.aug_replacement,cont.ann_replacement) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || sep_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.sep_cap,cont.ann_cap) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.sep_replacement,cont.ann_replacement) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || oct_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.oct_cap,cont.ann_cap) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.oct_replacement,cont.ann_replacement) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || nov_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.nov_cap,cont.ann_cap) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.nov_replacement,cont.ann_replacement) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint)
								end), 0.0)
							+ coalesce(coalesce(' || dec_emis_sql || ',0.0) 
								- 
								(case 
									when allowable_type = ''C'' then 
										coalesce(cont.dec_cap,cont.ann_cap) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
									when allowable_type = ''R'' then 
										coalesce(cont.dec_replacement,cont.ann_replacement) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
								end), 0.0)

						else -- no monthly values to worry about
							coalesce(' || emis_sql || ',0.0)
							- 
							(case 
								when allowable_type = ''C'' then 
									cont.ann_cap * ' || number_of_days_in_year || ' 
								when allowable_type = ''R'' then 
									cont.ann_replacement * ' || number_of_days_in_year || ' 
							end)
					end'
			end || ' as emis_reduction,



/*			' || emis_sql || ' as inv_emissions,*/

			' || case 
				when not is_flat_file_inventory  then -- no monthly values to worry about
					emis_sql
				else
					'case 
						when ' || is_monthly_source_sql || ' then
							coalesce(' || jan_emis_sql || ', 0.0)
							+ coalesce(' || feb_emis_sql || ', 0.0)
							+ coalesce(' || mar_emis_sql || ', 0.0)
							+ coalesce(' || apr_emis_sql || ', 0.0)
							+ coalesce(' || may_emis_sql || ', 0.0)
							+ coalesce(' || jun_emis_sql || ', 0.0)
							+ coalesce(' || jul_emis_sql || ', 0.0)
							+ coalesce(' || aug_emis_sql || ', 0.0)
							+ coalesce(' || sep_emis_sql || ', 0.0)
							+ coalesce(' || oct_emis_sql || ', 0.0)
							+ coalesce(' || nov_emis_sql || ', 0.0)
							+ coalesce(' || dec_emis_sql || ', 0.0)

						else -- no monthly values to worry about
							' || emis_sql || '
					end'
			end || ' as inv_emissions,

			
	--				' || emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			case when allowable_type = ''C'' then 3 when allowable_type = ''R'' then 4 end as apply_order,
			substr(inv.' || fips_expression || ', 1, 2),
			substr(inv.' || fips_expression || ', 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || int_control_strategy_id || '::integer,
			null::integer as control_measures_id,
			null::varchar(255) as equation_type,
			cont.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' when is_flat_file_point_inventory then 'inv.longitude,inv.latitude,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when has_facility_name_column then 'inv.facility_name' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			'''',
			' || case 
				when is_flat_file_point_inventory then '

					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.jan_cap,cont.ann_cap) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.jan_replacement,cont.ann_replacement) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint) 
					end as jan_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.feb_cap,cont.ann_cap) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.feb_replacement,cont.ann_replacement) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint) 
					end as feb_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.mar_cap,cont.ann_cap) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.mar_replacement,cont.ann_replacement) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint) 
					end as mar_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.apr_cap,cont.ann_cap) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.apr_replacement,cont.ann_replacement) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint) 
					end as apr_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.may_cap,cont.ann_cap) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.may_replacement,cont.ann_replacement) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint) 
					end as may_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.jun_cap,cont.ann_cap) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.jun_replacement,cont.ann_replacement) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint) 
					end as jun_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.jul_cap,cont.ann_cap) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.jul_replacement,cont.ann_replacement) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint) 
					end as jul_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.aug_cap,cont.ann_cap) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.aug_replacement,cont.ann_replacement) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint) 
					end as aug_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.sep_cap,cont.ann_cap) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.sep_replacement,cont.ann_replacement) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint) 
					end as sep_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.oct_cap,cont.ann_cap) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.oct_replacement,cont.ann_replacement) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint) 
					end as oct_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.nov_cap,cont.ann_cap) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.nov_replacement,cont.ann_replacement) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint) 
					end as nov_final_emissions,
					case 
						when not (' || is_monthly_source_sql || ') then
							null::double precision
						when allowable_type = ''C'' then 
							coalesce(cont.dec_cap,cont.ann_cap) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint) 
						when allowable_type = ''R'' then 
							coalesce(cont.dec_replacement,cont.ann_replacement) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint) 
					end as dec_final_emissions' 
				else 
					'null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision,null::double precision'
			end || '

		FROM emissions.' || inv_table_name || ' inv

			-- see if the source was controlled, if so, use the controlled values
			left outer join emissions.' || detailed_result_table_name || ' cdr
			on cdr.source_id = inv.record_id
			and cdr.input_ds_id = inv.dataset_id
			and cdr.apply_order = 2

			-- see if the source was projected, if so, use the projected values, controlled values will override the projected values, if there are both...
			left outer join emissions.' || detailed_result_table_name || ' pdr
			on pdr.source_id = inv.record_id
			and pdr.input_ds_id = inv.dataset_id
			and pdr.apply_order = 1

			left outer join emf.pollutants p
			on p.name = inv.poll
			
			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.' || fips_expression || '
			and fipscode.country_num = ''0''' else '' end || '

			inner join (' || sql || ') cont
			on cont.record_id = inv.record_id

			-- tables for predicting measure
			left outer join emf.control_measure_sccs sccs
			-- scc filter
			on sccs."name" = inv.scc

			-- tables for predicting measure
			left outer join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = sccs.control_measures_id
			-- poll filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.' || fips_expression || ' or er.locale = substr(inv.' || fips_expression || ', 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::smallint >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::smallint)
			and abs(er.efficiency - case when ' || emis_sql || ' <> 0 then coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' else 0.0::double precision end) <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision

			left outer join emf.control_measures m
			on m.id = er.control_measures_id
			-- control program measure and technology filter
			' || case when control_program_measures_count > 0 and control_program_technologies_count > 0  then '
			and (
				(cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id))
				or 
				(cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology))
			)
			' when control_program_measures_count > 0 then '
			and cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id)
			' when control_program_technologies_count > 0 then '
			and cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology)
			' else '' end || '

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	
			(
				(
					allowable_type = ''C'' 
					and (
						coalesce(' || emis_sql || ',0.0) >= cont.ann_cap * ' || number_of_days_in_year || '
						or coalesce(' || jan_emis_sql || ',0.0) >= coalesce(cont.jan_cap,cont.ann_cap) * public.get_days_in_month(1::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || feb_emis_sql || ',0.0) >= coalesce(cont.feb_cap,cont.ann_cap) * public.get_days_in_month(2::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || mar_emis_sql || ',0.0) >= coalesce(cont.mar_cap,cont.ann_cap) * public.get_days_in_month(3::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || apr_emis_sql || ',0.0) >= coalesce(cont.apr_cap,cont.ann_cap) * public.get_days_in_month(4::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || may_emis_sql || ',0.0) >= coalesce(cont.may_cap,cont.ann_cap) * public.get_days_in_month(5::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || jun_emis_sql || ',0.0) >= coalesce(cont.jun_cap,cont.ann_cap) * public.get_days_in_month(6::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || jul_emis_sql || ',0.0) >= coalesce(cont.jul_cap,cont.ann_cap) * public.get_days_in_month(7::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || aug_emis_sql || ',0.0) >= coalesce(cont.aug_cap,cont.ann_cap) * public.get_days_in_month(8::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || sep_emis_sql || ',0.0) >= coalesce(cont.sep_cap,cont.ann_cap) * public.get_days_in_month(9::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || oct_emis_sql || ',0.0) >= coalesce(cont.oct_cap,cont.ann_cap) * public.get_days_in_month(10::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || nov_emis_sql || ',0.0) >= coalesce(cont.nov_cap,cont.ann_cap) * public.get_days_in_month(11::smallint, ' || inventory_year || '::smallint) 
						or coalesce(' || dec_emis_sql || ',0.0) >= coalesce(cont.dec_cap,cont.ann_cap) * public.get_days_in_month(12::smallint, ' || inventory_year || '::smallint)
					) 
				)
				or (allowable_type = ''R'')
			)

--			and cont.ann_pctred >= coalesce(inv.ceff, 0.0)
--			and ' || emis_sql || ' <> 0

			--remove plant closures from consideration
			and inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)

		order by inv.record_id, 
			--makes sure we get the highest ranking control packet record
			cont.allowable_type,
			cont.ranking,
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			er.efficiency - case when ' || emis_sql || ' <> 0 then coalesce(cont.ann_replacement, cont.ann_cap) * ' || number_of_days_in_year || ' / ' || emis_sql || ' else 0.0::double precision end';
	END IF;
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;