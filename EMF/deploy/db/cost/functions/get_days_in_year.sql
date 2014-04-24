CREATE OR REPLACE FUNCTION public.get_days_in_year("year" smallint) RETURNS smallint AS $$
DECLARE
BEGIN

	-- for Zero year... just return null for now...
	IF coalesce($1,0) = 0 THEN
		return null::smallint;
	END IF;
	
	return date_part('doy', ($1::text || '-12-31')::date);


END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

--select public.get_days_in_year(2005::smallint);
