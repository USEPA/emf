-- Function: emf.create_orl_table_indexes(table_name character varying)

-- DROP FUNCTION emf.create_orl_table_indexes(table_name character varying);
-- select public.create_flatfile10_table_indexes('sff_2012011302350936');
-- select length('rel_point_id_sff_20120113023540260');

CREATE OR REPLACE FUNCTION public.create_flatfile10_table_indexes(table_name character varying)
  RETURNS text AS
$BODY$
DECLARE
	index_name varchar(64) := '';
	sql text := '';
BEGIN

	-- Create Indexes....

	-- create region_cd btree index
	IF length('region_cd_' || table_name) >= 63 - 10 THEN
		index_name := 'region_cd_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'region_cd_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(region_cd);' || E'\n';

	-- create region_cd btree index
	IF length('country_cd_' || table_name) >= 63 - 11 THEN
		index_name := 'country_cd_' || substr(table_name, 13, 63);
	ELSE
		index_name := 'country_cd_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(country_cd);' || E'\n';

	-- create region_cd btree index
	IF length('tribal_code_' || table_name) >= 63 - 12 THEN
		index_name := 'tribal_code_' || substr(table_name, 14, 63);
	ELSE
		index_name := 'tribal_code_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(tribal_code);' || E'\n';

	-- create poll btree index
	IF length('poll_' || table_name) >= 63 - 5 THEN
		index_name := 'poll_' || substr(table_name, 7, 63);
	ELSE
		index_name := 'poll_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(poll);' || E'\n';

	-- create scc btree index
	IF length('scc_' || table_name) >= 63 - 4 THEN
		index_name := 'scc_' || substr(table_name, 6, 63);
	ELSE
		index_name := 'scc_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(scc);' || E'\n';

	-- create emis_type btree index
	IF length('emis_type_' || table_name) >= 63 - 10 THEN
		index_name := 'emis_type_' || substr(table_name, 12, 63);
	ELSE
		index_name := 'emis_type_' || table_name;
	END IF;
	sql := sql ||  'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(emis_type);' || E'\n';

	-- add point specific indexes--plantid, pointid, stackid, segment
	IF public.check_table_for_columns(table_name, 'facility_id,unit_id,rel_point_id,process_id', ',') THEN
		-- create facility_id btree index
		IF length('facility_id_' || table_name) >= 63 - 11 THEN
			index_name := 'facility_id_' || substr(table_name, 13, 63);
		ELSE
			index_name := 'facility_id_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(facility_id);' || E'\n';

		-- create unit_id btree index
		IF length('unit_id_' || table_name) >= 63 - 8 THEN
			index_name := 'unit_id_' || substr(table_name, 10, 63);
		ELSE
			index_name := 'unit_id_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(unit_id);' || E'\n';

		-- create rel_point_id btree index
		IF length('rel_point_id_' || table_name) >= 63 - 13 THEN
			index_name := 'rel_point_id_' || substr(table_name, 15, 63);
		ELSE
			index_name := 'rel_point_id_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(rel_point_id);' || E'\n';

		-- create process_id btree index
		IF length('process_id_' || table_name) >= 63 - 11 THEN
			index_name := 'process_id_' || substr(table_name, 13, 63);
		ELSE
			index_name := 'process_id_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(process_id);' || E'\n';
	END IF;

	IF public.check_table_for_columns(table_name, 'census_tract_cd', ',') THEN
		-- create census_tract_cd btree index
		IF length('census_tract_cd_' || table_name) >= 63 - 14 THEN
			index_name := 'census_tract_cd_' || substr(table_name, 16, 63);
		ELSE
			index_name := 'census_tract_cd_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(census_tract_cd);' || E'\n';
	END IF;

	IF public.check_table_for_columns(table_name, 'shape_id', ',') THEN
		-- create shape_id btree index
		IF length('shape_id_' || table_name) >= 63 - 7 THEN
			index_name := 'shape_id_' || substr(table_name, 9, 63);
		ELSE
			index_name := 'shape_id_' || table_name;
		END IF;
		sql := sql ||  'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(shape_id);' || E'\n';
	END IF;

	sql := sql ||  'vacuum analyze emissions.' || table_name || ';';
	return sql;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

--select public.create_orl_table_indexes('ds_ptinv_ptnonipm_2020cc_1068478967');vacuum analyze emissions.ds_ptinv_ptnonipm_2020cc_1068478967