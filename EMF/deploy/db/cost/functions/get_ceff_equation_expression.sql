CREATE OR REPLACE FUNCTION public.get_ceff_equation_expression(
	int_input_dataset_id integer,
	inventory_year integer,
	inv_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64)) RETURNS text AS $$
DECLARE
	inv_table_name character varying;
	emis_sql character varying;

	dataset_month smallint := 0;
	no_days_in_month smallint := 31;

	is_point_table boolean := false; 

	stkflow_expression text := public.get_stkflow_expression('inv');

	outlet_concentration_expression text ;
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = int_input_dataset_id
	into inv_table_name;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(int_input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
	into no_days_in_month;

	emis_sql := public.get_ann_emis_expression(inv_table_alias, no_days_in_month);

	outlet_concentration_expression := '(' || emis_sql || ')/*in ton*/ * 2000 /*in lb/ton*/ / (ceff_var1."value"::double precision)/*pollutant_molecular_weight*/
/ ' || inv_table_alias || '.annual_avg_hours_per_year /*in hrs/yr*/
/ 60 /*in min/hr*/
* ((0.7302 * 520) / (1)) /*V/n = RT/p*/
/ (
	(' || stkflow_expression || ' * 60.0) 
	* 520 
	/ (
		(' || inv_table_alias || '.stktemp) 
		+ 460.0
	)/*vol flow rate (scfm)*/
) * 10^6';

	-- build sql that calls ceff SQL equation 
	return '
	(case 
		' || case when not is_point_table then '' else '
		--Equation Type 1 
		when ' || is_point_table || '::boolean and coalesce(ceff_et."value",'''') = ''Type 1'' 
			and coalesce((ceff_var2."value"::double precision)/*outlet_concentration*/, 0.0) > 0.0
			and coalesce(' || stkflow_expression || ', 0) > 0 
			AND coalesce(' || inv_table_alias || '.stktemp, 0) > 0 
			AND coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0) > 1 
			and coalesce((' || emis_sql || '),0.0) > 1.0
			and not (
				(coalesce(' || inv_table_alias || '.STKVEL,0.0) <= 0.1 or coalesce(' || inv_table_alias || '.STKDIAM,0.0) <= 0.1) and coalesce(' || inv_table_alias || '.STKFLOW,0.0) <= 0.1 
			)
			and (ceff_var2."value"::double precision)/*outlet_concentration*/ < ' || outlet_concentration_expression || ' then '
			/*
CONSTRAINTS:
Don't do the emission reduction or cost calculation if:

a)	Annual Emissions of NOx (ann_value) is null or <=1
b)	Stack Temperature (stktmp) is null or =0
c)	Hours per Year (annual_avg_hours_per_year) is null or <=1
d)	[[Stack Diameter (stkdiam) is null or <=0.1] OR [Stack Velocity (stkvel) is null or <=0.1]] AND [Stack Flow Rate (stkflow) is null or <=0.1]
e)	If calculated outlet concentration is less than the acheivable outlet concentration
	achieved by the applicable measures, then obviously don't apply the measure, return a NULL ceff, the SQL will be able to ignore null ceffs

								Step 1a) Convert NEI Stack Velocity to volumetric flow rate under actual stack conditions (actual cubic ft per minute):

								Step 1b) Convert Actual Vol. flow rate to Standard Vol flow rate (scfm):

								Step 1c) Calculate NOx outlet concentration:

								-- Step 1b
								std_volumetric_flow_rate := stack_flow_rate * 520 / (stack_temperature + 460.0);

								-- Step 1c
								ceff := ((outlet_concentration * std_volumetric_flow_rate / 10 ^ 6 / ((0.7302 * 520) / (1)) * 60 * annual_avg_hours_per_year * pollutant_molecular_weight / 2000) / ann_emis) * 100;
			*/|| '
			(' || outlet_concentration_expression || ' - (ceff_var2."value"::double precision)/*outlet_concentration*/)
			/ (' || outlet_concentration_expression || ')
			* 100
		' end || '
		-- Filler when clause, just in case no when part shows up from conditional logic
		when 1 = 0 then null::double precision
		--Default to efficiencyrecord ceff
		else
			' || control_measure_efficiencyrecord_table_alias || '.efficiency
	end)';

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

/*
select public.get_ceff_equation_expression(
	7778, -- int_input_dataset_id
	2020, -- int_control_strategy_id
	'inv', --inv_table_alias character varying(64), 
	'ef');


							((' || emis_sql || ') -
								(
									(ceff_var2."value"::double precision)/*outlet_concentration*/ 
									* (
										(' || stkflow_expression || ' * 60.0) 
										* 520 
										/ (
											(' || inv_table_alias || '.stktemp) 
											+ 460.0
										)/*vol flow rate (scfm)*/
									) 
									/ 10 ^ 6 
									/ (
										(0.7302 * 520) 
										/ 
										(1)
									) 
									* 60 
									* ' || inv_table_alias || '.annual_avg_hours_per_year 
									* (ceff_var1."value"::double precision)/*pollutant_molecular_weight*/ 
									/ 2000
								) 
								/ (' || emis_sql || ')
							) 

*/


--2/19/2013 adding support for ff10 ds type formats...
CREATE OR REPLACE FUNCTION public.get_ceff_equation_expression(
	ann_emis_expression character varying, 
	inv_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64),
	is_point_inventory boolean) RETURNS text AS $$
DECLARE
	stkflow_expression text := public.get_stkflow_expression('inv');
	outlet_concentration_expression text ;
BEGIN

	outlet_concentration_expression := '(' || ann_emis_expression || ')/*in ton*/ * 2000 /*in lb/ton*/ / (ceff_var1."value"::double precision)/*pollutant_molecular_weight*/
/ ' || inv_table_alias || '.annual_avg_hours_per_year /*in hrs/yr*/
/ 60 /*in min/hr*/
* ((0.7302 * 520) / (1)) /*V/n = RT/p*/
/ (
	(' || stkflow_expression || ' * 60.0) 
	* 520 
	/ (
		(' || inv_table_alias || '.stktemp) 
		+ 460.0
	)/*vol flow rate (scfm)*/
) * 10^6';

	-- build sql that calls ceff SQL equation 
	return '
	(case 
		' || case when not is_point_inventory then '' else '
		--Equation Type 1 
		when ' || is_point_inventory || '::boolean and coalesce(ceff_et."value",'''') = ''Type 1'' 
			and coalesce((ceff_var2."value"::double precision)/*outlet_concentration*/, 0.0) > 0.0
			and coalesce(' || stkflow_expression || ', 0) > 0 
			AND coalesce(' || inv_table_alias || '.stktemp, 0) > 0 
			AND coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0) > 1 
			and coalesce((' || ann_emis_expression || '),0.0) > 1.0
			and not (
				(coalesce(' || inv_table_alias || '.STKVEL,0.0) <= 0.1 or coalesce(' || inv_table_alias || '.STKDIAM,0.0) <= 0.1) and coalesce(' || inv_table_alias || '.STKFLOW,0.0) <= 0.1 
			)
			and (ceff_var2."value"::double precision)/*outlet_concentration*/ < ' || outlet_concentration_expression || ' then '
			/*
CONSTRAINTS:
Don't do the emission reduction or cost calculation if:

a)	Annual Emissions of NOx (ann_value) is null or <=1
b)	Stack Temperature (stktmp) is null or =0
c)	Hours per Year (annual_avg_hours_per_year) is null or <=1
d)	[[Stack Diameter (stkdiam) is null or <=0.1] OR [Stack Velocity (stkvel) is null or <=0.1]] AND [Stack Flow Rate (stkflow) is null or <=0.1]
e)	If calculated outlet concentration is less than the acheivable outlet concentration
	achieved by the applicable measures, then obviously don't apply the measure, return a NULL ceff, the SQL will be able to ignore null ceffs

								Step 1a) Convert NEI Stack Velocity to volumetric flow rate under actual stack conditions (actual cubic ft per minute):

								Step 1b) Convert Actual Vol. flow rate to Standard Vol flow rate (scfm):

								Step 1c) Calculate NOx outlet concentration:

								-- Step 1b
								std_volumetric_flow_rate := stack_flow_rate * 520 / (stack_temperature + 460.0);

								-- Step 1c
								ceff := ((outlet_concentration * std_volumetric_flow_rate / 10 ^ 6 / ((0.7302 * 520) / (1)) * 60 * annual_avg_hours_per_year * pollutant_molecular_weight / 2000) / ann_emis) * 100;
			*/|| '
			(' || outlet_concentration_expression || ' - (ceff_var2."value"::double precision)/*outlet_concentration*/)
			/ (' || outlet_concentration_expression || ')
			* 100
		' end || '
		-- Filler when clause, just in case no when part shows up from conditional logic
		when 1 = 0 then null::double precision
		--Default to efficiencyrecord ceff
		else
			' || control_measure_efficiencyrecord_table_alias || '.efficiency
	end)';

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

