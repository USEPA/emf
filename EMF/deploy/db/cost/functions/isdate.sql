CREATE OR REPLACE FUNCTION public.isdate(text) returns boolean as 
$BODY$
DECLARE
BEGIN
  perform $1::date;
  return true;
exception when others then
  return false;
END;
$BODY$
language 'plpgsql' immutable;