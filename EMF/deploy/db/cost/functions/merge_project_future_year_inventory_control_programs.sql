/*
	delete from emf.control_strategy_control_packets_temp
	where control_strategy_id = 137;
	vacuum analyze emf.control_strategy_control_packets_temp;
	select public.merge_project_future_year_inventory_control_programs(137);
*/

CREATE OR REPLACE FUNCTION public.merge_project_future_year_inventory_control_programs(
	int_control_strategy_id integer) RETURNS void AS
$BODY$
DECLARE
	inventory_record RECORD;
	control_program RECORD;
	count integer := 0;
	sql character varying := '';
	control_program_dataset_filter_sql character varying := '';


	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	county_dataset_filter_sql text := '';
	inventory_year int;
BEGIN

	-- purge records specific for this strategy...

/*	delete from emf.control_strategy_control_packets_temp
	where control_strategy_id = int_control_strategy_id;

	delete from emf.control_strategy_projection_packets_temp
	where control_strategy_id = int_control_strategy_id;
*/
  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, 
			dt."name" as dataset_type
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
		and cpt."name" = ''Control''
		order by processing_order'
	LOOP
raise notice  '%', control_program.type;
	
		-- look at the closure control program inputs and table format (make sure all the right columns are in the table)
		IF control_program.type = 'Control' THEN


			execute
			--raise notice '%',
			 'insert into emf.control_strategy_control_packets_temp
				(
				control_strategy_id,
				fips,
				scc,
				poll,
				pri_cm_abbrev,
				ceff,
				reff,
				rpen,
				sic,
				mact,
				application_control,
				replacement,
				plantid,
				pointid,
				stackid,
				segment,
				compliance_date,
				naics
				)
			select 
				' || int_control_strategy_id || '::integer,
				fips,
				scc,
				poll,
				pri_cm_abbrev,
				ceff,
				reff,
				rpen,
				sic,
				mact,
				application_control,
				replacement,
				plantid,
				pointid,
				stackid,
				segment,
				compliance_date,
				naics
			FROM emissions.' || control_program.table_name || ' pc
			where 	' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '';

				
		-- look at the projection control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Projection' THEN

			-- currently no validation is required...

		END IF;
		
	
	END LOOP;


END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.merge_project_future_year_inventory_control_programs(int) OWNER TO emf;

