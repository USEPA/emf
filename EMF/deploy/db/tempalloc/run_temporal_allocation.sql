-- SELECT public.run_temporal_allocation(2, 125, 0);

CREATE OR REPLACE FUNCTION public.run_temporal_allocation(
  temporal_allocation_id integer,
  input_dataset_id integer,
  input_dataset_version integer
  ) RETURNS void AS $$
DECLARE
  inventory_table_name varchar(64) := '';
  inventory_dataset_type_name varchar(255) := '';
  
  xref_dataset_id integer;
  xref_dataset_version integer;
  monthly_profile_dataset_id integer;
  monthly_profile_dataset_version integer;
  monthly_profile_table_name varchar(64);
  weekly_profile_dataset_id integer;
  weekly_profile_dataset_version integer;
  weekly_profile_table_name varchar(64);

  monthly_result_dataset_id integer := null;
  monthly_result_table_name varchar(64) := '';
  daily_result_dataset_id integer := null;
  daily_result_table_name varchar(64) := '';
  
  inv_fips varchar(64) := 'inv.fips';
  inv_plantid varchar(64) := 'inv.plantid';
  inv_pointid varchar(64) := 'inv.pointid';
  inv_stackid varchar(64) := 'inv.stackid';
  inv_processid varchar(64) := 'inv.segment';
  inv_emissions varchar(64) := 'inv.ann_emis';
  
  xref_matching_sql text;
BEGIN

	-- get the inventory table name
	SELECT LOWER(i.table_name)
	  INTO inventory_table_name
	  FROM emf.internal_sources i
	 WHERE i.dataset_id = input_dataset_id;

	-- get the inventory dataset type
	SELECT dataset_types.name
	  INTO inventory_dataset_type_name
	  FROM emf.datasets
	  JOIN emf.dataset_types
	    ON dataset_types.id = datasets.dataset_type
	 WHERE datasets.id = input_dataset_id;

  -- set data field names
  IF inventory_dataset_type_name = 'Flat File 2010 Point' OR
     inventory_dataset_type_name = 'Flat File 2010 Nonpoint' THEN
		inv_fips := 'inv.region_cd';
		inv_plantid := 'inv.facility_id';
		inv_pointid := 'inv.unit_id';
		inv_stackid := 'inv.rel_point_id';
		inv_processid := 'inv.process_id';
		inv_emissions := 'inv.ann_value';
  END IF;
  
  -- get the cross-reference and profile dataset ids and versions
  SELECT ta.xref_dataset_id,
         ta.xref_dataset_version,
         ta.monthly_profile_dataset_id,
         ta.monthly_profile_dataset_version,
         ta.weekly_profile_dataset_id,
         ta.weekly_profile_dataset_version
    INTO xref_dataset_id,
         xref_dataset_version,
         monthly_profile_dataset_id,
         monthly_profile_dataset_version,
         weekly_profile_dataset_id,
         weekly_profile_dataset_version
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
  
  -- get monthly profile table name
  SELECT LOWER(i.table_name)
    INTO monthly_profile_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = monthly_profile_dataset_id;
  
  -- get weekly profile table name
  SELECT LOWER(i.table_name)
    INTO weekly_profile_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = weekly_profile_dataset_id;
  
  -- get the monthly result dataset info
  SELECT ta.monthly_result_dataset_id,
         LOWER(i.table_name)
    INTO monthly_result_dataset_id,
         monthly_result_table_name
    FROM emf.temporal_allocation ta
    JOIN emf.internal_sources i
      ON i.dataset_id = ta.monthly_result_dataset_id
   WHERE ta.id = temporal_allocation_id;
  
  -- get the daily result dataset info
  SELECT ta.daily_result_dataset_id,
         LOWER(i.table_name)
    INTO daily_result_dataset_id,
         daily_result_table_name
    FROM emf.temporal_allocation ta
    JOIN emf.internal_sources i
      ON i.dataset_id = ta.daily_result_dataset_id
   WHERE ta.id = temporal_allocation_id;

  xref_matching_sql := public.build_temporal_allocation_xref_sql(input_dataset_id, input_dataset_version, xref_dataset_id, xref_dataset_version, 'MONTHLY');

  -- initial case
  --   input is annual inventory
  --   output is monthly totals
  EXECUTE '
  INSERT INTO emissions.' || monthly_result_table_name || ' (
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         profile_id,
         fraction,
         month,
         total_emis,
         days_in_month,
         inv_record_id
  )
  SELECT ' || monthly_result_dataset_id || ',
         inv.poll,
         inv.scc,
         ' || inv_fips || ',
         ' || inv_plantid || ',
         ' || inv_pointid || ',
         ' || inv_stackid || ',
         ' || inv_processid || ',
         prof.profile_id,
         unnest(array[january, february, march, april, may, june, july, august, september, october, november, december]),
         unnest(array[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]),
         ' || inv_emissions || ' * unnest(array[january, february, march, april, may, june, july, august, september, october, november, december]),
         public.get_days_in_month(unnest(array[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12])::smallint, 2014::smallint),
         inv.record_id
    FROM emissions.' || inventory_table_name || ' inv
    JOIN (' || xref_matching_sql || ') xref
      ON xref.record_id = inv.record_id
    JOIN emissions.' || monthly_profile_table_name || ' prof
      ON prof.profile_id = xref.profile_id
   WHERE ' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || '
     AND ' || public.build_version_where_filter(monthly_profile_dataset_id, monthly_profile_dataset_version, 'prof');

  -- calculate monthly average day emissions
  EXECUTE '
  UPDATE emissions.' || monthly_result_table_name || '
     SET avg_day_emis = total_emis / days_in_month';

  -- now do monthly to daily using monthly results just created

  xref_matching_sql := public.build_temporal_allocation_xref_sql(input_dataset_id, input_dataset_version, xref_dataset_id, xref_dataset_version, 'WEEKLY');

  EXECUTE '
  INSERT INTO emissions.' || daily_result_table_name || '(
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         profile_type,
         profile_id,
         fraction,
         day,
         total_emis,
         inv_record_id
  )
  SELECT ' || daily_result_dataset_id || ',
         inv.poll,
         inv.scc,
         ' || inv_fips || ',
         ' || inv_plantid || ',
         ' || inv_pointid || ',
         ' || inv_stackid || ',
         ' || inv_processid || ',
         ''WEEKLY'',
         prof.profile_id,
         unnest(array[monday, tuesday, wednesday, thursday, friday, saturday, sunday]),
         unnest(array[''2014'' || monthly.month || ''01'', ''2014'' || monthly.month || ''02'', ''2014'' || monthly.month || ''03'', ''2014'' || monthly.month || ''04'', ''2014'' || monthly.month || ''05'', ''2014'' || monthly.month || ''06'', ''2014'' || monthly.month || ''07'']),
         monthly.avg_day_emis * 7 * unnest(array[monday, tuesday, wednesday, thursday, friday, saturday, sunday]),
         inv.record_id
    FROM emissions.' || inventory_table_name || ' inv
    JOIN (' || xref_matching_sql || ') xref
      ON xref.record_id = inv.record_id
    JOIN emissions.' || weekly_profile_table_name || ' prof
      ON prof.profile_id = xref.profile_id
    JOIN emissions.' || monthly_result_table_name || ' monthly
      ON monthly.inv_record_id = inv.record_id
   WHERE ' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || '
     AND ' || public.build_version_where_filter(weekly_profile_dataset_id, weekly_profile_dataset_version, 'prof');
    
END;
$$ LANGUAGE plpgsql;
