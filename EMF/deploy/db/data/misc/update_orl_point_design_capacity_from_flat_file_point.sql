
update emissions.DS_ptinv_ptnonipm_2020cc_nc_sc__va_1497667446
set 	
	DESIGN_CAPACITY = coalesce(pt.DESIGN_CAPACITY, ff10.DESIGN_CAPACITY), 
	DESIGN_CAPACITY_UNIT_NUMERATOR = coalesce(pt.DESIGN_CAPACITY_UNIT_NUMERATOR, case when array_length(string_to_array(DESIGN_CAPACITY_UNITS,'/'), 1) >= 1 then (string_to_array(DESIGN_CAPACITY_UNITS,'/'))[1] else null end), 
	DESIGN_CAPACITY_UNIT_DENOMINATOR = coalesce(pt.DESIGN_CAPACITY_UNIT_DENOMINATOR, case when array_length(string_to_array(DESIGN_CAPACITY_UNITS,'/'), 1) >= 2 then (string_to_array(DESIGN_CAPACITY_UNITS,'/'))[2] else null end)


--select *
FROM emissions.DS_ptipm_cap2005v2_nc_sc_va_579113009
/*DS_ptinv_ptnonipm_2020cc_nc_sc__va_1497667446*/ as pt

	inner join emissions.DS_SmokeFlatFile_POINT_20110517_csv_18may2011_v0_2028180293 as ff10

        on pt.fips = ff10.region_cd
        and pt.plantid = coalesce(ff10.agy_facility_id,ff10.facility_id)
        and pt.pointid = coalesce(ff10.agy_unit_id,ff10.unit_id)
        and pt.stackid = coalesce(ff10.agy_rel_point_id,ff10.rel_point_id)
        and pt.segment = coalesce(ff10.agy_process_id,ff10.process_id)
        and pt.scc = ff10.scc
        and pt.poll = ff10.poll;


select * from emissions.DS_SmokeFlatFile_POINT_20110517_csv_18may2011_v0_2028180293
where substring(region_cd, 1, 2) = '37';
