-- Function: get_least_cost_worksheet_emis_reduction(character varying, character varying, integer)

-- DROP FUNCTION get_least_cost_worksheet_emis_reduction(character varying, character varying, integer);

CREATE OR REPLACE FUNCTION public.get_least_cost_worksheet_emis_reduction(worksheet_table_name character varying, target_pollutant character varying, target_record_offset integer)
  RETURNS double precision AS
$BODY$
DECLARE
	emis_reduction double precision;
BEGIN

	execute 'SELECT sum(emis_reduction)
	from (
		SELECT distinct on (source, original_dataset_id, source_id) emis_reduction
		from (
			SELECT emis_reduction, marginal, original_dataset_id, record_id, source, source_id, source_poll_cnt
			FROM emissions.' || worksheet_table_name || '
			where status is null 
				and poll = ' || quote_literal(target_pollutant) || '
			ORDER BY marginal, emis_reduction desc, source_poll_cnt desc, record_id
			limit ' || target_record_offset || '
		) tbl
		ORDER BY source, original_dataset_id, source_id, marginal, emis_reduction desc, source_poll_cnt desc, record_id
	) tbl' 
	into emis_reduction;

	RETURN coalesce(emis_reduction,0.0);
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE
  COST 100;
ALTER FUNCTION public.get_least_cost_worksheet_emis_reduction(character varying, character varying, integer) OWNER TO postgres;
