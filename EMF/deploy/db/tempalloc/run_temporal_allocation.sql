
CREATE OR REPLACE FUNCTION public.run_temporal_allocation(
  temporal_allocation_id integer,
  input_dataset_id integer,
  input_dataset_version integer,
  monthly_result_dataset_id integer,
  daily_result_dataset_id integer,
  episodic_result_dataset_id integer
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
  
  resolution varchar(64);
  inventory_year smallint;
  start_day date;
  end_day date;

  monthly_result_table_name varchar(64) := '';
  daily_result_table_name varchar(64) := '';
  episodic_result_table_name varchar(64) := '';
  
  is_flat_file_inventory boolean := false;
  inv_fips varchar(64) := 'inv.fips';
  inv_plantid varchar(64) := 'inv.plantid';
  inv_pointid varchar(64) := 'inv.pointid';
  inv_stackid varchar(64) := 'inv.stackid';
  inv_processid varchar(64) := 'inv.segment';
  inv_emissions varchar(64) := 'inv.ann_emis';
  
  xref_matching_sql text;
  
  month_num_sql text := '';
  month_name_sql text := '';
  loop_date date;
  is_weekend boolean;
  add_day boolean;
  dates_sql text := '';
  day_name_sql text := '';
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
    is_flat_file_inventory := true;
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
  
  -- get output time period information
  SELECT LOWER(res.name),
         EXTRACT(YEAR FROM ta.start_day),
         ta.start_day,
         ta.end_day
    INTO resolution,
         inventory_year,
         start_day,
         end_day
    FROM emf.temporal_allocation ta
    JOIN emf.temporal_allocation_resolution res
      ON ta.resolution_id = res.id
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
  SELECT LOWER(i.table_name)
    INTO monthly_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = monthly_result_dataset_id;
  
  -- get the daily result dataset info
  SELECT LOWER(i.table_name)
    INTO daily_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = daily_result_dataset_id;
  
  -- get the episodic result dataset info
  SELECT LOWER(i.table_name)
    INTO episodic_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = episodic_result_dataset_id;

  xref_matching_sql := public.build_temporal_allocation_xref_sql(input_dataset_id, input_dataset_version, xref_dataset_id, xref_dataset_version, 'MONTHLY');

  -- build list of months to process
  FOR month_num IN EXTRACT(MONTH FROM start_day)..EXTRACT(MONTH FROM end_day) LOOP
    IF LENGTH(month_num_sql) > 0 THEN
      month_num_sql := month_num_sql || ',';
      month_name_sql := month_name_sql || ',';
    END IF;
    month_num_sql := month_num_sql || month_num;
    CASE month_num
      WHEN 1 THEN
        month_name_sql := month_name_sql || 'january';
      WHEN 2 THEN
        month_name_sql := month_name_sql || 'february';
      WHEN 3 THEN
        month_name_sql := month_name_sql || 'march';
      WHEN 4 THEN
        month_name_sql := month_name_sql || 'april';
      WHEN 5 THEN
        month_name_sql := month_name_sql || 'may';
      WHEN 6 THEN
        month_name_sql := month_name_sql || 'june';
      WHEN 7 THEN
        month_name_sql := month_name_sql || 'july';
      WHEN 8 THEN
        month_name_sql := month_name_sql || 'august';
      WHEN 9 THEN
        month_name_sql := month_name_sql || 'september';
      WHEN 10 THEN
        month_name_sql := month_name_sql || 'october';
      WHEN 11 THEN
        month_name_sql := month_name_sql || 'november';
      WHEN 12 THEN
        month_name_sql := month_name_sql || 'december';
    END CASE;
  END LOOP;

  -- calculate monthly totals from annual emissions
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
         inv_dataset_id,
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
         unnest(array[' || month_name_sql || ']),
         unnest(array[' || month_num_sql || ']),
         ' || inv_emissions || ' * unnest(array[' || month_name_sql || ']),
         public.get_days_in_month(unnest(array[' || month_num_sql || '])::smallint, ' || inventory_year || '::smallint),
         inv.dataset_id,
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

  IF resolution LIKE '%month%' THEN
    RETURN;
  END IF;

  -- now do monthly to daily using monthly results just created

  xref_matching_sql := public.build_temporal_allocation_xref_sql(input_dataset_id, input_dataset_version, xref_dataset_id, xref_dataset_version, 'WEEKLY');

  loop_date := start_day;
  FOR month_num IN EXTRACT(MONTH FROM start_day)..EXTRACT(MONTH FROM end_day) LOOP

    -- build list of days to process for current month
    dates_sql := '';
    day_name_sql := '';
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
          day_name_sql := day_name_sql || ',';
        END IF;
        
        dates_sql := dates_sql || to_char(loop_date, 'YYYYMMDD');
        day_name_sql := day_name_sql || to_char(loop_date, 'day');
      END IF;
      
      loop_date := loop_date + 1;
      EXIT WHEN loop_date > end_day OR EXTRACT(MONTH FROM loop_date) != month_num;
    END LOOP;

    -- skip month if no days to process
    IF LENGTH(dates_sql) = 0 THEN
      CONTINUE;
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
           ''WEEKLY'',
           prof.profile_id,
           unnest(array[' || day_name_sql || ']),
           unnest(array[' || dates_sql || ']),
           monthly.avg_day_emis * 7 * unnest(array[' || day_name_sql || ']),
           inv.dataset_id,
           inv.record_id
      FROM emissions.' || inventory_table_name || ' inv
      JOIN (' || xref_matching_sql || ') xref
        ON xref.record_id = inv.record_id
      JOIN emissions.' || weekly_profile_table_name || ' prof
        ON prof.profile_id = xref.profile_id
      JOIN emissions.' || monthly_result_table_name || ' monthly
        ON monthly.inv_dataset_id = inv.dataset_id
       AND monthly.inv_record_id = inv.record_id
     WHERE ' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || '
       AND ' || public.build_version_where_filter(weekly_profile_dataset_id, weekly_profile_dataset_version, 'prof') || '
       AND monthly.month = ' || month_num;
  END LOOP;
  
  IF resolution LIKE '%daily total%' THEN
    RETURN;
  END IF;
  
  -- sum daily totals and calculate average-day values
  EXECUTE '
  INSERT INTO emissions.' || episodic_result_table_name || '(
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         total_emis,
         days_in_episode,
         avg_day_emis,
         inv_dataset_id,
         inv_record_id
  )
  SELECT ' || episodic_result_dataset_id || ',
         result.poll,
         result.scc,
         result.fips,
         result.plantid,
         result.pointid,
         result.stackid,
         result.processid,
         SUM(result.total_emis),
         COUNT(result.record_id),
         SUM(result.total_emis) / COUNT(result.record_id),
         result.inv_dataset_id,
         result.inv_record_id
    FROM emissions.' || daily_result_table_name || ' result
GROUP BY result.inv_dataset_id, result.inv_record_id, result.poll,
         result.scc,
         result.fips,
         result.plantid,
         result.pointid,
         result.stackid,
         result.processid';
  
END;
$$ LANGUAGE plpgsql;
