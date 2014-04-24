--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0beta4
-- Dumped by pg_dump version 9.0beta4
-- Started on 2011-06-14 01:10:36

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
-- TOC entry 21084 (class 1259 OID 371268)
-- Dependencies: 10
-- Name: regulatory_codes; Type: TABLE; Schema: reference; Owner: emf; Tablespace: 
--

CREATE TABLE regulatory_codes (
    code character varying(255) primary key,
    description character varying(255),
    type character varying(255),
    part_description character varying(255),
    subpart_description character varying(255),
    unit_regulation character varying(255),
    process_regulation character varying(255),
    epa_note character varying(255),
    last_inventory_year character varying(255),
    map_to character varying(255)
);


ALTER TABLE reference.regulatory_codes OWNER TO emf;



--
-- TOC entry 21362 (class 0 OID 371268)
-- Dependencies: 21084
-- Data for Name: regulatory_codes; Type: TABLE DATA; Schema: reference; Owner: emf
--

INSERT INTO regulatory_codes VALUES ('R59-0001', 'Autobody Refinish Coatings (VOC Rule)', '183(e)', '59', 'B', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R59-0002', 'Consumer Products (VOC Rule)', '183(e)', '59', 'C', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R59-0003', 'Architectural Coatings (1998)', '183(e)', '59', 'D', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R59-0004', 'Aerosol Spray Paints (2008)', '183(e)', '59', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0090', 'Polyvinyl Chloride & Copolymers Production', 'AREA SOURCE', '63', 'DDDDDD', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0091', 'Primary Copper', 'AREA SOURCE', '63', 'EEEEEE', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0094', 'Secondary Copper Smelting', 'AREA SOURCE', '63', 'FFFFFF', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0095', 'Primary Nonferrous Metals', 'AREA SOURCE', '63', 'GGGGGG', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0096', 'Acrylic Fibers/Modacrylic Fibers Production', 'AREA SOURCE', '63', 'LLLLLL', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0097', 'Carbon Black Production', 'AREA SOURCE', '63', 'MMMMMM', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0098', 'Flexible Polyurethane Foam Fabrication (Area)', 'AREA SOURCE', '63', 'OOOOOO', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0099', 'Chemical Manufacturing: Chromium Compounds', 'AREA SOURCE', '63', 'NNNNNN', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0100', 'Flexible Polyurethane Foam Production (Area)', 'AREA SOURCE', '63', 'OOOOOO', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0101', 'Lead Acid Battery Manufacturing', 'AREA SOURCE', '63', 'PPPPPP', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0102', 'Wood Preserving', 'AREA SOURCE', '63', 'QQQQQQ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0103', 'Clay Ceramics Manufacturing (AREA SOURCEs)', 'AREA SOURCE', '63', 'RRRRRR', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0104', 'Pressed & Blown Glass & Glassware Manufacturing', 'AREA SOURCE', '63', 'SSSSSS', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0105', 'Secondary Nonferrous Metals', 'AREA SOURCE', '63', 'TTTTTT', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0106', 'Stainless & Non-stainless Steel Manufacturing: Electric Arc Furnaces (EAF)', 'AREA SOURCE', '63', 'YYYYY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0108', 'Iron and Steel Foundries', 'AREA SOURCE', '63', 'ZZZZZ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0109', 'Autobody Refinishing Paint Shops', 'AREA SOURCE', '63', 'HHHHHH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0110', 'Miscellaneous Coatings', 'AREA SOURCE', '63', 'HHHHHH', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0111', 'Paint Stripping', 'AREA SOURCE', '63', 'HHHHHH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0112', 'Gas Distribution Bulk Terminals, Bulk Plants, &  Pipeline Facilities; and Gasoline Dispensiing Facilities', 'AREA SOURCE', '63', 'BBBBBB', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0113', 'Metal Fabrication and Finishing', 'AREA SOURCE', '63', 'XXXXXX', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0114', 'Fabricated Metal Products Manufacturing, Not Elsewhere Classified', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0115', 'Fabricated Plate Work', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0116', 'Fabricated Structural Metal Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0117', 'Heating Equipment Manufacturing, Except Electric', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0118', 'Industrial Machinery and Equipment:  Finishing Operations', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0119', 'Iron and Steel Forging', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0120', 'Plating & Polishing', 'AREA SOURCE', '63', 'WWWWWW', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0121', 'Primary Metal Products Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0122', 'Valves and Pipe Fittings Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0113');
INSERT INTO regulatory_codes VALUES ('R63-0125', 'Cyclic Crude and Intermediate Production', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0126', 'Ferroalloys Production (Area)', 'AREA SOURCE', '63', 'YYYYYY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0127', 'Industrial Inorganic Chemical Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0128', 'Industrial Organic Chemical Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0129', 'Inorganic Pigments  Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0132', 'Plastic Materials and Resins Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0133', 'Synthetic Rubber Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0146');
INSERT INTO regulatory_codes VALUES ('R63-0135', 'Chemical Preparations', 'AREA SOURCE', '63', 'BBBBBBB', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0136', 'Aluminum, Copper, & Other Nonferrous Foundries (Area)', 'AREA SOURCE', '63', 'ZZZZZZ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0137', 'Nonferrous Foundries, Not Elsewhere Classified', 'AREA SOURCE', '63', '', 'Yes', 'No', '', '2008', 'R63-0136');
INSERT INTO regulatory_codes VALUES ('R63-0138', 'Paint & Allied Products Manufacturing', 'AREA SOURCE', '63', 'CCCCCCC', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0139', 'Prepared Feeds Manufacturing', 'AREA SOURCE', '63', 'DDDDDDD', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0144', 'Industrial, Commercial and Institutional Boilers (AREA SOURCE) MACT', 'AREA SOURCE', '63', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0146', 'Chemical Manufacturing Area Source (CMAS)', 'AREA SOURCE', '63', 'VVVVVV', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0148', 'Asphalt Processing & Roofing Manufacturing', 'AREA SOURCE', '63', 'AAAAAAA', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0149', 'Brick & Structural Clay Products Manufacturing', 'AREA SOURCE', '63', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0151', 'ONG - Oil and Natural Gas Production AREA SOURCEs', 'AREA SOURCE', '63', 'HH', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('M_0105', 'Stationary Reciprocating Internal Combustion Engines', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('M_0107', 'Industrial/Commercial/ Institutional Boilers & Process Heaters', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('M_0108', 'Stationary Combustion Turbines', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('M_0715', 'Shipbuilding & Ship Repair (Surface Coating)', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('M_0801', 'Hazardous Waste Incineration', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('M_0960', 'Agricultural Chemicals and Pesticides Manufacturing', 'MACT', '', '', 'Yes', 'No', '', '2005', 'R63-0038');
INSERT INTO regulatory_codes VALUES ('M_1348', 'Viscose Process Manufacturing', 'MACT', '', '', 'Yes', 'No', '', '2005', '');
INSERT INTO regulatory_codes VALUES ('M_1414', 'Uranium Hexafluoride Production', 'MACT', '', '', 'Yes', 'No', '', '2005', '');
INSERT INTO regulatory_codes VALUES ('M_1802', 'Municipal Waste Combustors', 'MACT', '', '', 'Yes', 'Yes', '', '2002', '');
INSERT INTO regulatory_codes VALUES ('R63-0001', 'Dry Cleaning', 'MACT', '63', 'M', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0002', 'Coke Ovens: Charging, Topside, Door Leaks', 'MACT', '63', 'L', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0003', 'Hazardous Organic NESHAP (HON)', 'MACT', '63', 'G/H/F', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0004', 'Industrial Process Cooling Towers', 'MACT', '63', 'Q', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0005', 'Chrome Electroplating:  Decorative, Hard, Chromic Acid Anodizing', 'MACT', '63', 'N', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0005-01', 'Chromic Acid Anodizing', 'MACT', '63', 'N', 'Yes', 'No', '', '2008', 'R63-0005');
INSERT INTO regulatory_codes VALUES ('R63-0005-02', 'Decorative Chromium Electroplating', 'MACT', '63', 'N', 'Yes', 'No', '', '2008', 'R63-0005');
INSERT INTO regulatory_codes VALUES ('R63-0005-03', 'Hard Chromium Electroplating', 'MACT', '63', 'N', 'Yes', 'No', '', '2008', 'R63-0005');
INSERT INTO regulatory_codes VALUES ('R63-0006', 'Halogenated Solvent Cleaners', 'MACT', '63', 'T', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0007', 'Commercial EO Sterilizers', 'MACT', '63', 'O', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0007-01', 'Commercial Sterilization Facilities', 'MACT', '63', 'O', 'Yes', 'No', '', '2008', 'R63-0007');
INSERT INTO regulatory_codes VALUES ('R63-0007-02', 'Hospital Sterilizers', 'MACT', '63', 'O', 'Yes', 'No', '', '2008', 'R63-0107');
INSERT INTO regulatory_codes VALUES ('R63-0008', 'Gasoline Distribution (Stage I)', 'MACT', '63', 'R', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0009', 'Magnetic Tape', 'MACT', '63', 'EE', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0011-001', 'Polymers and Resins II: Non-Nylon Polyamides Production', 'MACT', '63', 'W', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0011-002', 'Polymers and Resins II: Epoxy Resins Production', 'MACT', '63', 'W', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0011-01', 'Polymers and Resins II: Non-Nylon Polyamides Production', 'MACT', '63', 'W', 'Yes', 'No', '', '2008', 'R63-0011-001');
INSERT INTO regulatory_codes VALUES ('R63-0011-02', 'Polymers and Resins II: Epoxy Resins Production', 'MACT', '63', 'W', 'Yes', 'No', '', '2008', 'R63-0011-002');
INSERT INTO regulatory_codes VALUES ('R63-0012', 'Secondary Lead Smelters', 'MACT', '63', 'X', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0013', 'Petroleum Refineries I', 'MACT', '63', 'CC', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0014', 'Aerospace Coatings', 'MACT', '63', 'GG', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0015', 'Marine Tank Vessel Loading Operations', 'MACT', '63', 'Y', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0016', 'Wood Furniture Surface Coatings', 'MACT', '63', 'JJ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0017', 'Ship Building Coatings', 'MACT', '63', 'II', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0017-01', 'Shipbuilding & Ship Repair (Surface Coating)', 'MACT', '63', 'II', 'Yes', 'No', '', '2008', 'R63-0017');
INSERT INTO regulatory_codes VALUES ('R63-0017-02', 'Shipbuilding & Ship Repair (Blasting/Welding)', 'MACT', '63', 'II', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0017-03', 'Shipbuilding & Ship Repair (Other)', 'MACT', '63', 'II', 'Yes', 'No', '', '2008', 'R63-0017');
INSERT INTO regulatory_codes VALUES ('R63-0018', 'Pulp and Paper I and III', 'MACT', '63', 'S', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0018-01', 'Pulp and Paper Production - Not Otherwise Sub-Classified', 'MACT', '63', 'S', 'Yes', 'No', '', '2008', 'R63-0018');
INSERT INTO regulatory_codes VALUES ('R63-0018-02', 'Pulp and Paper Production - NonMACT Facilities', 'MACT', '63', 'S', 'Yes', 'No', '', '2008', 'R63-0018');
INSERT INTO regulatory_codes VALUES ('R63-0019', 'Printing & Publishing', 'MACT', '63', 'KK', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0020', 'Offsite Waste and Recovery Operations', 'MACT', '63', 'DD', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-001', 'Polymers and Resins I: Butyl Rubber Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-002', 'Polymers and Resins I: Epichlorohydrin Elastomers Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-003', 'Polymers and Resins I: Ethylene-Propylene Rubber Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-004', 'Polymers and Resins I: Hypalon (TM) Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-005', 'Polymers and Resins I: Neoprene Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-006', 'Polymers and Resins I: Nitrile Butadiene Rubber Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-007', 'Polymers and Resins I: Polybutadiene Rubber Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-008', 'Polymers and Resins I: Polysulfide Rubber Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-009', 'Polymers and Resins I: Styrene-Butadiene Rubber & Latex Production', 'MACT', '63', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0021-01', 'Polymers and Resins I: Butyl Rubber Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-001');
INSERT INTO regulatory_codes VALUES ('R63-0021-02', 'Polymers and Resins I: Epichlorohydrin Elastomers Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-002');
INSERT INTO regulatory_codes VALUES ('R63-0021-03', 'Polymers and Resins I: Ethylene-Propylene Rubber Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-003');
INSERT INTO regulatory_codes VALUES ('R63-0021-04', 'Polymers and Resins I: Hypalon (TM) Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-004');
INSERT INTO regulatory_codes VALUES ('R63-0021-05', 'Polymers and Resins I: Neoprene Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-005');
INSERT INTO regulatory_codes VALUES ('R63-0021-06', 'Polymers and Resins I: Nitrile Butadiene Rubber Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-006');
INSERT INTO regulatory_codes VALUES ('R63-0021-07', 'Polymers and Resins I: Polybutadiene Rubber Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-007');
INSERT INTO regulatory_codes VALUES ('R63-0021-08', 'Polymers and Resins I: Polysulfide Rubber Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-008');
INSERT INTO regulatory_codes VALUES ('R63-0021-09', 'Polymers and Resins I: Styrene-Butadiene Rubber & Latex Production', 'MACT', '63', 'U', 'Yes', 'No', '', '2008', 'R63-0021-009');
INSERT INTO regulatory_codes VALUES ('R63-0022-001', 'Polymers and Resins IV: Acrylonitrile-Butadiene-Styrene Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-002', 'Polymers and Resins IV: Methyl Methacrylate-Acrylonitrile-Butadiene-Styrene Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-003', 'Polymers and Resins IV: Methyl Methacrylate-Butadiene-Styrene Terpolymers Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-004', 'Polymers and Resins IV: Polystyrene Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-005', 'Polymers and Resins IV: Styrene Acrylonitrile Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-006', 'Polymers and Resins IV: Nitrile Resins Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-007', 'Polymers and Resins IV: Polyethylene Terephthalate Production', 'MACT', '63', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0022-01', 'Polymers and Resins IV: Acrylonitrile-Butadiene-Styrene Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-001');
INSERT INTO regulatory_codes VALUES ('R63-0022-02', 'Polymers and Resins IV: Methyl Methacrylate-Acrylonitrile-Butadiene-Styrene Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-002');
INSERT INTO regulatory_codes VALUES ('R63-0022-03', 'Polymers and Resins IV: Methyl Methacrylate-Butadiene-Styrene Terpolymers Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-003');
INSERT INTO regulatory_codes VALUES ('R63-0022-04', 'Polymers and Resins IV: Polystyrene Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-004');
INSERT INTO regulatory_codes VALUES ('R63-0022-05', 'Polymers and Resins IV: Styrene Acrylonitrile Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-005');
INSERT INTO regulatory_codes VALUES ('R63-0022-06', 'Polymers and Resins IV: Nitrile Resins Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-006');
INSERT INTO regulatory_codes VALUES ('R63-0022-07', 'Polymers and Resins IV: Polyethylene Terephthalate Production', 'MACT', '63', 'JJJ', 'Yes', 'No', '', '2008', 'R63-0022-007');
INSERT INTO regulatory_codes VALUES ('R63-0023', 'Primary Aluminum', 'MACT', '63', 'LL', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0024', 'Pharmaceuticals NESHAP', 'MACT', '63', 'GGG', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0025', 'Flexible Polyurethane Foam Production', 'MACT', '63', 'III', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0026', 'ONG - Natural Gas Transmission and Storage MACT', 'MACT', '63', 'HHH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0027', 'Ferroalloys Production', 'MACT', '63', 'XXX', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0028', 'Mineral Wool', 'MACT', '63', 'DDD', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0029', 'Polyether Polyols', 'MACT', '63', 'PPP', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0031', 'Primary Lead Smelting', 'MACT', '63', 'TTT', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0032', 'Phosphate Fertilizer Production Plants', 'MACT', '63', 'BB', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0033', 'Phosphoric Acid Manufacturing Plants', 'MACT', '63', 'AA', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0034', 'Portland Cement Manufacturing', 'MACT', '63', 'LLL', 'No', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0035', 'Wool Fiberglass', 'MACT', '63', 'NNN', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0036', 'ONG - Oil and Natural Gas Production MACT', 'MACT', '63', 'HH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0037', 'Steel Pickling', 'MACT', '63', 'CCC', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0038', 'Pesticide Active Ingredient', 'MACT', '63', 'MMM', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-01', 'Acetal Resins (GMACT)', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-02', 'Acrylic/Modacrylic Fibers (GMACT)', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-03', 'Hydrogen Flouride (GMACT)', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-04', 'Polycarbonates (GMACT)', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-05', 'Carbon Black Production NESHAP', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-06', 'Cyanide Chemicals Manufacturing', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0039-07', 'Spandex Production (GMACT)', 'MACT', '63', 'YY', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0040', 'Industrial, Commercial and Institutional Boilers and Process Heaters MACT', 'MACT', '63', 'DDDDD', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0040-01', 'Industrial/Commercial/ Institutional Boilers & Process Heaters - coal', 'MACT', '63', 'DDDDD', 'No', 'Yes', '', '2008', 'R63-0040');
INSERT INTO regulatory_codes VALUES ('R63-0040-02', 'Industrial/Commercial/ Institutional Boilers & Process Heaters - gas', 'MACT', '63', 'DDDDD', 'No', 'Yes', '', '2008', 'R63-0040');
INSERT INTO regulatory_codes VALUES ('R63-0040-03', 'Industrial/Commercial/ Institutional Boilers & Process Heaters - oil', 'MACT', '63', 'DDDDD', 'No', 'Yes', '', '2008', 'R63-0040');
INSERT INTO regulatory_codes VALUES ('R63-0040-04', 'Industrial/Commercial/ Institutional Boilers & Process Heaters - wood or waste', 'MACT', '63', 'DDDDD', 'No', 'Yes', '', '2008', 'R63-0040');
INSERT INTO regulatory_codes VALUES ('R63-0041', 'Hazardous Waste Incineration', 'MACT', '63', 'EEE', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0041-01', 'Hazardous Waste Incineration: Commercial', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-02', 'Hazardous Waste Incineration: On-Site', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-03', 'Hazardous Waste Incineration: Cement Kilns', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-04', 'Hazardous Waste Incineration: Lightweight Aggregate Kilns', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-05', 'Hazardous Waste Incineration: Solid Fuel Boilers', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-06', 'Hazardous Waste Incineration: Liquid Fuel Boilers', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0041-07', 'Hazardous Waste Incineration: HCl Production Furnaces', 'MACT', '63', 'EEE', 'No', 'Yes', '', '2008', 'R63-0041');
INSERT INTO regulatory_codes VALUES ('R63-0042', 'Publicly Owned Treatment Works', 'MACT', '63', 'VVV', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0043', 'Amino/Phenolic Resins Production', 'MACT', '63', 'OOO', 'Yes', 'No', '', '2008', 'R63-0043-001');
INSERT INTO regulatory_codes VALUES ('R63-0043-001', 'Polymers and Resins III: Amino/Phenolic Resins Production', 'MACT', '63', 'OOO', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0044', 'Secondary Aluminum Production', 'MACT', '63', 'RRR', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0044-01', 'Secondary Aluminum Production: Non Sweat Furnaces', 'MACT', '63', 'RRR', 'Yes', 'No', '', '2008', 'R63-0044');
INSERT INTO regulatory_codes VALUES ('R63-0044-02', 'Secondary Aluminum Production: Sweat Furnaces', 'MACT', '63', 'RRR', 'Yes', 'No', '', '2008', 'R63-0044');
INSERT INTO regulatory_codes VALUES ('R63-0045', 'Pulp and Paper II (Combustion)', 'MACT', '63', 'MM', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0046', 'Solvent Extraction for Vegetable Oil', 'MACT', '63', 'GGGG', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0047', 'Nutritional Yeast Manufacturing', 'MACT', '63', 'CCCC', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0048', 'Petroleum Refineries II', 'MACT', '63', 'UUU', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0049', 'Wet Formed Fiberglass Mat Production', 'MACT', '63', 'HHHH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0050', 'Cellulose Products Manufacturing', 'MACT', '63', 'UUUU', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0051', 'Ethylene Processes', 'MACT', '63', 'XX', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0052', 'Metal Coil', 'MACT', '63', 'SSSS', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0053', 'Rubber Tire Manufacturing', 'MACT', '63', 'XXXX', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0054', 'Large Appliances', 'MACT', '63', 'NNNN', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0055', 'Polyvinyl Chloride & Copolymers Production', 'MACT', '63', 'J', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0056', 'Friction Products Manufacturing', 'MACT', '63', 'QQQQQ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0057', 'Paper & Other Web Coatings', 'MACT', '63', 'JJJJ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0058', 'Municipal Solid Waste Landfills', 'MACT', '63', 'AAAA', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0059', 'Coke Ovens: Pushing, Quenching & Battery Stacks', 'MACT', '63', 'CCCCC', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0060', 'Flexible Polyurethane Foam Fabrication', 'MACT', '63', 'MMMMM', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0061', 'Refractory Products Manufacturing', 'MACT', '63', 'SSSSS', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0062', 'Hydrochloric Acid Production', 'MACT', '63', 'NNNNN', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0063', 'Reinforced Plastics and Composites Production', 'MACT', '63', 'WWWW', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0064', 'Asphalt Processing & Roofing Manufacturing NESHAP', 'MACT', '63', 'LLLLL', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0065', 'Brick & Structural Clay Products', 'MACT', '63', 'JJJJJ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0066', 'Clay Ceramics Manufacturing', 'MACT', '63', 'KKKKK', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0067', 'Integrated Iron and Steel', 'MACT', '63', 'FFFFF', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0068', 'Semiconductors MACT', 'MACT', '63', 'BBBBB', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0069', 'Metal Furniture Surface Coating', 'MACT', '63', 'RRRR', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0070', 'Engine Test Cells/Stands MACT', 'MACT', '63', 'PPPPP', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0070-01', 'Engine Test Facilities', 'MACT', '63', 'PPPPP', 'Yes', 'No', '', '2008', 'R63-0070');
INSERT INTO regulatory_codes VALUES ('R63-0070-02', 'Rocket Engine Test Firing', 'MACT', '63', 'PPPPP', 'Yes', 'No', '', '2008', 'R63-0070');
INSERT INTO regulatory_codes VALUES ('R63-0071', 'Wood Building Products Coatings', 'MACT', '63', 'QQQQ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0072', 'Printing, Coating & Dyeing of Fabric', 'MACT', '63', 'OOOO', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0073', 'Site Remediation', 'MACT', '63', 'GGGGG', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0074', 'Primary Magnesium Refining', 'MACT', '63', 'TTTTT', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0075', 'Taconite Iron Ore Processing', 'MACT', '63', 'RRRRR', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0076', 'Miscellaneous Organic NESHAP (MON)', 'MACT', '63', 'FFFF', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0076-01', 'Ammonium Sulfate - Caprolactam By-Product Plants', 'MACT', '63', 'FFFF', 'Yes', 'No', '', '2008', 'R63-0076');
INSERT INTO regulatory_codes VALUES ('R63-0076-02', 'Carbonyl Sulfide (COS) Production', 'MACT', '63', 'FFFF', 'Yes', 'No', '', '2008', 'R63-0076');
INSERT INTO regulatory_codes VALUES ('R63-0077', 'Metal Can', 'MACT', '63', 'KKKK', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0078', 'Miscellaneous Coating Manufacturing', 'MACT', '63', 'HHHHH', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0079', 'Mercury Cell Chlor-Alkali Plants', 'MACT', '63', 'IIIII', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0080', 'Miscellaneous Metal Parts & Products Coatings', 'MACT', '63', 'MMMM', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0080-01', 'Asphalt/Coal Tar Application - Metal Pipes', 'MACT', '63', 'MMMM', 'Yes', 'No', '', '2008', 'R63-0080');
INSERT INTO regulatory_codes VALUES ('R63-0081', 'Lime Manufacturing', 'MACT', '63', 'AAAAA', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0082', 'Organic Liquids Distribution', 'MACT', '63', 'EEEE', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0083', 'Stationary Combustion Turbines MACT', 'MACT', '63', 'YYYY', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0083-01', 'Stationary Combustion Turbines - Natural Gas', 'MACT', '63', 'YYYY', 'No', 'Yes', '', '2008', 'R63-0083');
INSERT INTO regulatory_codes VALUES ('R63-0083-02', 'Stationary Combustion Turbines - Oil', 'MACT', '63', 'YYYY', 'No', 'Yes', '', '2008', 'R63-0083');
INSERT INTO regulatory_codes VALUES ('R63-0084', 'Plastics Parts Coatings', 'MACT', '63', 'PPPP', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0085', 'Iron and Steel Foundries', 'MACT', '63', 'EEEEE', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0086', 'Auto & Light Duty Trucks', 'MACT', '63', 'IIII', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0087', 'Stationary Reciprocating Internal Combustion Engines (RICE) Compression Ignition', 'MACT', '63', 'ZZZZ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0087-01', 'Stationary Reciprocating Internal Combustion Engines - Natural Gas', 'MACT', '63', 'ZZZZ', 'No', 'Yes', '', '2008', 'R63-0087');
INSERT INTO regulatory_codes VALUES ('R63-0087-02', 'Stationary Reciprocating Internal Combustion Engines - Oil', 'MACT', '63', 'ZZZZ', 'No', 'Yes', '', '2008', 'R63-0087');
INSERT INTO regulatory_codes VALUES ('R63-0088', 'Plywood and Composite Wood Products', 'MACT', '63', 'DDDD', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0089', 'Leather Finishing Operations', 'MACT', '63', 'TTTT', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0092', 'Primary Copper Smelting', 'MACT', '63', 'QQQ', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0093', 'Boat Manufacturing', 'MACT', '63', 'VVVV', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0107', 'Hospital EO Sterilizers', 'MACT', '63', 'WWWWW', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0141', 'Military MACT', 'MACT', '63', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0141-01', 'Utility Boilers: Coal', 'MACT', '63', '', 'No', 'Yes', '', '2008', 'R63-0142');
INSERT INTO regulatory_codes VALUES ('R63-0141-02', 'Utility Boilers: Natural Gas', 'MACT', '63', '', 'No', 'Yes', '', '2008', 'R63-0142');
INSERT INTO regulatory_codes VALUES ('R63-0141-03', 'Utility Boilers: Oil', 'MACT', '63', '', 'No', 'Yes', '', '2008', 'R63-0142');
INSERT INTO regulatory_codes VALUES ('R63-0141-04', 'Utility Boilers: Wood or Waste', 'MACT', '63', '', 'No', 'Yes', '', '2008', 'R63-0142');
INSERT INTO regulatory_codes VALUES ('R63-0142', 'Utility MACT', 'MACT', '63', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0143', 'Gold Mine Ore Processing and Production', 'MACT', '63', 'EEEEEEE', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0152', 'Stationary Reciprocating Internal Combustion Engines (RICE) Spark Ignition', 'MACT', '63', 'ZZZZ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0154', 'Pesticide Application MACT', 'MACT', '', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0155', 'Elemental Phosphorous', 'MACT', '63', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0001', 'National Uniform Emission Standards for Heat Exchangers', 'MACT & NSPS', '65', 'L', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0002', 'National Uniform Emission Standards for General Provisions', 'MACT & NSPS', '65', 'H', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0003', 'National Uniform Emission Standards for Wastewater Operations', 'MACT & NSPS', '65', 'K', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0004', 'National Uniform Emission Standards for Storage Vessels and Transfer Operations', 'MACT & NSPS', '65', 'I', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0005', 'National Uniform Emission Standards for Equipment Leaks and Ancillary Systems', 'MACT & NSPS', '65', 'J', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R65-0006', 'National Uniform Emission Standards for Process Vents and Control Devices', 'MACT & NSPS', '65', 'M', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0001', 'Vinyl Chloride', 'NESHAP', '61', 'F', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0002', 'Asbestos', 'NESHAP', '61', 'M', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0005', 'Coke Oven By-product plants', 'NESHAP', '61', 'L', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0006', 'Underground Uranium Mines', 'NESHAP', '61', 'B', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0010', 'National Emission Standards for Beryllium', 'NESHAP', '61', 'C', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0011', 'Beryllium Rocket Motor Firing NESHAP', 'NESHAP', '61', 'D', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0012', 'Radionuclide Emissions from Facilities Other Than DOE Facilities', 'NESHAP', '61', 'H', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0013', 'Radionuclide Emissions from Federal Facilities Not Covered by Subpart H', 'NESHAP', '61', 'I', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0014', 'Radionuclides from Elemental Phosphorus Plants', 'NESHAP', '61', 'K', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0015', 'Inorganic Arsenic Emissions from Glass Manufacturing Plants', 'NESHAP', '61', 'N', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0016', 'Inorganic Arsenic Emissions from Primary Copper Smelters', 'NESHAP', '61', 'O', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0017', 'Radon Emissions from DOE Facilities', 'NESHAP', '61', 'Q', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0018', 'Radon Emissions from Phosphogypsum Stacks', 'NESHAP', '61', 'R', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0019', 'Radon Emissions from the Disposal of Uranium Mill Tailings', 'NESHAP', '61', 'T', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R61-0020', 'Radon Emissions from Operating Mill Tailings', 'NESHAP', '61', 'W', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0001', 'Portland Cement', 'NSPS', '60', 'F', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0002', 'Sulfuric Acid Plants', 'NSPS', '60', 'H', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0003', 'Nitric Acid Plants', 'NSPS', '60', 'G', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0004', 'Asphalt Concrete', 'NSPS', '60', 'I', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0005', 'Basic Oxygen Process Furnaces', 'NSPS', '60', 'N', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0006', 'Secondary Brass & Bronze', 'NSPS', '60', 'M', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0007', 'Secondary Lead Smelters', 'NSPS', '60', 'L', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0009', 'Phosphate Fertilizers - Wet-Process Phosphoric Acid Plants', 'NSPS', '60', 'T', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0010', 'Phosphate Fertilizers - Superphosphoric Acid Plants', 'NSPS', '60', 'U', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0011', 'Phosphate Fertilizers - Diammonium Phosphate Plants', 'NSPS', '60', 'V', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0012', 'Phosphate Fertilizers - Triple Superphosphate Plants', 'NSPS', '60', 'W', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0013', 'Phosphate Fertilizers - Granular Triple Superphosphate Storage Facilities', 'NSPS', '60', 'X', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0014', 'Steel Plants: Electric Arc Furnaces', 'NSPS', '60', 'AA', 'Yes', 'Yes', '', '2008', 'R60-0015');
INSERT INTO regulatory_codes VALUES ('R60-0015', 'Steel Plants: Arc Furnaces (AAa, AA)', 'NSPS', '60', 'AAa', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0016', 'Coal Preparation & Processing Plant NSPS', 'NSPS', '60', 'Y', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0017', 'Primary Copper Smelters', 'NSPS', '60', 'P', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0018', 'Primary Lead Smelters', 'NSPS', '60', 'R', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0019', 'Primary Zinc Smelters', 'NSPS', '60', 'Q', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0020', 'Primary Aluminum Reduction Plants', 'NSPS', '60', 'S', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0021', 'Ferroalloy Production Facilities', 'NSPS', '60', 'Z', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0022', 'Kraft Pulp Mills', 'NSPS', '60', 'BB', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0023', 'Lime Manufacturing', 'NSPS', '60', 'HH', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0024', 'Volatile Organic Liquid Storage Vessels', 'NSPS', '60', 'K, Ka, Kb', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0024-01', 'Volatile Organic Liquid Storage Vessels', 'NSPS', '60', 'K, Ka, Kb', 'Yes', 'Yes', '', '2008', 'R60-0024');
INSERT INTO regulatory_codes VALUES ('R60-0024-02', 'Volatile Organic Liquid Storage Vessels (inc Petroleum Liquid Storage Vessels)', 'NSPS', '60', 'K, Ka, Kb', 'Yes', 'Yes', '', '2008', 'R60-0024');
INSERT INTO regulatory_codes VALUES ('R60-0025', 'Grain Elevators', 'NSPS', '60', 'DD', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0026', 'Industrial Boilers NSPS', 'NSPS', '60', 'D,Da,Db,Dc', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0026-01', 'Fossil-Fuel Fired Steam Generators', 'NSPS', '60', 'D', 'Yes', 'Yes', '', '2008', 'R60-0026');
INSERT INTO regulatory_codes VALUES ('R60-0026-02', 'Electric Utility Steam Generating Units', 'NSPS', '60', 'Da', 'Yes', 'Yes', '', '2008', 'R60-0026');
INSERT INTO regulatory_codes VALUES ('R60-0026-03', 'Industrial, Commercial, Institutional Steam Generating Units', 'NSPS', '60', 'Db', 'Yes', 'Yes', '', '2008', 'R60-0026');
INSERT INTO regulatory_codes VALUES ('R60-0026-04', 'Small Industrial, Commercial, Institutional Steam Generating Units', 'NSPS', '60', 'Dc', 'Yes', 'Yes', '', '2008', 'R60-0026');
INSERT INTO regulatory_codes VALUES ('R60-0027', 'Stationary Internal Combustion Engines NSPS', 'NSPS', '60', 'IIII, JJJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0028', 'Stationary Gas Turbines NSPS (Superseded by Stationary Combustion Turbines NSPS)', 'NSPS', '60', 'GG', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0029', 'Glass Manufacturing', 'NSPS', '60', 'CC', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0030', 'Ammonium Sulfate', 'NSPS', '60', 'PP', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0031', 'Bulk Gasoline Terminals', 'NSPS', '60', 'XX', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0032', 'Lead Acid Batteries', 'NSPS', '60', 'KK', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0033', 'Phosphate Rock', 'NSPS', '60', 'NN', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0034', 'Large Appliances Surface Coating', 'NSPS', '60', 'SS', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0035', 'Asphalt Roofing', 'NSPS', '60', 'UU', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0036', 'Metal Furniture Surface Coating', 'NSPS', '60', 'EE', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0037', 'Metal Coil Surface Coating', 'NSPS', '60', 'TT', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0038', 'Graphic Arts Industry/Publication Rotogravure Printing', 'NSPS', '60', 'QQ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0039', 'Beverage Can Surface Coating', 'NSPS', '60', 'WW', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0040', 'Pressure Sensitive Tape', 'NSPS', '60', 'RR', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0041', 'Metallic Mineral Processing', 'NSPS', '60', 'LL', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0042', 'Synthetic Fibers', 'NSPS', '60', 'HHH', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0043', 'Refineries: Equip. Leaks', 'NSPS', '60', 'GGG, GGGa', 'Yes', 'Yes', '', '2008', 'R65-0005');
INSERT INTO regulatory_codes VALUES ('R60-0044', 'Flexible Vinyl/Urethane Printing', 'NSPS', '60', 'FFF', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0045', 'Petroleum Dry Cleaners', 'NSPS', '60', 'JJJ', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0046', 'Wool Fiberglass Insulation', 'NSPS', '60', 'PPP', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0047', 'ONG - Onshore Natural Gas Plants - VOC Eq. Leaks', 'NSPS', '60', 'KKK', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0048', 'Nonmetallic Minerals', 'NSPS', '60', 'OOO', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0049', 'ONG - Onshore Natural Gas Plants - SO2', 'NSPS', '60', 'LLL', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0050', 'Basic Process Steelmaking Facilities', 'NSPS', '60', 'Na', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0051', 'Rubber Tire Manufacturing', 'NSPS', '60', 'BBB', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0052', 'Surface Coating of Plastic Parts', 'NSPS', '60', 'TTT', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0053', 'New Residential Woodheaters', 'NSPS', '60', 'AAA', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0054', 'Magnetic Tape Coating Facilities', 'NSPS', '60', 'SSS', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0055', 'Refineries: Wastewater', 'NSPS', '60', 'QQQ', 'Yes', 'Yes', '', '2008', 'R65-0003');
INSERT INTO regulatory_codes VALUES ('R60-0056', 'Polymeric Coating of Substrates', 'NSPS', '60', 'VVV', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0057', 'SOCMI Air Ox Unit Processes', 'NSPS', '60', 'III', 'Yes', 'Yes', '', '2008', 'R65-0006');
INSERT INTO regulatory_codes VALUES ('R60-0058', 'SOCMI Distillation', 'NSPS', '60', 'NNN', 'Yes', 'Yes', '', '2008', 'R65-0006');
INSERT INTO regulatory_codes VALUES ('R60-0059', 'Polymers Manufacturing Industry', 'NSPS', '60', 'DDD', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0060', 'Calciners and Dryers', 'NSPS', '60', 'UUU', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0061', 'SOCMI Reactor Processes', 'NSPS', '60', 'RRR', 'Yes', 'Yes', '', '2008', 'R65-0006');
INSERT INTO regulatory_codes VALUES ('R60-0062', 'SOCMI Wastewater', 'NSPS', '60', 'YYY', 'Yes', 'Yes', '', '2008', 'R65-0003');
INSERT INTO regulatory_codes VALUES ('R60-0064', 'Municipal Solid Waste Landfills EG: (WWW) (Cc)', 'NSPS', '60', 'WWW', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0066', 'Auto/Light Duty Truck Surface Coating', 'NSPS', '60', 'MM', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0069', 'Stationary Combustion Turbines NSPS', 'NSPS', '60', 'KKKK', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0071', 'SOCMI Equipment Leaks', 'NSPS', '60', 'VV,Vva', 'Yes', 'Yes', '', '2008', 'R65-0005');
INSERT INTO regulatory_codes VALUES ('R60-0072', 'Petroleum Refineries', 'NSPS', '60', 'J, Ja', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0073', 'Utility NSPS', 'NSPS', '60', '', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('ARP-0001', 'Acid Rain Program', 'OTHER', '', '', 'Yes', 'No', 'Electric generating units emitting SO2 and NOx subject to EPA''s acid rain program (40 CFR parts 72 - 78).  Also includes large stationary sources emitting SO2 participating in emission trading programs through ''opt in'' provision.', '', '');
INSERT INTO regulatory_codes VALUES ('CAI-0001', 'Clean Air Interstate Rule', 'OTHER', '', '', 'Yes', 'No', 'Sources controlled to meet requirements of EPA''s Clean Air Interstate Rule (40 CFR part 51.123 - 51.125).  Sources are electric generating units emitting SO2 and NOx controlled by Federal Implementation Plans or point, nonpoint and mobile sources selected', '', '');
INSERT INTO regulatory_codes VALUES ('CAI-0002', 'Clean Air Mercury Rule', 'OTHER', '', '', 'Yes', 'No', 'Coal fired power plants subject to control under EPA''s Clean Air Mercury Rule (40 CFR parts 60, 63, 72 and 75)', '', '');
INSERT INTO regulatory_codes VALUES ('MAI-0001', 'Maintenance Plan Control Meaure', 'OTHER', '', '', 'Yes', 'Yes', 'Sources or source categories subject to regulations developed to implement control measures in Maintenance Plans for attainment areas.', '', '');
INSERT INTO regulatory_codes VALUES ('NOX-0001', 'NOx Budget Trading Program', 'OTHER', '', '', 'Yes', 'No', 'Point, nonpoint and mobile sources controlled by states to meet requirements of EPA''s NOx Budget Trading Program (40 CFR Parts 51.121 and 51.122).', '', '');
INSERT INTO regulatory_codes VALUES ('NSR-0001', 'Nonattainment New Source Review', 'OTHER', '', '', 'Yes', 'Yes', 'Point sources in nonattainment areas with contols representing Lowest Achievable Emission Rate (LAER) required by Nonattainment New Source Review regulations in State Implementation Plans.', '', '');
INSERT INTO regulatory_codes VALUES ('PSD-0001', 'Prevention of Significant Deterioration', 'OTHER', '', '', 'Yes', 'Yes', 'Point sources in attainment areas with controls representing Best Available Control Technology (BACT) required by Prevention of Significant Deterioration regulations.', '', '');
INSERT INTO regulatory_codes VALUES ('RHP-0001', 'Regional Haze Program', 'OTHER', '', '', 'Yes', 'Yes', 'Sources of SO2, NOx, certain organic compounds,  PM, and Ammonia subject to Best Available Retrofit Technology (BART) under State Implementation Plans developed to meet requirements of EPA''s Regional Haze Program (40 CFR 51.308).Clean Air Visibility Rule', '', '');
INSERT INTO regulatory_codes VALUES ('SIP-0001', 'State Implementation Plan Control Measure', 'OTHER', '', '', 'Yes', 'Yes', 'Point, nonpoint and mobile sources subject to regulations adopted to implement control measures in State Implementation Plans.', '', '');
INSERT INTO regulatory_codes VALUES ('SLT-0001', 'State, Local, or Tribal local regulations', 'OTHER', '', '', 'Yes', 'Yes', 'Sources subject to control measures adopted by States, local agencies or Tribes to meet local requirements.', '', '');
INSERT INTO regulatory_codes VALUES ('VOL-0001', 'Voluntary Measure', 'OTHER', '', '', 'Yes', 'Yes', 'Sources that have voluntarily controlled emissions to develop emission reduction credits.', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0008', 'Sewage Treatment Plant Incineration', 'Sec 129', '60', 'O', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0063', 'Large Municipal Waste Combustors (MWC) NSPS', 'Sec 129', '60', 'Eb, Cb', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0063-01', 'Municipal Waste Combustors: Large', 'Sec 129', '60', 'Eb, Cb', 'Yes', 'No', '', '2008', 'R60-0063');
INSERT INTO regulatory_codes VALUES ('R60-0063-02', 'Municipal Waste Combustors: Small', 'Sec 129', '60', 'Eb, Cb', 'Yes', 'No', '', '2008', 'R60-0067');
INSERT INTO regulatory_codes VALUES ('R60-0065', 'Hospital, Medical, Infectious Waste Incineration (HMIWI)', 'Sec 129', '60', 'Ec and Ce', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0067', 'Small Municipal Waste Combustors (MWC) NSPS', 'Sec 129', '60', 'AAAA', 'Yes', 'Yes', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0068', 'Commercial and Industrial Solid Waste Incineration (CISWI)', 'Sec 129', '60', 'E', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R60-0070', 'Other Solid Waste Incinerators (OSWI) (EEEE, FFFF)', 'Sec 129', '60', 'EEEE, FFFF', 'Yes', 'No', '', '', '');
INSERT INTO regulatory_codes VALUES ('R63-0150', 'Sewage Sludge Incineration', 'Sec 129', '63', 'LLLL, MMMM', 'Yes', 'Yes', '', '', '');


-- Completed on 2011-06-14 01:10:39

--
-- PostgreSQL database dump complete
--

CREATE INDEX idx_regulatory_codes_code
  ON reference.regulatory_codes
  USING btree
  (code);