--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0beta4
-- Dumped by pg_dump version 9.0beta4
-- Started on 2011-06-08 15:51:53

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = reference, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 21007 (class 1259 OID 354734)
-- Dependencies: 10
-- Name: facility_source_type_codes; Type: TABLE; Schema: reference; Owner: emf; Tablespace: 
--

CREATE TABLE reference.facility_source_type_codes (
    code integer NOT NULL,
    description character varying(50),
    notes character varying(255)
);


ALTER TABLE reference.facility_source_type_codes OWNER TO emf;

--
-- TOC entry 21287 (class 0 OID 354734)
-- Dependencies: 21007
-- Data for Name: facility_source_type_codes; Type: TABLE DATA; Schema: reference; Owner: emf
--

INSERT INTO facility_source_type_codes VALUES ('100', 'Airport', 'Incl facilities with aircraft, helicopter, or seaplane engine emissions.  Does NOT incl co-located facilites that support LTO engine emissions, such as terminals and maint hangers.');
INSERT INTO facility_source_type_codes VALUES ('101', 'Marine Port', '');
INSERT INTO facility_source_type_codes VALUES ('102', 'Military Base', '');
INSERT INTO facility_source_type_codes VALUES ('103', 'Municipal Waste Combustor', 'Incl those that generate electricity');
INSERT INTO facility_source_type_codes VALUES ('104', 'Landfill', '');
INSERT INTO facility_source_type_codes VALUES ('105', 'Wastewater Treatment Facility', '');
INSERT INTO facility_source_type_codes VALUES ('106', 'Oil or Gas Field (On-shore)', '');
INSERT INTO facility_source_type_codes VALUES ('107', 'Tank Battery', '');
INSERT INTO facility_source_type_codes VALUES ('108', 'Gas Plant', '');
INSERT INTO facility_source_type_codes VALUES ('109', 'Off-shore Oil or Gas Platform', '');
INSERT INTO facility_source_type_codes VALUES ('110', 'Pipeline Compressor Station', 'Incl NG Transmission; does Not incl gas field gathering or processing facilities or gas plants');
INSERT INTO facility_source_type_codes VALUES ('112', 'Mines/Quarries', 'Other than sand and gravel');
INSERT INTO facility_source_type_codes VALUES ('113', 'Gravel or Sand Plant', 'Incl portable facilities');
INSERT INTO facility_source_type_codes VALUES ('114', 'Gasoline/Diesel Service Station', '');
INSERT INTO facility_source_type_codes VALUES ('115', 'Dry Cleaner - Petroleum Solvent/laundries', '');
INSERT INTO facility_source_type_codes VALUES ('116', 'Dry Cleaners - Perchloroethylene', '');
INSERT INTO facility_source_type_codes VALUES ('117', 'Auto Body Shops & Painters', '');
INSERT INTO facility_source_type_codes VALUES ('118', 'Animal Feedlots', '');
INSERT INTO facility_source_type_codes VALUES ('119', 'Crematories - Human', '');
INSERT INTO facility_source_type_codes VALUES ('120', 'Crematories - Animal', '');
INSERT INTO facility_source_type_codes VALUES ('121', 'Hot Mix Asphalt Plant', 'Incl portable facilities');
INSERT INTO facility_source_type_codes VALUES ('123', 'Concrete Batch Plant', '');
INSERT INTO facility_source_type_codes VALUES ('124', 'Portland Cement Manufacturing', 'Incl facilities with a kiln, some of which burn Haz Waste as part of their fuel');
INSERT INTO facility_source_type_codes VALUES ('125', 'Electricity Generation via Combustion', 'Incl facilities that are primarily fossil fuels, wood, biomass, LFG, POTW digester gas.  Does NOT incl MWCs, nuclear, wind, solar');
INSERT INTO facility_source_type_codes VALUES ('126', 'Petroleum Refinery', '');
INSERT INTO facility_source_type_codes VALUES ('127', 'Ethanol Biorefineries', '');
INSERT INTO facility_source_type_codes VALUES ('128', 'Soy Biofuel Plant', '');
INSERT INTO facility_source_type_codes VALUES ('129', 'Chemical Manufacturing', '');
INSERT INTO facility_source_type_codes VALUES ('130', 'Pharmaceutical Manufacturing', '');
INSERT INTO facility_source_type_codes VALUES ('131', 'Bulk Terminals/Bulk Plants', '');
INSERT INTO facility_source_type_codes VALUES ('132', 'Brick Manufacturing & Structural Clay', '');
INSERT INTO facility_source_type_codes VALUES ('133', 'Pulp and Paper Plant', '');
INSERT INTO facility_source_type_codes VALUES ('134', 'Plywood, Particleboard, OSB, etc', '');
INSERT INTO facility_source_type_codes VALUES ('135', 'Lumber/sawmills', '');
INSERT INTO facility_source_type_codes VALUES ('136', 'Woodwork, Furniture, Millwork', '');
INSERT INTO facility_source_type_codes VALUES ('137', 'Automobile/Truck Manufacturing', '');
INSERT INTO facility_source_type_codes VALUES ('138', 'Steel Mill', '');
INSERT INTO facility_source_type_codes VALUES ('139', 'Iron and Steel Foundries', '');
INSERT INTO facility_source_type_codes VALUES ('140', 'Primary Metal Production', '');
INSERT INTO facility_source_type_codes VALUES ('141', 'Secondary Metal Processing', '');
INSERT INTO facility_source_type_codes VALUES ('142', 'Breweries/Distilleries/Wineries', '');
INSERT INTO facility_source_type_codes VALUES ('143', 'Bakeries', '');
INSERT INTO facility_source_type_codes VALUES ('144', 'Printing/Publishing', '');
INSERT INTO facility_source_type_codes VALUES ('145', 'Textile Mill', '');
INSERT INTO facility_source_type_codes VALUES ('146', 'Grain Handling Facility', '');
INSERT INTO facility_source_type_codes VALUES ('147', 'Food Processing Facility', '');
INSERT INTO facility_source_type_codes VALUES ('148', 'Institutional - schools, hospitals, prisons', '');
INSERT INTO facility_source_type_codes VALUES ('149', 'Coke Battery', '');


--
-- TOC entry 21286 (class 2606 OID 354741)
-- Dependencies: 21007 21007
-- Name: facility_source_type_codes_pkey; Type: CONSTRAINT; Schema: reference; Owner: emf; Tablespace: 
--

ALTER TABLE ONLY facility_source_type_codes
    ADD CONSTRAINT facility_source_type_codes_pkey PRIMARY KEY (code);


-- Completed on 2011-06-08 15:51:57

--
-- PostgreSQL database dump complete
--

CREATE INDEX idx_facility_source_type_codes_code
  ON reference.facility_source_type_codes
  USING btree
  (code);

