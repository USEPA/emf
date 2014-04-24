CREATE OR REPLACE FUNCTION public.get_remaining_emis_expression(
	int_input_dataset_id integer,
	inventory_year integer,
	inv_table_alias character varying(64), 
	no_days_in_month integer,
	inv_override_table_alias character varying(64),
	control_strategy_measure_count integer,
	control_strategy_measure_table_alias character varying(64),
	control_measure_efficiencyrecord_table_alias character varying(64),
	has_rpen_column boolean
	) RETURNS text AS $$
DECLARE
BEGIN
	return 
		'( case when coalesce(' || control_measure_efficiencyrecord_table_alias || '.existing_measure_abbr, '''') <> '''' or ' || control_measure_efficiencyrecord_table_alias || '.existing_dev_code <> 0 then -- add on control
			' || public.get_ann_emis_expression(inv_table_alias, no_days_in_month) || ' * ' || 
			public.get_control_percent_reduction_expression(int_input_dataset_id,
				inventory_year,
				inv_table_alias, 
				no_days_in_month, 
				inv_override_table_alias, 
				control_strategy_measure_count, 
				control_strategy_measure_table_alias, 
				control_measure_efficiencyrecord_table_alias) || 
			' / 100.0 
		else -- replacement control
			(' || public.get_uncontrolled_ann_emis_expression(inv_table_alias, no_days_in_month, inv_override_table_alias, has_rpen_column) || ' * (1.0 - ' || 
			public.get_control_percent_reduction_expression(int_input_dataset_id,
				inventory_year,
				inv_table_alias, 
				no_days_in_month, 
				inv_override_table_alias, 
				control_strategy_measure_count, 
				control_strategy_measure_table_alias, 
				control_measure_efficiencyrecord_table_alias) || ' / 100.0))
		end )';

END;
$$ LANGUAGE plpgsql IMMUTABLE;

--select public.get_remaining_emis_expression(7778,2020,'inv', 31, null::character varying(64), 50, 'csm'::character varying(64), 'ef'::character varying(64), true);


CREATE OR REPLACE FUNCTION public.get_remaining_emis_expression(
	ann_emis_expression character varying, 
	pct_red_expression character varying,
	inv_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64),
	is_point_inventory boolean,
	control_strategy_measure_count integer,
	control_strategy_measure_table_alias character varying(64)
	) RETURNS text AS $$
DECLARE
BEGIN

	return 
		'( case when coalesce(' || control_measure_efficiencyrecord_table_alias || '.existing_measure_abbr, '''') <> '''' or ' || control_measure_efficiencyrecord_table_alias || '.existing_dev_code <> 0 then -- add on control
			(' || ann_emis_expression || ') * (' || 
			public.get_control_percent_reduction_expression(ann_emis_expression,
				inv_table_alias, 
				control_measure_efficiencyrecord_table_alias,
				is_point_inventory, 
				control_strategy_measure_count, 
				control_strategy_measure_table_alias) || 
			') / 100.0 
		else -- replacement control
			((' || public.get_uncontrolled_emis_expression(pct_red_expression, ann_emis_expression) || ') * (1.0 - (' || 
			public.get_control_percent_reduction_expression(ann_emis_expression,
				inv_table_alias, 
				control_measure_efficiencyrecord_table_alias,
				is_point_inventory, 
				control_strategy_measure_count, 
				control_strategy_measure_table_alias) || ') / 100.0))
		end )';

END;
$$ LANGUAGE plpgsql IMMUTABLE;

