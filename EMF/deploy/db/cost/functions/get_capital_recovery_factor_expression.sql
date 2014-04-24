CREATE OR REPLACE FUNCTION public.get_capital_recovery_factor_expression(
	control_measure_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64), 
	discount_rate double precision) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'case ' ||
			'when coalesce(' || discount_rate || ', 0.0) != 0.0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0.0) != 0.0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life) ' ||
			'else ' || control_measure_efficiencyrecord_table_alias || '.cap_rec_factor ' ||
		'end/*cap_recovery_factor*/';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_capital_recovery_factor_expression('cm', 'ef', 0.07);
