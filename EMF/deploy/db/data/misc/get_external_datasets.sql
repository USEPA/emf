CREATE OR REPLACE VIEW emf.get_external_datasets AS 
select external_dataset_access.id as external_dataset_access_id,
	datasets.name as dataset_name,
	datasets.description as dataset_description,
	"version" as dataset_version,
	dataset_types.name as dataset_type_name,
	external_app
from emf.external_dataset_access
	inner join emf.datasets
	on datasets.id = external_dataset_access.dataset_id
	inner join emf.dataset_types
	on dataset_types.id = datasets.dataset_type;

select * from emf.get_external_datasets
