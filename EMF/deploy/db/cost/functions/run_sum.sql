CREATE OR REPLACE FUNCTION public.run_sum(numeric, text, text)
  RETURNS numeric AS
$BODY$
   if {![info exists GD(sum.$2.$3)]} {
       set GD(sum.$2.$3) 0.00
   }
   if {[argisnull 1]} {
       return $GD(sum.$2.$3)
   } else {
       return [set GD(sum.$2.$3) [expr $GD(sum.$2.$3) + $1]]
   }
$BODY$
  LANGUAGE 'pltcl' VOLATILE
  COST 100;
ALTER FUNCTION public.run_sum(numeric, text, text) OWNER TO emf;


CREATE OR REPLACE FUNCTION public.run_sum(numeric, text)
  RETURNS numeric AS
'select run_sum($1,$2,statement_timestamp()::text)'
  LANGUAGE 'sql' IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.run_sum(numeric, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.run_sum(numeric, numeric, text, text)
  RETURNS numeric AS
$BODY$
   if {![info exists GD(sum.$3.$4)]} {
       set GD(sum.$3.$4) $1
   }
   if {[argisnull 1]} {
       return $GD(sum.$3.$4)
   } else {
       return [set GD(sum.$3.$4) [expr $GD(sum.$3.$4) + $2]]
   }
$BODY$
  LANGUAGE pltcl VOLATILE
  COST 100;
ALTER FUNCTION public.run_sum(numeric, numeric, text, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.run_sum(numeric, numeric, text)
  RETURNS numeric AS
'select run_sum($1,$2,$3,statement_timestamp()::text)'
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.run_sum(numeric, numeric, text) OWNER TO emf;

