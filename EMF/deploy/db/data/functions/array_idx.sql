--see http://wiki.postgresql.org/wiki/Array_Index
--Return the index of the first occurrence of a value in an array.
CREATE OR REPLACE FUNCTION public.array_idx(anyarray, anyelement)
  RETURNS int AS 
$$
  SELECT i FROM (
     SELECT generate_series(array_lower($1,1),array_upper($1,1))
  ) g(i)
  WHERE $1[i] = $2
  LIMIT 1;
$$ LANGUAGE sql STRICT IMMUTABLE;