CREATE OR REPLACE FUNCTION public.run_temporal_allocation(
  temporal_allocation_id integer,
  input_dataset_id integer,
  input_dataset_version integer
  ) RETURNS void AS $$
DECLARE
  inventory_table_name varchar(64) := '';
  inventory_dataset_type_name varchar(255) := '';

  detailed_result_dataset_id integer := null;
  detailed_result_table_name varchar(64) := '';
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
  
  -- get the detailed result dataset info
  SELECT ta.detailed_result_dataset_id,
         LOWER(i.table_name)
    INTO detailed_result_dataset_id,
         detailed_result_table_name
    FROM emf.temporal_allocation ta
    JOIN emf.internal_sources i
      ON i.dataset_id = ta.detailed_result_dataset_id
   WHERE ta.id = temporal_allocation_id;

  -- initial case
  --   input is ORL annual inventory
  --   output is monthly totals
  --   use default profile (emissions * 1/12)
  INSERT INTO emissions.detailed_result_table_name (
         dataset_id,
         poll,
         scc,
         fips,
         plantid,
         pointid,
         stackid,
         processid,
         record_type,
         profile_id,
         fraction,
         month,
         total_emissions
  )
  SELECT detailed_result_dataset_id,
         inv.poll,
         inv.scc,
         inv.fips,
         inv.plantid,
         inv.pointid,
         inv.stackid,
         inv.segment,
         'month',
         0,
         0.0833 AS fraction,
         1,
         inv.ann_emis * fraction
    FROM emissions.inv_table_name inv
   WHERE public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv');
    
END;
$$ LANGUAGE plpgsql;
