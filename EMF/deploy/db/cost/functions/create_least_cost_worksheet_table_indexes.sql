-- Function: create_least_cost_worksheet_table_indexes(character varying)

-- DROP FUNCTION public.create_least_cost_worksheet_table_indexes(character varying);

CREATE OR REPLACE FUNCTION public.create_least_cost_worksheet_table_indexes(table_name character varying(64))
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(64) := '';
	is_point_table boolean := false;
BEGIN
	table_name := lower(table_name);

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(table_name, 'plantid,pointid,stackid,segment', ',');

	-- Create Indexes....

	-- create apply_order btree index
	IF length('apply_order_' || table_name) >= 63 - 12 THEN	-- n + 1
		index_name := 'apply_order_' || substr(table_name, 14, 63); -- n + 2
	ELSE
		index_name := 'apply_order_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(apply_order)';

	-- create source btree index
	IF length('source_' || table_name) >= 63 - 7 THEN
		index_name := 'source_' || substr(table_name, 9, 63);
	ELSE
		index_name := 'source_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(scc, fips' || case when is_point_table then ', plantid, pointid, stackid, segment' else '' end || ')';

	-- create emis_reduction btree index
	IF length('emis_reduction_' || table_name) >= 63 - 15 THEN
		index_name := 'emis_reduction_' || substr(table_name, 17, 63);
	ELSE
		index_name := 'emis_reduction_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(emis_reduction)';

	-- create marginal btree index
	IF length('marginal_' || table_name) >= 63 - 9 THEN
		index_name := 'marginal_' || substr(table_name, 11, 63);
	ELSE
		index_name := 'marginal_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(marginal)';

	-- create apply_order2 btree index
	IF length('apply_order2_' || table_name) >= 63 - 13 THEN	-- n + 1
		index_name := 'apply_order2_' || substr(table_name, 15, 63); -- n + 2
	ELSE
		index_name := 'apply_order2_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(marginal, emis_reduction, record_id)';

	-- create record_id btree index
	IF length('record_id_' || table_name) >= 63 - 10 THEN
		index_name := 'record_id_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'record_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(record_id);';

	-- create source_id btree index
	IF length('source_id_' || table_name) >= 63 - 10 THEN
		index_name := 'source_id_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'source_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(source);
		ALTER TABLE emissions.' || table_name || '
			CLUSTER ON ' || index_name || ';';

/*	-- create cm_id btree index
	IF length('cm_id_' || table_name) >= 63 - 6 THEN
		index_name := 'cm_id_' || substr(table_name, 8, 63);
	ELSE
		index_name := 'cm_id_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(cm_id);';
*/
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.create_least_cost_worksheet_table_indexes(character varying) OWNER TO emf;
