CREATE OR REPLACE FUNCTION public.fix_profile_datasets() RETURNS void AS $$
DECLARE
	dataset RECORD;
BEGIN

  
	FOR dataset IN EXECUTE 
		'select datasets.id,
			datasets.name,
			internal_sources.table_name,
			dataset_types.name as dataset_type_name 
		from emf.datasets
			inner join emf.internal_sources
			on internal_sources.dataset_id = datasets.id
			inner join emf.dataset_types 
			on dataset_types.id = datasets.dataset_Type
			-- make sure the table exists
			inner join pg_class
			on lower(relname) = lower(internal_sources.table_name)
		where dataset_types.name in (''Chemical Speciation Profiles (GSPRO)'',''Chemical Speciation Combo Profiles (GSPRO_COMBO)'',''Pollutant to Pollutant Conversion (GSCNV)'')'
	LOOP
		--raise notice '%', 
IF dataset.dataset_type_name = 'Chemical Speciation Profiles (GSPRO)' THEN
	
	raise notice '%',  'ALTER TABLE emissions.' || dataset.table_name || ' ALTER code TYPE character varying(10);';

ELSIF dataset.dataset_type_name = 'Chemical Speciation Combo Profiles (GSPRO_COMBO)' THEN

	FOR i IN 1..10 LOOP
		raise notice '%',  'ALTER TABLE emissions.' || dataset.table_name || ' ALTER prof' || i || ' TYPE character varying(10);';
	END LOOP;


ELSIF dataset.dataset_type_name = 'Pollutant to Pollutant Conversion (GSCNV)' THEN

	raise notice '%',  'ALTER TABLE emissions.' || dataset.table_name || ' ALTER speciation_code TYPE character varying(10);';

END IF;

	END LOOP;

	return;
END;
$$ LANGUAGE plpgsql;

-- run populate procedure
select public.fix_profile_datasets();

