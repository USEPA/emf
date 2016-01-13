DELETE FROM aggregrated_efficiencyrecords;

INSERT INTO emf.aggregrated_efficiencyrecords 
     SELECT er.control_measures_id, 
            er.pollutant_id, 
            MAX(er.efficiency), 
            MIN(er.efficiency), 
            AVG(er.efficiency), 
            MAX(er.ref_yr_cost_per_ton), 
            MIN(er.ref_yr_cost_per_ton), 
            AVG(er.ref_yr_cost_per_ton), 
            AVG(er.rule_effectiveness), 
            AVG(er.rule_penetration)  
       FROM emf.control_measure_efficiencyrecords er 
   GROUP BY er.control_measures_id, er.pollutant_id;
