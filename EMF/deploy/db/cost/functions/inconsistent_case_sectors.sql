select "name", 
	case_sectors, 
	other_case_sectors

from cases.cases c
	full join (
		select cs.case_id, 
			concatenate_with_ampersand(cs.name) as case_sectors
		from (
			select cs.case_id, s.name
			from cases.case_sectors cs

			inner join emf.sectors s
			on s.id = cs.sector_id

			order by cs.case_id, s.name) cs
		group by cs.case_id
		) cs
	on cs.case_id = c.id

	full join (
		select case_id, 
			concatenate_with_ampersand(name) as other_case_sectors
		from (
			select distinct cs.case_id, s.name
			from (
				select case_id, sector_id
				from cases.cases_caseinputs
				union all
				select case_id, sector_id
				from cases.cases_casejobs
				union all
				select case_id, sector_id
				from cases.cases_parameters
			) cs

			inner join emf.sectors s
			on s.id = cs.sector_id

			order by cs.case_id, s.name) cos
		group by case_id
		) cos
	on cos.case_id = c.id

where coalesce(case_sectors,'') <> coalesce(other_case_sectors,'')
order by "name"
