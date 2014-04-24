CREATE OR REPLACE FUNCTION public.get_ann_emis_expression(
	inv_table_alias character varying(64), no_days_in_month integer) RETURNS text AS $$
DECLARE
BEGIN
	return 
		case 
			when coalesce(no_days_in_month, 0) != 0 then 
				'coalesce(' || inv_table_alias || '.avd_emis * ' || no_days_in_month || ', ' || inv_table_alias || '.ann_emis)' 
			else 
				'' || inv_table_alias || '.ann_emis' 
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_stkflow_expression('inv');
