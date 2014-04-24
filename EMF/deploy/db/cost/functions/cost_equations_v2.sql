
CREATE OR REPLACE FUNCTION public.get_cost_expressions(
	int_control_strategy_id integer,
	int_input_dataset_id integer,
	use_override_dataset boolean,
	inv_table_alias character varying(64), 
	control_measure_table_alias character varying(64), 
	equation_type_table_alias character varying(64), 
	control_measure_equation_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64), 
	control_strategy_measure_table_alias character varying(64), 
	gdplev_table_alias character varying(64), 
	inv_override_table_alias character varying(64), 
	gdplev_incr_table_alias character varying(64),
	OUT annual_cost_expression text, 
	OUT capital_cost_expression text, 
	OUT operation_maintenance_cost_expression text, 
	OUT fixed_operation_maintenance_cost_expression text, 
	OUT variable_operation_maintenance_cost_expression text, 
	OUT annualized_capital_cost_expression text, 
	OUT computed_cost_per_ton_expression text, 
	OUT actual_equation_type_expression text)  AS $$
DECLARE

	discount_rate double precision;
BEGIN

	SELECT cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO discount_rate;

	select public.get_cost_expressions(
		int_control_strategy_id,
		int_input_dataset_id,
		use_override_dataset,
		inv_table_alias, 
		control_measure_table_alias, 
		equation_type_table_alias, 
		control_measure_equation_table_alias, 
		control_measure_efficiencyrecord_table_alias, 
		control_strategy_measure_table_alias, 
		gdplev_table_alias, 
		inv_override_table_alias, 
		gdplev_incr_table_alias,
		discount_rate)
	into annual_cost_expression, 
		capital_cost_expression, 
		operation_maintenance_cost_expression, 
		fixed_operation_maintenance_cost_expression, 
		variable_operation_maintenance_cost_expression, 
		annualized_capital_cost_expression, 
		computed_cost_per_ton_expression, 
		actual_equation_type_expression;

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;



CREATE OR REPLACE FUNCTION public.get_cost_expressions(
	int_control_strategy_id integer,
	int_input_dataset_id integer,
	use_override_dataset boolean,
	inv_table_alias character varying(64), 
	control_measure_table_alias character varying(64), 
	equation_type_table_alias character varying(64), 
	control_measure_equation_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64), 
	control_strategy_measure_table_alias character varying(64), 
	gdplev_table_alias character varying(64), 
	inv_override_table_alias character varying(64), 
	gdplev_incr_table_alias character varying(64),
	discount_rate double precision,
	OUT annual_cost_expression text, 
	OUT capital_cost_expression text, 
	OUT operation_maintenance_cost_expression text, 
	OUT fixed_operation_maintenance_cost_expression text, 
	OUT variable_operation_maintenance_cost_expression text, 
	OUT annualized_capital_cost_expression text, 
	OUT computed_cost_per_ton_expression text, 
	OUT actual_equation_type_expression text)  AS $$
DECLARE
--	annualized_uncontrolled_emis_sql character varying;
	inv_table_name character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	remaining_emis_sql character varying;
	emis_reduction_sql character varying;
	get_strategty_ceff_equation_sql character varying;
	measures_count integer := 0;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	no_days_in_year smallint := 365;

	use_cost_equations boolean;

	is_point_table boolean := false; 
	has_design_capacity_columns boolean := false; 
	has_rpen_column boolean := false;
--	has_cpri_column boolean := false; 
--	has_primary_device_type_code_column boolean := false; 

	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	cost_year integer := null;
	inventory_year integer := null;

	--FOR NOW CONVERT all stack flow rates to cfm, all applicable equations are expecting these units!
	stkflow_expression text := '(' || public.get_stkflow_expression(inv_table_alias) || ' * 60)';
	
	convert_design_capacity_expression text;
	convert_design_capacity_expression_default_MW_units text;
	
	capital_recovery_factor_expression text;
	inv_ceff_expression varchar(69) := inv_table_alias || '.ceff';

	chained_gdp_adjustment_factor_expression text;

	
	--support for flat file ds types...
	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'fips';
	plantid_expression character varying(64) := 'plantid';
	pointid_expression character varying(64) := 'pointid';
	stackid_expression character varying(64) := 'stackid';
	segment_expression character varying(64) := 'segment';
	is_flat_file_inventory boolean := false;
	is_flat_file_point_inventory boolean := false;
	inv_pct_red_expression character varying(256);
	annualized_emis_sql character varying;
	design_capacity_units_expression character varying(64) := 'design_capacity_unit_numerator,design_capacity_unit_denominator';

	so2_emis_sql text;

	--type 14 variables
	t14_use_equation text;
	t14_fa text;
	t14_fd text;
	t14_noducts text;
	t14_cpm text;
	t14_tci text;
	t14_tac text;

	--type 15 variables
	t15_use_equation text;
	t15_fa text;
	t15_fd text;
	t15_noducts text;
	t15_ec1 text;
	t15_ec2 text;
	t15_pm_emis_rate text;
	t15_cpm text;
	t15_tci text;
	t15_tac text;

	--type 16 variables
	t16_use_equation text;
	t16_fa text;
	t16_noscrubbers text;
	t16_so2_mole_conc text;
	t16_tci text;
	t16_tac text;

	--type 17 variables
	t17_use_equation text;
	t17_fa text;
	t17_fd text;
	t17_noducts text;
	t17_pm_conc text;
	t17_so2_conc text;
	t17_tci text;
	t17_tac text;

	--type 18 variables
	t18_use_equation text;
	t18_fa text;
	t18_fd text;
	t18_so2_conc text;
	t18_tci text;
	t18_tac text;

	--type 19 variables
	t19_use_equation text;
	t19_fa text;
	t19_fd text;
	t19_noducts text;
	t19_so2_conc text;
	t19_tci text;
	t19_tac text;

BEGIN
	-- see if control strategy has only certain measures specified
	SELECT count(id)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count; 

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = int_input_dataset_id
	into inv_table_name;

	--get dataset type name
	select dataset_types."name"
	from emf.datasets
	inner join emf.dataset_types
	on datasets.dataset_type = dataset_types.id
	where datasets.id = int_input_dataset_id
	into dataset_type_name;

	--if Flat File 2010 Types then change primary key field expression variables...
	IF dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		fips_expression := 'region_cd';
		plantid_expression := 'facility_id';
		pointid_expression := 'unit_id';
		stackid_expression := 'rel_point_id';
		segment_expression := 'process_id';
		inv_ceff_expression := 'ann_pct_red';
		design_capacity_units_expression  := 'design_capacity_units';
		is_flat_file_inventory := true;
		IF dataset_type_name = 'Flat File 2010 Point' THEN
			is_flat_file_point_inventory := true;
		END IF;
		convert_design_capacity_expression := public.get_convert_design_capacity_expression(inv_table_alias, '');
		convert_design_capacity_expression_default_MW_units := public.get_convert_design_capacity_expression(inv_table_alias, 'MW');
	ELSE
		fips_expression := 'fips';
		plantid_expression := 'plantid';
		pointid_expression := 'pointid';
		stackid_expression := 'stackid';
		segment_expression := 'segment';
		inv_ceff_expression := 'ceff';
		design_capacity_units_expression := 'design_capacity_unit_numerator,design_capacity_unit_denominator';
		convert_design_capacity_expression := public.get_convert_design_capacity_expression(inv_table_alias, '', '');
		convert_design_capacity_expression_default_MW_units := public.get_convert_design_capacity_expression(inv_table_alias, 'MW', '');
	END If;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.use_cost_equations,
		cs.cost_year,
		cs.analysis_year
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO use_cost_equations,
		cost_year,
		inventory_year;

	capital_recovery_factor_expression := public.get_capital_recovery_factor_expression(control_measure_table_alias, control_measure_efficiencyrecord_table_alias, discount_rate);

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,' || design_capacity_units_expression, ',');

	-- see if there is plant column in the inventory
	--has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	--has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(int_input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
	into no_days_in_month;

	select public.get_days_in_year(inventory_year::smallint)
	into no_days_in_year;
	
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

	chained_gdp_adjustment_factor_expression := '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision))';
	

	IF NOT is_flat_file_inventory THEN
		inv_pct_red_expression := 'coalesce(' || inv_table_alias || '.ceff, inv_ovr.ceff) * coalesce(coalesce(' || inv_table_alias || '.reff, inv_ovr.reff) / 100, 1.0)' || case when has_rpen_column then ' * coalesce(coalesce(' || inv_table_alias || '.rpen, inv_ovr.rpen) / 100, 1.0)' else '' end;
		emis_sql := public.get_ann_emis_expression(inv_table_alias, no_days_in_month);
		annualized_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_year || ', inv.ann_emis)' else 'inv.ann_emis' end;
	ELSE
		inv_pct_red_expression := 'coalesce(inv.ann_pct_red, inv_ovr.ceff)';
		emis_sql := 'inv.ann_value';
		annualized_emis_sql := 'inv.ann_value';
	END IF;
	so2_emis_sql := 'inv_ovr.so2_ann_value';



--	uncontrolled_emis_sql := public.get_uncontrolled_ann_emis_expression(inv_table_alias, no_days_in_month, inv_override_table_alias, has_rpen_column);
	uncontrolled_emis_sql := public.get_uncontrolled_emis_expression(inv_pct_red_expression, emis_sql);
	
--	emis_sql := public.get_ann_emis_expression(inv_table_alias, no_days_in_month);
	
	-- build sql that calls ceff SQL equation 
/*	get_strategty_ceff_equation_sql := public.get_ceff_equation_expression(
		int_input_dataset_id, -- int_input_dataset_id
		inventory_year, -- inventory_year
		inv_table_alias, --inv_table_alias character varying(64), 
		control_measure_efficiencyrecord_table_alias);*/
	get_strategty_ceff_equation_sql := public.get_ceff_equation_expression(
		annualized_emis_sql, 
		inv_table_alias, 
		control_measure_efficiencyrecord_table_alias,
		is_point_table);


/*	percent_reduction_sql := public.get_control_percent_reduction_expression(int_input_dataset_id,
		inventory_year,
		inv_table_alias, 
		no_days_in_month, 
		inv_override_table_alias, 
		measures_count, 
		control_strategy_measure_table_alias, 
		control_measure_efficiencyrecord_table_alias);*/
	percent_reduction_sql := public.get_control_percent_reduction_expression(
		emis_sql,
		inv_table_alias, 
		control_measure_efficiencyrecord_table_alias,
		is_point_table, 
		measures_count, 
		control_strategy_measure_table_alias);

	
--	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
/*	remaining_emis_sql := public.get_remaining_emis_expression(int_input_dataset_id,
		inventory_year,
		inv_table_alias, 
		no_days_in_month, 
		inv_override_table_alias, 
		measures_count, 
		control_strategy_measure_table_alias, 
		control_measure_efficiencyrecord_table_alias, 
		has_rpen_column);*/
	remaining_emis_sql := public.get_remaining_emis_expression(
		emis_sql, 
		inv_pct_red_expression,
		inv_table_alias, 
		control_measure_efficiencyrecord_table_alias,
		is_point_table,
		measures_count,
		control_strategy_measure_table_alias);

/*	emis_reduction_sql := public.get_emis_reduction_expression(int_input_dataset_id,
		inventory_year,
		inv_table_alias, 
		no_days_in_month, 
		inv_override_table_alias, 
		measures_count, 
		control_strategy_measure_table_alias, 
		control_measure_efficiencyrecord_table_alias, 
		has_rpen_column);*/
	emis_reduction_sql := public.get_emis_reduction_expression(
		emis_sql,
		inv_pct_red_expression,
		percent_reduction_sql,
		control_measure_efficiencyrecord_table_alias);
--	'(case when coalesce(' || control_measure_efficiencyrecord_table_alias || '.existing_measure_abbr, '''') <> '''' or ' || control_measure_efficiencyrecord_table_alias || '.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100)::double precision';

/*raise notice '%', uncontrolled_emis_sql;
raise notice '%', emis_sql;
raise notice '%', get_strategty_ceff_equation_sql;
raise notice '%', percent_reduction_sql;
raise notice '%', remaining_emis_sql;
raise notice '%', emis_reduction_sql;
*/








-- TYPE 14 definition Fabric Filter ---------------
/*

F_a=(V_Exhaust )(DC)/60	Equation 1

Where:
Fa	= Exhaust flowrate, ACFM
VExhaust	= Relative Exhaust Volume, ACF/MMBtu	--> ' || control_measure_equation_table_alias || '.value1
DC	= Design Capacity of Unit, MMBtu/hr	--> convert_design_capacity_expression

t14_fa := '(' || control_measure_equation_table_alias || '.value5 * (' || convert_design_capacity_expression || ') / 60.0 || ')';

F_a = ' || control_measure_equation_table_alias || '.value5 * (' || convert_design_capacity_expression || ') / 60.0

F_d=F_a ((460+68)/(460+T))(1-%_Moist/100)	Equation 2

t14_fd := '((' || t14_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value2 / 100.0))';

F_d = (' || control_measure_equation_table_alias || '.value5 * (' || convert_design_capacity_expression || ') / 60.0) * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value2 / 100.0)


Where:
Fd	= Exhaust flowrate, DSCFM
Fa	= Exhaust flowrate, ACFM
T	= Assumed Stack Gas Temperature, °F --> ' || inv_table_alias || '.stktemp
%Moist	= Assumed Stack Gas Moisture Content, %	--> ' || control_measure_equation_table_alias || '.value1

t14_noducts := '(case when  ' || t14_fd || ' <= 154042.0 then 1 else ceiling(' || t14_fd || ' / 154042.0) end)';


TCI=(105.91)(F_d )+(699754.7)+[(0.560) (√(F_a )/#_Ducts )^2 ]+[(1096.141) e^(0.017)(√(F_a )/#_Ducts )  ]+[(33.977) e^(0.014)(√(F_a )/#_Ducts )  ]

t14_tci := '((105.91) * (' || t14_fd || ')+(699754.7)+((0.560) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' )^2) + ((1096.141) * exp((0.017) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' ))) + ((33.977) * exp((0.014) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' ))))';


Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
#Ducts	= If Fd ≤ 154042, #Ducts = 1;
If Fd > 154042, #Ducts = Fd / 154042

select case when  154044.0 <= 154042.0 then 1 else ceiling(1540404.0 / 154042.0) end

B-1b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=[(17.44)(〖Op〗_Hrs )]+{(TCI)[(0.072)+(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1))]}+{(F_a )[(4.507)+(0.0000124)(〖Op〗_Hrs )-(4.184)(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1))]}+{(F_d )(〖Op〗_Hrs )[(0.00376)+(0.00181)(C_PM )]}

t14_tac := '[(17.44)(' || inv_table_alias || '.annual_avg_hours_per_year || ')]+{(' || t14_tci || ')[(0.072)+(' || capital_recovery_factor_expression || ')]}+{(' || t14_fa || ')[(4.507)+(0.0000124)(' || inv_table_alias || '.annual_avg_hours_per_year || ')-(4.184)(' || capital_recovery_factor_expression || ')]}+{(' || t14_fd || ')(' || inv_table_alias || '.annual_avg_hours_per_year || ')[(0.00376)+(0.00181)(C_PM )]}';

Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
OpHrs	= Annual operating hours of unit (hrs/yr)
TCI	= Total Capital Investment ($)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
CPM	= Concentration of PM in stack gas, grains per dry standard cubic foot (gr/dscf)
i	= Interest rate expressed as a fraction of 1 (percentage divided by 100)
EqLife	= Estimated equipment life, years

Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  value1 --> % Moisture
Inventory Inputs:  
	design capacity
	design capacity units
	stack temperature
	stack flow rate (in cfm)
	operating hours (in hrs/yr)


Questions:
Is PM emission rate based on pm2_5 or pm10?
Is PM emission rate based on measure inlet or outlet rate?
*/

t14_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 14'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
--use brenda shines approach
t14_fa := '(' || stkflow_expression || ' * 60.0)';
t14_fd := '((' || t14_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value1 / 100.0))';
t14_noducts := '(case when  ' || t14_fd || ' <= 154042.0 then 1 else round(' || t14_fd || ' / 154042.0) end)';
t14_cpm := '(' || emis_sql || ') * 1.725 * 15.4323584 / (' || t14_fd || ')'; /*1 ton/year = 1.725 grams/minute (from David)  1 gram = 15.4323584 grains */
t14_tci := '((105.91) * (' || t14_fd || ')+(699754.7)+((0.560) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' )^2) + ((1096.141) * exp((0.017) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' ))) + ((33.977) * exp((0.014) * (sqrt(' || t14_fa || ')/' || t14_noducts || ' ))))';
t14_tac := '((17.44) * (' || inv_table_alias || '.annual_avg_hours_per_year))+((' || t14_tci || ') * ((0.072)+(' || capital_recovery_factor_expression || ')))+((' || t14_fa || ') * ((4.507)+(0.0000124) * (' || inv_table_alias || '.annual_avg_hours_per_year)-(4.184) * (' || capital_recovery_factor_expression || ')))+((' || t14_fd || ') * (' || inv_table_alias || '.annual_avg_hours_per_year) * ((0.00376)+(0.00181) * (' || t14_cpm || ')))';

-- TYPE 15 definition Electrostatic Precipitator  ---------------
/*
TCI={(12.265)(〖EC〗_1 ) [(5.266)(F_a )]^(〖EC〗_2 ) }+[(0.784)(F_a/#_Ducts )]+(#_Ducts ){[(2237.13)(e^(0.017)(√(F_a )/#_Ducts )  )]+[(69.345)(e^(0.014)(√(F_a )/#_Ducts )  )]+(17588.69)}

Where:
EC1	= First equipment cost factor for ESP;
If Fa ≥ 9495, EC1 = 57.87;
If Fa < 9495, EC1 = 614.55
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
EC2	= Second equipment cost factor for ESP;
If Fa ≥ 9495, EC2 = 0.8431;
If Fa < 9495, EC2 = 0.6276
#Ducts	= If Fa < 308084, #Ducts = 1;
If 308084 ≤ Fa < 462126, #Ducts = 2;
If 462126 ≤ Fa < 616168, #Ducts = 3;
If Fa ≥ 616168, #Ducts = 4

B-2b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=[(10.074)(〖Op〗_Hrs )]+[(0.052)(F_a )]+{(0.00656)(1.04+(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1)))((〖EC〗_1 ) [(5.266)(F_a )]^(〖EC〗_2 ) )}+[(0.021)(〖Op〗_Hrs )(E_PM )(DC)]+{(0.0000117)(F_a )(〖Op〗_Hrs )[(1.895)+((479.85) (1/√(F_a ))^1.18 )]}+[(0.000715)(〖Op〗_Hrs )(F_a )]+{(0.04+(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1)))(#_Ducts )[(0.783) (√(F_a )/#_Ducts )^2+(2237.44)(e^(0.0165)(√(F_a )/#_Ducts )  )+(69.355)(e^(0.0140)(√(F_a )/#_Ducts )  )+(17591.15)]}

Where:
OpHrs	= Annual operating hours of unit (hrs/yr)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
i	= Interest rate expressed as a fraction of 1 (percentage divided by 100)
EqLife	= Estimated equipment life, years 
EC1	= First equipment cost factor for ESP;
If Fa ≥ 9495, EC1 = 57.87;
If Fa < 9495, EC1 = 614.55
EC2	= Second equipment cost factor for ESP;
If Fa ≥ 9495, EC2 = 0.8431;
If Fa < 9495, EC2 = 0.6276
EPM	= PM emission rate, pounds per million British thermal units (lb/MMBtu)
DC	= Design capacity of boiler, million British thermal units per hour (MMBtu/hr)
#Ducts	= If Fa < 308084, #Ducts = 1;
If 308084 ≤ Fa < 462126, #Ducts = 2;
If 462126 ≤ Fa < 616168, #Ducts = 3;
If Fa ≥ 616168, #Ducts = 4

Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  value1 --> % Moisture
Inventory Inputs:  
	design capacity
	design capacity units
	stack temperature
	stack flow rate (in cfm)
	operating hours (in hrs/yr)

Questions:
Is PM emission rate based on pm2_5 or pm10?
Is PM emission rate based on measure inlet or outlet rate?
*/

t15_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 15'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
--use brenda shines approach
t15_fa := '(' || stkflow_expression || ' * 60.0)';
t15_fd := '((' || t15_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value1 / 100.0))';
t15_ec1 := '(case when  ' || t15_fa || ' < 9495.0 then 614.55 else 57.87 end)';
t15_ec2 := '(case when  ' || t15_fa || ' < 9495.0 then 0.6276 else 0.8431 end)';
t15_noducts := '(case when  ' || t15_fa || ' < 308084.0 then 1 when  ' || t15_fa || ' >= 308084.0 and ' || t15_fa || ' < 462126.0 then 2 when  ' || t15_fa || ' >= 462126.0 and ' || t15_fa || ' < 616168.0 then 3 else 4 end)';
t15_pm_emis_rate := '(' || emis_sql || ') * 2000.0 / 365.0 / 24.0 / (3.412 * ' || convert_design_capacity_expression || ')';
t15_tci := '((12.265) * (' || t15_ec1 || ') * ((5.266) * (' || t15_fa || ' ))^(' || t15_ec2 || ') )+((0.784) * (' || t15_fa || '/' || t15_noducts || '))+(' || t15_noducts || ') * (((2237.13) * (exp((0.017) * (sqrt(' || t15_fa || ')/' || t15_noducts || '))))+((69.345) * (exp((0.014) * (sqrt(' || t15_fa || ')/' || t15_noducts || '))))+(17588.69))';
t15_tac := '((10.074) * (' || inv_table_alias || '.annual_avg_hours_per_year))+((0.052) * (' || t15_fa || '))+((0.00656) * (1.04+((' || capital_recovery_factor_expression || '))) * ((' || t15_ec1 || ') * ((5.266) * (' || t15_fa || '))^(' || t15_ec2 || ') ))+((0.021) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t15_pm_emis_rate || ') * (3.412 * ' || convert_design_capacity_expression || '))+((0.0000117) * (' || t15_fa || ') * (' || inv_table_alias || '.annual_avg_hours_per_year) * ((1.895)+((479.85) * (1/sqrt(' || t15_fa || '))^1.18 )))+((0.000715) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t15_fa || '))+((0.04+((' || capital_recovery_factor_expression || '))) * (' || t15_noducts || ') * ((0.783) * (sqrt(' || t15_fa || ')/(' || t15_noducts || '))^2+(2237.44) * (exp((0.0165) * (sqrt(' || t15_fa || ')/(' || t15_noducts || '))))+(69.355) * (exp((0.0140) * (sqrt(' || t15_fa || ')/(' || t15_noducts || '))))+(17591.15)))';


-- TYPE 16 definition WET SCRUBBER  ---------------
/*

APPENDIX B-3:  WET SCRUBBER COST EQUATIONS
________________________________________

B-3a:  TOTAL CAPITAL INVESTMENT (TCI)

TCI=[(2.88)(#_Scrub )(F_a )]+[(1076.54)(#_Scrub ) √((F_a ) )]+[(9.759)(F_a )]+[(360.463) √((F_a ) )]

Where:
#Scrub	= If Fa < 149602, #Scrub = 1;
If 149602 ≤ Fa < 224403, #Scrub = 2;
If 224403 ≤ Fa < 299204, #Scrub = 3;
If 299204 ≤ Fa < 374005, #Scrub = 4;
If Fa ≥ 374005, #Scrub = 5
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)

B-3b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=[(#_Scrub )(TCI)(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1))]+[(0.04)(TCI)]+{(20.014)(#_Scrub )(F_a )(〖Op〗_Hrs )[C_SO2-(C_SO2 )((100-98)/(100-(98)(C_SO2 ) ))]}+[(16.147)(#_Scrub )(〖Op〗_Hrs )]+{(0.0000117)(F_a )(〖Op〗_Hrs )(#_Scrub )[((479.85) (1/√(F_a ))^1.18 )+(6.895)]}+[(0.0000133)(〖Op〗_Hrs )(#_Scrub )(F_a )]

Where:
#Scrub	= If Fa < 149602, #Scrub = 1;
If 149602 ≤ Fa < 224403, #Scrub = 2;
If 224403 ≤ Fa < 299204, #Scrub = 3;
If 299204 ≤ Fa < 374005, #Scrub = 4;
If Fa ≥ 374005, #Scrub = 5
TCI	= Total Capital Investment ($)
i	= Interest rate expressed as a fraction of 1 (percentage divided by 100)
EqLife	= Estimated equipment life, years 
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
CSO2	= Mole fraction of SO2 in exhaust gas
OpHrs	= Annual operating hours of unit (hrs/yr)

CSO2 calculation:

Calculate volume (ft3/lb-mole) of NOx in gaseous form under standard conditions (60 F, 1 atm) using Ideal Gas Law, pV = nRT or V/n = RT/p, where:
	V = volume in ft3
	n = molecular weight of SO2 (64.06 lb/lb-mole)
	R = gas constant (0.7302 atm-ft3/lb-mole R)
	T = absolute temperature in Rankin (F + 460) = 60 + 460 = (520 R)
	p = pressure in atmospheres (1 atm)
Ideal Gas Law approximates the volume of a gas under certain conditions.

V/n = (0.7302 x 520) / (1) = 379.7 ft3/lb-mole

Calculate SO2 emissions (lb-mole/yr):

	n = 53.33 tons/yr SO2  x  2000 lb/ton  x  1 lb-mole / 64.06 lbs SO2 = 1,665 lb-mole/yr

Convert SO2 emissions (lb-mole/yr) to SO2 volumetric flowrate (ft3/min):

1,665 lb-mole/yr x 1 yr/8736 hrs x 1 hr/60 min x 379.7ft3/lb-mole = 1.206 ft3/min

	Calculate outlet concentration of SO2 (ppmv):

ppmv SO2 = ( SO2 emissions (ft3/min) / Stack vol flow rate scfm ) x 10^6

ppmv SO2 = ( 1.206 ft3/min / 20,170 ft3/min ) x 10^6  = 59.79 ppmv

mole fraction SO2 = 59.79 ppmv / 10^6 = 5.979e-5 mole fraction SO2

Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  NONE
Inventory Inputs:  
	stack flow rate (in cfm)
	operating hours (in hrs/yr)

Questions:
Is SO2 concentration based on measure inlet or outlet rate?
*/

t16_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 16'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
t16_fa := '(' || stkflow_expression || ' * 60.0)';
t16_noscrubbers := '(case when  ' || t16_fa || ' < 149602.0 then 1 when  ' || t16_fa || ' >= 149602.0 and ' || t16_fa || ' < 224403.0 then 2 when  ' || t16_fa || ' >= 224403.0 and ' || t16_fa || ' < 299204.0 then 3 when  ' || t16_fa || ' >= 299204.0 and ' || t16_fa || ' < 374005.0 then 4 else 5 end)';
t16_so2_mole_conc := '((' || emis_sql || ') * 2000.0 / (64.06) / ' || inv_table_alias || '.annual_avg_hours_per_year / 60 * ((0.7302 * 520) / (1.0)) / (	(' || t16_fa || ') * 520 / ((' || inv_table_alias || '.stktemp) + 460.0)))';
t16_tci := '((2.88) * (' || t16_noscrubbers || ') * (' || t16_fa || '))+((1076.54) * (' || t16_noscrubbers || ') * sqrt(' || t16_fa || '))+((9.759) * (' || t16_fa || '))+((360.463) * sqrt(' || t16_fa || '))';
t16_tac := '((' || t16_noscrubbers || ') * (' || t16_tci || ') * (' || capital_recovery_factor_expression || '))+((0.04) * (' || t16_tci || '))+((20.014) * (' || t16_noscrubbers || ') * (' || t16_fa || ') * (' || inv_table_alias || '.annual_avg_hours_per_year) * ((' || t16_so2_mole_conc || ')-(' || t16_so2_mole_conc || ') * ((100.0-98.0)/(100.0-(98.0) * (' || t16_so2_mole_conc || ') ))))+((16.147) * (' || t16_noscrubbers || ') * (' || inv_table_alias || '.annual_avg_hours_per_year))+((0.0000117) * (' || t16_fa || ') * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t16_noscrubbers || ') * (((479.85) * (1/sqrt(' || t16_fa || '))^1.18 )+(6.895)))+((0.0000133) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t16_noscrubbers || ') * (' || t16_fa || '))';




-- TYPE 17 definition WET SCRUBBER  ---------------
/*

APPENDIX B-4:  DRY INJECTION/FABRIC FILTER SYSTEM (DIFF) COST EQUATIONS
________________________________________

B-4a:  TOTAL CAPITAL INVESTMENT (TCI)

TCI=[(143.76)(F_d )]+[(0.610) (√(F_a )/#_Ducts )^2 ]+[(1757.65) e^(0.017)(√(F_a )/#_Ducts )  ]+[(59.973) e^(0.014)(√(F_a )/#_Ducts )  ]+(931911.04)

Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
#Ducts	= If Fd ≤ 154042, #Ducts = 1;
If Fd > 154042, #Ducts = Fd / 154042

B-4b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=[(0.00162)(〖Op〗_Hrs )(F_d )]+[(17.314)(〖Op〗_Hrs )]+[(0.00000105)(C_SO2 )(F_d )(〖Op〗_Hrs )]+[(0.0000372)(〖Op〗_Hrs )(F_a )]+[(0.000181)(〖Op〗_Hrs )(C_PM )(F_d )]+[(0.847)(1-(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1)))(F_a )]+[(0.04)+(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1))]{[(0.032)(TCI)]+[(0.606) (√(F_a )/#_Ducts )^2 ]+[(1757.65) e^(0.017)(√(F_a )/#_Ducts )  ]+[(53.973) e^(0.014)(√(F_a )/#_Ducts )  ]+(13689.81)}

Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
OpHrs	= Annual operating hours of unit (hrs/yr)
CSO2	= Concentration of SO2 in stack gas, dry parts per million by volume (ppmvd)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
CPM	= Concentration of PM in stack gas, grains per dry standard cubic foot (gr/dscf)
i	= Interest rate expressed as a fraction of 1 (percentage divided by 100)
EqLife	= Estimated equipment life, years 
#Ducts	= If Fd ≤ 154042, #Ducts = 1;
If Fd > 154042, #Ducts = Fd / 154042

Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  value1 --> % Moisture
Inventory Inputs:  
	stack temperature
	stack flow rate (in cfm)
	operating hours (in hrs/yr)

Questions:
Is SO2 and PM (also which PM) concentration based on measure inlet or outlet rate?
*/

t17_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 17'' and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
--use brenda shines approach
t17_fa := '(' || stkflow_expression || ' * 60.0)';
t17_fd := '((' || t17_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value1 / 100.0))';
t17_noducts := '(case when  ' || t17_fd || ' < 154042.0 then 1 else round(' || t17_fd || ' / 154042.0) end)';
t17_pm_conc := '(' || emis_sql || ') * 1.725 * 15.4323584 / (' || t17_fd || ')'; /*1 ton/year = 1.725 grams/minute (from David)  1 gram = 15.4323584 grains */
t17_so2_conc := '((' || so2_emis_sql || ') * 2000.0 / (64.06) / ' || inv_table_alias || '.annual_avg_hours_per_year / 60 * ((0.7302 * 520) / (1.0)) / (	(' || t17_fa || ') * 520 / ((' || inv_table_alias || '.stktemp) + 460.0))) * 10^6';
t17_tci := '((143.76) * (' || t17_fd || '))+((0.610) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')^2 )+((1757.65) * exp((0.017) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')))+((59.973) * exp((0.014) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')))+(931911.04)';
t17_tac := '((0.00162) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t17_fd || '))+((17.314) * (' || inv_table_alias || '.annual_avg_hours_per_year))+((0.00000105) * (' || t17_so2_conc || ' ) * (' || t17_fd || ') * (' || inv_table_alias || '.annual_avg_hours_per_year))+((0.0000372) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t17_fa || '))+((0.000181) * (' || inv_table_alias || '.annual_avg_hours_per_year) * (' || t17_pm_conc || ') * (' || t17_fd || '))+((0.847) * (1-(' || capital_recovery_factor_expression || ')) * (' || t17_fa || '))+((0.04)+(' || capital_recovery_factor_expression || ')) * (((0.032) * (' || t17_tci || '))+((0.606) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')^2 )+((1757.65) * exp((0.017) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')))+((53.973) * exp((0.014) * (sqrt(' || t17_fa || ')/' || t17_noducts || ')))+(13689.81))';


-- TYPE 18 definition INCREASED CAUSTIC INJECTION RATE FOR EXISTING DRY INJECTION CONTROL  ---------------
/*
APPENDIX B-5:  INCREASED CAUSTIC INJECTION RATE FOR EXISTING DRY INJECTION CONTROL COST EQUATIONS
________________________________________

B-5a:  TOTAL CAPITAL INVESTMENT (TCI)

TCI=0

Where:
N/A

B-5b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=(0.00000387)(C_SO2 )(F_d )(〖Op〗_Hrs )

Where:
CSO2	= Concentration of SO2 in stack gas, dry parts per million by volume (ppmvd)
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
OpHrs	= Annual operating hours of unit (hrs/yr)

Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  value1 --> % Moisture
Inventory Inputs:  
	stack temperature
	stack flow rate (in cfm)
	operating hours (in hrs/yr)

Questions:
Is SO2 concentration based on measure inlet or outlet rate?
*/

t18_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 18'' and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
--use brenda shines approach
t18_fa := '(' || stkflow_expression || ' * 60.0)';
t18_fd := '((' || t17_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value1 / 100.0))';
t18_so2_conc := '((' || emis_sql || ') * 2000.0 / (64.06) / ' || inv_table_alias || '.annual_avg_hours_per_year / 60 * ((0.7302 * 520) / (1.0)) / (	(' || t17_fa || ') * 520 / ((' || inv_table_alias || '.stktemp) + 460.0))) * 10^6';
t18_tci := '0.0';
t18_tac := '(0.00000387) * (' || t18_so2_conc || ') * (' || t18_fd || ') * (' || inv_table_alias || '.annual_avg_hours_per_year)';


-- TYPE 19 definition SPRAY DRYER ABSORBER  ---------------
/*
APPENDIX B-6:  SPRAY DRYER ABSORBER COST EQUATIONS
________________________________________

B-6a:  TOTAL CAPITAL INVESTMENT (TCI)

TCI=[(143.76)(F_d )]+[(0.610) (√(F_a )/#_Ducts )^2 ]+[(17412.26) e^(0.017)(√(F_a )/#_Ducts )  ]+[(53.973) e^(0.014)(√(F_a )/#_Ducts )  ]+(931911.04)

Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
#Ducts	= If Fd ≤ 154042, #Ducts = 1;
If Fd > 154042, #Ducts = Fd / 154042

B-6b:  TOTAL ANNUALIZED COSTS (TAC)

TAC=(〖Op〗_Hrs ){[(0.00162)(F_d )]+[(0.000000684)(C_SO2 )(F_d )]+[(0.0000372)(F_a )]+(21.157)}+{[0.072+(((i) (1+i)^(〖Eq〗_Life ))/((1+i)^(〖Eq〗_Life )-1))](TCI)}

Where:
Fd	= Exhaust Flowrate, dry standard cubic feet per minute (DSCFM)
Fa	= Exhaust Flowrate, actual cubic feet per minute (ACFM)
OpHrs	= Annual operating hours of unit (hrs/yr)
CSO2	= Concentration of SO2 in stack gas, dry parts per million by volume (ppmvd)
TCI	= Total Capital Investment ($)
i	= Interest rate expressed as a fraction of 1 (percentage divided by 100)
EqLife	= Estimated equipment life, years
 
Equation Type Definition:

Measure Specific Equation Type Variable Inputs:  value1 --> % Moisture
Inventory Inputs:  
	stack temperature
	stack flow rate (in cfm)
	operating hours (in hrs/yr)

Questions:
Is SO2 concentration based on measure inlet or outlet rate?

*/

t19_use_equation := 'coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 19'' and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0.0) <> 0.0';
--use brenda shines approach
t19_fa := '(' || stkflow_expression || ' * 60.0)';
t19_fd := '((' || t17_fa || ') * ((460.0 + 68.0)/(460.0 + ' || inv_table_alias || '.stktemp)) * (1.0 - ' || control_measure_equation_table_alias || '.value1 / 100.0))';
t19_noducts := '(case when  ' || t17_fd || ' < 154042.0 then 1 else round(' || t17_fd || ' / 154042.0) end)';
t19_so2_conc := '((' || emis_sql || ') * 2000.0 / (64.06) / ' || inv_table_alias || '.annual_avg_hours_per_year / 60 * ((0.7302 * 520) / (1.0)) / (	(' || t17_fa || ') * 520 / ((' || inv_table_alias || '.stktemp) + 460.0))) * 10^6';
t19_tci := '((143.76) * (' || t19_fd || '))+((0.610) * (sqrt(' || t19_fa || ')/' || t19_noducts || ')^2 )+((17412.26) * exp((0.017) * (sqrt(' || t19_fa || ')/' || t19_noducts || ')))+((53.973) * exp((0.014) * (sqrt(' || t19_fa || ')/' || t19_noducts || ')))+(931911.04)';
t19_tac := '(' || inv_table_alias || '.annual_avg_hours_per_year) * (((0.00162) * (' || t19_fd || '))+((0.000000684) * (' || t19_so2_conc || ') * (' || t19_fd || '))+((0.0000372) * (' || t19_fa || '))+(21.157))+((0.072+(' || capital_recovery_factor_expression || ')) * (' || t19_tci || '))';




----- END of equation definitions -----


	-- prepare annual_cost_expression 
	annual_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 
				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * ' || '
					(
					/*annualized_capital_cost*/ 
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * ' || convert_design_capacity_expression || ' 
					* case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and ' || convert_design_capacity_expression || ' >= 600.0 then 1.0
						when ' || convert_design_capacity_expression || ' >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end /*scaling_factor*/
					* 1000) /*capital_cost*/
					* ' || capital_recovery_factor_expression || '
					+ 
					/*operation_maintenance_cost*/
					coalesce(' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * 1000/*fixed_operation_maintenance_cost*/, 0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/, 0)
					)

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value3 else ' || control_measure_equation_table_alias || '.value7 end)/*annual_cost_multiplier*/ * ((3.412 * ' || convert_design_capacity_expression || '/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value4 else ' || control_measure_equation_table_alias || '.value8 end)/*annual_cost_exponent*/)
					)

				' end || '

				' || case when not is_point_table then '' else '

				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then (1028000/ ' || stkflow_expression || ') ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * ' || stkflow_expression || ' * 0.9383/*capital_cost*/
					* 
					' || capital_recovery_factor_expression || ')/*annualized_capital_cost*/ 
					+ (
					(0.486 * 6.9 * ' || stkflow_expression || ')/*fixed_operation_maintenance_cost*/
					+ (0.486 * 0.0015 * 8736 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(990000 + 9.836 * ' || stkflow_expression || ')/*capital_cost*/ 
					* ' || capital_recovery_factor_expression || '/*annualized_capital_cost*/ 
					+ (
					75800/*fixed_operation_maintenance_cost*/ + (12.82 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(2882540 + 244.74 * ' || stkflow_expression || ')/*capital_cost*/ 
					* ' || capital_recovery_factor_expression || '/*annualized_capital_cost*/ 
					+ (
					749170/*fixed_operation_maintenance_cost*/ + (148.40 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(3449803 + 135.86 * ' || stkflow_expression || ')/*capital_cost*/ 
					* ' || capital_recovery_factor_expression || '/*annualized_capital_cost*/ 
					+ (
					797667/*fixed_operation_maintenance_cost*/ + (58.84 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then 
							(2882540 + (244.74 * ' || stkflow_expression || ') + (((1028000 / ' || stkflow_expression || ') ^ 0.6)) * 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
						else
							(2882540 + (244.74 * ' || stkflow_expression || ') + 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
					end/*capital_cost*/ 
					* ' || capital_recovery_factor_expression || '/*annualized_capital_cost*/ 
					+ (749170 + (148.40 * ' || stkflow_expression || ') + (3.35 + (0.000729 * 8736 ) * (' || stkflow_expression || ') ^ 0.9383))/*operation_maintenance_cost*/
					)

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' >= 5.0 then 
							/*capital_cost*/case 
								when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * ' || stkflow_expression || '
								else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
							end 
							* ' || capital_recovery_factor_expression || ' 
							+ 0.04 * 
							/*capital_cost*/case 
								when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * ' || stkflow_expression || '
								else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
							end 
							+ 
							/*operation_maintenance_cost*/case 
								when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value2/*om_control_cost_factor*/ * ' || stkflow_expression || '
								else ' || control_measure_equation_table_alias || '.value4/*default_om_cpt_factor*/ * ' || emis_reduction_sql || '
							end
						else
							' || control_measure_equation_table_alias || '.value5/*default_annualized_cpt_factor*/ * ' || emis_reduction_sql || '
					end
					)

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					((((' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/)/*capital_cost*/ * (' || capital_recovery_factor_expression || '))/*annualized_capital_cost*/ 
					+ (((' || control_measure_equation_table_alias || '.value4/*electricity_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value5/*electricity_constant*/) + ((' || control_measure_equation_table_alias || '.value6/*dust_disposal_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value7/*dust_disposal_constant*/) + ((' || control_measure_equation_table_alias || '.value8/*bag_replacement_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value9/*bag_replacement_constant*/))/*operation_maintenance_cost*/
					)

				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					((' || convert_design_capacity_expression_default_MW_units || ' * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/ * (' || capital_recovery_factor_expression || '))/*annualized_capital_cost*/ 
					+ ((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * ' || convert_design_capacity_expression_default_MW_units || ' * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year)/*variable_operation_maintenance_cost*/ + (' || convert_design_capacity_expression_default_MW_units || ' * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/)/*operation_maintenance_cost*/

					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * ' || convert_design_capacity_expression || ' <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * ' || convert_design_capacity_expression || ' < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/
					)
					
				' end || '

				' || case when not is_point_table then '' else '

				-- Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0.0) <> 0.0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
						(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
						+ (coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
					)/*operation_maintenance_cost*/
					+ (coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) + coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000) ^ 0.6 * ' || capital_recovery_factor_expression || '/*annualized_capital_cost*/
					)
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						Capital Cost = var1*input1^var2+var3*input1^var4 
						O&M Cost = var5+var6*input1^var7+var8*input1^var9+var10*input3+var11*input2
						Annual Cost = Capital Cost * capital_recovery_factor + O&M Cost


						where

						input1 = boiler size in MMBtu/hr
						input2 = boiler emissions in ton/yr
						input3 = boiler exhaust flowrate in ft3/sec
						var1 = Capital cost size multiplier No.1
						var2 = Capital cost exponent No. 1
						var3 = Capital cost size multiplier No.2
						var4 = Capital cost exponent No. 2
						var5 = O&M known costs
						var6 = O&M cost size multiplier No.1
						var7 = O&M cost size exponent No. 1
						var8 = O&M cost size multiplier No. 2
						var9 = O&M cost size exponent No. 2
						var10 = O&M cost flowrate multiplier
						var11 = O&M cost emissions multiplier
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || control_measure_equation_table_alias || '.value1 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value2 + ' || control_measure_equation_table_alias || '.value3 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value4)/*capital_cost*/ * (' || capital_recovery_factor_expression || ') 
					+ (' || control_measure_equation_table_alias || '.value5 + ' || control_measure_equation_table_alias || '.value6 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value7 + ' || control_measure_equation_table_alias || '.value8 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value9 + ' || control_measure_equation_table_alias || '.value10 * (' || stkflow_expression || ' / 60.0) + ' || control_measure_equation_table_alias || '.value11 * ' || emis_reduction_sql || ')/*operation_maintenance_cost*/
					)
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 14 Fabric Filter Equations
				when ' || t14_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t14_tac || ')
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 15
				when ' || t15_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t15_tac || ')
				' end || '

				' || case when not is_point_table then '' else '
				-- Equation Type 16
				when ' || t16_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t16_tac || ')

				-- Equation Type 17
				when ' || t17_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t17_tac || ')

				-- Equation Type 18
				when ' || t18_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t18_tac || ')

				-- Equation Type 19
				when ' || t19_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t19_tac || ')
				' end || '

				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				else 
		' else '
	' end || '
				' || emis_reduction_sql || ' * (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) <> 0.0 and coalesce(' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton, 0.0) <> 0.0 then (' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_incr_table_alias || '.chained_gdp as double precision)) * ' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton else ' || chained_gdp_adjustment_factor || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton end)

	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

	-- prepare capital_cost_expression 
	capital_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * ' || convert_design_capacity_expression || ' 
					* (case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and ' || convert_design_capacity_expression || ' >= 600.0 then 1.0
						when ' || convert_design_capacity_expression || ' >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end) /*scaling_factor*/
					* 1000) /*capital_cost*/
					)

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * ' || convert_design_capacity_expression || '/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)
					)

				' end || '

				' || case when not is_point_table then '' else '
				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then (1028000/ ' || stkflow_expression || ') ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * ' || stkflow_expression || ' * 0.9383)/*capital_cost*/
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(990000 + 9.836 * ' || stkflow_expression || ')/*capital_cost*/ 
					)

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(2882540 + 244.74 * ' || stkflow_expression || ')/*capital_cost*/ 
					)

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(3449803 + 135.86 * ' || stkflow_expression || ')/*capital_cost*/ 
					)

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then 
							(2882540 + (244.74 * ' || stkflow_expression || ') + (((1028000 / ' || stkflow_expression || ') ^ 0.6)) * 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
						else
							(2882540 + (244.74 * ' || stkflow_expression || ') + 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
					end/*capital_cost*/ 
					)

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * ' || stkflow_expression || '
						else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
					end 
					)

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
						(
							(
								(' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ 
									* ' || stkflow_expression || '
								) 
								+ ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/
							) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/
						)
					)/*capital_cost*/
					)

				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || convert_design_capacity_expression_default_MW_units || ' * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/
					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * ' || convert_design_capacity_expression || ' <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * ' || convert_design_capacity_expression || ' < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					)
				' end || '

				' || case when not is_point_table then '' else '
				--Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)
					) 
					* ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000) ^ 0.6
					)

				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						Capital Cost = var1*input1^var2+var3*input1^var4 
						O&M Cost = var5+var6*input1^var7+var8*input1^var9+var10*input3+var11*input2
						Annual Cost = Capital Cost * capital_recovery_factor + O&M Cost


						where

						input1 = boiler size in MMBtu/hr
						input2 = boiler emissions in ton/yr
						input3 = boiler exhaust flowrate in ft3/sec
						var1 = Capital cost size multiplier No.1
						var2 = Capital cost exponent No. 1
						var3 = Capital cost size multiplier No.2
						var4 = Capital cost exponent No. 2
						var5 = O&M known costs
						var6 = O&M cost size multiplier No.1
						var7 = O&M cost size exponent No. 1
						var8 = O&M cost size multiplier No. 2
						var9 = O&M cost size exponent No. 2
						var10 = O&M cost flowrate multiplier
						var11 = O&M cost emissions multiplier
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(' || control_measure_equation_table_alias || '.value1 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value2 + ' || control_measure_equation_table_alias || '.value3 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value4)/*capital_cost*/ 
					
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 14 Fabric Filter Equations
				when ' || t14_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t14_tci || ')
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 15
				when ' || t15_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t15_tci || ')
				' end || '

				' || case when not is_point_table then '' else '
				-- Equation Type 16
				when ' || t16_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t16_tci || ')

				-- Equation Type 17
				when ' || t17_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t17_tci || ')

				-- Equation Type 18
				when ' || t18_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t18_tci || ')

				-- Equation Type 19
				when ' || t19_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t19_tci || ')
				' end || '

				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				else 
		' else '
	' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * ref_yr_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					' || emis_reduction_sql || ' * (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) <> 0.0 and coalesce(' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton, 0.0) <> 0.0 then (' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_incr_table_alias || '.chained_gdp as double precision)) * ' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton else ' || chained_gdp_adjustment_factor || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton end) * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

	-- prepare operation_maintenance_cost_expression 
	operation_maintenance_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					/*operation_maintenance_cost*/
					coalesce(' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * 1000/*fixed_operation_maintenance_cost*/, 0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/, 0)
					)

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value3 else ' || control_measure_equation_table_alias || '.value7 end)/*annual_cost_multiplier*/ * ((3.412 * ' || convert_design_capacity_expression || '/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value4 else ' || control_measure_equation_table_alias || '.value8 end)/*annual_cost_exponent*/)
					- (
						(case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * ' || convert_design_capacity_expression || '/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)/*capital_cost*/
						* (' || capital_recovery_factor_expression || ')/*annualized_capital_cost*/
					)
					)
					
				' end || '
				' || case when not is_point_table then '' else '
				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
						(0.486 * 6.9 * ' || stkflow_expression || ')/*fixed_operation_maintenance_cost*/
						+ 0.486 * 0.0015 * 8736 * ' || stkflow_expression || '/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					75800/*fixed_operation_maintenance_cost*/ + (12.82 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					749170/*fixed_operation_maintenance_cost*/ + (148.40 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					797667/*fixed_operation_maintenance_cost*/ + (58.84 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(749170 + (148.40 * ' || stkflow_expression || ') + (3.35 + (0.000729 * 8736 ) * (' || stkflow_expression || ') ^ 0.9383))/*operation_maintenance_cost*/
					)

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value2/*om_control_cost_factor*/ * ' || stkflow_expression || '
						else ' || control_measure_equation_table_alias || '.value4/*default_om_cpt_factor*/ * ' || emis_reduction_sql || '
					end/*operation_maintenance_cost*/
					)

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(((' || control_measure_equation_table_alias || '.value4/*electricity_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value5/*electricity_constant*/) + ((' || control_measure_equation_table_alias || '.value6/*dust_disposal_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value7/*dust_disposal_constant*/) + ((' || control_measure_equation_table_alias || '.value8/*bag_replacement_factor*/ * ' || stkflow_expression || ') + ' || control_measure_equation_table_alias || '.value9/*bag_replacement_constant*/))/*operation_maintenance_cost*/
					)

				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * ' || convert_design_capacity_expression_default_MW_units || ' * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year)/*variable_operation_maintenance_cost*/ + (' || convert_design_capacity_expression_default_MW_units || ' * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/)/*operation_maintenance_cost*/
					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * ' || convert_design_capacity_expression || ' <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * ' || convert_design_capacity_expression || ' < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*//*annual_cost*/
					- ' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * ' || convert_design_capacity_expression || ' <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * ' || convert_design_capacity_expression || ' < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (' || capital_recovery_factor_expression || ')/*annualized_capital_cost*/
					)
				' end || '
				' || case when not is_point_table then '' else '
					
				-- Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
					+ (coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
					)
				' end || '


				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						Capital Cost = var1*input1^var2+var3*input1^var4 
						O&M Cost = var5+var6*input1^var7+var8*input1^var9+var10*input3+var11*input2
						Annual Cost = Capital Cost * capital_recovery_factor + O&M Cost


						where

						input1 = boiler size in MMBtu/hr
						input2 = boiler emissions in ton/yr
						input3 = boiler exhaust flowrate in ft3/sec
						var1 = Capital cost size multiplier No.1
						var2 = Capital cost exponent No. 1
						var3 = Capital cost size multiplier No.2
						var4 = Capital cost exponent No. 2
						var5 = O&M known costs
						var6 = O&M cost size multiplier No.1
						var7 = O&M cost size exponent No. 1
						var8 = O&M cost size multiplier No. 2
						var9 = O&M cost size exponent No. 2
						var10 = O&M cost flowrate multiplier
						var11 = O&M cost emissions multiplier
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(' || control_measure_equation_table_alias || '.value5 + ' || control_measure_equation_table_alias || '.value6 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value7 + ' || control_measure_equation_table_alias || '.value8 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value9 + ' || control_measure_equation_table_alias || '.value10 * (' || stkflow_expression || ' / 60.0) + ' || control_measure_equation_table_alias || '.value11 * ' || emis_reduction_sql || ')/*operation_maintenance_cost*/
					
				' end || '

				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				-- Default Approach
				else 
		' else '
	' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * case when coalesce(ceff, 0.0) <> 0.0 and coalesce(ref_yr_incremental_cost_per_ton, 0.0) <> 0.0 then ref_yr_incremental_cost_per_ton else ref_yr_cost_per_ton end;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					' || emis_reduction_sql || ' * (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) <> 0.0 and coalesce(' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton, 0.0) <> 0.0 then (' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_incr_table_alias || '.chained_gdp as double precision)) * ' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton else ' || chained_gdp_adjustment_factor || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton end)
					 - coalesce(' || emis_reduction_sql || ' * (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) <> 0.0 and coalesce(' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton, 0.0) <> 0.0 then (' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_incr_table_alias || '.chained_gdp as double precision)) * ' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton else ' || chained_gdp_adjustment_factor || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton end) * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision * ' || capital_recovery_factor_expression || ', 0)
	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

	-- prepare fixed_operation_maintenance_cost_expression 
	fixed_operation_maintenance_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * 1000/*fixed_operation_maintenance_cost*/
					)

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
				' end || '
				' || case when not is_point_table then '' else '

				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(0.486 * 6.9 * ' || stkflow_expression || ')/*fixed_operation_maintenance_cost*/
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					75800/*fixed_operation_maintenance_cost*/

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					749170.0/*fixed_operation_maintenance_cost*/

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					797667.0/*fixed_operation_maintenance_cost*/

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || convert_design_capacity_expression_default_MW_units || ' * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/
					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
				' end || '
				' || case when not is_point_table then '' else '
					
				-- Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
					)
				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
				' end || '
				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				else 
		' else '
	' end || '
					null::double precision
	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

	-- prepare variable_operation_maintenance_cost_expression 
	variable_operation_maintenance_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * ' || convert_design_capacity_expression || ' * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/
					)

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
					
				' end || '
				' || case when not is_point_table then '' else '
				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					0.486 * 0.0015 * 8736 * ' || stkflow_expression || '/*variable_operation_maintenance_cost*/
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(12.82 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(148.40 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(58.84 * ' || stkflow_expression || ')/*variable_operation_maintenance_cost*/
					)

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision

				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * ' || convert_design_capacity_expression_default_MW_units || ' * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year))/*variable_operation_maintenance_cost*/
					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
				' end || '
				' || case when not is_point_table then '' else '
					
				-- Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000)
					)
				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					|| chained_gdp_adjustment_factor_expression || ' * 
					null::double precision
				' end || '
				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				else 
			' else '
		' end || '
					null::double precision
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare annualized_capital_cost_expression 
	annualized_capital_cost_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * ' || convert_design_capacity_expression || ' 
					* (case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and ' || convert_design_capacity_expression || ' >= 600.0 then 1.0
						when ' || convert_design_capacity_expression || ' >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end) /*scaling_factor*/
					* 1000) /*capital_cost*/
					* (' || capital_recovery_factor_expression || ')
					)
					
				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * ' || convert_design_capacity_expression || '/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)
					* (' || capital_recovery_factor_expression || ')
					)
				' end || '
				' || case when not is_point_table then '' else '

				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then (1028000/ ' || stkflow_expression || ') ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * ' || stkflow_expression || ' * 0.9383)/*capital_cost*/
					* (' || capital_recovery_factor_expression || ')
					)

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(990000 + 9.836 * ' || stkflow_expression || ')/*capital_cost*/ 
					* (' || capital_recovery_factor_expression || ')
					)

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(2882540 + 244.74 * ' || stkflow_expression || ')/*capital_cost*/ 
					* (' || capital_recovery_factor_expression || ')
					)

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(3449803 + 135.86 * ' || stkflow_expression || ')/*capital_cost*/ 
					* (' || capital_recovery_factor_expression || ')
					)

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' < 1028000 then 
							(2882540 + (244.74 * ' || stkflow_expression || ') + (((1028000 / ' || stkflow_expression || ') ^ 0.6)) * 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
						else
							(2882540 + (244.74 * ' || stkflow_expression || ') + 93.3 * 1.1 * ' || stkflow_expression || ' * 0.9383)
					end/*capital_cost*/ 
					* (' || capital_recovery_factor_expression || ')
					)

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					case 
						when ' || stkflow_expression || ' >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * ' || stkflow_expression || '
						else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
					end 
					* (' || capital_recovery_factor_expression || ')
					)

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
						(
							(
								(' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ 
									* ' || stkflow_expression || '
								) 
								+ ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/
							) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/
						)
					)/*capital_cost*/
					* (' || capital_recovery_factor_expression || ')
					)
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(' || convert_design_capacity_expression_default_MW_units || ' * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / ' || convert_design_capacity_expression_default_MW_units || ') ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/
					* (' || capital_recovery_factor_expression || ')
					)

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * ' || convert_design_capacity_expression || ' <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * ' || convert_design_capacity_expression || ' < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * ' || convert_design_capacity_expression || ' >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (' || capital_recovery_factor_expression || ')
					)
				' end || '
				' || case when not is_point_table then '' else '

				--Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(
					(
					coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)
					) 
					* ((' || stkflow_expression || '/*stack_flow_rate*/ * 520 / (' || inv_table_alias || '.stktemp/*stack_temperature*/ + 460.0)) / 150000) ^ 0.6
					* (' || capital_recovery_factor_expression || ')
					)

				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' and coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then '
					/*
						Capital Cost = var1*input1^var2+var3*input1^var4 
						O&M Cost = var5+var6*input1^var7+var8*input1^var9+var10*input3+var11*input2
						Annual Cost = Capital Cost * capital_recovery_factor + O&M Cost


						where

						input1 = boiler size in MMBtu/hr
						input2 = boiler emissions in ton/yr
						input3 = boiler exhaust flowrate in ft3/sec
						var1 = Capital cost size multiplier No.1
						var2 = Capital cost exponent No. 1
						var3 = Capital cost size multiplier No.2
						var4 = Capital cost exponent No. 2
						var5 = O&M known costs
						var6 = O&M cost size multiplier No.1
						var7 = O&M cost size exponent No. 1
						var8 = O&M cost size multiplier No. 2
						var9 = O&M cost size exponent No. 2
						var10 = O&M cost flowrate multiplier
						var11 = O&M cost emissions multiplier
					*/|| chained_gdp_adjustment_factor_expression || ' * 
					(' || control_measure_equation_table_alias || '.value1 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value2 + ' || control_measure_equation_table_alias || '.value3 * 3.412 * ' || convert_design_capacity_expression || ' ^ ' || control_measure_equation_table_alias || '.value4)/*capital_cost*/ * (' || capital_recovery_factor_expression || ') 
					
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 14 Fabric Filter Equations
				when ' || t14_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t14_tci || ') * (' || capital_recovery_factor_expression || ')
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 15
				when ' || t15_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t15_tci || ') * (' || capital_recovery_factor_expression || ')
				' end || '

				' || case when not is_point_table then '' else '
				-- Equation Type 16
				when ' || t16_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t16_tci || ') * (' || capital_recovery_factor_expression || ')

				-- Equation Type 17
				when ' || t17_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t17_tci || ') * (' || capital_recovery_factor_expression || ')

				-- Equation Type 18
				when ' || t18_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t18_tci || ') * (' || capital_recovery_factor_expression || ')

				-- Equation Type 19
				when ' || t19_use_equation || ' then '
					/*
					*/|| chained_gdp_adjustment_factor_expression || ' * (' || t19_tci || ') * (' || capital_recovery_factor_expression || ')
				' end || '

				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::double precision
				else 
		' else '
	' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * ref_yr_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					' || emis_reduction_sql || ' * (case when coalesce(' || inv_table_alias || '.' || inv_ceff_expression || ', 0.0) <> 0.0 and coalesce(' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton, 0.0) <> 0.0 then (' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_incr_table_alias || '.chained_gdp as double precision)) * ' || control_measure_efficiencyrecord_table_alias || '.incremental_cost_per_ton else ' || chained_gdp_adjustment_factor || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton end) * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (' || capital_recovery_factor_expression || ')
	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

	computed_cost_per_ton_expression := 
	'case 
		when coalesce((' || emis_reduction_sql || '), 0) <> 0 then 
			(' || annual_cost_expression || ')
			/*annual_cost*/
			/ (' || emis_reduction_sql || ')
		else null::double precision
	end';


	-- prepare annualized_capital_cost_expression 
	actual_equation_type_expression := '(' ||
	case 
		when use_cost_equations then 
			'case 

				' || case when not has_design_capacity_columns then '' else '
				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' then
					case 
						when coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then 
							''Type 1''
						else
							''-Type 1''
					end

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' then
					case 
						when coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(3.412 * ' || convert_design_capacity_expression || ', 0) <= 2000.0 then 
							''Type 2''
						else
							''-Type 2''
					end
				' end || '
				' || case when not is_point_table then '' else '

				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then 
							''Type 3''
						else
							''-Type 3''
					end

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then
							''Type 4''
						else
							''-Type 4''
					end

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then
							''Type 5''
						else
							''-Type 5''
					end

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then
							''Type 6''
						else
							''-Type 6''
					end

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then 
							''Type 7''
						else
							''-Type 7''
					end

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then 
							''Type 8''
						else
							''-Type 8''
					end

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 then 
							''Type 9''
						else
							''-Type 9''
					end

				' end || '
				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' then
					case 
						when coalesce(' || convert_design_capacity_expression_default_MW_units || ', 0) <> 0 then 
							''Type 10''
						else
							''-Type 10''
					end

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' then
					case 
						when coalesce(' || convert_design_capacity_expression || ', 0) <> 0 then 
							''Type 11''
						else
							''-Type 11''
					end

				' end || '
				' || case when not is_point_table then '' else '
				--Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' then
					case 
						when coalesce(' || stkflow_expression || ', 0) <> 0 and coalesce(' || inv_table_alias || '.stktemp, 0) <> 0 then 
							''Type 12''
						else
							''-Type 12''
					end

				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 13
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 13'' then
					case 
						when coalesce(' || convert_design_capacity_expression || ', 0) <> 0 and coalesce(' || stkflow_expression || ', 0) <> 0 then 
							''Type 13''
						else
							''-Type 13''
					end

				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 14 Fabric Filter Equations
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 14'' then
					case 
						when ' || t14_use_equation || ' then
							''Type 14''
						else
							''-Type 14''
					end
				' end || '

				' || case when not has_design_capacity_columns then '' else '
				-- Equation Type 15
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 15'' then
					case 
						when ' || t15_use_equation || ' then
							''Type 15''
						else
							''-Type 15''
					end
				' end || '

				' || case when not is_point_table then '' else '
				-- Equation Type 16
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 16'' then
					case 
						when ' || t16_use_equation || ' then
							''Type 16''
						else
							''-Type 16''
					end

				-- Equation Type 17
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 17'' then
					case 
						when ' || t17_use_equation || ' then
							''Type 17''
						else
							''-Type 17''
					end

				-- Equation Type 18
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 18'' then
					case 
						when ' || t18_use_equation || ' then
							''Type 18''
						else
							''-Type 18''
					end

				-- Equation Type 19
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 19'' then
					case 
						when ' || t19_use_equation || ' then
							''Type 19''
						else
							''-Type 19''
					end

				' end || '

				-- Filler when clause, just in case no when part shows up from conditional logic
				when 1 = 0 then null::character varying
				else 
		' else '
	' end || '
					''''
	' || case 
		when use_cost_equations then '
			end 
		' else '
	' end || ')';

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

/*
select (public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv'::character varying, --inv_table_alias character varying(64), 
	'm'::character varying, --control_measure_table_alias character varying(64), 
	'et'::character varying, --equation_type_table_alias character varying(64), 
	'eq'::character varying, --control_measure_equation_table_alias
	'ef'::character varying, --control_measure_efficiencyrecord_table_alias
	'csm'::character varying, --control_strategy_measure_table_alias
	'gdplev'::character varying, --gdplev_incr_table_alias
	'inv_ovr'::character varying, --inv_override_table_alias
	'gdplev_incr'::character varying --gdplev_incr_table_alias
	)).annual_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).capital_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).annualized_capital_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).fixed_operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).variable_operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr', --inv_override_table_alias
	'gdplev_incr' --gdplev_incr_table_alias
	)).computed_cost_per_ton_expression;
*/