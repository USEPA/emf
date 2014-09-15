CREATE OR REPLACE FUNCTION public.run_temporal_allocation_daily(
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
  
  inv_filter text := '';
  resolution varchar(64);
  inventory_year smallint;
  start_day date;
  end_day date;

  monthly_result_table_name varchar(64) := '';
  episodic_result_table_name varchar(64) := '';
  
  inv_plantid varchar(64) := 'facility_id';
  inv_pointid varchar(64) := 'unit_id';
  inv_stackid varchar(64) := 'rel_point_id';
  inv_processid varchar(64) := 'process_id';
  
  days_in_month smallint;
  column_sql text := '';
  loop_date date;
  is_weekend boolean;
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
  
  -- get the monthly result dataset info
  SELECT LOWER(i.table_name)
    INTO monthly_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = monthly_result_dataset_id;
  
  -- get the episodic result dataset info
  SELECT LOWER(i.table_name)
    INTO episodic_result_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = episodic_result_dataset_id;

  IF resolution LIKE '%month%' THEN

    FOR month_num IN EXTRACT(MONTH FROM start_day)..EXTRACT(MONTH FROM end_day) LOOP

      -- build list of columns to sum for current month
      days_in_month := public.get_days_in_month(month_num::smallint, inventory_year::smallint);

      column_sql := 'inv.dayval1';
      FOR i IN 2..days_in_month LOOP
        column_sql := column_sql || ' + inv.dayval' || i;
      END LOOP;
  
--      EXECUTE '
      RAISE NOTICE '%', '
      INSERT INTO emissions.' || monthly_result_table_name || ' (
             dataset_id,
             poll,
             scc,
             fips,
             plantid,
             pointid,
             stackid,
             processid,
             month,
             total_emis,
             days_in_month,
             inv_dataset_id,
             inv_record_id
      )
      SELECT ' || monthly_result_dataset_id || ',
             inv.poll,
             inv.scc,
             inv.region_cd,
             ' || inv_plantid || ',
             ' || inv_pointid || ',
             ' || inv_stackid || ',
             ' || inv_processid || ',
             ' || month_num || ',
             ' || column_sql || ',
             ' || days_in_month || ',
             inv.dataset_id,
             inv.record_id
        FROM emissions.' || inventory_table_name || ' inv
       WHERE ' || inv_filter || '
         AND inv.monthnum = ' || month_num;
  
      -- calculate monthly average day emissions
      EXECUTE '
      UPDATE emissions.' || monthly_result_table_name || '
         SET avg_day_emis = total_emis / days_in_month';
       
    END LOOP;
      
  END IF;
  
END;
$$ LANGUAGE plpgsql;
