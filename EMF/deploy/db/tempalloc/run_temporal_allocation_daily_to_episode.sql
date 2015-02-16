CREATE OR REPLACE FUNCTION public.run_temporal_allocation_daily_to_episode(
  temporal_allocation_id integer,
  input_dataset_id integer,
  input_dataset_version integer,
  episodic_result_dataset_id integer
  ) RETURNS void AS $$
DECLARE
  inventory_table_name varchar(64) := '';
  inventory_dataset_type_name varchar(255) := '';
  
  inv_filter text := '';
  resolution varchar(64);
  start_day date;
  end_day date;

  episodic_result_table_name varchar(64) := '';
  
  inv_plantid varchar(64) := 'facility_id';
  inv_pointid varchar(64) := 'unit_id';
  inv_stackid varchar(64) := 'rel_point_id';
  inv_processid varchar(64) := 'process_id';
  
  days_in_month smallint;
  column_sql text := '';
  
  episode_sql text := '';
  loop_date date;
  is_weekend boolean;
  add_day boolean;
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
  
  -- get the episodic result dataset info
  SELECT LOWER(i.table_name)
    INTO episodic_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = episodic_result_dataset_id;
  
  loop_date := start_day;
  FOR month_num IN EXTRACT(MONTH FROM start_day)..EXTRACT(MONTH FROM end_day) LOOP
  
    -- build list of columns to sum for episode dates in current month
    days_in_month := 0;
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
        days_in_month := days_in_month + 1;
        
        IF LENGTH(column_sql) > 0 THEN
          column_sql := column_sql || ' + ';
        END IF;
        
        column_sql := column_sql || 'inv.dayval' || to_char(loop_date, 'FMDD');
      END IF;
    
      loop_date := loop_date + 1;
      EXIT WHEN loop_date > end_day OR EXTRACT(MONTH FROM loop_date) != month_num;
    END LOOP;
    
    -- skip month if no days to process
    IF LENGTH(column_sql) = 0 THEN
      CONTINUE;
    END IF;
  
    IF LENGTH(episode_sql) > 0 THEN
      episode_sql := episode_sql || ' UNION ';
    END IF;
  
    episode_sql := episode_sql || '
      SELECT inv.poll,
             inv.scc,
             inv.region_cd AS fips,
             ' || inv_plantid || ' AS plantid,
             ' || inv_pointid || ' AS pointid,
             ' || inv_stackid || ' AS stackid,
             ' || inv_processid || ' AS processid,
             ' || column_sql || ' AS emis,
             ' || days_in_month || ' AS days,
             inv.dataset_id
        FROM emissions.' || inventory_table_name || ' inv
       WHERE ' || inv_filter || '
         AND inv.monthnum = ' || month_num;
             
  END LOOP;

--   RAISE NOTICE '%', '
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
         inv_dataset_id
  )
  SELECT ' || episodic_result_dataset_id || ',
         v.poll,
         v.scc,
         v.fips,
         v.plantid,
         v.pointid,
         v.stackid,
         v.processid,
         SUM(emis),
         SUM(days),
         v.dataset_id
    FROM (' || episode_sql || ') v
GROUP BY v.dataset_id,
         v.poll,
         v.scc,
         v.fips,
         v.plantid,
         v.pointid,
         v.stackid,
         v.processid';

  -- calculate episodic average day emissions
  EXECUTE '
  UPDATE emissions.' || episodic_result_table_name || '
     SET avg_day_emis = total_emis / days_in_episode';
  
END;
$$ LANGUAGE plpgsql;
