CREATE OR REPLACE FUNCTION public.get_convert_design_capacity_expression(
	inv_table_alias character varying(64), 
	unit_numerator_default_value character varying(10), 
	unit_denominator_default_value character varying(10)) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, ' || 
		case 
			when trim(coalesce(unit_numerator_default_value, '')) != '' then 
				'case 
					when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then ' || quote_literal(unit_numerator_default_value) || '::character varying
					else coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')
				end' 
			else 
				'coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')' 
		end
		|| ', ' ||
		case 
			when trim(coalesce(unit_denominator_default_value, '')) != '' then 
				'case 
					when length(coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) = 0 then ' || quote_literal(unit_denominator_default_value) || '::character varying
					else coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')
				end' 
			else 
				'coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')' 
		end
		|| ')';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_convert_design_capacity_expression('inv', 'MW', 'hr');

CREATE OR REPLACE FUNCTION public.get_convert_design_capacity_expression(
	inv_table_alias character varying(64), 
	units_default_value character varying(20)) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, ' || 
		case 
			when trim(coalesce(units_default_value, '')) != '' then 
				'case 
					when length(coalesce(' || inv_table_alias || '.design_capacity_units, '''')) = 0 then ' || quote_literal(units_default_value) || '::character varying
					else coalesce(' || inv_table_alias || '.design_capacity_units, '''')
				end' 
			else 
				'coalesce(' || inv_table_alias || '.design_capacity_units, '''')' 
		end
		|| ')';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

