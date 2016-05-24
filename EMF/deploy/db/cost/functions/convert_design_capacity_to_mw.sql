DROP FUNCTION IF EXISTS public.convert_design_capacity_to_mw(double precision, character varying, character varying);
DROP FUNCTION IF EXISTS public.convert_design_capacity_to_mw(double precision, character varying);

CREATE OR REPLACE FUNCTION public.convert_design_capacity_to_mw(design_capacity double precision, design_capacity_unit_numerator character varying, design_capacity_unit_denominator character varying, combustion_efficiency double precision)
  RETURNS double precision AS
$BODY$
DECLARE
	converted_design_capacity double precision;
	unit_numerator character varying;
	unit_denominator character varying;
	efficiency_factor double precision;
BEGIN

        --default if not known
        unit_numerator := coalesce(trim(upper(design_capacity_unit_numerator)), '');
	unit_denominator := coalesce(trim(upper(design_capacity_unit_denominator)), '');

	efficiency_factor := coalesce(combustion_efficiency, 100);
	efficiency_factor := efficiency_factor / 100.0;

        --if you don't know the units then you can't convert the design capacity
	IF length(unit_numerator) = 0 THEN
		return converted_design_capacity;
	END IF;


/* FROM Larry Sorrels at the EPA
        1) E6BTU does mean mmBTU.

        2)  1 MW = 3.412 million BTU/hr (or mmBTU/hr).   And conversely, 1
        mmBTU/hr = 1/3.412 (or 0.2931) MW.

        3)  All of the units listed below are convertible, but some of the
        conversions will be more difficult than others.  The ft3, lb, and ton
        will require some additional conversions to translate mass or volume
        into an energy term such as MW or mmBTU/hr.  Applying some density
        measure (which is mass/volume) will likely be necessary.   Let me know
        if you need help with the conversions. 
*/

        --capacity is already in the right units...
        --no conversion is necessary, these are the expected units.
	IF (unit_numerator = 'MW' and unit_denominator = '') THEN
		return efficiency_factor * design_capacity;
	END IF;

	IF (unit_numerator = 'KW' and unit_denominator = '') THEN
		return efficiency_factor * design_capacity / 1000.0;
	END IF;

        IF (unit_numerator = 'MMBTU'
            or unit_numerator = 'E6BTU'
            or unit_numerator = 'BTU'
            or unit_numerator = 'HP'
            or unit_numerator = 'BLRHP') THEN

		--convert numerator unit
		IF (unit_numerator = 'MMBTU'
		    or unit_numerator = 'E6BTU') THEN
			converted_design_capacity := design_capacity / 3.412;
		END IF;
		IF (unit_numerator = 'BTU') THEN
			converted_design_capacity := design_capacity / 3.412 / 1000000.0;
		END IF;
		IF (unit_numerator = 'HP') THEN
			converted_design_capacity := design_capacity * 0.000746;
		END IF;
		IF (unit_numerator = 'BLRHP') THEN
			converted_design_capacity := design_capacity * 0.000981;
		END IF;
--            IF (unit_numerator = 'FT3') THEN
--                converted_design_capacity := design_capacity * 0.000981;

		--convert denominator unit, if missing ASSUME per hr
		IF (unit_denominator = '' or unit_denominator = 'HR'
		    or unit_denominator = 'H') THEN
			return efficiency_factor * converted_design_capacity;
		END IF;
		IF (unit_denominator = 'D' or unit_denominator = 'DAY') THEN
			return efficiency_factor * converted_design_capacity * 24.0;
		END IF;
		IF (unit_denominator = 'M' or unit_denominator = 'MIN') THEN
			return efficiency_factor * converted_design_capacity / 60.0;
		END IF;
		IF (unit_denominator = 'S' or unit_denominator = 'SEC') THEN
			return efficiency_factor * converted_design_capacity / 3600.0;
		END IF;
	END IF;
	return null;
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 100;
ALTER FUNCTION public.convert_design_capacity_to_mw(double precision, character varying, character varying, double precision) OWNER TO postgres;


CREATE OR REPLACE FUNCTION public.convert_design_capacity_to_mw(design_capacity double precision, design_capacity_units character varying, combustion_efficiency double precision)
  RETURNS double precision AS
$BODY$
DECLARE
	converted_design_capacity double precision := null;
	unit_numerator character varying;
	unit_denominator character varying;
BEGIN

        --default if not known and uppercase/trim string
	design_capacity_units := coalesce(trim(upper(design_capacity_units)), '');
	unit_numerator := split_part(design_capacity_units, '/', 1);
	unit_denominator := split_part(design_capacity_units, '/', 2);

	return public.convert_design_capacity_to_mw(design_capacity, unit_numerator, unit_denominator, combustion_efficiency);
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 100;
ALTER FUNCTION public.convert_design_capacity_to_mw(double precision, character varying, double precision) OWNER TO postgres;

