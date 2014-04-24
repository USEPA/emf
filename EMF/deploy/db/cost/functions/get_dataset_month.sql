-- Function: get_dataset_month(dataset_id integer)

-- DROP FUNCTION select get_dataset_month(dataset_id integer);

CREATE OR REPLACE FUNCTION public.get_dataset_month(dataset_id integer)
  RETURNS smallint AS
$BODY$
DECLARE
	dataset_name varchar(255) := '';
	dataset_month smallint := 0;
	start_year smallint := null;
	start_month smallint := null;
	stop_year smallint := null;
	stop_month smallint := null;
BEGIN

	-- get the dataset information
	select lower(ds.name),
		EXTRACT(YEAR FROM ds.start_date_time),
		EXTRACT(MONTH FROM ds.start_date_time),
		EXTRACT(YEAR FROM ds.stop_date_time),
		EXTRACT(MONTH FROM ds.stop_date_time)
	from emf.datasets ds
	where ds.id = dataset_id
	into dataset_name,
		start_year,
		start_month,
		stop_year,
		stop_month;

	-- look at the start and stop time...
	IF start_month = stop_month and start_year = stop_year THEN
		dataset_month := start_month;
	-- look at the name to see if there is a date in it...
	ELSE
		IF position('_jan' in dataset_name) > 0 or position('_january' in dataset_name) > 0 THEN
			dataset_month := 1;
		ELSIF position('_feb' in dataset_name) > 0 or position('_february' in dataset_name) > 0 THEN
			dataset_month := 2;
		ELSIF position('_mar' in dataset_name) > 0 or position('_march' in dataset_name) > 0 THEN
			dataset_month := 3;
		ELSIF position('_apr' in dataset_name) > 0 or position('_april' in dataset_name) > 0 THEN
			dataset_month := 4;
		ELSIF position('_may' in dataset_name) > 0 THEN
			dataset_month := 5;
		ELSIF position('_jun' in dataset_name) > 0 or position('_june' in dataset_name) > 0 THEN
			dataset_month := 6;
		ELSIF position('_jul' in dataset_name) > 0 or position('_july' in dataset_name) > 0 THEN
			dataset_month := 7;
		ELSIF position('_aug' in dataset_name) > 0 or position('_august' in dataset_name) > 0 THEN
			dataset_month := 8;
		ELSIF position('_sep' in dataset_name) > 0 or position('_september' in dataset_name) > 0 THEN
			dataset_month := 9;
		ELSIF position('_oct' in dataset_name) > 0 or position('_october' in dataset_name) > 0 THEN
			dataset_month := 10;
		ELSIF position('_nov' in dataset_name) > 0 or position('_november' in dataset_name) > 0 THEN
			dataset_month := 11;
		ELSIF position('_dec' in dataset_name) > 0 or position('_december' in dataset_name) > 0 THEN
			dataset_month := 12;
		END IF;
	END IF;

	-- return Zero if no month was found
	RETURN dataset_month;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.get_dataset_month(dataset_id integer) OWNER TO emf;
