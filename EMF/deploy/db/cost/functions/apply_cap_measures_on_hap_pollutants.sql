/*
select * from emf.control_strategies where name = 'test mer nc so2 costing type 3 4 11';
select * from emf.strategy_results where control_strategy_id = 156;

select public.apply_cap_measures_on_hap_pollutants(156, 
	'{ 6307, 6309, 6311, 6313 }');

select public.apply_cap_measures_on_hap_pollutants(157, 
	'{ 6323 }');
*/


CREATE OR REPLACE FUNCTION public.apply_cap_measures_on_hap_pollutants(intControlStrategyId integer, 
	intStrategyResultIds int[]) RETURNS void AS $$
DECLARE
	intInputDatasetId integer;
	intInputDatasetVersion integer;
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';

	inventory_detailed_result_dataset_id integer := null;
	inventory_detailed_result_table_name varchar(64) := '';

	target_pollutant_id integer := 0;
	target_pollutant character varying(255) := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	has_rpen_column boolean := false;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	remaining_emis_sql character varying;
	inventory_sectors character varying := '';
	has_cpri_column boolean := false; 
	has_primary_device_type_code_column boolean := false; 

	datasetIds text:= '';

	detailedResults RECORD;
	inventoryWithDetailedResults RECORD;

	--support for flat file ds types...
	dataset_type_name character varying(255) := '';
	fips_expression character varying(64) := 'fips';
	plantid_expression character varying(64) := 'plantid';
	pointid_expression character varying(64) := 'pointid';
	stackid_expression character varying(64) := 'stackid';
	segment_expression character varying(64) := 'segment';
	is_flat_file_inventory boolean := false;
	is_flat_file_point_inventory boolean := false;
	inv_pct_red_expression character varying(256);
	inv_ceff_expression character varying(64) := 'ceff';
	longitude_expression character varying(64) := 'xloc';
	latitude_expression character varying(64) := 'yloc';
	plant_name_expression character varying(64) := 'plant';
BEGIN

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.name,
		cs.pollutant_id,
		cs.cost_year,
		cs.analysis_year,
		p.name
	FROM emf.control_strategies cs
		inner join emf.pollutants p
		on p.id = cs.pollutant_id
	where cs.id = intControlStrategyId
	INTO strategy_name,
		target_pollutant_id,
		cost_year,
		inventory_year,
		target_pollutant;

	FOR detailedResults IN  
		SELECT i.table_name, sr.detailed_result_dataset_id
		FROM emf.strategy_results sr
			inner join emf.internal_sources i
			on i.dataset_id = sr.detailed_result_dataset_id
		where sr.id =any (intStrategyResultIds)
	LOOP
		detailed_result_dataset_id := detailedResults.detailed_result_dataset_id;
		detailed_result_table_name := detailedResults.table_name;

raise notice '%', detailed_result_dataset_id || ' ' || detailed_result_table_name;

		FOR inventoryWithDetailedResults IN  
			SELECT i.table_name, sr.dataset_id, sr.dataset_version, i2.table_name as detailed_result_dataset_table_name, sr.detailed_result_dataset_id
			FROM emf.strategy_results sr
				inner join emf.internal_sources i
				on i.dataset_id = sr.dataset_id
				inner join emf.internal_sources i2
				on i2.dataset_id = sr.detailed_result_dataset_id
			where sr.id =any (intStrategyResultIds)
		LOOP
			intInputDatasetId := inventoryWithDetailedResults.dataset_id;
			intInputDatasetVersion := inventoryWithDetailedResults.dataset_version;
			inv_table_name := inventoryWithDetailedResults.table_name;
			inventory_detailed_result_dataset_id := inventoryWithDetailedResults.detailed_result_dataset_id;
			inventory_detailed_result_table_name := inventoryWithDetailedResults.detailed_result_dataset_table_name;
	
raise notice '%', intInputDatasetId || ' ' || intInputDatasetVersion || ' ' || inv_table_name || ' ' || inventory_detailed_result_dataset_id || ' ' || inventory_detailed_result_table_name;

			--get the inventory sector(s)
			select public.concatenate_with_ampersand(distinct name)
			from emf.sectors s
				inner join emf.datasets_sectors ds
				on ds.sector_id = s.id
			where ds.dataset_id = intInputDatasetId
			into inventory_sectors;

			--get dataset type name
			select dataset_types."name"
			from emf.datasets
			inner join emf.dataset_types
			on datasets.dataset_type = dataset_types.id
			where datasets.id = intInputDatasetId
			into dataset_type_name;

			--if Flat File 2010 Types then change primary key field expression variables...
			IF dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint' THEN
				fips_expression := 'region_cd';
				plantid_expression := 'facility_id';
				pointid_expression := 'unit_id';
				stackid_expression := 'rel_point_id';
				segment_expression := 'process_id';
				inv_ceff_expression := 'ann_pct_red';
				is_flat_file_inventory := true;
				IF dataset_type_name = 'Flat File 2010 Point' THEN
					longitude_expression := 'longitude';
					latitude_expression := 'latitude';
					plant_name_expression := 'facility_name';
				END IF;
			ELSE
				is_flat_file_inventory := false;
				fips_expression := 'fips';
				plantid_expression := 'plantid';
				pointid_expression := 'pointid';
				stackid_expression := 'stackid';
				segment_expression := 'segment';
				inv_ceff_expression := 'ceff';
				longitude_expression := 'xloc';
				latitude_expression := 'yloc';
				plant_name_expression := 'plant';
			END If;

			-- see if there are point specific columns in the inventory
			is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');

			-- see if there is a sic column in the inventory
			has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

			-- see if there is a naics column in the inventory
			has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

			-- see if there is a rpen column in the inventory
			has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

			-- see if there is lat & long columns in the inventory
			has_latlong_columns := public.check_table_for_columns(inv_table_name, '' || longitude_expression || ',' || latitude_expression || '', ',');

			-- see if there is plant column in the inventory
			has_plant_column := public.check_table_for_columns(inv_table_name, '' || plant_name_expression || '', ',');

			-- see if there is plant column in the inventory
			has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

			-- see if there is primary_device_type_code column in the inventory
			has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

			-- get month of the dataset, 0 (Zero) indicates an annual inventory
			select public.get_dataset_month(intInputDatasetId)
			into dataset_month;

			select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
			into no_days_in_month;

--			uncontrolled_emis_sql := public.get_uncontrolled_ann_emis_expression('inv', no_days_in_month, null::character varying(64), has_rpen_column);
--			emis_sql := public.get_ann_emis_expression('inv', no_days_in_month);
raise notice '%', dataset_type_name;
raise notice '%', is_flat_file_inventory;


			IF NOT is_flat_file_inventory THEN
				inv_pct_red_expression := 'inv.ceff  * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end;
				emis_sql := public.get_ann_emis_expression('inv', no_days_in_month);
			ELSE
				inv_pct_red_expression := 'inv.ann_pct_red';
				emis_sql := 'inv.ann_value';
			END IF;
			uncontrolled_emis_sql := public.get_uncontrolled_emis_expression(inv_pct_red_expression, emis_sql);


	execute
--	raise notice '%', 
	'insert into emissions.' || inventory_detailed_result_table_name || ' 
		(
		dataset_id,
		cm_abbrev,
		poll,
		scc,
		fips,
		plantid, 
		pointid, 
		stackid, 
		segment,
		control_eff,
		rule_pen,
		rule_eff,
		percent_reduction,
		inv_ctrl_eff,
		inv_rule_pen,
		inv_rule_eff,
		final_emissions,
		emis_reduction,
		inv_emissions,
		input_emis,
		output_emis,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		input_ds_id,
		cs_id,
		cm_id,
		equation_type,
		original_dataset_id,
		sector,
		xloc,
		yloc,
		plant,
		REPLACEMENT_ADDON,
		EXISTING_MEASURE_ABBREVIATION,
		EXISTING_PRIMARY_DEVICE_TYPE_CODE,
		strategy_name,
		control_technology,
		source_group,
		apply_order
		)
	-- distinct on (inv.record_id) makes sure we dont double control the source record...
	select DISTINCT ON (inv.record_id) 
		' || inventory_detailed_result_dataset_id || '::integer as dataset_id,
		dr.cm_abbrev as abbreviation,
		inv.poll,
		dr.scc,
		dr.fips,
		dr.plantid,
		dr.pointid,
		dr.stackid,
		dr.segment,
		dr.control_eff,
		dr.rule_pen,
		dr.rule_eff,
		dr.percent_reduction,
		inv.' || inv_ceff_expression || ' as inv_ctrl_eff,
		' || case when not is_point_table and not is_flat_file_inventory then 'inv.rpen' else '100.0' end || ' as inv_rule_pen,
		' || case when not is_flat_file_inventory then 'inv.reff' else '100.0' end || ' as inv_rule_eff,
		case when coalesce(dr.replacement_addon, '''') = ''A'' then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - dr.percent_reduction / 100) as final_emissions,
		' || emis_sql || ' - case when coalesce(dr.replacement_addon, '''') = ''A'' then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - dr.percent_reduction / 100) as emis_reduction,
		' || emis_sql || ' as inv_emissions,
		case when coalesce(dr.replacement_addon, '''') = ''A'' then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end as input_emis,
		case when coalesce(dr.replacement_addon, '''') = ''A'' then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - dr.percent_reduction / 100) as output_emis,
		substr(inv.' || fips_expression || ', 1, 2) as fipsst,
		substr(inv.' || fips_expression || ', 3, 3) as fipscty,
		' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ' as sic,
		' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ' as naics,
		inv.record_id as source_id,
		' || intInputDatasetId || '::integer as input_ds_id,
		' || intControlStrategyId || '::integer as cs_id,
		dr.cm_id as control_measures_id,
		null as equation_type,
		dr.original_dataset_id,
		' || quote_literal(inventory_sectors) || ' as sector,

		' || case when has_latlong_columns then 'inv.' || longitude_expression || ' as xloc,inv.' || latitude_expression || ' as yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
		' || case when has_plant_column then 'inv.' || plant_name_expression || ' as plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',

		dr.replacement_addon,
		dr.EXISTING_MEASURE_ABBREVIATION,
		dr.EXISTING_PRIMARY_DEVICE_TYPE_CODE,
		dr.strategy_name,
		dr.control_technology,
		dr.source_group, 
		dr.apply_order
	FROM emissions.' || detailed_result_table_name || ' dr

		inner join emissions.' || inv_table_name || ' inv
		on inv.' || fips_expression || ' = dr.fips
		and inv.scc = dr.scc
		' || case when is_point_table then 
		'and inv.' || plantid_expression || ' = dr.plantid
		and inv.' || pointid_expression || ' = dr.pointid
		and inv.' || stackid_expression || ' = dr.stackid
		and inv.' || segment_expression || ' = dr.segment' 
		else 
		'and dr.plantid is null
		and dr.pointid is null
		and dr.stackid is null
		and dr.segment is null' 
		end || '

--			on inv.dataset_id = coalesce(dr.original_dataset_id, dr.input_dataset_id)
--			on inv.record_id = dr.source_id

		-- get hap pollutants from inventory based on mapping (i.e., PM2_5 --> 100027 "4-Nitrophenol")
		inner join reference.cap_measure_to_hap_mapping chm
		on chm.pollutant = dr.poll
		and chm.eis_pollutant_code = inv.poll

		' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
		on fipscode.state_county_fips = inv.' || fips_expression || '
		and fipscode.country_num = ''0''' else '' end || '


	where ' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'inv') || '

		-- target only detailed result sources that have a mapped pollutant
		and dr.poll in (select distinct pollutant from reference.cap_measure_to_hap_mapping chm)

		-- disinclude inventory source records that have already been controlled
		and inv.record_id not in 
			(
			select invdr.source_id 
			from emissions.' || inventory_detailed_result_table_name || ' invdr
			where ' || public.build_version_where_filter(inventory_detailed_result_dataset_id, 0, 'invdr') || '
				and invdr.input_ds_id = ' || intInputDatasetId || '
--				and coalesce(dr.original_dataset_id, dr.input_ds_id) = ' || intInputDatasetId || '
			)

		';

		END LOOP;

	END LOOP;

return;

END;
$$ LANGUAGE plpgsql;




/*
		inner join emissions.' || inventory_detailed_result_table_name || ' invdr
		on inv.record_id <> invdr.source_id 
		and ' || public.build_version_where_filter(inventory_detailed_result_dataset_id, 0, 'invdr') || '
		and dr.input_ds_id = ' || intInputDatasetId || '

select *
from reference.cap_measure_to_hap_mapping

CREATE INDEX DS_cap_measure_to_hap_mapping_1970862123_poll
	ON reference.cap_measure_to_hap_mapping
	USING btree
	(pollutant);

CREATE INDEX DS_cap_measure_to_hap_mapping_1970862123_eispoll
	ON reference.cap_measure_to_hap_mapping
	USING btree
	(eis_pollutant_code);
vacuum analyze reference.cap_measure_to_hap_mapping;
*/
