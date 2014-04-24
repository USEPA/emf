CREATE OR REPLACE FUNCTION public.get_days_in_month("month" smallint, "year" smallint) RETURNS smallint AS $$
DECLARE
	the_date timestamp;
BEGIN
--	return date_part('day', the_date);

	-- Zero month, indicates a yearly type... just return null for now...
	IF $1 = 0 THEN
		return null::smallint;
	END IF;
	
	the_date := ($1 || '/1/' || $2)::TIMESTAMP;
	
	return date_part('day',
		(($2::text || '-' || $1::text || '-01')::date
			+ '1 month'::interval
			- '1 day'::interval));

--	SELECT EXTRACT(HOUR FROM TIMESTAMP '2001-02-16 20:38:40');

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

--select public.get_days_in_month(0::smallint, 2004::smallint);
