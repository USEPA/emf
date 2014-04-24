-- Utility Functions
--select public.convert_design_capacity_to_mw(150, 'MMBtu', 'hr');

CREATE OR REPLACE FUNCTION public.convert_design_capacity_to_mw(design_capacity double precision, design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying) returns double precision AS $$
DECLARE
	converted_design_capacity double precision;
	unit_numerator character varying;
	unit_denominator character varying;
BEGIN

        --default if not known
        unit_numerator := coalesce(trim(upper(design_capacity_unit_numerator)), '');
	unit_denominator := coalesce(trim(upper(design_capacity_unit_denominator)), '');

        --if you don't know the units then you can't convert the design capacity
	IF length(unit_numerator) = 0 THEN
		return converted_design_capacity;
	END IF;


/* FROM Larry Sorrels at the EPA
        1) E6BTU does mean mmBTU.

        2)  1 MW = 3.412 million BTU/hr (or mmBTU/hr).   And conversely, 1
        mmBTU/hr = 1/3.412 (or 0.2931) MW.

        3)  All of the units listed below are convertible, but some of the
        conversions will be more difficult than others.  The ft3, lb, and ton
        will require some additional conversions to translate mass or volume
        into an energy term such as MW or mmBTU/hr.  Applying some density
        measure (which is mass/volume) will likely be necessary.   Let me know
        if you need help with the conversions. 
*/

        --capacity is already in the right units...
        --no conversion is necessary, these are the expected units.
	IF (unit_numerator = 'MW' and unit_denominator = '') THEN
		return design_capacity;
	END IF;

        IF (unit_numerator = 'MMBTU'
            or unit_numerator = 'E6BTU'
            or unit_numerator = 'BTU'
            or unit_numerator = 'HP'
            or unit_numerator = 'BLRHP') THEN

		--convert numerator unit
		IF (unit_numerator = 'MMBTU'
		    or unit_numerator = 'E6BTU') THEN
			converted_design_capacity := design_capacity / 3.412;
		END IF;
		IF (unit_numerator = 'BTU') THEN
			converted_design_capacity := design_capacity / 3.412 / 1000000.0;
		END IF;
		IF (unit_numerator = 'HP') THEN
			converted_design_capacity := design_capacity * 0.000746;
		END IF;
		IF (unit_numerator = 'BLRHP') THEN
			converted_design_capacity := design_capacity * 0.000981;
		END IF;
--            IF (unit_numerator = 'FT3') THEN
--                converted_design_capacity := design_capacity * 0.000981;

		--convert denominator unit, if missing ASSUME per hr
		IF (unit_denominator = '' or unit_denominator = 'HR'
		    or unit_denominator = 'H') THEN
			return converted_design_capacity;
		END IF;
		IF (unit_denominator = 'D' or unit_denominator = 'DAY') THEN
			return converted_design_capacity * 24.0;
		END IF;
		IF (unit_denominator = 'M' or unit_denominator = 'MIN') THEN
			return converted_design_capacity / 60.0;
		END IF;
		IF (unit_denominator = 'S' or unit_denominator = 'SEC') THEN
			return converted_design_capacity / 3600.0;
		END IF;
	END IF;
	return null;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION public.calculate_capital_recovery_factor(discount_rate double precision, 
	equipment_life double precision) returns double precision AS $$
DECLARE
BEGIN
	IF coalesce(discount_rate, 0) = 0 or coalesce(equipment_life, 0) = 0 THEN
		return null;
	END IF;

	return (discount_rate * (1 + discount_rate) ^ equipment_life) / ((discount_rate + 1) ^ equipment_life - 1);
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;



-- Cost Equation Functions

CREATE OR REPLACE FUNCTION public.get_default_costs(
	discount_rate double precision, 
	equipment_life double precision,
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision, 
	emis_reduction double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate annual cost
	annual_cost := emis_reduction * ref_yr_cost_per_ton;
	-- calculate capital cost
	capital_cost := annual_cost  * capital_annualized_ratio;
	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;
	-- calculate operation maintenance cost
	operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;



-- Type 1
CREATE OR REPLACE FUNCTION public.get_type1_equation_costs(
	control_measure_id integer, 
	measure_abbreviation character varying(10),
	discount_rate double precision, 
	equipment_life double precision,
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	design_capacity double precision,
 	capital_cost_multiplier double precision,
	fixed_om_cost_multiplier double precision,
	variable_om_cost_multiplier double precision,
	scaling_factor_model_size double precision,
	scaling_factor_exponent double precision,
	capacity_factor double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
	scaling_factor double precision;
	fixed_operation_maintenance_cost double precision;
	variable_operation_maintenance_cost double precision;
BEGIN
	-- NOTES:
	-- design capacity must in the units MW

	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate scaling factor
	scaling_factor := 
		case 
			when (measure_abbreviation = 'NSCR_UBCW' or measure_abbreviation = 'NSCR_UBCT') and design_capacity >= 600.0 then 1.0
			when design_capacity >= 500.0 then 1.0
			else scaling_factor_model_size ^ scaling_factor_exponent
		end;

	-- calculate capital cost
	capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000;

	-- calculate operation maintenance cost
	-- calculate fixed operation maintenance cost
	fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
	-- calculate variable operation maintenance cost
	variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
	-- calculate total operation maintenance cost
	operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 2
CREATE OR REPLACE FUNCTION public.get_type2_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	design_capacity double precision, 
	capital_cost_multiplier double precision,
	capital_cost_exponent double precision, 
	annual_cost_multiplier double precision,
	annual_cost_exponent double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- NOTES:
	-- design capacity must in the units mmBtu/hr

	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

	-- calculate operation maintenance cost
	operation_maintenance_cost := annual_cost - annualized_capital_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 3
CREATE OR REPLACE FUNCTION public.get_type3_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
	capital_cost_factor double precision := 192;
	gas_flow_rate_factor double precision := 0.486;
	retrofit_factor double precision := 1.1;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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
	--  NOTE: Document does NOT include stack_flow_rate
	fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

	-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
	-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
	--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
	--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
	-- 	Darin says that the seconds to minutes conversion is not necessary
	variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

	-- calculate operation maintenance cost
	-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
	operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 4
CREATE OR REPLACE FUNCTION public.get_type4_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 5
CREATE OR REPLACE FUNCTION public.get_type5_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 6
CREATE OR REPLACE FUNCTION public.get_type6_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 7
CREATE OR REPLACE FUNCTION public.get_type7_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 8
CREATE OR REPLACE FUNCTION public.get_type8_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, 
	capital_control_cost_factor double precision,
	om_control_cost_factor double precision,
	default_capital_cpt_factor double precision,
	default_om_cpt_factor double precision,
	default_annualized_cpt_factor double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- * Comments *




	-- * Comments *

	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 9 - EGU PM Control Equations
CREATE OR REPLACE FUNCTION public.get_type9_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	stack_flow_rate double precision, -- in cfm
	total_equipment_cost_factor double precision,
	total_equipment_cost_constant double precision,
	equipment_to_capital_cost_multiplier double precision,
	electricity_factor double precision,
	electricity_constant double precision,
	dust_disposal_factor double precision,
	dust_disposal_constant double precision,
	bag_replacement_factor double precision,
	bag_replacement_constant double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- * Comments *




	-- * Comments *

	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(cap_recovery_factor, 0) = 0 and coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

	-- calculate operation maintenance cost
	operation_maintenance_cost := 
		((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost :=  annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- Type 10
CREATE OR REPLACE FUNCTION public.get_type10_equation_costs(
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	design_capacity double precision, 
	annual_avg_hours_per_year double precision, 
	capital_cost_multiplier double precision,
	capital_cost_exponent double precision, 
	variable_operation_maintenance_cost_multiplier double precision,
	fixed_operation_maintenance_cost_multiplier double precision,
	fixed_operation_maintenance_cost_exponent double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(cap_recovery_factor, 0) = 0 and coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 11 - SO2 Non EGU Control Equations
CREATE OR REPLACE FUNCTION public.get_type11_equation_costs(
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	capital_annualized_ratio double precision, 
	emis_reduction double precision, 
	design_capacity double precision, 	-- needs to be in units of mmBTU/hr
	low_default_cost_per_ton double precision,
	low_boiler_capacity_range double precision, 
	medium_default_cost_per_ton double precision,
	medium_boiler_capacity_range double precision,
	high_default_cost_per_ton double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(cap_recovery_factor, 0) = 0 and coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

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

END;
$$ LANGUAGE plpgsql IMMUTABLE;




-- Type 12
CREATE OR REPLACE FUNCTION public.get_type12_equation_costs(
	emis_reduction double precision, 	-- ton/yr
	stack_flow_rate double precision,   	-- cfm
	stack_temperature double precision, 	-- F
	capital_recovery_factor double precision, 
	total_capital_investment_fixed_factor double precision,
	total_capital_investment_variable_factor double precision,
	annual_operating_cost_fixed_factor double precision,
	annual_operating_cost_variable_factor double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- * Comments *




	-- * Comments *


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

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;



-- Cost Equation Factory Method
CREATE OR REPLACE FUNCTION public.get_strategy_costs(
	use_cost_equations boolean, 
	control_measure_id integer, 
	measure_abbreviation character varying(10), 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision,  
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision,
	equation_type character varying(255), 
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	variable_coefficient3 double precision, 
	variable_coefficient4 double precision, 
	variable_coefficient5 double precision, 
	variable_coefficient6 double precision, 
	variable_coefficient7 double precision, 
	variable_coefficient8 double precision, 
	variable_coefficient9 double precision, 
	variable_coefficient10 double precision, 
	stack_flow_rate double precision, 
	design_capacity double precision, 
	design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying, 
	ceff double precision, 
	ref_yr_incremental_cost_per_ton double precision,  
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision, 
	OUT actual_equation_type character varying(255)
--	,OUT valid_cost boolean
	)  AS $$
DECLARE
	converted_design_capacity double precision;
	valid_cost boolean;
BEGIN
	-- at first, we can only assume that everything is right...
	valid_cost := true;

	-- Each Cost Equation Function will return costs in the cost year that is specified in the emf.control_measure_equations table, 
	-- after the costs are calculated we can adjust the costs to the reference cost year.

	-- try cost equations first, then maybe use default approach, if needed
	IF use_cost_equations THEN
		IF equation_type is not null THEN
			-- Type 1
			IF equation_type = 'Type 1' THEN

				converted_design_capacity := public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator);

				IF coalesce(design_capacity, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type1_equation_costs(control_measure_id, 
						measure_abbreviation,
						discount_rate, 
						equipment_life,
						capital_recovery_factor, 
						emis_reduction, 
						converted_design_capacity,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5, 
						variable_coefficient6) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 1';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 1';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 1';
			END IF;

			-- Type 2
			IF equation_type = 'Type 2' THEN

				converted_design_capacity := public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator);
				-- convert design capacity to mmBtu/hr
				converted_design_capacity := 3.412 * converted_design_capacity;
				IF coalesce(converted_design_capacity, 0) <> 0 THEN
-- NOTES
-- design capacity must be less than or equal to 2000 MMBTU/hr (or 586.1665 MW))
-- use incremental variables if appropriate already controlled
					IF (converted_design_capacity <= 2000.0) THEN
						select costs.annual_cost,
							costs.capital_cost,
							costs.operation_maintenance_cost,
							costs.annualized_capital_cost,
							costs.computed_cost_per_ton
						from public.get_type2_equation_costs(control_measure_id, 
							discount_rate, 
							equipment_life, 
							capital_recovery_factor, 
							emis_reduction, 
							converted_design_capacity, 
							case when coalesce(ceff, 0.0) = 0.0 then variable_coefficient1 else variable_coefficient5 end,
							case when coalesce(ceff, 0.0) = 0.0 then variable_coefficient2 else variable_coefficient6 end,
							case when coalesce(ceff, 0.0) = 0.0 then variable_coefficient3 else variable_coefficient7 end,
							case when coalesce(ceff, 0.0) = 0.0 then variable_coefficient4 else variable_coefficient8 end) as costs
						into annual_cost,
							capital_cost,
							operation_maintenance_cost,
							annualized_capital_cost,
							computed_cost_per_ton;
						IF annual_cost is not null THEN
							valid_cost := true;
							actual_equation_type := 'Type 2';
						ELSE
							valid_cost := false;
							actual_equation_type := '-Type 2';
						END IF;
						-- adjust costs to the reference cost year
						annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
						capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
						operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
						annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
						computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
						return;
					END IF;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 2';
			END IF;

			-- Type 7
			IF equation_type = 'Type 7' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type7_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 7';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 7';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 7';
			END IF;

			-- Type 8
			IF equation_type = 'Type 8' THEN
--let missing stkflow to go thorugh
--				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type8_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5) as costs

					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 8';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 8';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				--END IF;
				valid_cost := false;
				actual_equation_type := '-Type 8';
			END IF;

			-- Type 9
			IF equation_type = 'Type 9' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type9_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5, 
						variable_coefficient6, 
						variable_coefficient7, 
						variable_coefficient8, 
						variable_coefficient9) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 9';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 9';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 9';
			END IF;

			-- Type 11
			IF equation_type = 'Type 11' THEN

				-- convert design capacity to mmBTU/hr
				converted_design_capacity := 3.412 * public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator);

				IF coalesce(converted_design_capacity, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type11_equation_costs(discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						capital_annualized_ratio, 
						emis_reduction, 
						converted_design_capacity, 
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 11';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 11';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 11';
			END IF;

		END IF;
	END IF;

	-- Use Default Approach for calculating the costs
	-- don't adjust costs, they are already in reference cost year dollars
	select costs.annual_cost,
		costs.capital_cost,
		costs.operation_maintenance_cost,
		costs.annualized_capital_cost,
		costs.computed_cost_per_ton
	from public.get_default_costs(discount_rate, 
		equipment_life, 
		capital_annualized_ratio, 
		capital_recovery_factor, 
		case when coalesce(ceff, 0.0) <> 0.0 and coalesce(ref_yr_incremental_cost_per_ton, 0.0) <> 0.0 then ref_yr_incremental_cost_per_ton else ref_yr_cost_per_ton end, 
		emis_reduction) as costs
	into annual_cost,
		capital_cost,
		operation_maintenance_cost,
		annualized_capital_cost,
		computed_cost_per_ton;
	-- if no annual cost is specified then assume the cost is unspecified
	IF annual_cost is not null THEN
		valid_cost := true;
	ELSE
		valid_cost := false;
	END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

/*
-- 2nd Version -- Cost Equation Factory Method
-- Added new inputs -- 
	annual_avg_hours_per_year double precision, 
*/
CREATE OR REPLACE FUNCTION public.get_strategy_costs(
	use_cost_equations boolean, 
	control_measure_id integer, 
	measure_abbreviation character varying(10), 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision,  
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision,
	equation_type character varying(255), 
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	variable_coefficient3 double precision, 
	variable_coefficient4 double precision, 
	variable_coefficient5 double precision, 
	variable_coefficient6 double precision, 
	variable_coefficient7 double precision, 
	variable_coefficient8 double precision, 
	variable_coefficient9 double precision, 
	variable_coefficient10 double precision, 
	stack_flow_rate double precision, 
	design_capacity double precision, 
	design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying, 
	annual_avg_hours_per_year double precision, 
	ceff double precision, 
	ref_yr_incremental_cost_per_ton double precision,  
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision, 
	OUT actual_equation_type character varying(255)
--	,OUT valid_cost boolean
	)  AS $$
DECLARE
	converted_design_capacity double precision;
	valid_cost boolean;
BEGIN
	-- at first, we can only assume that everything is right...
	valid_cost := true;

	-- Each Cost Equation Function will return costs in the cost year that is specified in the emf.control_measure_equations table, 
	-- after the costs are calculated we can adjust the costs to the reference cost year.

	-- try cost equations first, then maybe use default approach, if needed
	IF use_cost_equations THEN
		IF equation_type is not null THEN
		
			-- Type 3
			IF equation_type = 'Type 3' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type3_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 3';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 3';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 3';
			END IF;

			-- Type 4
			IF equation_type = 'Type 4' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type4_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 4';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 4';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 4';
			END IF;

			-- Type 5
			IF equation_type = 'Type 5' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type5_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 5';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 5';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 5';
			END IF;

			-- Type 6
			IF equation_type = 'Type 6' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type6_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 6';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 6';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 6';
			END IF;		

			-- Type 10
			IF equation_type = 'Type 10' THEN
				--default units numerator to MW
--				design_capacity_unit_numerator := case when len(coalesce(design_capacity_unit_numerator, '')) = 0 then 'MW' else design_capacity_unit_numerator end;
				converted_design_capacity := 
					public.convert_design_capacity_to_mw(design_capacity, 
					case 
						when length(coalesce(design_capacity_unit_numerator, '')) = 0 then 
							'MW'::character varying
						else
							design_capacity_unit_numerator
					end
					, design_capacity_unit_denominator);
--					case 
--						when len(coalesce(design_capacity_unit_numerator, '')) = 0 then 
--							public.convert_design_capacity_to_mw(design_capacity, 'MW'::character varying, design_capacity_unit_denominator)
--						else
--							public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator)
--					end;


				IF coalesce(design_capacity, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type10_equation_costs(
						discount_rate, 
						equipment_life,
						capital_recovery_factor, 
						emis_reduction, 
						converted_design_capacity,
						annual_avg_hours_per_year,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5) as costs
					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 10';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 10';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 10';

			ELSE
				--call first version of this function

				select costs.annual_cost,
					costs.capital_cost,
					costs.operation_maintenance_cost,
					costs.annualized_capital_cost,
					costs.computed_cost_per_ton,
					costs.actual_equation_type
				from public.get_strategy_costs(
					use_cost_equations, 
					control_measure_id, 
					measure_abbreviation, 
					discount_rate, 
					equipment_life, 
					capital_annualized_ratio, 
					capital_recovery_factor, 
					ref_yr_cost_per_ton,  
					emis_reduction, 
					ref_yr_chained_gdp_adjustment_factor,
					equation_type, 
					variable_coefficient1, 
					variable_coefficient2, 
					variable_coefficient3, 
					variable_coefficient4, 
					variable_coefficient5, 
					variable_coefficient6, 
					variable_coefficient7, 
					variable_coefficient8, 
					variable_coefficient9, 
					variable_coefficient10, 
					stack_flow_rate, 
					design_capacity, 
					design_capacity_unit_numerator, 
					design_capacity_unit_denominator,
					ceff,
					ref_yr_incremental_cost_per_ton) as costs
				into annual_cost,
					capital_cost,
					operation_maintenance_cost,
					annualized_capital_cost,
					computed_cost_per_ton,
					actual_equation_type;
				return;
			END IF;
		END IF;
	END IF;

	-- Use Default Approach for calculating the costs
	-- don't adjust costs, they are already in reference cost year dollars
	select costs.annual_cost,
		costs.capital_cost,
		costs.operation_maintenance_cost,
		costs.annualized_capital_cost,
		costs.computed_cost_per_ton
	from public.get_default_costs(discount_rate, 
		equipment_life, 
		capital_annualized_ratio, 
		capital_recovery_factor, 
		case when coalesce(ceff, 0.0) <> 0.0 and coalesce(ref_yr_incremental_cost_per_ton, 0.0) <> 0.0 then ref_yr_incremental_cost_per_ton else ref_yr_cost_per_ton end, 
		emis_reduction) as costs
	into annual_cost,
		capital_cost,
		operation_maintenance_cost,
		annualized_capital_cost,
		computed_cost_per_ton;
	-- if no annual cost is specified then assume the cost is unspecified
	IF annual_cost is not null THEN
		valid_cost := true;
	ELSE
		valid_cost := false;
	END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


/*
-- 3rd Version -- Cost Equation Factory Method
-- Added new inputs -- 
	stack_temperature double precision, 
*/
CREATE OR REPLACE FUNCTION public.get_strategy_costs(
	use_cost_equations boolean, 
	control_measure_id integer, 
	measure_abbreviation character varying(10), 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision,  
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision,
	equation_type character varying(255), 
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	variable_coefficient3 double precision, 
	variable_coefficient4 double precision, 
	variable_coefficient5 double precision, 
	variable_coefficient6 double precision, 
	variable_coefficient7 double precision, 
	variable_coefficient8 double precision, 
	variable_coefficient9 double precision, 
	variable_coefficient10 double precision, 
	stack_flow_rate double precision, 
	stack_temperature double precision, 
	design_capacity double precision, 
	design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying, 
	annual_avg_hours_per_year double precision, 
	ceff double precision, 
	ref_yr_incremental_cost_per_ton double precision,  
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT variable_operation_maintenance_cost double precision, 
	OUT fixed_operation_maintenance_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision, 
	OUT actual_equation_type character varying(255)
--	,OUT valid_cost boolean
	)  AS $$
DECLARE
	converted_design_capacity double precision;
	valid_cost boolean;
BEGIN
	-- at first, we can only assume that everything is right...
	valid_cost := true;

	-- Each Cost Equation Function will return costs in the cost year that is specified in the emf.control_measure_equations table, 
	-- after the costs are calculated we can adjust the costs to the reference cost year.

	-- try cost equations first, then maybe use default approach, if needed
	IF use_cost_equations THEN
		IF equation_type is not null THEN
		
			-- Type 12
			IF equation_type = 'Type 12' THEN

				IF coalesce(stack_flow_rate, 0) <> 0 and coalesce(stack_temperature, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.variable_operation_maintenance_cost,
						costs.fixed_operation_maintenance_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type12_equation_costs(
						emis_reduction, 
						stack_flow_rate, 
						stack_temperature, 
						capital_recovery_factor, 
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4) as costs

					into annual_cost,
						capital_cost,
						variable_operation_maintenance_cost,
						fixed_operation_maintenance_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						valid_cost := true;
						actual_equation_type := 'Type 12';
					ELSE
						valid_cost := false;
						actual_equation_type := '-Type 12';
					END IF;
					-- adjust costs to the reference cost year
					annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost;
					capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost;
					variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_operation_maintenance_cost;
					fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_operation_maintenance_cost;
					operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * operation_maintenance_cost;
					annualized_capital_cost := ref_yr_chained_gdp_adjustment_factor * annualized_capital_cost;
					computed_cost_per_ton := ref_yr_chained_gdp_adjustment_factor * computed_cost_per_ton;
					return;
				END IF;
				valid_cost := false;
				actual_equation_type := '-Type 12';

			ELSE
				--call second version of this function

				select costs.annual_cost,
					costs.capital_cost,
					costs.variable_operation_maintenance_cost,
					costs.fixed_operation_maintenance_cost,
					costs.operation_maintenance_cost,
					costs.annualized_capital_cost,
					costs.computed_cost_per_ton
				from public.get_strategy_costs(
					use_cost_equations, 
					control_measure_id, 
					measure_abbreviation, 
					discount_rate, 
					equipment_life, 
					capital_annualized_ratio, 
					capital_recovery_factor, 
					ref_yr_cost_per_ton,  
					emis_reduction, 
					ref_yr_chained_gdp_adjustment_factor,
					equation_type, 
					variable_coefficient1, 
					variable_coefficient2, 
					variable_coefficient3, 
					variable_coefficient4, 
					variable_coefficient5, 
					variable_coefficient6, 
					variable_coefficient7, 
					variable_coefficient8, 
					variable_coefficient9, 
					variable_coefficient10, 
					stack_flow_rate, 
					design_capacity, 
					design_capacity_unit_numerator, 
					design_capacity_unit_denominator,
					annual_avg_hours_per_year,
					ceff,
					ref_yr_incremental_cost_per_ton) as costs
				into annual_cost,
					capital_cost,
					variable_operation_maintenance_cost,
					fixed_operation_maintenance_cost,
					operation_maintenance_cost,
					annualized_capital_cost,
					computed_cost_per_ton;
				return;
			END IF;
		END IF;
	END IF;

	-- Use Default Approach for calculating the costs
	-- don't adjust costs, they are already in reference cost year dollars
	select costs.annual_cost,
		costs.capital_cost,
		costs.operation_maintenance_cost,
		costs.annualized_capital_cost,
		costs.computed_cost_per_ton
	from public.get_default_costs(discount_rate, 
		equipment_life, 
		capital_annualized_ratio, 
		capital_recovery_factor, 
		case when coalesce(ceff, 0.0) <> 0.0 and coalesce(ref_yr_incremental_cost_per_ton, 0.0) <> 0.0 then ref_yr_incremental_cost_per_ton else ref_yr_cost_per_ton end, 
		emis_reduction) as costs
	into annual_cost,
		capital_cost,
		operation_maintenance_cost,
		annualized_capital_cost,
		computed_cost_per_ton;
	-- if no annual cost is specified then assume the cost is unspecified
	IF annual_cost is not null THEN
		valid_cost := true;
	ELSE
		valid_cost := false;
	END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

/*
select public.get_type2_equation_costs(14036, 0.07, 
	20, null, 
	8.76, 1.6, 
	36.635404454865181711606096131301, 33206.3,
	0.65, 2498.1,
	0.73);
select public.convert_design_capacity_to_mw(125, 'E6BTU', 
	null);

select public.get_strategy_costs(true::boolean, 14036, 
	'NSCRIBDO', 0.07, 
	20, 10, 
	null, 2022.25,  
	8.76, 1.6,
	'Type 2', 
	value1, value2, 
	value3, value4, 
	value5, value6, 
	value7, value8, 
	value9, value10, 
	null, 125,'E6BTU', null),value1, value2, 
	value3, value4, 
	value5, value6, 
	value7, value8, 
	value9, value10
from emf.control_measure_equations
where control_measure_id in (
select id
from emf.control_measures
where abbreviation = 'NSCRIBDO'
);	

82400.9;0.65;5555.6;0.79;79002.2;0.65;8701.5;0.65
select *
from emf.control_measure_efficiencyrecords
where control_measures_id in (
select id
from emf.control_measures
where abbreviation = 'NSCRIBCW'
)
select *
from emf.control_measure_equations
where control_measure_id = 13683;

select public.get_equation_type(13683);

select name
from emf.control_measure_sccs
where control_measures_id =13709

select *
from emf.control_measure_efficiencyrecords
where control_measures_id = 13736


*/

/*
select costs.* 
from emf.control_measures
cross join  public.get_strategy_costs(true, 1, 
	0.07, 20.0, 
	1.47, null, 
	2000, 45.5, 
	null, null, 
	null, null) costs) costs3;


select *
from emf.control_measures 
where id in 
(
select control_measure_id 
from emf.control_measure_equations 
where equation_type_id = (select id from emf.equation_types where name = 'Type 2') 
)
order by abbreviation

select *
from emf.control_measures 
where id in 
(
select control_measures_id 
from emf.control_measure_Efficiencyrecords 
where incremental_cost_per_ton is not null and incremental_cost_per_ton <> cost_per_ton 
)
order by abbreviation

select public.get_strategy_costs(
	true, --use_cost_equations boolean, 
	0, --control_measure_id integer, 
	'ASDSDA', --measure_abbreviation character varying(10), 
	7, --discount_rate double precision, 
	null, --equipment_life double precision, 
	null, --capital_annualized_ratio double precision, 
	0.243890694, --capital_recovery_factor double precision, 
	null, --ref_yr_cost_per_ton double precision,  
	1, --emis_reduction double precision, 
	1, --ref_yr_chained_gdp_adjustment_factor double precision,
	'Type 12', --equation_type character varying(255), 
	20000, --variable_coefficient1 double precision, 
	null, --variable_coefficient2 double precision, 
	4000, --variable_coefficient3 double precision, 
	null, --variable_coefficient4 double precision, 
	null, --variable_coefficient5 double precision, 
	null, --variable_coefficient6 double precision, 
	null, --variable_coefficient7 double precision, 
	null, --variable_coefficient8 double precision, 
	null, --variable_coefficient9 double precision, 
	null, --variable_coefficient10 double precision, 
	2449, --stack_flow_rate double precision, 
	551, --stack_temperature double precision, 
	null, --design_capacity double precision, 
	null, --design_capacity_unit_numerator character varying, 
	null, --design_capacity_unit_denominator character varying, 
	null, --annual_avg_hours_per_year double precision, 
	null, --ceff double precision, 
	null --ref_yr_incremental_cost_per_ton double precision,  
	);

-- adjust acfm to scfm...
--	select (2449 * 520 / (551 + 460.0))

*/
