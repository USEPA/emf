-- SELECT public.clean_temporal_allocation_xref(450);

CREATE OR REPLACE FUNCTION public.clean_temporal_allocation_xref(
  xref_dataset_id integer
  ) RETURNS void AS $$

DECLARE
  xref_table_name varchar(64) := '';

BEGIN

  -- get the cross-reference table name
  SELECT LOWER(i.table_name)
    INTO xref_table_name
    FROM emf.internal_sources i
   WHERE i.dataset_id = xref_dataset_id;

  -- pad region code to 6 digits
  EXECUTE '
  UPDATE emissions.' || xref_table_name || '
     SET fips = ''0'' || fips
   WHERE LENGTH(fips) = 5';
  
  -- replace empty values with null
  EXECUTE '
  UPDATE emissions.' || xref_table_name || '
     SET scc = CASE WHEN scc IN (''0'', ''-9'', '''') THEN NULL ELSE scc END,
         fips = CASE WHEN fips IN (''0'', ''-9'', '''') THEN NULL ELSE fips END,
         plantid = CASE WHEN plantid IN (''0'', ''-9'', '''') THEN NULL ELSE plantid END,
         pointid = CASE WHEN pointid IN (''0'', ''-9'', '''') THEN NULL ELSE pointid END,
         stackid = CASE WHEN stackid IN (''0'', ''-9'', '''') THEN NULL ELSE stackid END,
         processid = CASE WHEN processid IN (''0'', ''-9'', '''') THEN NULL ELSE processid END,
         poll = CASE WHEN poll IN (''0'', ''-9'', '''') THEN NULL ELSE poll END
   WHERE scc IN (''0'', ''-9'', '''')
      OR fips IN (''0'', ''-9'', '''')
      OR plantid IN (''0'', ''-9'', '''')
      OR pointid IN (''0'', ''-9'', '''')
      OR stackid IN (''0'', ''-9'', '''')
      OR processid IN (''0'', ''-9'', '''')
      OR poll IN (''0'', ''-9'', '''')';
END;
$$ LANGUAGE plpgsql;
