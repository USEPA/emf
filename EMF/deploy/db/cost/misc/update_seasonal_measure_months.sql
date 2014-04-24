

delete from emf.control_measure_months 
where control_measure_months.control_measure_id in (
select cm.id
from emf.control_measures cm
where cm.name like '%Fall%'
	or cm.name like '%Spring%'
	or cm.name like '%Summer%'
	or cm.name like '%Winter%'
	);

insert into emf.control_measure_months (
  control_measure_id,
  list_index,
  "month"
)
select 
	cm.id, --cm.name, 
season_month.list_index	, 
season_month.month	
from emf.control_measures cm 
inner join (
	select 'Spring' as season, 3 as "month", 0 as list_index
	union all select 'Spring' as season, 4, 1 as list_index
	union all select 'Spring' as season, 5, 2 as list_index
	union all select 'Summer' as season, 6, 0 as list_index
	union all select 'Summer' as season, 7, 1 as list_index
	union all select 'Summer' as season, 8, 2 as list_index
	union all select 'Fall' as season, 9, 0 as list_index
	union all select 'Fall' as season, 10, 1 as list_index
	union all select 'Fall' as season, 11, 2 as list_index
	union all select 'Winter' as season, 12, 0 as list_index
	union all select 'Winter' as season, 1, 1 as list_index
	union all select 'Winter' as season, 2, 2 as list_index
) season_month
on strpos(cm.name, season_month.season) > 0
where cm.name like '%Fall%'
	or cm.name like '%Spring%'
	or cm.name like '%Summer%'
	or cm.name like '%Winter%';

