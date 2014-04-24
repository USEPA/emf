	-- Utility Functions

-- CEFF Equation Functions


-- Type 1
CREATE OR REPLACE FUNCTION public.get_type1_equation_ceff(
	ann_emis double precision, 		-- ton/yr
	stack_flow_rate double precision,	-- cfs (acfm NOT scfm) 
--	stack_diameter double precision, 	-- ft
	stack_temperature double precision, 	-- F
	annual_avg_hours_per_year double precision, -- Hrs/yr
	pollutant_molecular_weight double precision, -- lb/lb-mole
	outlet_concentration double precision, 	-- ppmv
	OUT computed_ceff double precision)  AS $$
DECLARE
	std_volumetric_flow_rate double precision := 0.0; -- acfm
	ceff double precision := 0.0; -- acfm
BEGIN
	-- NOTES:
/*
	Step 1a) Convert NEI Stack Velocity to volumetric flow rate under actual stack conditions (actual cubic ft per minute):

	Step 1b) Convert Actual Vol. flow rate to Standard Vol flow rate (scfm):

	Step 1c) Calculate NOx outlet concentration:
*/

	-- Step 1b
	std_volumetric_flow_rate := stack_flow_rate * 520 / (stack_temperature + 460.0);

	-- Step 1c
	ceff := ((outlet_concentration * std_volumetric_flow_rate / 10 ^ 6 / ((0.7302 * 520) / (1)) * 60 * annual_avg_hours_per_year * pollutant_molecular_weight / 2000) / ann_emis) * 100;
--	ann_emis * 2000 / pollutant_molecular_weight / annual_avg_hours_per_year /60 * (0.7302 * 520) / (1) / std_volumetric_flow_rate * 10 ^ 6;
	
	

	-- calculate computed ceff
	computed_ceff := ceff;
END;
$$ LANGUAGE plpgsql IMMUTABLE;




-- Cost Equation Factory Method
CREATE OR REPLACE FUNCTION public.get_strategy_ceff_equation(
	equation_type character varying(255), 
	ann_emis double precision, 		-- ton/yr
	stack_flow_rate double precision,	-- cfs (acfm NOT scfm) 
--	stack_diameter double precision, 	-- ft
	stack_temperature double precision, 	-- F
	annual_avg_hours_per_year double precision, -- Hrs/yr
	ceff double precision, 			-- %
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	OUT computed_ceff double precision, 
	OUT actual_equation_type character varying(255)
	)  AS $$
DECLARE
	valid_ceff boolean;
BEGIN
	-- at first, we can only assume that everything is right...
	valid_ceff := true;

	-- Each Cost Equation Function will return costs in the cost year that is specified in the emf.control_measure_equations table, 
	-- after the costs are calculated we can adjust the costs to the reference cost year.

	-- try cost equations first, then maybe use default approach, if needed
	IF equation_type is not null THEN
		-- Type 1
		IF equation_type = 'Type 1' THEN

			IF coalesce(stack_flow_rate, 0) <> 0 AND coalesce(stack_temperature, 0) <> 0 AND coalesce(annual_avg_hours_per_year, 0) <> 0 THEN
				select equation.computed_ceff
				from public.get_type1_equation_ceff(ann_emis, 
					stack_flow_rate, 
					stack_temperature,
					annual_avg_hours_per_year,
					variable_coefficient1,
					variable_coefficient2) as equation
				into computed_ceff;
				IF computed_ceff is not null THEN
					valid_ceff := true;
					actual_equation_type := 'Type 1';
				ELSE
					valid_ceff := false;
					actual_equation_type := '-Type 1';
				END IF;
				return;
			END IF;
			valid_ceff := false;
			actual_equation_type := '-Type 1';
		END IF;


	END IF;

	-- Use Default Approach for calculating the ceff
	select ceff
	into computed_ceff;
	-- if no annual cost is specified then assume the cost is unspecified
	IF computed_ceff is not null THEN
		valid_ceff := true;
	ELSE
		valid_ceff := false;
	END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;



select public.get_strategy_ceff_equation(
	'Type 1', 
	1.0, 
	2449.0, 
	551.0, 
	8736.0, 
	null::double precision, 
	46.0,
	10.0)
from emf.control_measures
where abbreviation = 'NSCRIBDO';	

/*
CREATE OR REPLACE FUNCTION public.get_strategy_ceff_equation(
	equation_type character varying(255), 
	ann_emis double precision, 		-- ton/yr
	stack_flow_rate double precision,	-- cfs (acfm NOT scfm) 
--	stack_diameter double precision, 	-- ft
	stack_temperature double precision, 	-- F
	annual_avg_hours_per_year double precision, -- Hrs/yr
	ceff double precision, 			-- %
	variable_coefficient1 double precision, 
	OUT computed_ceff double precision, 
	OUT actual_equation_type character varying(255)
	)  AS $$

*/
