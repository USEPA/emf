CREATE OR REPLACE FUNCTION public.run_multi_pollutant_max_emis_red_strategy_finalize(intControlStrategyId integer, intInputDatasetId integer, 
	intInputDatasetVersion integer, intStrategyResultId int) RETURNS void AS $$
DECLARE
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant character varying(255) := '';
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	has_constraints boolean := null;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	ref_cost_year integer := 2013;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_target_pollutant integer := 0;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	has_rpen_column boolean := false;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	remaining_emis_sql character varying;
	inventory_sectors character varying := '';
	include_unspecified_costs boolean := true; 
	has_pm_target_pollutant boolean := false; 
	has_cpri_column boolean := false; 
	has_primary_device_type_code_column boolean := false; 
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = intInputDatasetId
	into inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = intStrategyResultId
	into detailed_result_dataset_id,
		detailed_result_table_name;

	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = intInputDatasetId
	into inventory_sectors;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = intControlStrategyId 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = intControlStrategyId
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.name,
		cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100,
		coalesce(cs.include_unspecified_costs,true),
		p.name
	FROM emf.control_strategies cs
		left outer join emf.pollutants p
		on p.id = cs.pollutant_id
	where cs.id = intControlStrategyId
	INTO strategy_name,
		target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		include_unspecified_costs,
		target_pollutant;


	-- see if there are pm target pollutant for the stategy...
	has_pm_target_pollutant := case when target_pollutant = 'PM10' or target_pollutant = 'PM25-PRI' then true else false end;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is plant column in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if there is plant column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.replacement_control_min_eff_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = intControlStrategyId
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		replacement_control_min_eff_diff_constraint;



	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

target_pollutant := 1;

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(intInputDatasetId)
	into dataset_month;

	IF dataset_month = 1 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 2 THEN
		no_days_in_month := 29;
	ELSIF dataset_month = 3 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 4 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 5 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 6 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 7 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 8 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 9 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 10 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 11 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 12 THEN
		no_days_in_month := 31;
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


--	uncontrolled_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end;
--	annualized_uncontrolled_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end;
	uncontrolled_emis_sql := 'case when dr.source_id is not null then case when (1 - coalesce(dr.Percent_Reduction / 100, 0.0)) != 0 then dr.final_emissions / (1 - coalesce(dr.Percent_Reduction / 100, 1.0)) else 0.0::double precision end else (' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end || ') end';
	emis_sql := 'case when dr.source_id is not null then dr.final_emissions else (' ||
			case 
				when dataset_month != 0 then 
					'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' 
				else 
					'inv.ann_emis' 
			end || ') end';
	
	annualized_uncontrolled_emis_sql := 'case when dr.source_id is not null then case when (1 - coalesce(dr.Percent_Reduction / 100, 0.0)) != 0 then dr.final_emissions / (1 - coalesce(dr.Percent_Reduction / 100, 1.0)) else 0.0::double precision end else (' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end || ') end';


	uncontrolled_emis_sql := '(' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.navd_emis * ' || no_days_in_month || ', inv.nann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.nann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end || ')';
	emis_sql := 
			case 
				when dataset_month != 0 then 
					'coalesce(inv.navd_emis * ' || no_days_in_month || ', inv.nann_emis)' 
				else 
					'inv.nann_emis' 
			end;
	
	annualized_uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.navd_emis * 365, inv.nann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.nann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;


	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	-- relative emission reduction from inventory emission (i.e., ann emis in inv 100 tons (add on control gives addtl 50% red --> 50 tons
	-- whereas a 95% ceff replacement control on a source with an existing control of 90% reduction needs to back out source to an 
	-- uncontrolled emission 100 / (1 - 0.9) = 1000 tons giving a and then applying new control gives 950 tons reduced giving a 95% control
	remaining_emis_sql := 
		'( case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end )';
/*
	remaining_emis_sql := 
		'(' || emis_sql || '
		- ( case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end ))';


case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.nceff' else 'coalesce(inv.nceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.nreff' else 'coalesce(inv.nreff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.nrpen' else 'coalesce(inv.nrpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then -- has existing control
	' || emis_sql || ' - (' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
else -- has NO existing control
	' || emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0)
end
*/
			

	return;
END;
$$ LANGUAGE plpgsql;



--new
