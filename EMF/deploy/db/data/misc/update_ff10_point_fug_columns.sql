DO $$
DECLARE
    r record;
    has_column boolean;
BEGIN
    FOR r IN SELECT table_name 
               FROM emf.internal_sources
              WHERE dataset_id IN (
                 SELECT id 
                   FROM emf.datasets 
                  WHERE dataset_type = (
                      SELECT id 
                        FROM emf.dataset_types 
                       WHERE name = 'Flat File 2010 Point'))
    LOOP
        SELECT COUNT(*) INTO has_column
          FROM information_schema.columns
         WHERE table_schema ILIKE 'emissions'
           AND table_name ILIKE r.table_name 
           AND column_name ILIKE 'fug_width_ydim';
        IF has_column THEN
            RAISE NOTICE 'Updating table %', r.table_name;
            EXECUTE 'ALTER TABLE emissions.' || r.table_name || ' ' || 
                    'RENAME COLUMN fug_width_ydim TO fug_width_xdim';
            EXECUTE 'ALTER TABLE emissions.' || r.table_name || ' ' || 
                    'RENAME COLUMN fug_length_xdim TO fug_length_ydim';
        END IF;
    END LOOP;
END$$;
