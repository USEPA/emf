--/usr/local/pgsql/bin/psql -U emf -d EMF -f ./update_reference_table.sql
-- Insert new records to reference.invtable on EPA sage
insert into reference.invtable ("name", "mode", cas, spec4id, react, keep, factor, voctog, species, explicit, activity, nti, units, descrptn, casdesc)
select 
	lpad(pollutant_category_name, 11, '')  as "name",
	'' as "mode",
	pollutant_code as cas,
	null as spec4id,
	null as react,
	'' as keep,
	'1' as factor,
	'' as voctog,
	'' as species,
	'' as explicit,
	'' as activity,
	null as nti,
	'tons/yr' as units,
	lpad(pollutant_category_name, 40, '') as descrptn,
	'' as casdesc
--,ds_invtable.*
from emissions.DS_EIS_pollutants_Mar2012_1217250508 polls

where pollutant_code not in (
	select cas
	from reference.invtable
	);

-- -- Insert new records to reference.pollutant_codes
insert into reference.pollutant_codes (pollutant_code, pollutant_code_desc, hap_category_name, chemical_formula, molecular_weight, active, last_modification_date, valid_v20, valid_v30, 
"comment", urban_hap_33, urban_hap_baseline_33, nato_teq, who_teq, i_teq, ozoneprecursor, pmprecursor, hap_or_cap, pollutant_rank, smoke_name, factor, spec4id, keep, voctog, species, explicit)
select
  pollutant_code as pollutant_code, 
  description as pollutant_code_desc,
  pollutant_category_name as hap_category_name,
  '' as chemical_formula,
  null as molecular_weight,
  '' as active,
  null as last_modification_date,
  '' as valid_v20,
  '' as valid_v30 ,
  '' as comment,
  null as urban_hap_33 ,
  null as urban_hap_baseline_33,
  null as nato_teq,
  null as who_teq ,
  null as i_teq ,
  null as ozoneprecursor ,
  null as pmprecursor,
  type as hap_or_cap,
  null as pollutant_rank,
  '' as smoke_name,
  1.0 as factor,
  null as spec4id,
  '' as keep,
  '' as voctog,
  '' as species,
  '' as explicit

from emissions.DS_EIS_pollutants_Mar2012_1217250508
where pollutant_code not in (
	select pollutant_code
	from reference.pollutant_codes
	);


-- add four columns to reference.states and add update data using table reference.state_county_fips_codes
alter table reference.states add column state_minlon double precision default 0;
alter table reference.states add column state_maxlon double precision default 0;
alter table reference.states add column state_minlat double precision default 0;
alter table reference.states add column state_maxlat double precision default 0;

UPDATE reference.states as st SET state_minlon = (SELECT state_minlon FROM reference.state_county_fips_codes where st_abbr = st.state_abbr group by state_minlon);
UPDATE reference.states as st SET state_maxlon = (SELECT state_maxlon FROM reference.state_county_fips_codes where st_abbr = st.state_abbr group by state_maxlon);
UPDATE reference.states as st SET state_minlat = (SELECT state_minlat FROM reference.state_county_fips_codes where st_abbr = st.state_abbr group by state_minlat);
UPDATE reference.states as st SET state_maxlat = (SELECT state_maxlat FROM reference.state_county_fips_codes where st_abbr = st.state_abbr group by state_maxlat);
