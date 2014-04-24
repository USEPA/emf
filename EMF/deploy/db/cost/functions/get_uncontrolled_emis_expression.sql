
CREATE OR REPLACE FUNCTION public.get_uncontrolled_emis_expression(
	pct_red_expression character varying,
	emis_expression character varying
	) RETURNS text AS $$
DECLARE
BEGIN
--(1 - coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0))
	return 
		'(' 
			|| 'case when (1 - coalesce((' || pct_red_expression || ') / 100.0, 0)) != 0 then (' || emis_expression || ') / (1 - coalesce((' || pct_red_expression || ') / 100.0, 0)) else 0.0::double precision end' 
		|| ')';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

