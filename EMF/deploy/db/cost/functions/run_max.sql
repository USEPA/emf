CREATE OR REPLACE FUNCTION public.run_max(double precision, text, text)
  RETURNS numeric AS
$BODY$
	if {![info exists GD(max.$2.$3)]} {
		set GD(max.$2.$3) 0.00
	}
	if {[argisnull 1]} {
		return $GD(max.$2.$3)
	} else {
		if {$GD(max.$2.$3) > $1} {
			return $GD(max.$2.$3)
		} else {
			set GD(max.$2.$3) [expr $1]
			return [set GD(max.$2.$3) [expr $1]]
		}
	}
$BODY$
  LANGUAGE 'pltcl' IMMUTABLE;
ALTER FUNCTION public.run_max(double precision, text, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.running_max_previous_value(double precision, text, text)
  RETURNS double precision AS
$BODY$
	
	if {![info exists GD(max.$2.$3)]} {
		set GD(max.$2.$3) 0.00
	}
	if {[argisnull 1]} {
		return $GD(max.$2.$3)
	} else {
		if {$GD(max.$2.$3) > $1} {
			return $GD(max.$2.$3)
		} else {
			set max_prev_value [expr $GD(max.$2.$3)]
			set GD(max.$2.$3) [expr $1]
			return $max_prev_value
		}
	}


$BODY$
  LANGUAGE 'pltcl' IMMUTABLE;
ALTER FUNCTION public.running_max_previous_value(double precision, text, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.run_max(double precision, text)
  RETURNS numeric AS
'select run_max($1,$2,statement_timestamp()::text)'
  LANGUAGE 'sql' IMMUTABLE STRICT;
ALTER FUNCTION public.run_max(double precision, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.running_max_previous_value(double precision, text)
  RETURNS double precision AS
'select running_max_previous_value($1,$2,statement_timestamp()::text)'
  LANGUAGE 'sql' IMMUTABLE STRICT;
ALTER FUNCTION public.running_max_previous_value(double precision, text) OWNER TO emf;
