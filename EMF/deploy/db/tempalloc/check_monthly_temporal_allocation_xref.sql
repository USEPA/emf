CREATE OR REPLACE FUNCTION public.check_monthly_temporal_allocation_xref(
  temporal_allocation_id integer,
  inv_dataset_id integer,
  inv_dataset_version integer,
  messages_dataset_id integer
  ) RETURNS void AS $$
DECLARE
  inv_table_name varchar(64) := '';
  inv_dataset_type_name varchar(255) := '';
  
  messages_table_name varchar(64) := '';
  
  profile_dataset_id integer;
  profile_dataset_version integer;
  profile_table_name varchar(64);
  
  inv_dataset_filter_sql text := '';
  
  inv_fips varchar(64) := 'inv.fips';
  inv_plantid varchar(64) := 'plantid';
  inv_pointid varchar(64) := 'pointid';
  inv_stackid varchar(64) := 'stackid';
  inv_processid varchar(64) := 'segment';
BEGIN

  -- get the inventory table name
  SELECT LOWER(i.table_name)
    INTO inv_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = inv_dataset_id;

  -- get the inventory dataset type
  SELECT dataset_types.name
    INTO inv_dataset_type_name
    FROM emf.datasets
    JOIN emf.dataset_types
      ON dataset_types.id = datasets.dataset_type
   WHERE datasets.id = inv_dataset_id;

  -- set data field names
  IF inv_dataset_type_name = 'Flat File 2010 Point' OR
     inv_dataset_type_name = 'Flat File 2010 Nonpoint' THEN
    inv_fips := 'inv.region_cd';
    inv_plantid := 'inv.facility_id';
    inv_pointid := 'inv.unit_id';
    inv_stackid := 'inv.rel_point_id';
    inv_processid := 'inv.process_id';
  END IF;
  
  -- check if inventory has point source characteristics
  IF (public.check_table_for_columns(inv_table_name, inv_plantid || ',' || inv_pointid || ',' || inv_stackid || ',' || inv_processid, ',')) THEN
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
    INTO inv_dataset_filter_sql
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
  
  -- build version info into inventory filter
  inv_dataset_filter_sql := 
    '(' || public.build_version_where_filter(inv_dataset_id, inv_dataset_version, 'inv') || ')' || 
    COALESCE(' AND ' || inv_dataset_filter_sql, '');

  -- get the messages dataset table name
  SELECT LOWER(i.table_name)
    INTO messages_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = messages_dataset_id;

  -- check for sources that don't have a profile assigned
  EXECUTE '
  INSERT INTO emissions.' || messages_table_name || ' (
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         message
  )
   SELECT ' || messages_dataset_id || ',
          inv.poll,
          inv.scc,
          ' || inv_fips || ',
          ' || inv_plantid || ',
          ' || inv_pointid || ',
          ' || inv_stackid || ',
          ' || inv_processid || ',
          ''No monthly profile assigned to source''
     FROM emissions.' || inv_table_name || ' inv
LEFT JOIN temp_alloc_xref xref
       ON xref.record_id = inv.record_id
    WHERE ' || inv_dataset_filter_sql || '
      AND xref.profile_id IS NULL';
  
  -- get the monthly profile dataset id and version
  SELECT ta.monthly_profile_dataset_id,
         ta.monthly_profile_dataset_version
    INTO profile_dataset_id,
         profile_dataset_version
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
  
  -- get profile table name
  SELECT LOWER(i.table_name)
    INTO profile_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = profile_dataset_id;
  
  -- check for profile ids that are missing
  EXECUTE '
  INSERT INTO emissions.' || messages_table_name || ' (
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         profile_id,
         message
  )
   SELECT ' || messages_dataset_id || ',
          inv.poll,
          inv.scc,
          ' || inv_fips || ',
          ' || inv_plantid || ',
          ' || inv_pointid || ',
          ' || inv_stackid || ',
          ' || inv_processid || ',
          xref.profile_id,
          ''Missing monthly profile''
     FROM emissions.' || inv_table_name || ' inv
     JOIN temp_alloc_xref xref
       ON xref.record_id = inv.record_id
LEFT JOIN emissions.' || profile_table_name || ' prof
       ON prof.profile_id = xref.profile_id
      AND ' || public.build_version_where_filter(profile_dataset_id, profile_dataset_version, 'prof') || '
    WHERE ' || inv_dataset_filter_sql || '
      AND prof.profile_id IS NULL';
END;
$$ LANGUAGE plpgsql;