--NOTES: Make sure an include the dataset types to include in the WHERE clause for both the dataset_type and dataset_types_keywords SQL statements
--	

--create dataset_type insert script
select 'insert into emf.dataset_types ("name",description,min_files,max_files,"external",default_sortorder,importer_classname,exporter_classname,lock_owner,lock_date,table_per_dataset) 
values(' || quote_literal(dt.name) || ',' || quote_literal(dt.description) || ',' || dt.min_files || ',' || dt.max_files || ',' || dt.external || ',' || quote_literal(dt.default_sortorder) || ',' || quote_literal(dt.importer_classname) || ',' || quote_literal(dt.exporter_classname) || ',null::character varying(255),null::timestamp without time zone,' || dt.table_per_dataset || ');' as insert_sql
from emf.dataset_types dt
where name in (
'Allowable Packet','Area-to-point Conversions (Line-based)','ASM Archive Report (CSV)','BEIS3 Emission Factors (Line-based)','Biogenic land use data (External)','Bioseasons data (External)','CEM annually summed data (CEMSUM)','CEM Hour-Specific Inventory (PTHOUR External Multifile)','CEM Hour-Specific Point Inventory (PTHOUR)','Chemical Speciation Combo Profiles (GSPRO_COMBO)','Chemical Speciation Cross-Reference (GSREF)','Chemical Speciation Profiles (GSPRO)','CMAQ Model Ready Emissions: Merged','CMAQ Model Ready Emissions: Sector-specific (External)','Cntlmat Projection Report (External)','Comma Separated Values (CSV)','Control Packet','Control Strategy Detailed Result','Control Strategy Least Cost Control Measure Worksheet','Control Strategy Least Cost Curve Summary','Country, state, and county names and data (COSTCY)','Daily profiles for creating daily EGUs','Day-Specific Point Inventory (PTDAY)','Day-Specific Point Inventory (PTDAY External Multifile)','Elevated source configuration (PELVCONFIG)','EMF Job Header','External File (External)','Grid Descriptions (Line-based)','Gridding Cross Reference (A/MGREF)','Growth and Controls Configurations (Line-based)','Holiday Identifications (Line-based)','IDA Activity','IDA Mobile','IDA Nonpoint/Nonroad','IDA Point','Inventory Table Data (INVTABLE)','IPM Hour-Specific Inventory (PTHOUR External Multifile)','List of Counties (CSV)','Log summary','Log summary level 1','Log summary level 3','MACT description (MACTDESC)','MCIP outputs (External)','Mechanism conversions (Line-based)','Meteorology File (External)','Mobile codes (MCODES, Line-based)','Monthly profiles for creating daily EGUs','NAICS description file (NAICSDESC)','NHAPEXCLUDE (Line-based)','NIF3.0 Nonpoint Inventory','NIF3.0 Nonroad Inventory','NIF3.0 Onroad Inventory','NIF3.0 Point Inventory','Ocean fraction (External)','Onroad Temperature Adjustment Factors (CSV)','ORIS Description (ORISDESC)','ORL Agfire Inventory (ARINV)','ORL AVGFIRE HAP (ARINV)','ORL Day-Specific Fire Data Inventory (PTDAY)','ORL Fire Inventory (PTINV)','ORL Merged Inventory','ORL Nonpoint Inventory (ARINV)','ORL Nonptfire (ARINV)','ORL Nonroad (ARINV) HAP 12 months External Multifile (multiple files per month)','ORL Nonroad Inventory, 12 months (ARINV, External Multifile)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory, 12 months (MBINV, External Multifile)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','Plant Closure (CSV)','Point-Source Stack Replacements (PSTK)','Pollutant to Pollutant Conversion (GSCNV)','Projection Packet','Report Configurations (Line-based)','SCC Descriptions (CSV)','Sector List (CSV)','Sector List Sensitivity Override (External)','Sensitivity Adjustment Factors (CSV)','Shapefile Catalog (CSV)','Shapefile (External)','SIC Descriptions (Line-based)','Smkmerge Dates (External Multifile)','Smkmerge report state annual summary (CSV)','Smkmerge Report state (External Multifile)','Smkreport cell','Smkreport county','Smkreport county daily (External Multifile)','Smkreport county-moncode annual','Smkreport county-moncode daily (External Multifile)','Smkreport county-SCC (External)','Smkreport grid cell-county (External)','Smkreport grid cell (External)','Smkreport mrggrid adjustment','Smkreport plant-cell (External)','Smkreport plant-county-ORIS (External)','Smkreport state','Smkreport state-cell (External)','Smkreport state daily (External Multifile)','Smkreport state-grid','Smkreport state-MACT','Smkreport state-MACT (External)','Smkreport state-NAICS','Smkreport state-NAICS (External)','Smkreport state-SCC','Smkreport state-SCC daily (External Multifile)','Smkreport state-scc daily (External Multifile)','Smkreport state-SCC-spec_profile','Smkreport state-SCC-srgcode','Smkreport state-SIC','Smkreport state-SIC (External)','SMOKE Report','SMOKE Report (External)','SMOKE Report (External Multifile)','SMOKE time log (External)','Spatial Surrogates (A/MGPRO)','Spatial Surrogates (External Multifile)','Speciation Tool carbons','Speciation Tool control file','Speciation Tool gas profiles','Speciation Tool gas species map','Speciation Tool mechanism','Speciation Tool mechanism description','Speciation Tool PM profiles','Speciation Tool PM species map','Speciation Tool profile-process list','Speciation Tool profile weights','Speciation Tool species info','Speciation Tool static profiles','Speciation Tool toxic file','Species Tagging File (GSTAG)','Species Tagging List (CSV)','State Comparison Tolerance (CSV)','State Summary','Strategy County Summary','Strategy Impact Summary','Strategy Measure Summary','Strategy Messages (CSV)','Surrogate Code Mapping (CSV)','Surrogate Code Mapping (Line- based)','Surrogate Descriptions (SRGDESC)','Surrogate Specifications (CSV)','Surrogate Tool Control Variables (CSV)','Surrogate Tool Generation Controls (CSV)','Temporal Cross Reference (A/M/PTREF)','Temporal Profile (A/M/PTPRO)','Text file (Line-based)'
)

union all
--create keywords insert script
select 'insert into emf.keywords ("name") 
values(' || quote_literal(kw.name) || ');' as insert_sql
from emf.keywords kw

union all
--create emf.dataset_types_keywords insert script
select 'insert into emf.dataset_types_keywords (dataset_type_id, list_index, keyword_id, "value")
select (select id from emf.dataset_types where "name" = ' || quote_literal(dt."name") || ') as dataset_type_id,
  (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_keywords where dataset_type_id = (select id from emf.dataset_types where "name" = ' || quote_literal(dt."name") || ')) as list_index,
  (select id from emf.keywords where "name" = ' || quote_literal(kw."name") || ') as keyword_id,
  ' || quote_literal(dtkw."value") || ' as "value";' as insert_sql
from emf.dataset_types_keywords dtkw
inner join emf.keywords kw
on kw.id = dtkw.keyword_id
inner join emf.dataset_types dt
on dt.id = dtkw.dataset_type_id
where dt."name" in (
'Allowable Packet','Area-to-point Conversions (Line-based)','ASM Archive Report (CSV)','BEIS3 Emission Factors (Line-based)','Biogenic land use data (External)','Bioseasons data (External)','CEM annually summed data (CEMSUM)','CEM Hour-Specific Inventory (PTHOUR External Multifile)','CEM Hour-Specific Point Inventory (PTHOUR)','Chemical Speciation Combo Profiles (GSPRO_COMBO)','Chemical Speciation Cross-Reference (GSREF)','Chemical Speciation Profiles (GSPRO)','CMAQ Model Ready Emissions: Merged','CMAQ Model Ready Emissions: Sector-specific (External)','Cntlmat Projection Report (External)','Comma Separated Values (CSV)','Control Packet','Control Strategy Detailed Result','Control Strategy Least Cost Control Measure Worksheet','Control Strategy Least Cost Curve Summary','Country, state, and county names and data (COSTCY)','Daily profiles for creating daily EGUs','Day-Specific Point Inventory (PTDAY)','Day-Specific Point Inventory (PTDAY External Multifile)','Elevated source configuration (PELVCONFIG)','EMF Job Header','External File (External)','Grid Descriptions (Line-based)','Gridding Cross Reference (A/MGREF)','Growth and Controls Configurations (Line-based)','Holiday Identifications (Line-based)','IDA Activity','IDA Mobile','IDA Nonpoint/Nonroad','IDA Point','Inventory Table Data (INVTABLE)','IPM Hour-Specific Inventory (PTHOUR External Multifile)','List of Counties (CSV)','Log summary','Log summary level 1','Log summary level 3','MACT description (MACTDESC)','MCIP outputs (External)','Mechanism conversions (Line-based)','Meteorology File (External)','Mobile codes (MCODES, Line-based)','Monthly profiles for creating daily EGUs','NAICS description file (NAICSDESC)','NHAPEXCLUDE (Line-based)','NIF3.0 Nonpoint Inventory','NIF3.0 Nonroad Inventory','NIF3.0 Onroad Inventory','NIF3.0 Point Inventory','Ocean fraction (External)','Onroad Temperature Adjustment Factors (CSV)','ORIS Description (ORISDESC)','ORL Agfire Inventory (ARINV)','ORL AVGFIRE HAP (ARINV)','ORL Day-Specific Fire Data Inventory (PTDAY)','ORL Fire Inventory (PTINV)','ORL Merged Inventory','ORL Nonpoint Inventory (ARINV)','ORL Nonptfire (ARINV)','ORL Nonroad (ARINV) HAP 12 months External Multifile (multiple files per month)','ORL Nonroad Inventory, 12 months (ARINV, External Multifile)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory, 12 months (MBINV, External Multifile)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','Plant Closure (CSV)','Point-Source Stack Replacements (PSTK)','Pollutant to Pollutant Conversion (GSCNV)','Projection Packet','Report Configurations (Line-based)','SCC Descriptions (CSV)','Sector List (CSV)','Sector List Sensitivity Override (External)','Sensitivity Adjustment Factors (CSV)','Shapefile Catalog (CSV)','Shapefile (External)','SIC Descriptions (Line-based)','Smkmerge Dates (External Multifile)','Smkmerge report state annual summary (CSV)','Smkmerge Report state (External Multifile)','Smkreport cell','Smkreport county','Smkreport county daily (External Multifile)','Smkreport county-moncode annual','Smkreport county-moncode daily (External Multifile)','Smkreport county-SCC (External)','Smkreport grid cell-county (External)','Smkreport grid cell (External)','Smkreport mrggrid adjustment','Smkreport plant-cell (External)','Smkreport plant-county-ORIS (External)','Smkreport state','Smkreport state-cell (External)','Smkreport state daily (External Multifile)','Smkreport state-grid','Smkreport state-MACT','Smkreport state-MACT (External)','Smkreport state-NAICS','Smkreport state-NAICS (External)','Smkreport state-SCC','Smkreport state-SCC daily (External Multifile)','Smkreport state-scc daily (External Multifile)','Smkreport state-SCC-spec_profile','Smkreport state-SCC-srgcode','Smkreport state-SIC','Smkreport state-SIC (External)','SMOKE Report','SMOKE Report (External)','SMOKE Report (External Multifile)','SMOKE time log (External)','Spatial Surrogates (A/MGPRO)','Spatial Surrogates (External Multifile)','Speciation Tool carbons','Speciation Tool control file','Speciation Tool gas profiles','Speciation Tool gas species map','Speciation Tool mechanism','Speciation Tool mechanism description','Speciation Tool PM profiles','Speciation Tool PM species map','Speciation Tool profile-process list','Speciation Tool profile weights','Speciation Tool species info','Speciation Tool static profiles','Speciation Tool toxic file','Species Tagging File (GSTAG)','Species Tagging List (CSV)','State Comparison Tolerance (CSV)','State Summary','Strategy County Summary','Strategy Impact Summary','Strategy Measure Summary','Strategy Messages (CSV)','Surrogate Code Mapping (CSV)','Surrogate Code Mapping (Line- based)','Surrogate Descriptions (SRGDESC)','Surrogate Specifications (CSV)','Surrogate Tool Control Variables (CSV)','Surrogate Tool Generation Controls (CSV)','Temporal Cross Reference (A/M/PTREF)','Temporal Profile (A/M/PTPRO)','Text file (Line-based)'
)
--order by dt."name"

union all
--create emf.sectors insert script
select 'insert into emf.sectors ("name",description) 
values(' || quote_literal(s."name") || ',' || quote_literal(s.description) || ');' as insert_sql
from emf.sectors s;

/*
CREATE TABLE emf.dataset_types
(
  id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  description text NOT NULL,
  min_files integer NOT NULL DEFAULT 1,
  max_files integer NOT NULL DEFAULT 1,
  "external" boolean NOT NULL DEFAULT false,
  default_sortorder text DEFAULT ''::text,
  importer_classname character varying(255),
  exporter_classname character varying(255),
  lock_owner character varying(255),
  lock_date timestamp without time zone,
  table_per_dataset integer NOT NULL DEFAULT 1,
  CONSTRAINT dataset_types_pkey PRIMARY KEY (id),
  CONSTRAINT dataset_types_name_key UNIQUE (name)
)

CREATE TABLE emf.keywords
(
  id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  CONSTRAINT keywords_pkey PRIMARY KEY (id),
  CONSTRAINT keywords_name_key UNIQUE (name)
)

CREATE TABLE emf.dataset_types_keywords
(
  id serial NOT NULL,
  dataset_type_id integer NOT NULL,
  list_index integer,
  keyword_id integer NOT NULL,
  "value" character varying(255),
  CONSTRAINT dataset_types_keywords_pkey PRIMARY KEY (id),
  CONSTRAINT dataset_types_keywords_dataset_type_id_fkey FOREIGN KEY (dataset_type_id)
      REFERENCES emf.dataset_types (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT dataset_types_keywords_keyword_id_fkey FOREIGN KEY (keyword_id)
      REFERENCES emf.keywords (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT dataset_types_keywords_dataset_type_id_key UNIQUE (dataset_type_id, keyword_id)
)

CREATE TABLE emf.sectors
(
  id serial NOT NULL,
  "name" character varying(255) NOT NULL,
  description text,
  lock_owner character varying(255),
  lock_date timestamp without time zone,
  CONSTRAINT sectors_pkey PRIMARY KEY (id),
  CONSTRAINT sectors_name_key UNIQUE (name)
)
*/