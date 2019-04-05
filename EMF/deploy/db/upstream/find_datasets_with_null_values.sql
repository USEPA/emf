DROP FUNCTION IF EXISTS find_datasets_with_null_values();
CREATE OR REPLACE FUNCTION find_datasets_with_null_values()
RETURNS TABLE (dataset_name TEXT, dataset_type TEXT, columns TEXT, module_name TEXT, module_type_name TEXT, module_type_version TEXT) AS $$
DECLARE
  dst_row RECORD;
  ds_row RECORD;
  col_row RECORD;
  rec_count INT;
  col_names TEXT;
  module_id INT;
BEGIN

  -- fetch dataset types that have mandatory columns
  FOR dst_row IN SELECT id, name FROM emf.dataset_types WHERE file_format IN (SELECT DISTINCT file_format_id FROM emf.fileformat_columns WHERE mandatory) ORDER BY name LOOP
  
    -- fetch datasets for the current type
    <<dataset_loop>>
    FOR ds_row IN EXECUTE 'SELECT dataset_id, datasets.name, SUBSTR(table_name, 0, 64) table_name FROM emf.internal_sources JOIN emf.datasets ON datasets.id = dataset_id WHERE status = ''Created by Module Runner'' AND dataset_type = $1 ORDER BY datasets.name' USING dst_row.id LOOP
    
      col_names := '';
      
      -- get list of mandatory columns for the dataset type
      FOR col_row IN EXECUTE 'SELECT fileformat_columns.name FROM emf.fileformat_columns JOIN emf.dataset_types ON file_format_id = file_format WHERE mandatory AND dataset_types.id = $1' USING dst_row.id LOOP
      
        -- check if any columns are null
        EXECUTE 'SELECT COUNT(*) FROM emissions.' || ds_row.table_name || ' WHERE ' || col_row.name || ' IS NULL' INTO rec_count;
        
        IF rec_count > 0 THEN
          col_names := col_names || col_row.name || ' ';
        END IF;
      
      END LOOP;
      
      IF col_names != '' THEN
        dataset_name := ds_row.name;
        dataset_type := dst_row.name;
        columns := col_names;
        
        -- find module that created the dataset
        EXECUTE 'SELECT module_id, placeholder_name FROM modules.history JOIN modules.history_datasets ON history_id = history.id WHERE dataset_id = $1' USING ds_row.dataset_id INTO module_id;
        
        EXECUTE 'SELECT modules.name, module_types.name, version FROM modules.modules JOIN modules.module_types_versions ON module_type_version_id = module_types_versions.id JOIN modules.module_types ON module_type_id = module_types.id WHERE modules.id = $1' USING module_id INTO module_name, module_type_name, module_type_version;
        
        RETURN NEXT;
      END IF;
    
    END LOOP;
  
  END LOOP;
  
END;
$$ LANGUAGE plpgsql;
