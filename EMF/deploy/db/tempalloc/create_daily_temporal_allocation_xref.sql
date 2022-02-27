-- SELECT public.create_daily_temporal_allocation_xref(3, 11, 0);

CREATE OR REPLACE FUNCTION public.create_daily_temporal_allocation_xref(
  temporal_allocation_id integer,
  inv_dataset_id integer,
  inv_dataset_version integer
  ) RETURNS void AS $$
DECLARE
  use_daily_profile boolean := false;
  profile_type varchar := 'WEEKLY';

  inv_is_point_table boolean := false;

  inv_table_name varchar(64) := '';
  inv_dataset_type_name varchar(255) := '';
  
  xref_dataset_id integer;
  xref_dataset_version integer;
  xref_table_name varchar(64) := '';
  xref_dataset_filter_sql text := '';
  
  inv_dataset_filter_sql text := '';
  sql text := '';
  
  inv_fips varchar(64) := 'inv.fips';
  inv_fips_exp varchar(255) := '';
  inv_plantid varchar(64) := 'inv.plantid';
  inv_pointid varchar(64) := 'inv.pointid';
  inv_stackid varchar(64) := 'inv.stackid';
  inv_processid varchar(64) := 'inv.segment';
BEGIN

  -- get the inventory table name
  SELECT LOWER(i.table_name)
    INTO inv_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = inv_dataset_id;

  -- check if inventory has point source columns
  inv_is_point_table := 
    public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',') or
    public.check_table_for_columns(inv_table_name, 'facility_id,unit_id,rel_point_id,process_id', ',');

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
  
  -- pad inventory region code to match cross-reference
  inv_fips_exp := '
    CASE 
      WHEN LENGTH(' || inv_fips || ') = 5 THEN ''0'' || ' || inv_fips || '
      ELSE ' || inv_fips || '
    END';
  
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
  
  -- get the cross-reference and profile dataset ids and versions
  SELECT ta.xref_dataset_id,
         ta.xref_dataset_version,
         ta.daily_profile_dataset_id IS NOT NULL
    INTO xref_dataset_id,
         xref_dataset_version,
         use_daily_profile
    FROM emf.temporal_allocation ta
   WHERE ta.id = temporal_allocation_id;
   
  IF use_daily_profile THEN
    profile_type = 'DAILY';
  END IF;

  -- get the cross-reference table name
  SELECT LOWER(i.table_name)
    INTO xref_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = xref_dataset_id;
  
  -- build the cross-reference version filtering clause
  SELECT public.build_version_where_filter(xref_dataset_id, xref_dataset_version, 'xref') 
    INTO xref_dataset_filter_sql;
  
  EXECUTE '
  DROP TABLE IF EXISTS temp_alloc_xref;
  CREATE TEMP TABLE temp_alloc_xref (record_id INTEGER NOT NULL, profile_id VARCHAR(15) NOT NULL);
  CREATE INDEX record_id ON temp_alloc_xref (record_id);';
  
  -- use WITH clause so filtered inventory can be used throughout query
  sql := '
    WITH inv AS (
      SELECT *
        FROM emissions.' || inv_table_name || ' inv
       WHERE ' || inv_dataset_filter_sql || ')';

  -- add dummy SELECT to avoid issues with UNIONs in subsequent statements
  sql := sql || '
  SELECT null AS record_id, null AS profile_id, null AS ranking
   WHERE 1 = 0';

  IF inv_is_point_table THEN
    -- 1. Country/state/county code, SCC, plant ID, point ID, stack ID, segment, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 1 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || '
       AND  xref.stackid = ' || inv_stackid || '
       AND  xref.processid = ' || inv_processid || '
       AND  xref.poll = inv.poll)
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NOT NULL
       AND xref.processid IS NOT NULL
       AND xref.poll IS NOT NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 2. Country/state/county code, SCC, plant ID, point ID, stack ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 2 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || '
       AND  xref.stackid = ' || inv_stackid || '
       AND  xref.poll = inv.poll)
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NOT NULL
       AND xref.processid IS NULL
       AND xref.poll IS NOT NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 3. Country/state/county code, SCC, plant ID, point ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 3 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || '
       AND  xref.poll = inv.poll)
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NULL
       AND xref.processid IS NULL
       AND xref.poll IS NOT NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 4. Country/state/county code, SCC, plant ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 4 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.poll = inv.poll)
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NULL
       AND xref.stackid IS NULL
       AND xref.processid IS NULL
       AND xref.poll IS NOT NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 5. Country/state/county code, SCC, plant ID, point ID, stack ID, and segment
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 5 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || '
       AND  xref.stackid = ' || inv_stackid || '
       AND  xref.processid = ' || inv_processid || ')
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NOT NULL
       AND xref.processid IS NOT NULL
       AND xref.poll IS NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 6. Country/state/county code, SCC, plant ID, point ID, and stack ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 6 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || '
       AND  xref.stackid = ' || inv_stackid || ')
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NOT NULL
       AND xref.processid IS NULL
       AND xref.poll IS NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 7. Country/state/county code, SCC, plant ID, and point ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 7 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || '
       AND  xref.pointid = ' || inv_pointid || ')
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NOT NULL
       AND xref.stackid IS NULL
       AND xref.processid IS NULL
       AND xref.poll IS NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 8. Country/state/county code, SCC, and plant ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 8 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.scc = inv.scc
       AND  xref.plantid = ' || inv_plantid || ')
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NOT NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NULL
       AND xref.stackid IS NULL
       AND xref.processid IS NULL
       AND xref.poll IS NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';

    -- 9. Country/state/county code and plant ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, 9 AS ranking
      FROM emissions.' || xref_table_name || ' xref
      JOIN inv
        ON (xref.fips = ' || inv_fips_exp || '
       AND  xref.plantid = ' || inv_plantid || ')
     WHERE xref.fips IS NOT NULL
       AND xref.scc IS NULL
       AND xref.plantid IS NOT NULL
       AND xref.pointid IS NULL
       AND xref.stackid IS NULL
       AND xref.processid IS NULL
       AND xref.poll IS NULL
       AND ' || xref_dataset_filter_sql || '
       AND xref.profile_type = ''' || profile_type || '''';
  END IF;

  -- 10. Country/state/county code, SCC, and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 10 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (xref.fips = ' || inv_fips_exp || '
     AND  xref.scc = inv.scc
     AND  xref.poll = inv.poll)
   WHERE xref.fips IS NOT NULL
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NOT NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 11. Country/state code, SCC, and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 11 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (SUBSTR(xref.fips, 1, 3) = SUBSTR(' || inv_fips_exp || ', 1, 3)
     AND  xref.scc = inv.scc
     AND  xref.poll = inv.poll)
   WHERE xref.fips IS NOT NULL
     AND SUBSTR(xref.fips, 4, 3) = ''000''
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NOT NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 12. SCC and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 12 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (xref.scc = inv.scc
     AND  xref.poll = inv.poll)
   WHERE xref.fips IS NULL
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NOT NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 13. Country/state/county code and SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 13 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (xref.fips = ' || inv_fips_exp || '
     AND  xref.scc = inv.scc)
   WHERE xref.fips IS NOT NULL
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 14. Country/state code and SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 14 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (SUBSTR(xref.fips, 1, 3) = SUBSTR(' || inv_fips_exp || ', 1, 3)
     AND  xref.scc = inv.scc)
   WHERE xref.fips IS NOT NULL
     AND SUBSTR(xref.fips, 4, 3) = ''000''
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 15. SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 15 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (xref.scc = inv.scc)
   WHERE xref.fips IS NULL
     AND xref.scc IS NOT NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 16. Country/state/county code
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 16 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (xref.fips = ' || inv_fips_exp || ')
   WHERE xref.fips IS NOT NULL
     AND xref.scc IS NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';
  
  -- 17. Country/state code
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, 17 AS ranking
    FROM emissions.' || xref_table_name || ' xref
    JOIN inv
      ON (SUBSTR(xref.fips, 1, 3) = SUBSTR(' || inv_fips_exp || ', 1, 3))
   WHERE xref.fips IS NOT NULL
     AND SUBSTR(xref.fips, 4, 3) = ''000''
     AND xref.scc IS NULL
     AND xref.plantid IS NULL
     AND xref.pointid IS NULL
     AND xref.stackid IS NULL
     AND xref.processid IS NULL
     AND xref.poll IS NULL
     AND ' || xref_dataset_filter_sql || '
     AND xref.profile_type = ''' || profile_type || '''';

  -- return matches with best ranking
  sql := '
  SELECT DISTINCT ON (tbl.record_id)
         tbl.record_id, tbl.profile_id
    FROM (' || sql || ') tbl
   ORDER BY tbl.record_id, tbl.ranking';

  EXECUTE '
  INSERT INTO temp_alloc_xref (record_id, profile_id) ' || sql || ';
  ANALYZE temp_alloc_xref;';
END;
$$ LANGUAGE plpgsql;
