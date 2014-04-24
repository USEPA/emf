-- Function: emf.create_orl_table_indexes(table_name character varying)

-- DROP FUNCTION emf.create_orl_table_indexes(table_name character varying);

CREATE OR REPLACE FUNCTION public.create_orl_table_indexes(table_name character varying)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(64) := '';
	is_point_table boolean := false;
	has_poll_column boolean := false;
	has_scc_column boolean := false;
	has_mact_column boolean := false;
	has_sic_column boolean := false;
BEGIN

	-- see if there are point specific columns to be indexed
	is_point_table := public.check_table_for_columns(table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there are poll column to be indexed
	has_poll_column := public.check_table_for_columns(table_name, 'poll', ',');

	-- see if there are scc column to be indexed
	has_scc_column := public.check_table_for_columns(table_name, 'scc', ',');

	-- see if there is a mact column in the inventory
	has_mact_column := public.check_table_for_columns(table_name, 'mact', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(table_name, 'sic', ',');
					
	-- Create Indexes....

	-- create record_id btree index
	IF length('recordid_' || table_name) >= 63 - 9 THEN
		index_name := 'recordid_' || substr(table_name, 11, 63);
	ELSE
		index_name := 'recordid_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(record_id)';

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

	IF has_poll_column THEN
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
	END IF;

	IF has_scc_column THEN
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
	END IF;

	-- add point specific indexes--plantid, pointid, stackid, segment
	IF is_point_table THEN
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
	END IF;

	IF has_mact_column THEN
		-- create poll btree index
		IF length('poll_' || table_name) >= 63 - 5 THEN
			index_name := 'mact_' || substr(table_name, 7, 63);
		ELSE
			index_name := 'mact_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(mact)';
	END IF;

	IF has_sic_column THEN
		-- create poll btree index
		IF length('sic_' || table_name) >= 63 - 4 THEN
			index_name := 'sic_' || substr(table_name, 6, 63);
		ELSE
			index_name := 'sic_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(sic)';
	END IF;

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.create_orl_table_indexes(table_name character varying) OWNER TO emf;

--select public.create_orl_table_indexes('ds_ptinv_ptnonipm_2020cc_1068478967');vacuum analyze emissions.ds_ptinv_ptnonipm_2020cc_1068478967