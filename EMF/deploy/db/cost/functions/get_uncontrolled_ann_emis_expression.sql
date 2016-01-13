CREATE OR REPLACE FUNCTION public.get_uncontrolled_ann_emis_expression(
	inv_table_alias character varying(64), 
	no_days_in_month integer,
	inv_override_table_alias character varying(64),
	has_rpen_column boolean
	) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'(' ||
			case 
				when coalesce(no_days_in_month, 0) != 0 then 
					'case when (1 - coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(' || inv_table_alias || '.avd_emis * ' || no_days_in_month || ', ' || inv_table_alias || '.ann_emis) / (1 - coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then ' || inv_table_alias || '.ann_emis / (1 - coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when inv_override_table_alias is null then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end || ')';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_uncontrolled_ann_emis_expression('inv', 31, null::character varying(64), true);
