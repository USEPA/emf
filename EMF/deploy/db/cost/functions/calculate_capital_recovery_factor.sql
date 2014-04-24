CREATE OR REPLACE FUNCTION public.calculate_capital_recovery_factor(discount_rate double precision, equipment_life double precision)
  RETURNS double precision AS
$BODY$
DECLARE
BEGIN
	IF coalesce(discount_rate, 0) = 0 or coalesce(equipment_life, 0) = 0 THEN
		return null;
	END IF;

	return (discount_rate * (1 + discount_rate) ^ equipment_life) / ((discount_rate + 1) ^ equipment_life - 1);
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.calculate_capital_recovery_factor(double precision, double precision) OWNER TO postgres;
