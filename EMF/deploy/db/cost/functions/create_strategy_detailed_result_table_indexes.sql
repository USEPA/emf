CREATE OR REPLACE FUNCTION public.create_strategy_detailed_result_table_indexes(table_name character varying)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(64) := '';
BEGIN
	table_name := lower(table_name);

	-- create source_id btree index
	IF length('source_id_' || table_name) >= 63 - 10 THEN
		index_name := 'source_id_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'source_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(source_id)';

	-- create record_id btree index
	IF length('record_id_' || table_name) >= 63 - 10 THEN
		index_name := 'record_id_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'record_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(record_id);
		ALTER TABLE emissions.' || table_name || ' CLUSTER ON ' || index_name || ';';

	-- create region_cd btree index
	IF length('region_cd_' || table_name) >= 63 - 10 THEN
		index_name := 'region_cd_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'region_cd_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(region_cd)';

	-- create scc btree index
	IF length('scc_' || table_name) >= 63 - 4 THEN
		index_name := 'scc_' || substr(table_name, 6, 63);
	ELSE
		index_name := 'scc_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(scc)';

	-- create facility_id btree index
	IF length('facility_id_' || table_name) >= 63 - 12 THEN
		index_name := 'facility_id_' || substr(table_name, 14, 63);
	ELSE
		index_name := 'facility_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(facility_id)';

	-- create unit_id btree index
	IF length('unit_id_' || table_name) >= 63 - 8 THEN
		index_name := 'unit_id_' || substr(table_name, 10, 63);
	ELSE
		index_name := 'unit_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(unit_id)';

	-- create rel_point_id btree index
	IF length('rel_point_id_' || table_name) >= 63 - 13 THEN
		index_name := 'rel_point_id_' || substr(table_name, 15, 63);
	ELSE
		index_name := 'rel_point_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(rel_point_id)';

	-- create process_id btree index
	IF length('process_id_' || table_name) >= 63 - 11 THEN
		index_name := 'process_id_' || substr(table_name, 13, 63);
	ELSE
		index_name := 'process_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(process_id)';

	-- create poll btree index
	IF length('poll_' || table_name) >= 63 - 5 THEN
		index_name := 'poll_' || substr(table_name, 7, 63);
	ELSE
		index_name := 'poll_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(poll)';

	-- create cm_id btree index
	IF length('cm_id_' || table_name) >= 63 - 6 THEN
		index_name := 'cm_id_' || substr(table_name, 8, 63);
	ELSE
		index_name := 'cm_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(cm_id)';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.create_strategy_detailed_result_table_indexes(character varying) OWNER TO emf;
