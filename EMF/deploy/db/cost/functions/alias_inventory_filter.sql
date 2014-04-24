CREATE OR REPLACE FUNCTION public.alias_inventory_filter(filter text, table_alias varchar)
  RETURNS text AS
$BODY$
DECLARE
BEGIN

	RETURN 
	--select regexp_replace('chaka dinosaur like diNosaur holly sleaze dinosAur stack river dInosaur will farrel', 'dinosaUr', 'inv.dinosaur', 'gi');
	regexp_replace(
		regexp_replace(
			regexp_replace(
				regexp_replace(
					regexp_replace(
						regexp_replace(
							regexp_replace(
								regexp_replace(
									regexp_replace(
										regexp_replace(
											regexp_replace(
												regexp_replace(
													filter, 
												'avd_emis', table_alias || '.avd_emis', 'gi'), 
											'ann_emis', table_alias || '.ann_emis', 'gi'), 
										'scc', table_alias || '.scc', 'gi'), 
									'fips', table_alias || '.fips', 'gi'), 
								'plantid', table_alias || '.plantid', 'gi'), 
							'pointid', table_alias || '.pointid', 'gi'), 
						'stackid', table_alias || '.stackid', 'gi'), 
					'segment', table_alias || '.segment', 'gi'),
				'sic', table_alias || '.sic', 'gi'),
			'mact', table_alias || '.mact', 'gi'),
		'naics', table_alias || '.naics', 'gi'),
	'poll', table_alias || '.poll', 'gi');

END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE
  COST 100;
ALTER FUNCTION public.alias_inventory_filter(text, varchar) OWNER TO emf;
