/*

select * from emf.control_strategies where strategy_type_id = 10;
select * from emf.strategy_results where control_strategy_id = 137;
select * from emf.strategy_result_types;

truncate emissions.ds_nc_ptnonipm_projection_test_strategy_msgs__20110824151406300;
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 6090); --quicker
SELECT public.populate_project_future_year_inventory_strategy_messages(153, 6077); --slow
SELECT public.build_project_future_year_inventory_matching_hierarchy_sql(952, 0, 4120, 1, '', null, 2);
SELECT public.validate_project_future_year_inventory_control_programs(137);
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 5583)
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 5589)
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 6109)
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 6464);

SELECT public.populate_project_future_year_inventory_strategy_messages(162, 6540);
*/


DROP FUNCTION IF EXISTS public.populate_project_future_year_inventory_strategy_messages(integer, integer);

CREATE OR REPLACE FUNCTION public.populate_project_future_year_inventory_strategy_messages(
	int_control_strategy_id integer, 
--	input_dataset_id integer, 
--	input_dataset_version integer, 
	strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	strat_inv_filter text := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	control_program RECORD;
	inventory_record RECORD;
	county_dataset_filter_sql text := '';
	control_program_dataset_filter_sql text := '';
	inventory_year integer := null;
	is_point_table boolean := false;
	sql character varying := '';
	compliance_date_cutoff_daymonth varchar(256) := '';
	effective_date_cutoff_daymonth varchar(256) := '';
	cnt integer := 0;
	unused_plant_closure_packet_records_sql text := '';
	unused_control_packet_records_sql text := '';
	inventory_count integer := 0;
	
	--support for flat file ds types...
	dataset_type_name character varying(255) = '';
	fips_expression character varying(64) = 'fips';
	plantid_expression character varying(64) = 'plantid';
	pointid_expression character varying(64) = 'pointid';
	stackid_expression character varying(64) = 'stackid';
	segment_expression character varying(64) = 'segment';

	cp_fips_expression character varying(64) := 'fips';
	cp_plantid_expression character varying(64) := 'plantid';
	cp_pointid_expression character varying(64) := 'pointid';
	cp_stackid_expression character varying(64) := 'stackid';
	cp_segment_expression character varying(64) := 'segment';
	cp_mact_expression character varying(64) := 'mact';

	is_monthly_source_sql character varying := 'coalesce(jan_value,feb_value,mar_value,apr_value,may_value,jun_value,jul_value,aug_value,sep_value,oct_value,nov_value,dec_value) is not null';
	is_annual_source_sql character varying := 'coalesce(jan_value,feb_value,mar_value,apr_value,may_value,jun_value,jul_value,aug_value,sep_value,oct_value,nov_value,dec_value) is null and ann_value is not null';
	is_control_packet_extended_format boolean := false;
	is_flat_file_inventory boolean := false;
BEGIN

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into strategy_messages_dataset_id,
		strategy_messages_table_name;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT case when length(trim(cs.filter)) > 0 then '(' || cs.filter || ')' else null end,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO strat_inv_filter,
		inventory_year,
		county_dataset_id,
		county_dataset_version;

	-- load the Compliance and Effective Date Cutoff Day/Month (Stored as properties)
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_COMPLIANCE_DATE_CUTOFF_MONTHDAY'
	into compliance_date_cutoff_daymonth;
	compliance_date_cutoff_daymonth := coalesce(compliance_date_cutoff_daymonth, '07/01');	--default just in case
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_EFFECTIVE_DATE_CUTOFF_MONTHDAY'
	into effective_date_cutoff_daymonth;
	effective_date_cutoff_daymonth := coalesce(effective_date_cutoff_daymonth, '07/01');	--default just in case
	

	-- setup temp table -- it will help track issues...
	EXECUTE '
		CREATE TEMP TABLE packet_issues (
			packet_type character varying(255),
			packet_name character varying(255),
			packet_dataset_id integer,
			packet_dataset_version integer,
			packet_dataset_name character varying(255),
			inventory_dataset_id integer,
			inventory_dataset_version integer,
			inventory_dataset_name character varying(255),
			applies_to_inventory boolean
		) ON COMMIT DROP;';




	select count(1)
	from emf.input_datasets_control_strategies inv
	where inv.control_strategy_id = int_control_strategy_id
	into inventory_count;



	-- cursor over inventories
	FOR inventory_record IN EXECUTE 
		'select lower(i.table_name) as table_name, 
			inv.dataset_id, 
			inv.dataset_version,
			d.name as dataset_name,
			dt.name as dataset_type_name
		 from emf.input_datasets_control_strategies inv

			inner join emf.internal_sources i
			on i.dataset_id = inv.dataset_id

			inner join emf.datasets d
			on d.id = inv.dataset_id

			inner join emf.dataset_types dt
			on d.dataset_type = dt.id

		where inv.control_strategy_id = ' || int_control_strategy_id
	LOOP

		-- get the input dataset info
		select inventory_record.table_name, inventory_record.dataset_type_name
		into inv_table_name, dataset_type_name;

		--if Flat File 2010 Types then change primary key field expression variables...
		IF dataset_type_name = 'Flat File 2010 Point' or dataset_type_name = 'Flat File 2010 Nonpoint' THEN
			fips_expression := 'region_cd';
			plantid_expression := 'facility_id';
			pointid_expression := 'unit_id';
			stackid_expression := 'rel_point_id';
			segment_expression := 'process_id';
			is_flat_file_inventory := true;
		ELSE
			fips_expression := 'fips';
			plantid_expression := 'plantid';
			pointid_expression := 'pointid';
			stackid_expression := 'stackid';
			segment_expression := 'segment';
			is_flat_file_inventory := false;
		END If;

		-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
		IF county_dataset_id is not null THEN
			county_dataset_filter_sql := ' and inv.' || fips_expression || ' in (SELECT fips
				FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
				where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
		END IF;

		-- see if there are point specific columns in the inventory
		is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');
		
		-- build version info into where clause filter
		inv_filter := '(' || public.build_version_where_filter(inventory_record.dataset_id, inventory_record.dataset_version, 'inv') || ')' || coalesce(' and ' || public.alias_filter(strat_inv_filter, inv_table_name, 'inv'), '');


		-- now lets evaluate each packet and see what we find
		-- Things to look for:
		-- For Plant Closures:  
		--	- Program doesn't apply to any inventory, then report this
		--	- If Program does apply to inventory in some fashion, then report packets (plant closures) records that don't affect 
		--	  any sources in that inventory
		
		-- For Projections, Controls, and Allowables:  
		--	- Program doesn't apply to any inventory, then report this
		--	- Program does apply to at least one inventory, then report other inventories that aren't affected
		--	- If Program does apply to inventory in some fashion, then report packets records that don't affect 
		--	  any sources in that inventory

		-- First look at Plant Closures
		
raise notice '%', 'now lets evaluate each packet and see what we find' || clock_timestamp();

		FOR control_program IN EXECUTE 
			'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
				cp.start_date, cp.end_date, 
				cp.dataset_id, cp.dataset_version,
				dt."name" as dataset_type, d."name" as dataset_name
			 from emf.control_strategy_programs csp

				inner join emf.control_programs cp
				on cp.id = csp.control_program_id

				inner join emf.control_program_types cpt
				on cpt.id = cp.control_program_type_id

				inner join emf.internal_sources i
				on i.dataset_id = cp.dataset_id

				inner join emf.datasets d
				on d.id = i.dataset_id

				inner join emf.dataset_types dt
				on dt.id = d.dataset_type
			where csp.control_strategy_id = ' || int_control_strategy_id || '
--				and cpt."name" = ''Plant Closure''
			order by processing_order'
		LOOP

			-- see if there any issues with the plant closure file
			IF control_program.type = 'Plant Closure' THEN

				--raise notice '%', 
				execute 
				'select 
					count(1) as cnt
				FROM emissions.' || control_program.table_name || ' pc

						inner join emissions.' || inv_table_name || ' inv
						on pc.fips = inv.' || fips_expression || '
						' || case when not is_point_table then '
						and pc.plantid is null
						and pc.pointid is null
						and pc.stackid is null
						and pc.segment is null
						' else '
						and pc.plantid = inv.' || plantid_expression || '
						and coalesce(pc.pointid, inv.' || pointid_expression || ') = inv.' || pointid_expression || '
						and coalesce(pc.stackid, inv.' || stackid_expression || ') = inv.' || stackid_expression || '
						and coalesce(pc.segment, inv.' || segment_expression || ') = inv.' || segment_expression || '
						' end || '
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where 	
						-- only keep if before cutoff date
						coalesce(pc.effective_date::timestamp without time zone, ''1/1/1900''::timestamp without time zone) < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '
				limit 1' -- only need to return one...
				into cnt;


				-- if there are packet records that are applied, then go ahead and report the packet records that didn't affect any sources...
				if cnt > 0 then

					unused_plant_closure_packet_records_sql := unused_plant_closure_packet_records_sql || 
							(case when length(unused_plant_closure_packet_records_sql) > 0 then ' union all ' else '' end) || 
					'select 
						pc.record_id as packet_rec_id,
						pc.dataset_id as packet_ds_id,
						pc.fips,
						pc.plantid, 
						pc.pointid, 
						pc.stackid, 
						pc.segment, 
						' || quote_literal(control_program.control_program_name) || '::character varying(255) as control_program,
						''Plant'' || case when pc.plant is not null and length(pc.plant) > 0 then coalesce('', '' || pc.plant || '','', '''') else '''' end || '' is missing from the inventory.'' as "comment",
						pc.effective_date

					FROM emissions.' || control_program.table_name || ' pc

						left outer join emissions.' || inv_table_name || ' inv
						on pc.fips = inv.' || fips_expression || '
						' || case when not is_point_table then '
						and pc.plantid is null
						and pc.pointid is null
						and pc.stackid is null
						and pc.segment is null
						' else '
						and pc.plantid = inv.' || plantid_expression || '
						and coalesce(pc.pointid, inv.' || pointid_expression || ') = inv.' || pointid_expression || '
						and coalesce(pc.stackid, inv.' || stackid_expression || ') = inv.' || stackid_expression || '
						and coalesce(pc.segment, inv.' || segment_expression || ') = inv.' || segment_expression || '
						' end || '
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where 	
						-- only keep if before cutoff date
						coalesce(pc.effective_date::timestamp without time zone, ''1/1/1900''::timestamp without time zone) < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '
						and inv.record_id is null';
						
				ELSE
				
					unused_plant_closure_packet_records_sql := unused_plant_closure_packet_records_sql || 
							(case when length(unused_plant_closure_packet_records_sql) > 0 then ' union all ' else '' end) || 
					'select 
						pc.record_id as packet_rec_id,
						pc.dataset_id as packet_ds_id,
						pc.fips,
						pc.plantid, 
						pc.pointid, 
						pc.stackid, 
						pc.segment, 
						' || quote_literal(control_program.control_program_name) || '::character varying(255) as control_program,
						''Plant'' || case when pc.plant is not null and length(pc.plant) > 0 then coalesce('', '' || pc.plant || '','', '''') else '''' end || '' is missing from the inventory.'' as "comment",
						pc.effective_date
					FROM emissions.' || control_program.table_name || ' pc

					where 	
						-- only keep if before cutoff date
						coalesce(pc.effective_date::timestamp without time zone, ''1/1/1900''::timestamp without time zone) < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '';
						
				end if;

			-- see if there any matching issues with the projection, control, or allowable packet
			ELSEIF control_program.type = 'Projection' or control_program.type = 'Control' or control_program.type = 'Allowable' THEN

				-- make sure the dataset type is right...
				IF (control_program.type = 'Projection' and (control_program.dataset_type = 'Projection Packet' or control_program.dataset_type = 'Projection Packet Extended')) 
					or (control_program.type = 'Control' and (control_program.dataset_type = 'Control Packet' or control_program.dataset_type = 'Control Packet Extended'))  
					or (control_program.type = 'Allowable' and (control_program.dataset_type = 'Allowable Packet' or control_program.dataset_type = 'Allowable Packet Extended')) THEN
raise notice '%', 'first see if packet affects anything -- packet type, ' || control_program.type || ', packet, ' || control_program.dataset_id || ', inv ' || inventory_record.dataset_id;

					--if control packet is in the Extended Pack Format, then change the column names to the newer format
					IF strpos(control_program.dataset_type, 'Extended') > 0 THEN
						cp_fips_expression := 'region_cd';
						cp_plantid_expression := 'facility_id';
						cp_pointid_expression := 'unit_id';
						cp_stackid_expression := 'rel_point_id';
						cp_segment_expression := 'process_id';
						cp_mact_expression := 'reg_code';
					ELSE
						cp_fips_expression := 'fips';
						cp_plantid_expression := 'plantid';
						cp_pointid_expression := 'pointid';
						cp_stackid_expression := 'stackid';
						cp_segment_expression := 'segment';
						cp_mact_expression := 'mact';
					END If;


					-- see if control packet is in the extended format
					is_control_packet_extended_format := public.check_table_for_columns(control_program.table_name, 'region_cd', ',');

					-- first see if there is any issues
					execute '
					select count(1) 
					 
					from (
						' ||

						public.build_project_future_year_inventory_matching_hierarchy_sql(
							inventory_record.dataset_id, --inv_dataset_id integer, 
							inventory_record.dataset_version, --inv_dataset_version integer, 
							control_program.dataset_id, --control_program_dataset_id integer, 
							control_program.dataset_version, --control_program_dataset_version integer, 
							null::varchar, --select_columns varchar, 
--							'fips,plantid,pointid,stackid,segment,scc,poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message', --select_columns varchar, 
							strat_inv_filter,--null'substring(fips,1,2) = ''37''', --inv_filter text,
							county_dataset_id, --1279 county_dataset_id integer,
							county_dataset_version, --county_dataset_version integer,
							case 
								when control_program.type = 'Control' then 
									'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone and application_control = ''Y''' 
									|| case 
										when is_flat_file_inventory and is_control_packet_extended_format then 
											' and (
												(' || is_annual_source_sql || ' and ann_pctred is not null)
												or 
												(' || is_monthly_source_sql || ' and coalesce(ann_pctred,jan_pctred,feb_pctred,mar_pctred,apr_pctred,may_pctred,jun_pctred,jul_pctred,aug_pctred,sep_pctred,oct_pctred,nov_pctred,dec_pctred) is not null)

											)' 
										when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
											' and ann_pctred is not null'
										else --dont include monthly packets
											' and ceff is not null'
									end
								when control_program.type = 'Allowable' then 
									'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone' 
									|| case 
										when is_flat_file_inventory and is_control_packet_extended_format then 
											' and (
												(' || is_annual_source_sql || ' and coalesce(ann_cap,ann_replacement) is not null)
												or 
												(' || is_monthly_source_sql || ' and coalesce(ann_cap,jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement) is not null)

											)' 
										when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
											' and coalesce(ann_cap,ann_replacement) is not null'
										else --dont include monthly packets
											' and coalesce(cap,replacement) is not null'
									end
								else 
									case 
										when is_flat_file_inventory and is_control_packet_extended_format then 
											'(
												(' || is_annual_source_sql || ' and ann_proj_factor is not null)
												or 
												(' || is_monthly_source_sql || ' and coalesce(ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor) is not null)

											)'
										when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
											'ann_proj_factor is not null'
										else --dont include monthly packets
											'proj_factor is not null'
									end								 
							end,
							3 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
							)

					|| 'limit 1 -- only need to return one...
					) tbl'
					into cnt;

raise notice '%', 'first see if packet affects anything -- packet type, ' || control_program.type || ', packet, ' || control_program.dataset_id || ', inv ' || inventory_record.dataset_id || ', cnt ' || cnt;
					if cnt > 0 then

						unused_control_packet_records_sql := unused_control_packet_records_sql || 
							(case when length(unused_control_packet_records_sql) > 0 then ' union all ' else '' end) || 

							'select  
								record_id, packet_rec_id,packet_ds_id,' || cp_fips_expression || ' as fips,scc,' || cp_plantid_expression || ' as plantid,' || cp_pointid_expression || ' as pointid,' || cp_stackid_expression || ' as stackid,' || cp_segment_expression || ' as segment,poll,sic,
								' || cp_mact_expression || ' as mact,naics,compliance_date,control_program,ranking
							from ( ' || public.build_project_future_year_inventory_matching_hierarchy_sql(
								inventory_record.dataset_id, --inv_dataset_id integer, 
								inventory_record.dataset_version, --inv_dataset_version integer, 
								control_program.dataset_id, --control_program_dataset_id integer, 
								control_program.dataset_version, --control_program_dataset_version integer, 
								'record_id as packet_rec_id,dataset_id as packet_ds_id,' || cp_fips_expression || ',scc,' || cp_plantid_expression || ',' || cp_pointid_expression || ',' || cp_stackid_expression || ',' || cp_segment_expression || ',poll,sic,' || 
								case when control_program.type = 'Allowable' and control_program.dataset_type = 'Allowable Packet' then 'null::varchar(6) as mact' else '' || cp_mact_expression || '' end || 
								',naics,' || 
								case when control_program.type = 'Projection' then 'null::timestamp without time zone as compliance_date' else 'compliance_date' end || 
								',' || quote_literal(control_program.control_program_name) || ' as control_program', --select_columns varchar, 
--								'fips,plantid,pointid,stackid,segment,scc,poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message', --select_columns varchar, 
								strat_inv_filter,--null'substring(fips,1,2) = ''37''', --inv_filter text,
								county_dataset_id, --1279 county_dataset_id integer,
								county_dataset_version, --county_dataset_version integer,
								case 
									when control_program.type = 'Control' then 
										'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone and application_control = ''Y''' 
										|| case 
											when is_flat_file_inventory and is_control_packet_extended_format then 
												' and (
													(' || is_annual_source_sql || ' and ann_pctred is not null)
													or 
													(' || is_monthly_source_sql || ' and coalesce(ann_pctred,jan_pctred,feb_pctred,mar_pctred,apr_pctred,may_pctred,jun_pctred,jul_pctred,aug_pctred,sep_pctred,oct_pctred,nov_pctred,dec_pctred) is not null)

												)' 
											when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
												' and ann_pctred is not null'
											else --dont include monthly packets
												' and ceff is not null'
										end
									when control_program.type = 'Allowable' then 
										'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone' 
										|| case 
											when is_flat_file_inventory and is_control_packet_extended_format then 
												' and (
													(' || is_annual_source_sql || ' and coalesce(ann_cap,ann_replacement) is not null)
													or 
													(' || is_monthly_source_sql || ' and coalesce(ann_cap,jan_cap,feb_cap,mar_cap,apr_cap,may_cap,jun_cap,jul_cap,aug_cap,sep_cap,oct_cap,nov_cap,dec_cap,jan_replacement,feb_replacement,mar_replacement,apr_replacement,may_replacement,jun_replacement,jul_replacement,aug_replacement,sep_replacement,oct_replacement,nov_replacement,dec_replacement) is not null)

												)' 
											when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
												' and coalesce(ann_cap,ann_replacement) is not null'
											else --dont include monthly packets
												' and coalesce(cap,replacement) is not null'
										end
									else 
										case 
											when is_flat_file_inventory and is_control_packet_extended_format then 
												'(
													(' || is_annual_source_sql || ' and ann_proj_factor is not null)
													or 
													(' || is_monthly_source_sql || ' and coalesce(ann_proj_factor,jan_proj_factor,feb_proj_factor,mar_proj_factor,apr_proj_factor,may_proj_factor,jun_proj_factor,jul_proj_factor,aug_proj_factor,sep_proj_factor,oct_proj_factor,nov_proj_factor,dec_proj_factor) is not null)

												)' 
											when is_control_packet_extended_format then --dont include monthly packets, since we aren't dealing with monthly emissions (ff10 formats)
												'ann_proj_factor is not null'
											else --dont include monthly packets
												'proj_factor is not null'
										end								 
								end,
								2 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
								)

						|| ') tbl';

--raise notice '%', unused_control_packet_records_sql;
					-- packet didn't affect any inventory sources...
					ELSE

						unused_control_packet_records_sql := unused_control_packet_records_sql || 
							(case when length(unused_control_packet_records_sql) > 0 then ' union all ' else '' end) || 

							'select null::integer as record_id,record_id as packet_rec_id,dataset_id as packet_ds_id,' || cp_fips_expression || ' as fips,scc,' || cp_plantid_expression || ' as plantid,' || cp_pointid_expression || ' as pointid,' || cp_stackid_expression || ' as stackid,' || cp_segment_expression || ' as segment,poll,sic,' || 
								case when control_program.type = 'Allowable' and control_program.dataset_type = 'Allowable Packet' then 'null::varchar(6)' else '' || cp_mact_expression || '' end || 
								' as mact,naics,' || 
								case when control_program.type = 'Projection' then 'null::timestamp without time zone as compliance_date' else 'compliance_date' end || 
								',' || quote_literal(control_program.control_program_name) || ' as control_program, null::double precision as ranking

							FROM emissions.' || control_program.table_name || ' pc
							where 	' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '';



					
					END IF;

				END IF;
			END IF;

			execute
			--raise notice '%',
			 'insert into packet_issues
				(
				packet_type, 
				packet_name, 
				packet_dataset_id, 
				packet_dataset_version, 
				packet_dataset_name, 
				inventory_dataset_id, 
				inventory_dataset_version, 
				inventory_dataset_name, 
				applies_to_inventory
				)
			select 
				' || quote_literal(control_program.type) || ',
				' || quote_literal(control_program.control_program_name) || ',
				' || quote_literal(control_program.dataset_id) || ',
				' || quote_literal(control_program.dataset_version) || ',
				' || quote_literal(control_program.dataset_name) || ',
				' || quote_literal(inventory_record.dataset_id) || ',
				' || quote_literal(inventory_record.dataset_version) || ',
				' || quote_literal(inventory_record.dataset_name) || ',
				' || case when coalesce(cnt, 0) > 0 then true else false end || '::boolean;';

raise notice '%', 'check inventory ' || inventory_record.dataset_name || ' against ' || control_program.table_name || clock_timestamp();

		END LOOP;

		
	END LOOP;


	-- First report packet doesn't affect any inventories
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		status,
		control_program,
		message_type,
		message,
		inventory
		)
		select 
		' || strategy_messages_dataset_id || '::integer,
		''Warning''::character varying(11) as status,
		packet_name::character varying(255) as control_program,
		''Packet Level''::character varying(255) as message_type,
		''Control Program doesn''''t apply to any inventories.''::character varying(255) as message,
		null::character varying(255) as inventory
		from packet_issues
		group by packet_name
		having sum(case when not applies_to_inventory then 1 else 0 end) = sum(1)';



	-- Second report packet affected some inventories, but not all, let client know which ones weren't affected
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		status,
		control_program,
		message_type,
		message,
		inventory
		)
		select 
		' || strategy_messages_dataset_id || '::integer,
		''Warning''::character varying(11) as status,
		packet_name::character varying(255) as control_program,
		''Packet Level''::character varying(255) as message_type,
		''Control Program doesn''''t apply to any sources in the inventory, '' || inventory_dataset_name || ''.''::character varying(255) as message,
		inventory_dataset_name as inventory
		from packet_issues
		where packet_name in (
			select packet_name 
			from packet_issues
			group by packet_name
			having sum(case when applies_to_inventory then 1 else 0 end) <> sum(1)
				and sum(case when applies_to_inventory then 1 else 0 end) > 0
		)
		and applies_to_inventory = false';

	-- report issues where packet didn't affect any sources...
	if length(unused_control_packet_records_sql) > 0 then


--		raise notice '%', 
		execute
		'insert into emissions.' || strategy_messages_table_name || ' 
			(
			dataset_id, 
			packet_fips, 
			packet_scc, 
			packet_plantid, 
			packet_pointid, 
			packet_stackid, 
			packet_segment, 
			packet_poll, 
			status,
			control_program,
			message_type,
			message,
			packet_sic,
			packet_mact,
			packet_naics,
			--replacement,
			packet_compliance_effective_date
			)
		select distinct on (packet_ds_id,packet_rec_id) 
			' || strategy_messages_dataset_id || '::integer,
			fips, 
			scc, 
			plantid, 
			pointid, 
			stackid, 
			segment, 
			poll, 
			''Warning''::character varying(11) as status,
			control_program,
			''Packet Level''::character varying(255) as message_type,
			''Packet record does not affect any inventory records.'' as message,
			sic,
			mact,
			naics,
			--replacement,
			compliance_date

		from (
			select *, count(1) OVER w as cnt

			from (
				' || unused_control_packet_records_sql || ') tbl
				WINDOW w AS (PARTITION BY packet_rec_id,packet_ds_id)
			) tbl
		where cnt = ' || inventory_count || '
		order by packet_ds_id,packet_rec_id
		';

	END IF;

	-- report issues where plant closure packet didn't affect any sources...
	if length(unused_plant_closure_packet_records_sql) > 0 then


		execute
		--raise notice '%',
		 'insert into emissions.' || strategy_messages_table_name || ' 
			(
			dataset_id, 
			packet_fips, 
			packet_plantid, 
			packet_pointid, 
			packet_stackid, 
			packet_segment, 
			status,
			control_program,
			message_type,
			message,
			packet_compliance_effective_date
			)
		select distinct on (packet_ds_id,packet_rec_id) 
			' || strategy_messages_dataset_id || '::integer,
			fips, 
			plantid, 
			pointid, 
			stackid, 
			segment, 
			''Warning''::character varying(11) as status,
			control_program,
			''Packet Level''::character varying(255) as message_type,
			''Packet record does not affect any inventory records.'' as message,
			effective_date::timestamp without time zone
		from (
			select *, count(1) OVER w as cnt

			from (
				' || unused_plant_closure_packet_records_sql || '
			) tbl
			WINDOW w AS (PARTITION BY packet_rec_id,packet_ds_id)
		) tbl
		where cnt = ' || inventory_count || '
		order by packet_ds_id,packet_rec_id
		';
	END IF;


END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;