SELECT revisions.dataset_id, datasets.name, revisions."version", string_agg(what order by revisions.id, ', ') as what_find_replace, string_agg(why order by revisions.id, ', ') as why_find_replace
FROM emf.revisions
	inner join emf.datasets
	on revisions.dataset_id = datasets.id
where revisions.what ilike 'Replaced %with% for column %'
group by revisions.dataset_id, revisions."version", datasets.name;