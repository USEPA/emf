-- Cement
-- new measure NSNCRCMWT created via import

-- mark NBINTCEMK as obsolete
UPDATE emf.control_measures
   SET cm_class_id = (SELECT id FROM emf.control_measure_classes WHERE name = 'Obsolete'),
       last_modified_time = NOW(),
       description = E'Marked as Obsolete due to SRA review in Dec. 2016. (01/23/2017)\n\n' || description
 WHERE abbreviation = 'NBINTCEMK';

-- remove SCC 30500602 (only used by measures NLNBUCMDY, NMKFRCMDY, NSCRCMDY)
DELETE FROM emf.control_measure_sccs
 WHERE name = '30500602';

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation IN ('NLNBUCMDY', 'NMKFRCMDY', 'NSCRCMDY');

-- add SCCs 30500622, 30500623 and reference for NSNCRCMDY
INSERT INTO emf.control_measure_sccs (control_measures_id, name)
SELECT id, scc
  FROM emf.control_measures
 CROSS JOIN (SELECT UNNEST(ARRAY['30500622', '30500623']) AS scc) v
 WHERE abbreviation = 'NSNCRCMDY';

INSERT INTO emf.control_measure_references (control_measure_id, reference_id, list_index) 
VALUES (
(SELECT id FROM emf.control_measures WHERE abbreviation = 'NSNCRCMDY'),
(SELECT id FROM emf.references WHERE description LIKE 'See: EPA Cement Manufacturing Enforcement Initiative (https://www.epa.gov/enforcement/cement-manufacturing-enforcement-initiative )%'),
(SELECT MAX(list_index) + 1
   FROM emf.control_measure_references
  WHERE control_measure_id = (SELECT id FROM emf.control_measures WHERE abbreviation = 'NSNCRCMDY')));

UPDATE emf.control_measures
   SET description = REPLACE(description, '(SCC 30500606)', '(SCC 30500606, 30500622, 20500623)'),
       last_modified_time = NOW()
 WHERE abbreviation = 'NSNCRCMDY';

-- update review date for all cement measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NBINTCEMK', 'NDSCRBCCK', 'NDSCRCMDY', 'NDSCRCMWT', 
                        'NLNBUCMDY', 'NLNBUCMWT', 'NMKFRCMDY', 'NMKFRCMWT', 
                        'NSCRCMDY', 'NSNCNCMDY', 'NSNCRBCCK', 'NSNCRCMDY', 
                        'NSNCRCMWT');


-- Gas Turbines
-- new measures NSCRDGTNG, NSCRSGTNG, NSCRWGTNG, NSTINGTNG, NWTINGTNG created via import

-- update review date for all gas turbine measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NCATCGTNG', 'NDLNCGTNG', 'NEMXDGTNG', 'NEMXWGTNG', 
                        'NLNBUGTNG', 'NSCRDGTNG', 'NSCRSGTNG', 'NSCRWGTNG', 
                        'NSTINGTNG', 'NWTINGTNG');


-- Glass Furnaces
-- new measures NOEASGMGN, NOEASGMCN, NOEASGMFT, NOEASGMPD created via import

-- mark CATCFGMFT as emerging
UPDATE emf.control_measures
   SET cm_class_id = (SELECT id FROM emf.control_measure_classes WHERE name = 'Emerging'),
       last_modified_time = NOW(),
       description = E'Marked as Emerging due to SRA review in Dec. 2016. (01/23/2017)\n\n' || description
 WHERE abbreviation = 'CATCFGMFT';

-- update review date for all glass furnace measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('CATCFGMFT', 'NCLPTGMCN', 'NCUPHGMPD', 'NDOXYFGMG', 
                        'NELBOGMCN', 'NELBOGMFT', 'NELBOGMGN', 'NELBOGMPD', 
                        'NLNBUGMCN', 'NLNBUGMFT', 'NLNBUGMPD', 'NOEASGMCN', 
                        'NOEASGMFT', 'NOEASGMGN', 'NOEASGMPD', 'NOXYFGMCN', 
                        'NOXYFGMFT', 'NOXYFGMGN', 'NOXYFGMPD', 'NSCRGMCN', 
                        'NSCRGMFT', 'NSCRGMPD');


-- IC Engines Gas Fired
-- new measures NNSCRLCNGNS, NNSCRLECNGNS, NNSCRAFRIINGNS created via import

-- update review date for all IC engine measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NAFRCICENG', 'NAFRICGS', 'NAFRIICGS', 'NIRICGS', 
                        'NLCICE2SLBNG', 'NLCICE2SNG', 'NLECICEGAS', 'NLECICENG', 
                        'NNSCRAFRIINGNS', 'NNSCRINGI4', 'NNSCRINGNS', 'NNSCRLCNGNS', 
                        'NNSCRLECNGNS', 'NSCRICE4SNG');


-- ICI Boilers Coal Fired
-- update review date for all coal boiler measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NDSCRIBCF', 'NDSCRIBCS', 'NLNBUIBCW', 'NLNSCRIBCW', 
                        'NLNSNCRIBCW', 'NSCRIBCC', 'NSCRIBCW', 'NSNCRIBCC', 
                        'NSNCRIBCS', 'NSNCRIBCW');


-- ICI Boilers Gas Fired
-- create new reference
INSERT INTO emf.references (description) 
VALUES (
'Andover Technology Partners. Cost of Emission Control Technologies, presentation by Jim Staudt, PhD, at the ICAC-MARAMA Meeting, May 18-19, 2011. http://www.marama.org/presentations/2011_ICACAdvancesCT/Staudt_MARAMA_051811.pdf');

-- update efficiencies and add reference for NLNBUIBNG, NLNBUIBPG
UPDATE emf.control_measure_efficiencyrecords
   SET efficiency = 75.0
 WHERE control_measures_id IN (
SELECT id FROM emf.control_measures WHERE abbreviation IN ('NLNBUIBNG', 'NLNBUIBPG'));

INSERT INTO emf.control_measure_references (control_measure_id, reference_id, list_index)
VALUES (
(SELECT id FROM emf.control_measures WHERE abbreviation = 'NLNBUIBNG'),
(SELECT id FROM emf.references WHERE description LIKE 'Andover Technology Partners. Cost of Emission Control Technologies%'),
(SELECT MAX(list_index) + 1
   FROM emf.control_measure_references
  WHERE control_measure_id = (SELECT id FROM emf.control_measures WHERE abbreviation = 'NLNBUIBNG')));

INSERT INTO emf.control_measure_references (control_measure_id, reference_id, list_index)
VALUES (
(SELECT id FROM emf.control_measures WHERE abbreviation = 'NLNBUIBPG'),
(SELECT id FROM emf.references WHERE description LIKE 'Andover Technology Partners. Cost of Emission Control Technologies%'),
(SELECT MAX(list_index) + 1
   FROM emf.control_measure_references
  WHERE control_measure_id = (SELECT id FROM emf.control_measures WHERE abbreviation = 'NLNBUIBPG')));

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation IN ('NLNBUIBNG', 'NLNBUIBPG');

-- remove invalid SCC 10210079 (only used by NDSCRIBPG)
DELETE FROM emf.control_measure_sccs
 WHERE name = '10210079';

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation = 'NDSCRIBPG';

-- remove SCC 10201403 from NBFIBNG, NBFIBPG, NLNBFIBNG, NLNBFIBPG
DELETE FROM emf.control_measure_sccs
 WHERE name = '10201403'
   AND control_measures_id IN (
SELECT id FROM emf.control_measures WHERE abbreviation IN ('NBFIBNG', 'NBFIBPG', 'NLNBFIBNG', 'NLNBFIBPG'));

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation IN ('NBFIBNG', 'NBFIBPG', 'NLNBFIBNG', 'NLNBFIBPG');

-- update review date for all gas boiler measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NBFIBNG', 'NBFIBPG', 'NDSCRIBPG', 'NLNBFIBNG', 
                        'NLNBFIBPG', 'NLNBUIBNG', 'NLNBUIBPG', 'NLNDSCRIBPG', 
                        'NLNSCRIBNG', 'NLNSCRICBG', 'NLNSNCRIBNG', 'NLNSNCRICBG', 
                        'NSCRIBNG', 'NSCRICBG', 'NSNCRIBNG', 'NSNCRICBG');


-- ICI Boilers Oil Fired
-- new measures NLNSCRIBRO, NSCRIBRO created via import

-- update efficiencies for NLNBUIBDO, NLNBUIBRO
UPDATE emf.control_measure_efficiencyrecords
   SET efficiency = 60.0
 WHERE control_measures_id IN (
SELECT id FROM emf.control_measures WHERE abbreviation IN ('NLNBUIBDO', 'NLNBUIBRO'));

-- remove SCC 10201403 from NBFIBRO, NLNBFIBRO
DELETE FROM emf.control_measure_sccs
 WHERE name = '10201403'
   AND control_measures_id IN (
SELECT id FROM emf.control_measures WHERE abbreviation IN ('NBFIBRO', 'NLNBFIBRO'));

-- add SCC 10201403 for NBFIBDO, NLNBFIBDO
INSERT INTO emf.control_measure_sccs (control_measures_id, name)
SELECT id, '10201403' FROM emf.control_measures WHERE abbreviation IN ('NBFIBRO', 'NLNBFIBRO');

-- add distillate oil SCCs 10200506, 10300505 for NBFIBDO, NLNBFIBDO, NLNBUIBDO, NLNSCRIBDO, NLNSNCRIBDO, NSCRIBDO, NSNCRIBDO
INSERT INTO emf.control_measure_sccs (control_measures_id, name)
SELECT id, scc
  FROM emf.control_measures
 CROSS JOIN (SELECT UNNEST(ARRAY['10200506', '10300505']) AS scc) v
 WHERE abbreviation IN ('NBFIBDO', 'NLNBFIBDO', 'NLNBUIBDO', 'NLNSCRIBDO', 'NLNSNCRIBDO', 'NSCRIBDO', 'NSNCRIBDO');

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation IN ('NBFIBDO', 'NLNBFIBDO', 'NLNBUIBDO', 'NLNSCRIBDO', 'NLNSNCRIBDO', 'NSCRIBDO', 'NSNCRIBDO');

-- add residual oil SCCs 10200406, 10300403, 10300405 for NBFIBRO, NLNBFIBRO, NLNBUIBRO, NLNSNCRIBRO, NSNCRIBRO
INSERT INTO emf.control_measure_sccs (control_measures_id, name)
SELECT id, scc
  FROM emf.control_measures
 CROSS JOIN (SELECT UNNEST(ARRAY['10200406', '10300403', '10300405']) AS scc) v
 WHERE abbreviation IN ('NBFIBRO', 'NLNBFIBRO', 'NLNBUIBRO', 'NLNSNCRIBRO', 'NSNCRIBRO');

UPDATE emf.control_measures
   SET last_modified_time = NOW()
 WHERE abbreviation IN ('NBFIBRO', 'NLNBFIBRO', 'NLNBUIBRO', 'NLNSNCRIBRO', 'NSNCRIBRO');

-- update review date for all oil boiler measures
UPDATE emf.control_measures
   SET date_reviewed = '2016-12-01'
 WHERE abbreviation IN ('NBFIBDO', 'NBFIBRO', 'NLNBFIBDO', 'NLNBFIBRO', 
                        'NLNBUIBDO', 'NLNBUIBRO', 'NLNSCRIBDO', 'NLNSCRIBRO', 
                        'NLNSNCRIBDO', 'NLNSNCRIBRO', 'NSCRIBDO', 'NSCRIBRO', 
                        'NSNCRIBDO', 'NSNCRIBRO');
