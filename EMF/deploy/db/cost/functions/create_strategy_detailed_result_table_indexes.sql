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
	IF length('record_id' || table_name) >= 63 - 10 THEN
		index_name := 'record_id_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'record_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(record_id);
		ALTER TABLE emissions.' || table_name || ' CLUSTER ON ' || index_name || ';';

	-- create fips btree index
	IF length('fips_' || table_name) >= 63 - 5 THEN
		index_name := 'fips_' || substr(table_name, 7, 63);
	ELSE
		index_name := 'fips_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(fips)';

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

	-- create plantid btree index
	IF length('plantid_' || table_name) >= 63 - 8 THEN
		index_name := 'plantid_' || substr(table_name, 10, 63);
	ELSE
		index_name := 'plantid_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(plantid)';

	-- create pointid btree index
	IF length('pointid_' || table_name) >= 63 - 8 THEN
		index_name := 'pointid_' || substr(table_name, 10, 63);
	ELSE
		index_name := 'pointid_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(pointid)';

	-- create stackid btree index
	IF length('stackid_' || table_name) >= 63 - 8 THEN
		index_name := 'stackid_' || substr(table_name, 10, 63);
	ELSE
		index_name := 'stackid_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(stackid)';

	-- create segment btree index
	IF length('segment_' || table_name) >= 63 - 8 THEN
		index_name := 'segment_' || substr(table_name, 10, 63);
	ELSE
		index_name := 'segment_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(segment)';

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
