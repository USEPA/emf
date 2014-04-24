update emf.control_measure_efficiencyrecords set ref_yr_cost_per_ton = cost_per_ton * (select chained_gdp from reference.gdplev where annual = 2006) / (select chained_gdp from reference.gdplev where gdplev.annual = cost_year);

vacuum analyze emf.control_measure_efficiencyrecords;

