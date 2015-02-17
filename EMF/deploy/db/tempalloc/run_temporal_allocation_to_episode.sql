
CREATE OR REPLACE FUNCTION public.run_temporal_allocation_to_episode(
  temporal_allocation_id integer,
  input_dataset_id integer,
  daily_result_dataset_id integer,
  episodic_result_dataset_id integer
  ) RETURNS void AS $$
DECLARE
  daily_result_table_name varchar(64) := '';
  episodic_result_table_name varchar(64) := '';
BEGIN
  
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
   WHERE result.inv_dataset_id = ' || input_dataset_id || '
GROUP BY result.inv_dataset_id, result.inv_record_id, result.poll,
         result.scc,
         result.fips,
         result.plantid,
         result.pointid,
         result.stackid,
         result.processid';
  
END;
$$ LANGUAGE plpgsql;
