CREATE OR REPLACE FUNCTION public.populate_sources_table(
	inv_table_name varchar(64),
	inv_filter text
) RETURNS void AS $$
DECLARE
	is_point_table boolean := false;

	--support for flat file ds types...
	is_orl boolean := false;
	is_ff10 boolean := false;
	fips_expression character varying(64) := 'fips';
	plantid_expression character varying(64) := 'plantid';
	pointid_expression character varying(64) := 'pointid';
	stackid_expression character varying(64) := 'stackid';
	segment_expression character varying(64) := 'segment';
BEGIN
--	is_orl := public.check_table_for_columns(inv_table_name, 'fips', ',');
	is_ff10 := public.check_table_for_columns(inv_table_name, 'region_cd', ',');

	--if Flat File 2010 Types then change primary key field expression variables...
	IF is_ff10 THEN
		fips_expression := 'region_cd';
		plantid_expression := 'facility_id';
		pointid_expression := 'unit_id';
		stackid_expression := 'rel_point_id';
		segment_expression := 'process_id';
	ELSE
		fips_expression := 'fips';
		plantid_expression := 'plantid';
		pointid_expression := 'pointid';
		stackid_expression := 'stackid';
		segment_expression := 'segment';
	END If;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, '' || plantid_expression || ',' || pointid_expression || ',' || stackid_expression || ',' || segment_expression || '', ',');

	execute 
	'insert into emf.sources (scc, fips ' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ', source)
	select distinct on (inv.scc, inv.' || fips_expression || '' || case when is_point_table = false then '' else ', inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || '' end || ')
		inv.scc, inv.' || fips_expression || ' ' || case when is_point_table = false then '' else ', inv.' || plantid_expression || ', inv.' || pointid_expression || ', inv.' || stackid_expression || ', inv.' || segment_expression || '' end || ',
		inv.scc || inv.' || fips_expression || ' || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.' || plantid_expression || ', ''''), 15) || rpad(coalesce(inv.' || pointid_expression || ', ''''), 15) || rpad(coalesce(inv.' || stackid_expression || ', ''''), 15) || rpad(coalesce(inv.' || segment_expression || ', ''''), 15)' end || '
	FROM emissions.' || inv_table_name || ' inv
	where not exists (select 1 
		from emf.sources 
		where sources.source = inv.scc || inv.' || fips_expression || ' || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.' || plantid_expression || ', ''''), 15) || rpad(coalesce(inv.' || pointid_expression || ', ''''), 15) || rpad(coalesce(inv.' || stackid_expression || ', ''''), 15) || rpad(coalesce(inv.' || segment_expression || ', ''''), 15)' end || '
		)
/*
		left outer join emf.sources
		on sources.source = inv.scc || inv.' || fips_expression || ' || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.' || plantid_expression || ', ''''), 15) || rpad(coalesce(inv.' || pointid_expression || ', ''''), 15) || rpad(coalesce(inv.' || stackid_expression || ', ''''), 15) || rpad(coalesce(inv.' || segment_expression || ', ''''), 15)' end || '
		sources.scc = inv.scc
		and sources.fips = inv.' || fips_expression || '
		' || case when is_point_table = false then '
		and sources.plantid is null
		and sources.pointid is null
		and sources.stackid is null
		and sources.segment is null
		' else '
		and sources.plantid = inv.' || plantid_expression || '
		and sources.pointid = inv.' || pointid_expression || '
		and sources.stackid = inv.' || stackid_expression || '
		and sources.segment = inv.' || segment_expression || '
		' end || '

		 where sources.id is null 
*/			' || case when length(coalesce(inv_filter, '')) > 0 then ' and (' || public.alias_inventory_filter(inv_filter, 'inv') || ')' else '' end || '
		';

	return;
END;
$$ LANGUAGE plpgsql;


--select public.populate_sources_table('DS_point_cap2005_epa_1697358408','version IN (0) AND dataset_id=4441');
--select public.populate_sources_table('ds_test_lc_error_at_epa_mergedorl_010217189_20110601010217206','(substring(fips,1,2)=''37'') ');analyze emf.sources;
