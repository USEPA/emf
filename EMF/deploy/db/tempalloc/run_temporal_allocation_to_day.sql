
CREATE OR REPLACE FUNCTION public.run_temporal_allocation_to_day(
  temporal_allocation_id integer,
  input_dataset_id integer,
  input_dataset_version integer,
  monthly_result_dataset_id integer,
  daily_result_dataset_id integer
  ) RETURNS void AS $$
DECLARE
  inventory_table_name varchar(64) := '';
  inventory_dataset_type_name varchar(255) := '';
  
  weekly_profile_dataset_id integer;
  weekly_profile_dataset_version integer;
  weekly_profile_table_name varchar(64);
  daily_profile_dataset_id integer;
  daily_profile_dataset_version integer;
  daily_profile_table_name varchar(64);
  
  use_daily_profile boolean := false;
  xref_type varchar(10);
  prof_table_name varchar(64);
  
  inv_filter text := '';
  resolution varchar(64);
  start_day date;
  end_day date;

  monthly_result_table_name varchar(64) := '';
  daily_result_table_name varchar(64) := '';
  
  is_flat_file_inventory boolean := false;
  inv_fips varchar(64) := 'inv.fips';
  inv_plantid varchar(64) := 'plantid';
  inv_pointid varchar(64) := 'pointid';
  inv_stackid varchar(64) := 'stackid';
  inv_processid varchar(64) := 'segment';
    
  emis_sql text;
  where_sql text;
  
  loop_date date;
  is_weekend boolean;
  add_day boolean;
  dates_sql text := '';
  column_sql text := '';
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

  IF inventory_dataset_type_name = 'Flat File 2010 Point' OR
     inventory_dataset_type_name = 'Flat File 2010 Nonpoint' THEN

    -- set data field names
    is_flat_file_inventory := true;
    inv_fips := 'inv.region_cd';
    inv_plantid := 'facility_id';
    inv_pointid := 'unit_id';
    inv_stackid := 'rel_point_id';
    inv_processid := 'process_id';
      
  END IF;
  
  -- check if inventory has point source characteristics
  IF (public.check_table_for_columns(inventory_table_name, inv_plantid || ',' || inv_pointid || ',' || inv_stackid || ',' || inv_processid, ',')) THEN
    inv_plantid := 'inv.' || inv_plantid;
    inv_pointid := 'inv.' || inv_pointid;
    inv_stackid := 'inv.' || inv_stackid;
    inv_processid := 'inv.' || inv_processid;
  ELSE
    inv_plantid := 'NULL';
    inv_pointid := 'NULL';
    inv_stackid := 'NULL';
    inv_processid := 'NULL';
  END IF;
  
  -- get inventory filter if specified
  SELECT CASE
           WHEN LENGTH(TRIM(ta.filter)) > 0 THEN 
             '(' || public.alias_inventory_filter(ta.filter, 'inv') || ')' 
           ELSE NULL 
         END
    INTO inv_filter
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
  
  -- build version info into inventory filter
  inv_filter := 
    '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || 
    COALESCE(' AND ' || inv_filter, '');
  
  -- get the cross-reference and profile dataset ids and versions
  SELECT ta.weekly_profile_dataset_id,
         ta.weekly_profile_dataset_version,
         ta.daily_profile_dataset_id,
         ta.daily_profile_dataset_version
    INTO weekly_profile_dataset_id,
         weekly_profile_dataset_version,
         daily_profile_dataset_id,
         daily_profile_dataset_version
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
  
  -- get output time period information
  SELECT LOWER(res.name),
         ta.start_day,
         ta.end_day
    INTO resolution,
         start_day,
         end_day
    FROM emf.temporal_allocation ta
    JOIN emf.temporal_allocation_resolution res
      ON ta.resolution_id = res.id
   WHERE ta.id = temporal_allocation_id;
  
  -- check if daily or weekly profiles should be used
  IF daily_profile_dataset_id IS NOT NULL THEN
    use_daily_profile := true;
  END IF;
  
  -- get weekly profile table name
  IF NOT use_daily_profile THEN
    SELECT LOWER(i.table_name)
      INTO weekly_profile_table_name
      FROM emf.internal_sources i
     WHERE i.dataset_id = weekly_profile_dataset_id;
  END IF;
  
  -- get daily profile table name
  IF use_daily_profile THEN
    SELECT LOWER(i.table_name)
      INTO daily_profile_table_name
      FROM emf.internal_sources i
     WHERE i.dataset_id = daily_profile_dataset_id;
  END IF;
  
  -- get the monthly result dataset info
  SELECT LOWER(i.table_name)
    INTO monthly_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = monthly_result_dataset_id;
  
  -- get the daily result dataset info
  SELECT LOWER(i.table_name)
    INTO daily_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = daily_result_dataset_id;

  -- now do monthly to daily using monthly results just created

  IF use_daily_profile THEN
    xref_type = 'DAILY';
    prof_table_name = daily_profile_table_name;
  ELSE
    xref_type = 'WEEKLY';
    prof_table_name = weekly_profile_table_name;
  END IF;

  loop_date := start_day;
  FOR month_num IN EXTRACT(MONTH FROM start_day)..EXTRACT(MONTH FROM end_day) LOOP

    -- build list of days to process for current month
    dates_sql := '';
    column_sql := '';
    LOOP
      is_weekend := EXTRACT(DOW FROM loop_date) = 0 OR EXTRACT(DOW FROM loop_date) = 6;

      IF resolution LIKE '%weekday%' THEN
        add_day := NOT is_weekend;
      ELSIF resolution LIKE '%weekend%' THEN
        add_day := is_weekend;
      ELSE
        add_day := true;
      END IF;

      IF add_day THEN
        IF LENGTH(dates_sql) > 0 THEN
          dates_sql := dates_sql || ',';
          column_sql := column_sql || ',';
        END IF;
        
        dates_sql := dates_sql || to_char(loop_date, 'YYYYMMDD');
        IF use_daily_profile THEN
          column_sql := column_sql || 'day' || to_char(loop_date, 'FMDD');
        ELSE
          column_sql := column_sql || to_char(loop_date, 'day');
        END IF;
      END IF;
      
      loop_date := loop_date + 1;
      EXIT WHEN loop_date > end_day OR EXTRACT(MONTH FROM loop_date) != month_num;
    END LOOP;

    -- skip month if no days to process
    IF LENGTH(dates_sql) = 0 THEN
      CONTINUE;
    END IF;
    
    IF use_daily_profile THEN
      emis_sql := 'monthly.total_emis * unnest(array[' || column_sql || '])';
      where_sql := public.build_version_where_filter(daily_profile_dataset_id, daily_profile_dataset_version, 'prof') || ' AND prof.month = ' || month_num;
    ELSE
      emis_sql := 'monthly.avg_day_emis * 7 * unnest(array[' || column_sql || '])';
      where_sql := public.build_version_where_filter(weekly_profile_dataset_id, weekly_profile_dataset_version, 'prof');
    END IF;

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
           inv_dataset_id,
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
           ''' || xref_type || ''',
           prof.profile_id,
           unnest(array[' || column_sql || ']),
           unnest(array[' || dates_sql || ']),
           ' || emis_sql || ',
           inv.dataset_id,
           inv.record_id
      FROM emissions.' || inventory_table_name || ' inv
      JOIN temp_alloc_xref xref
        ON xref.record_id = inv.record_id
      JOIN emissions.' || prof_table_name || ' prof
        ON prof.profile_id::varchar(15) = xref.profile_id
      JOIN emissions.' || monthly_result_table_name || ' monthly
        ON monthly.inv_dataset_id = inv.dataset_id
       AND monthly.inv_record_id = inv.record_id
     WHERE ' || inv_filter || '
       AND ' || where_sql || '
       AND monthly.month = ' || month_num;
  END LOOP;
  
END;
$$ LANGUAGE plpgsql;
