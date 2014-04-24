CREATE TABLE public.projection_shapefiles
(
  id serial NOT NULL,
  name character varying(255) NOT NULL,
  table_schema character varying(64) NOT NULL,
  table_name character varying(64) NOT NULL,
  prj_text character varying(2048),
  srid integer NOT NULL,
  "type" character varying(30) NOT NULL,
  CONSTRAINT projection_shapefiles_name_const UNIQUE (name)
);
ALTER TABLE public.projection_shapefiles OWNER TO emf;

insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'fe_2007_us_state_WGS84', 'public', 'us_state_shape_wgs84', 'GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]',4326,'state';
insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'US_counties_NAD83', 'public', 'us_county_shape_nad83', 'GEOGCS["NAD83",DATUM["North_American_Datum_1983",SPHEROID["GRS 1980",6378137,298.257222101,AUTHORITY["EPSG","7019"]],AUTHORITY["EPSG","6269"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4269"]]',4269,'county';
insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'Point WGS 84', '', '', 'GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]',4326,'point';
insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'Point NAD 83', '', '', 'GEOGCS["NAD83",DATUM["North_American_Datum_1983",SPHEROID["GRS 1980",6378137,298.257222101,AUTHORITY["EPSG","7019"]],AUTHORITY["EPSG","6269"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4269"]]',4269,'point';
insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'fe_2007_us_state_NAD83', 'public', 'us_state_shape', 'GEOGCS["GCS_North_American_1983",DATUM["D_North_American_1983",SPHEROID["GRS_1980",6378137,298.257222101]],PRIMEM["Greenwich",0],UNIT["Degree",0.017453292519943295]]',4269,'state';
insert into public.projection_shapefiles (name, table_schema, table_name, prj_text, srid, "type")
select 'US_counties_WGS84', 'public', 'us_county_shape', 'GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]]',4326,'county';
