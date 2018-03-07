UPDATE emf.equation_type_variables
SET name = 'Capital Cost Multiplier ($/kW)'
WHERE equation_type_id = (SELECT id FROM emf.equation_types WHERE name = 'Type 1')
AND file_col_position = 1;

UPDATE emf.equation_type_variables
SET name = 'Fixed Cost Multiplier ($/kW)'
WHERE equation_type_id = (SELECT id FROM emf.equation_types WHERE name = 'Type 1')
AND file_col_position = 2;

UPDATE emf.equation_type_variables
SET name = 'Variable Cost Multiplier ($/kW)'
WHERE equation_type_id = (SELECT id FROM emf.equation_types WHERE name = 'Type 1')
AND file_col_position = 3;

UPDATE emf.equation_type_variables
SET name = 'Typical Capital Cost ($/acfm)'
WHERE equation_type_id = (SELECT id FROM emf.equation_types WHERE name = 'Type 8')
AND file_col_position = 1;

UPDATE emf.equation_type_variables
SET name = 'Typical O&M Cost ($/acfm)'
WHERE equation_type_id = (SELECT id FROM emf.equation_types WHERE name = 'Type 8')
AND file_col_position = 2;

UPDATE emf.equation_types
SET equation = 
'Scaling Factor = (Model Plant boiler capacity / MW) ^ (Scaling Factor Exponent)
Capital Cost = Capital Cost Multiplier x Design Capacity x Combustion Efficiency x Scaling Factor x 1,000
Fixed O&M = Fixed O&M Cost Multiplier x Design Capacity x 1,000
Variable O&M = Variable O&M Cost Multiplier x Design Capacity x Capacity Factor x 8,760'
WHERE name = 'Type 1';

UPDATE emf.equation_types
SET equation =
'Annual Cost = Annual Cost Multiplier x (Design Capacity x Combustion Efficiency)^(Annual Cost Exponent) + Annual Cost Base
Capital Cost = Capital Cost Multiplier x (Design Capacity x Combustion Efficiency)^(Capital Cost Exponent) + Capital Cost Base'
WHERE name = 'Type 2';

UPDATE emf.equation_types
SET equation =
'Annual Cost = Annual Cost Multiplier x (Emissions Reduction [in tons/day]) ^ Exponent + Base
Capital Cost = Capital Cost Multiplier x (Emissions Reduction [in tons/day]) ^ Exponent + Base'
WHERE name = 'Type 2a';

UPDATE emf.equation_types
SET equation =
'Annual Cost = Annual Cost Multiplier x e^((Annual Cost Exponent x Design Capacity x Combustion Efficiency))
Capital Cost = Capital Cost Multiplier x e^((Capital Cost Exponent x Design Capacity x Combustion Efficiency))'
WHERE name = 'Type 2b';

UPDATE emf.equation_types
SET equation =
'Capital Cost = ((1028000/Min. stack flow rate)^0.6) x Capital Cost factor x Gas Flow Rate factor x Retrofit factor x Stack Flow rate x 0.9383
O&M Cost = Gas Flowrate Factor x (Fixed O&M Rate + (Variable O&M Rate x 8736)) x Stack flow rate
Total Cost = (Capital cost x CRF) + O&M Cost

Notes:
Min Stack Flow Rate < 1028000 acfm
Capital Cost factor = $192 / kw
Gas flow rate factor = 0.486 KW/acfm
Fixed O&M Rate = $6.9/kW
Variable O&M Rate = $0.0015/kW'
WHERE name = 'Type 3';

UPDATE emf.equation_types
SET equation =
'stack_flow_rate (scfm) = stack_flow_rate (acfm) x 520 / (stack_temperature + 460)
Total Capital Investment (TCI) = (Fixed TCI + Variable TCI) x (stack_flow_rate (scfm)/150,000)^.6
Annual Operating Cost (AOC) = (AOC fixed + AOC variable) x (stack_flow_rate (scfm)/150,000)
Total Annual Cost (TAC) = AOC + Capital Recovery Factor (CRF) x TCI'
WHERE name = 'Type 12';

UPDATE emf.equation_types
SET equation =
'TAC = ($0.00000387)(CSO2)(Fd)(OpHrs)

Variable Name and Value
CSO2(ppmvd) = Concentration of stack gas
Fd (dscfm) = Dry exhaust flow rate
OpHrs (hours/year) = Annual operating hours'
WHERE name = 'Type 18';
