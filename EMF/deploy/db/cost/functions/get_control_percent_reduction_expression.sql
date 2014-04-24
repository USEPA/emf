CREATE OR REPLACE FUNCTION public.get_control_percent_reduction_expression(
	int_input_dataset_id integer,
	inventory_year integer,
	inv_table_alias character varying(64), 
	no_days_in_month integer,
	inv_override_table_alias character varying(64),
	control_strategy_measure_count integer,
	control_strategy_measure_table_alias character varying(64),
	control_measure_efficiencyrecord_table_alias character varying(64)
	) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'(' || 
		public.get_ceff_equation_expression(
			int_input_dataset_id, 
			inventory_year, 
			inv_table_alias, 
			control_measure_efficiencyrecord_table_alias) || '
		* ' || case when control_strategy_measure_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_effectiveness, ' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness' end || ' * ' || case when control_strategy_measure_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_penetration, ' || control_measure_efficiencyrecord_table_alias || '.rule_penetration)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_penetration' end || ' / 100 / 100)';

END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_control_percent_reduction_expression(7778,2020,'inv', 31, null::character varying(64), 50, 'csm'::character varying(64), 'ef'::character varying(64));

CREATE OR REPLACE FUNCTION public.get_control_percent_reduction_expression(
	ann_emis_expression character varying, 
	inv_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64),
	is_point_inventory boolean,
	control_strategy_measure_count integer,
	control_strategy_measure_table_alias character varying(64)
	) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'(' || 
		public.get_ceff_equation_expression(
			ann_emis_expression, 
			inv_table_alias, 
			control_measure_efficiencyrecord_table_alias, 
			is_point_inventory) || '
		* ' || case when control_strategy_measure_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_effectiveness, ' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness' end || ' * ' || case when control_strategy_measure_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_penetration, ' || control_measure_efficiencyrecord_table_alias || '.rule_penetration)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_penetration' end || ' / 100 / 100)';

END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_control_percent_reduction_expression('7778','inv', 'ef'::character varying(64), 'true'::boolean, 10, 'csm'::character varying(64));

