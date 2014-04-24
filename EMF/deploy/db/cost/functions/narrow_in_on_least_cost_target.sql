--select public.narrow_in_on_least_cost_target('cslcm__measure_worksheet_nc_ptnonipm_20080420_20080420054230884', 
--	'NOX', 
--	36797.935745);

--select count(1) from emissions.cslcm__measure_worksheet_nc_ptnonipm_20080420_20080420054230884

--select public.get_least_cost_worksheet_emis_reduction('CSLCM__ptnonipm_MeasureWorksheet_200804182204_20080418220454967', 'NOX', 20305);

--SELECT public.populate_least_cost_strategy_detailed_result(9, 2474, 0, 224, 36797.935745::double precision);

CREATE OR REPLACE FUNCTION public.narrow_in_on_least_cost_target(worksheet_table_name varchar(64), 
	target_pollutant varchar, 
	domain_wide_emis_reduction double precision) RETURNS integer AS $$
DECLARE
	target_record_offset integer;
	prev_target_record_offset integer := 0;
	target_record_offset_diff integer;
	emis_reduction double precision;
	max_emis_reduction double precision;
	record_count integer;
	continue_trying boolean := true;
	tries_around_target integer := 0;
BEGIN

	-- get a record count
	execute 'SELECT count(1)
		FROM emissions.' || worksheet_table_name || '
		where status is null 
			and poll = ' || quote_literal(target_pollutant)
		into record_count;

	-- get maximum emission reduction that can be acheived...
	max_emis_reduction := public.get_least_cost_worksheet_emis_reduction(worksheet_table_name, 
			target_pollutant, 
			record_count);

	IF domain_wide_emis_reduction >= max_emis_reduction THEN
		RETURN record_count;
	END IF;
	
	-- start at the end of the table
	target_record_offset := record_count;

	WHILE continue_trying LOOP

		emis_reduction := public.get_least_cost_worksheet_emis_reduction(worksheet_table_name, 
			target_pollutant, 
			target_record_offset);

--		raise notice '%', 'next starting point, first look at record_offset = ' || coalesce(target_record_offset,0) || ', previous_record_offset = ' || coalesce(prev_target_record_offset,0) || ', record_count = ' || record_count || ', emis_reduction = ' || coalesce(emis_reduction,0) || ' - ' || clock_timestamp();

		target_record_offset_diff := abs(target_record_offset - prev_target_record_offset);
		prev_target_record_offset := target_record_offset;
		--calculate new record offset to try
		IF emis_reduction > domain_wide_emis_reduction THEN
--			target_record_offset := target_record_offset - (target_record_offset_diff / 2 );
			target_record_offset := target_record_offset - case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
		ELSE
/*			-- first pass through....
			IF target_record_offset >= record_count THEN 
				target_record_offset := target_record_offset - case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
			ELSE
				target_record_offset := target_record_offset + case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
			END IF;
			-- first pass through....
			IF target_record_offset = record_count THEN 
				target_record_offset := target_record_offset - case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
			ELSE
				target_record_offset := target_record_offset + case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
			END IF;
*/			target_record_offset := target_record_offset + case when target_record_offset_diff <> 1 then target_record_offset_diff / 2 else 1 end;
--			target_record_offset := target_record_offset + (target_record_offset_diff / 2 );
		END IF;
		IF target_record_offset_diff > 1 THEN
			continue_trying := true;
		ELSEIF target_record_offset > record_count THEN
			continue_trying := false;
		ELSEIF target_record_offset_diff = 1 AND emis_reduction < domain_wide_emis_reduction THEN
			continue_trying := true;
		ELSEIF target_record_offset_diff = 1 AND emis_reduction > domain_wide_emis_reduction THEN
			tries_around_target := tries_around_target + 1;
			IF tries_around_target > 1 THEN
				continue_trying := false;
			ELSE
				continue_trying := true;
			END IF;
		ELSE
			continue_trying := false;
		END IF;
	END LOOP;

	RETURN target_record_offset;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
