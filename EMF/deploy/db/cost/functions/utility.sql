CREATE OR REPLACE FUNCTION public.multiply(double precision, double precision)
  RETURNS double precision AS
'select $1 * $2'
  LANGUAGE 'sql' IMMUTABLE STRICT;
ALTER FUNCTION public.multiply(double precision, double precision) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.join_with_ampersand(text, text)
  RETURNS text AS
$BODY$select $1||'&'||$2$BODY$
  LANGUAGE 'sql' IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.join_with_ampersand(text, text) OWNER TO emf;


CREATE OR REPLACE FUNCTION public.join_with_pipe(text, text)
  RETURNS text AS
$BODY$select $1||'|'||$2$BODY$
  LANGUAGE 'sql' IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.join_with_pipe(text, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public."add"(double precision, double precision)
  RETURNS double precision AS
'select $1 + $2'
  LANGUAGE 'sql' IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public."add"(double precision, double precision) OWNER TO emf;


-- USER DEFINED Aggregates

--DROP AGGREGATE public.concatenate_with_pipe("text");
CREATE AGGREGATE public.concatenate_with_pipe("text") (
  SFUNC=join_with_pipe,
  STYPE=text
);
ALTER AGGREGATE public.concatenate_with_pipe("text") OWNER TO emf;

--DROP AGGREGATE public.concatenate_with_ampersand("text");
CREATE AGGREGATE public.concatenate_with_ampersand("text") (
  SFUNC=join_with_ampersand,
  STYPE=text
);
ALTER AGGREGATE public.concatenate_with_ampersand("text") OWNER TO emf;

--DROP AGGREGATE public.run_sum(double precision);
CREATE AGGREGATE public.run_sum(double precision) (
  SFUNC=add,
  STYPE=float8
);
ALTER AGGREGATE public.run_sum(double precision) OWNER TO emf;

--DROP AGGREGATE public.times(double precision);
CREATE AGGREGATE public.times(double precision) (
  SFUNC=multiply,
  STYPE=float8
);
ALTER AGGREGATE public.times(double precision) OWNER TO emf;