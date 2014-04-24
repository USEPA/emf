CREATE OR REPLACE FUNCTION public.get_stkflow_expression(
	inv_table_alias character varying(64)) RETURNS text AS $$
DECLARE
BEGIN
	return 
	'(case '
		|| 'when (coalesce(' || inv_table_alias || '.STKVEL,0.0) <= 0.1 or coalesce(' || inv_table_alias || '.STKDIAM,0.0) <= 0.1) and coalesce(' || inv_table_alias || '.STKFLOW,0.0) > 0.1 then ' || inv_table_alias || '.STKFLOW '
		|| 'when coalesce(' || inv_table_alias || '.STKVEL,0.0) <> 0.0 and coalesce(' || inv_table_alias || '.STKDIAM,0.0) <> 0.0 then pi() * (' || inv_table_alias || '.STKDIAM/2)^2 * ' || inv_table_alias || '.STKVEL '
		|| 'else null::double precision '
	|| 'end)';
END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

--select public.get_stkflow_expression('inv');
