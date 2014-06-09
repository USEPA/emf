-- SELECT public.build_temporal_allocation_xref_sql(11, 0, 450, 0);

CREATE OR REPLACE FUNCTION public.build_temporal_allocation_xref_sql(
  inv_dataset_id integer,
  inv_dataset_version integer,
  xref_dataset_id integer,
  xref_dataset_version integer
  ) RETURNS text AS $$
DECLARE
  inv_is_point_table boolean := false;

  inv_table_name varchar(64) := '';
  inv_dataset_type_name varchar(255) := '';
	inv_dataset_filter_sql text := '';
  xref_table_name varchar(64) := '';
  xref_dataset_filter_sql text := '';
  
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
  
  -- build the inventory version filtering clause
  SELECT public.build_version_where_filter(inv_dataset_id, inv_dataset_version, 'inv') 
    INTO inv_dataset_filter_sql;

  -- get the cross-reference table name
  SELECT LOWER(i.table_name)
    INTO xref_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = xref_dataset_id;
  
  -- build the cross-reference version filtering clause
  SELECT public.build_version_where_filter(xref_dataset_id, xref_dataset_version, 'xref') 
    INTO xref_dataset_filter_sql;
  
  -- use WITH clause so filtered inventory can be used throughout query
  sql := '
    WITH inv AS (
      SELECT *
        FROM emissions.' || inv_table_name || ' inv
       WHERE ' || inv_dataset_filter_sql || ')';

  -- add dummy SELECT to avoid issues with UNIONs in subsequent statements
  sql := sql || '
  SELECT null AS record_id, null AS profile_id, null AS profile_type, null AS ranking
   WHERE 1 = 0';

  IF inv_is_point_table THEN
    -- 1. Country/state/county code, SCC, plant ID, point ID, stack ID, segment, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 1 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 2. Country/state/county code, SCC, plant ID, point ID, stack ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 2 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 3. Country/state/county code, SCC, plant ID, point ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 3 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 4. Country/state/county code, SCC, plant ID, and pollutant
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 4 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 5. Country/state/county code, SCC, plant ID, point ID, stack ID, and segment
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 5 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 6. Country/state/county code, SCC, plant ID, point ID, and stack ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 6 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 7. Country/state/county code, SCC, plant ID, and point ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 7 AS ranking
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
       AND ' || xref_dataset_filter_sql;

    -- 8. Country/state/county code, SCC, and plant ID
    sql := sql || '
     UNION ALL
    SELECT inv.record_id, xref.profile_id, xref.profile_type, 8 AS ranking
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
       AND ' || xref_dataset_filter_sql;

  END IF;

  -- 9. Country/state/county code, SCC, and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 9 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 10. Country/state code, SCC, and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 10 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 11. SCC and pollutant
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 11 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 12. Country/state/county code and SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 12 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 13. Country/state code and SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 13 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 14. SCC
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 14 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 15. Country/state/county code
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 15 AS ranking
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
     AND ' || xref_dataset_filter_sql;
  
  -- 16. Country/state code
  sql := sql || '
   UNION ALL
  SELECT inv.record_id, xref.profile_id, xref.profile_type, 16 AS ranking
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
     AND ' || xref_dataset_filter_sql;

  RETURN sql; 
END;
$$ LANGUAGE plpgsql;
