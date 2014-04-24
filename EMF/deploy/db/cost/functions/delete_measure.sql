--select public.delete_measure(622);

-- the script below drop statement does not work if the previous parameter is control_measure_id
-- postgres prompt to delete the function first -- by Jizhen
drop function if exists public.delete_measure(integer);

-- delete_measure
CREATE OR REPLACE FUNCTION public.delete_measure(
	cm_id integer) RETURNS void AS $$
DECLARE
--	cm_id int := control_measure_id;
BEGIN
	delete from emf.control_measure_sccs
	where control_measures_id 
	in (cm_id);

	delete from emf.aggregrated_efficiencyrecords
	where control_measures_id 
	in (cm_id);

	delete from emf.control_measure_efficiencyrecords
	where control_measures_id 
	in (cm_id);

	delete from emf.control_measure_references
	where control_measure_id 
	in (cm_id);

	delete from emf.control_measure_sectors
	where control_measure_id 
	in (cm_id);

	delete from emf.control_strategy_measures
	where control_measure_id 
	in (cm_id);

	delete from emf.control_measure_equations
	where control_measure_id
	in (cm_id);

	delete from emf.control_measure_properties
	where control_measure_id
	in (cm_id);

	delete from emf.control_measure_months
	where control_measure_id
	in (cm_id);

	delete from emf.control_measure_nei_devices
	where control_measure_id
	in (cm_id);

	delete from emf.control_measures
	where id 
	in (cm_id);

END;
$$ LANGUAGE plpgsql VOLATILE;
