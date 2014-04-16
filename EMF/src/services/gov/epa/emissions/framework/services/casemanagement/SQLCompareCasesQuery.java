package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLCompareCasesQuery {

    private HibernateSessionFactory sessionFactory;
    
    public SQLCompareCasesQuery(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String createCompareQuery(int[] caseIds) throws EmfException {
        StringBuilder sql = new StringBuilder();
        String tabSelectList = "case0.tab";
        String nameSelectList = "case0.\"name\"";
        String envVarSelectList = "case0.env_var";
        String regionSelectList = "case0.region";
        String sectorSelectList = "case0.sector";
        String jobSelectList = "case0.job";
        String programSelectList = "case0.program";
        String tabRunningSelectList = "case0.tab";
        String nameRunningSelectList = "case0.\"name\"";
        String envVarRunningSelectList = "case0.env_var";
        String regionRunningSelectList = "case0.region";
        String sectorRunningSelectList = "case0.sector";
        String jobRunningSelectList = "case0.job";
        String programRunningSelectList = "case0.program";
        String matchSelectList = "case0.\"value\" = case0.\"value\"";
        Case caseObj = getCase(caseIds[0]);
        String valueSelectList = "regexp_replace(regexp_replace(case0.\"value\", E'\n', ' ', 'gi'), E'\"', '', 'gi') as \"" + caseObj.getAbbreviation().getName() + "\"";
        sql.append("select ");
        for (int i = 1; i < caseIds.length; ++i) {
            tabSelectList += ",case" + i + ".tab";
            nameSelectList += ",case" + i + ".\"name\"";
            envVarSelectList += ",case" + i + ".env_var";
            regionSelectList += ",case" + i + ".region";
            sectorSelectList += ",case" + i + ".sector";
            jobSelectList += ",case" + i + ".job";
            programSelectList += ",case" + i + ".program";
            matchSelectList += "and case0.\"value\" = case" + i + ".\"value\"";
            caseObj = getCase(caseIds[i]);
            valueSelectList += ", regexp_replace(regexp_replace(case" + i + ".\"value\", E'\n', ' ', 'gi'), E'\"', '', 'gi') as \"" + caseObj.getAbbreviation().getName() + "\"";
        }
        sql.append(" coalesce(" + tabSelectList + ") as tab, ");
        sql.append(" regexp_replace(regexp_replace(coalesce(" + nameSelectList + "), E'\n', ' ', 'gi'), E'\"', '', 'gi') as \"name\", ");
        sql.append(" coalesce(" + envVarSelectList + ") as env_var,");
        sql.append(" coalesce(" + regionSelectList + ") as region,");
        sql.append(" coalesce(" + sectorSelectList + ") as sector,");
        sql.append(" coalesce(" + jobSelectList + ") as job,");
        sql.append(" coalesce(" + programSelectList + ") as program,");
        sql.append(" case when " + matchSelectList + " then 'true'::character varying(5) else 'false'::character varying(5) end as match,");
//    --  null::boolean as match,");
//        case0."value" as cs0,");
//        case1."value" as cs1");
        sql.append(" " + valueSelectList);
        sql.append(" from ");
        for (int i = 0; i < caseIds.length; ++i) {

            //add join condition
            if (i > 0)
                sql.append(" full join ");
            sql.append(" (");
            sql.append(buildIndividualCaseQuery(caseIds[i]));
            sql.append(" ) as case" + i);
            //add join condition
            if (i > 0) {
                sql.append(" on coalesce(case" + i + ".tab, '') = coalesce(" + tabRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".name, '') = coalesce(" + nameRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".region, '') = coalesce(" + regionRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".sector, '') = coalesce(" + sectorRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".program, '') = coalesce(" + programRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".job, '') = coalesce(" + jobRunningSelectList + ", '')");
                sql.append(" and coalesce(case" + i + ".env_var, '') = coalesce(" + envVarRunningSelectList + ", '')");

                tabRunningSelectList += ",case" + i + ".tab";
                nameRunningSelectList += ",case" + i + ".\"name\"";
                envVarRunningSelectList += ",case" + i + ".env_var";
                regionRunningSelectList += ",case" + i + ".region";
                sectorRunningSelectList += ",case" + i + ".sector";
                jobRunningSelectList += ",case" + i + ".job";
                programRunningSelectList += ",case" + i + ".program";
            }
        
        }
        
        sql.append(" order by case when coalesce(" + tabSelectList + ") = 'Summary' then 0 when coalesce(" + tabSelectList + ") = 'Inputs' then 1 when coalesce(" + tabSelectList + ") = 'Parameters' then 2 else 3 end,"); 
        sql.append(" coalesce(" + nameSelectList + ")");

        return sql.toString();
    }
    
    public String createCompareOutputsQuery(int[] caseIds, String gridName) throws EmfException {
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < caseIds.length; ++i) {
            if ( i > 0 ) sql.append( " union all " );
        sql.append(" (select ");
        sql.append(" cases_caseinputs.case_id as caseid, " );
        sql.append(" inputnames.\"name\", ");
        sql.append(" input_envt_vars.\"name\" as env_var,");
        sql.append(" georegions.abbr as region,");
        //sql.append(" coalesce(sectors.\"name\", '') as sector,");
        sql.append(" coalesce(cases_casejobs.\"name\", '') as job,");
        sql.append(" internal_sources.table_name as table, ");
        sql.append(" programs.name as program,");
        sql.append(" datasets.id as datasetid, ");
        sql.append(" versions.\"version\" as version ");


        sql.append(" from cases.cases_caseinputs");

        sql.append(" left outer join emf.datasets");
        sql.append(" on datasets.id = cases_caseinputs.dataset_id");

        sql.append(" left outer join emf.dataset_types");
        sql.append(" on dataset_types.id = cases_caseinputs.dataset_type_id");

        sql.append(" left outer join cases.cases_casejobs");
        sql.append(" on cases_casejobs.id = cases_caseinputs.case_job_id");

        sql.append(" left outer join cases.input_envt_vars ");
        sql.append(" on input_envt_vars.id = cases_caseinputs.envt_vars_id");

        sql.append(" left outer join cases.inputnames ");
        sql.append(" on inputnames.id = cases_caseinputs.input_name_id");

        sql.append(" left outer join cases.programs ");
        sql.append(" on programs.id = cases_caseinputs.program_id");

        sql.append(" left outer join emf.georegions ");
        sql.append(" on georegions.id = cases_caseinputs.region_id");

        sql.append(" left outer join emf.sectors ");
        sql.append(" on sectors.id = cases_caseinputs.sector_id");

        sql.append(" left outer join cases.subdirs ");
        sql.append(" on subdirs.id = cases_caseinputs.subdir_id");
        
        sql.append(" left outer join emf.internal_sources ");
        sql.append(" on datasets.id = internal_sources.dataset_id ");

        sql.append(" left outer join emf.versions ");
        sql.append(" on versions.id = cases_caseinputs.version_id");
        sql.append(" where cases_caseinputs.case_id = " + caseIds[i]);
        sql.append(" and input_envt_vars.\"name\" = 'SECTORLIST' " 
                +" and georegions.\"name\" = '" + gridName + "')");
        }
  
        return sql.toString();
    }

    private StringBuilder buildIndividualCaseQuery(int caseId) {
        StringBuilder sql = new StringBuilder();

        
//Summary Tab Info being flattened out...
        sql.append("select ");
//        sql.append("null::integer as input_name_id, ");
//        sql.append("null::integer as region_id,  ");
//        sql.append("null::integer as sector_id,  ");
//        sql.append("null::integer as program_id,  ");
//        sql.append("null::integer as case_job_id,  ");
//        sql.append("null::integer as envt_vars_id, ");
//        sql.append("null::integer as param_name_id, ");

        sql.append("'Summary' as tab,  ");
        sql.append("summary_tab.\"key\" as \"name\",  ");
        sql.append("null::character varying(255) as env_var, ");
        sql.append("null::character varying(255) as region, ");
        sql.append("null::character varying(255) as sector, ");
        sql.append("null::character varying(255) as job, ");
        sql.append("null::character varying(255) as program, ");
        sql.append("summary_tab.\"value\" ");
        sql.append("from ( ");
        sql.append(buildGetCaseColumnValueSQL(caseId, "Name", "name"));
//        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Description", "description"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Run Status", "run_status"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Is Final", "is_final"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Copied From", "template_used"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Last Modified Date", "last_modified_date"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "# of met layers", "num_met_layers"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "# of emission layers", "num_emissions_layers"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Start Date", "start_date"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Future Year", "future_year"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "End Date & Time", "end_date"));
        sql.append(" union all " + buildGetCaseColumnValueSQL(caseId, "Version", "model_version"));

        sql.append(" union all SELECT 'Project' as \"key\", projects.name as \"value\" FROM cases.cases inner join emf.projects on projects.id = cases.project_id where cases.id = " + caseId);
//        sql.append(" union all SELECT 'Sectors' as \"key\", string_agg(sectors.name, ',' ORDER BY sectors.name) as \"value\" FROM cases.case_sectors inner join emf.sectors on sectors.id = case_sectors.sector_id where case_sectors.case_id = " + caseId);
        sql.append(" union all SELECT 'Last Modified By' as \"key\", users.\"name\" as \"value\" FROM cases.cases inner join emf.users on users.id = cases.user_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Model' as \"key\", model_to_runs.\"name\" as \"value\" FROM cases.cases inner join cases.model_to_runs on model_to_runs.id = cases.model_to_run_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Modeling Region' as \"key\", regions.\"name\" as \"value\" FROM cases.cases inner join emf.regions on regions.id = cases.modeling_region_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Regions' as \"key\", string_agg(georegions.name, ', ' ORDER BY georegions.name) as \"value\" FROM cases.case_regions inner join emf.georegions on georegions.id = case_regions.region_id where case_regions.case_id = " + caseId);
        sql.append(" union all SELECT 'Downstream Model' as \"key\", air_quality_models.\"name\" as \"value\" FROM cases.cases inner join cases.air_quality_models on air_quality_models.id = cases.air_quality_model_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Speciation' as \"key\", speciations.\"name\" as \"value\" FROM cases.cases inner join cases.speciations on speciations.id = cases.speciation_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Meteorological Year' as \"key\", meteorlogical_years.\"name\" as \"value\" FROM cases.cases inner join cases.meteorlogical_years on meteorlogical_years.id = cases.meteorlogical_year_id where cases.id = " + caseId);
        sql.append(" union all SELECT 'Base Year' as \"key\", emissions_years.\"name\" as \"value\" FROM cases.cases inner join cases.emissions_years on emissions_years.id = cases.emissions_year_id where cases.id = " + caseId);
  
        sql.append(" ) summary_tab");

        
//Inputs Tab Data
        sql.append(" union all");

        sql.append(" select ");
//        sql.append(" cases_caseinputs.case_id, ");
//        sql.append(" cases_caseinputs.input_name_id, ");
//        sql.append(" cases_caseinputs.region_id, ");
//        sql.append(" cases_caseinputs.sector_id, ");
//        sql.append(" cases_caseinputs.program_id, ");
//        sql.append(" cases_caseinputs.case_job_id, ");

//        sql.append(" cases_caseinputs.envt_vars_id,");
//        sql.append(" null::integer as param_name_id,");

        sql.append(" 'Inputs' as tab, ");
        sql.append(" inputnames.\"name\", ");
        sql.append(" input_envt_vars.\"name\" as env_var,");
        sql.append(" georegions.\"name\" as region,");
        sql.append(" coalesce(sectors.\"name\", '') as sector,");
        sql.append(" coalesce(cases_casejobs.\"name\", '') as job,");
        sql.append(" programs.name as program,");
        sql.append(" datasets.name || ' [v' || coalesce(versions.version || '', '') || ']' as \"value\"");


        sql.append(" from cases.cases_caseinputs");

        sql.append(" left outer join emf.datasets");
        sql.append(" on datasets.id = cases_caseinputs.dataset_id");

        sql.append(" left outer join emf.dataset_types");
        sql.append(" on dataset_types.id = cases_caseinputs.dataset_type_id");

        sql.append(" left outer join cases.cases_casejobs");
        sql.append(" on cases_casejobs.id = cases_caseinputs.case_job_id");

        sql.append(" left outer join cases.input_envt_vars ");
        sql.append(" on input_envt_vars.id = cases_caseinputs.envt_vars_id");
          
        sql.append(" left outer join cases.inputnames ");
        sql.append(" on inputnames.id = cases_caseinputs.input_name_id");

        sql.append(" left outer join cases.programs ");
        sql.append(" on programs.id = cases_caseinputs.program_id");

        sql.append(" left outer join emf.georegions ");
        sql.append(" on georegions.id = cases_caseinputs.region_id");

        sql.append(" left outer join emf.sectors ");
        sql.append(" on sectors.id = cases_caseinputs.sector_id");

        sql.append(" left outer join cases.subdirs ");
        sql.append(" on subdirs.id = cases_caseinputs.subdir_id");

        sql.append(" left outer join emf.versions ");
        sql.append(" on versions.id = cases_caseinputs.version_id");
        sql.append(" where cases_caseinputs.case_id = " + caseId);

//Parameters Tab Data
        sql.append(" union all");

        sql.append(" select ");

//            cases_parameters.case_id, 
//        sql.append(" null::integer as input_name_id,"); 
//        sql.append(" cases_parameters.region_id, ");
//        sql.append(" cases_parameters.sector_id, ");
//        sql.append(" cases_parameters.program_id, ");
//        sql.append(" cases_parameters.case_job_id as parameter_case_job_id, ");
//        sql.append(" cases_casejobs.\"name\" as case_job_name, ");
//        sql.append(" cases_parameters.env_vars_id, ");
//        sql.append(" cases_parameters.param_name_id,");


        sql.append(" 'Parameters' as tab, ");
        sql.append(" parameter_names.\"name\", ");
        sql.append(" parameter_env_vars.\"name\" as env_var,");
        sql.append(" georegions.\"name\" as region,");
        sql.append(" coalesce(sectors.\"name\", '') as sector,");
        sql.append(" coalesce(cases_casejobs.\"name\", '') as job,");
        sql.append(" programs.name as program,");
        sql.append(" cases_parameters.env_value as \"value\"");

        sql.append(" from cases.cases_parameters");

        sql.append(" left outer join cases.cases ");
        sql.append(" on cases.id = cases_parameters.case_id");

        sql.append(" left outer join cases.parameter_env_vars ");
        sql.append(" on parameter_env_vars.id = cases_parameters.env_vars_id");

        sql.append(" left outer join cases.parameter_names ");
        sql.append(" on parameter_names.id = cases_parameters.param_name_id");

        sql.append(" left outer join cases.programs ");
        sql.append(" on programs.id = cases_parameters.program_id");

        sql.append(" left outer join emf.georegions ");
        sql.append(" on georegions.id = cases_parameters.region_id");

        sql.append(" left outer join emf.sectors ");
        sql.append(" on sectors.id = cases_parameters.sector_id");

        sql.append(" left outer join cases.value_types ");
        sql.append(" on value_types.id = cases_parameters.val_type_id");

        sql.append(" left outer join cases.cases_casejobs");
        sql.append(" on cases_casejobs.id = cases_parameters.case_job_id");

        sql.append(" where cases_parameters.case_id = " + caseId);
        
        //--Jobs Tab Data
        sql.append(" union all");

        sql.append(" select");

//            sql.append(" null::integer as input_name_id, ");
//            sql.append(" cases_casejobs.region_id, ");
//            sql.append(" cases_casejobs.sector_id, ");
//            sql.append(" null::integer as program_id, ");
//            sql.append(" null::integer as parameter_case_job_id, ");
//            sql.append(" cases_casejobs.\"name\" as case_job_name, ");
//            sql.append(" null::integer as env_vars_id, ");
//            sql.append(" null::integer as param_name_id,");


            sql.append(" 'Jobs' as tab, ");
            sql.append(" cases_casejobs.\"name\", ");
            sql.append(" null::character varying(255) as env_var,");
            sql.append(" georegions.\"name\" as region,");
            sql.append(" coalesce(sectors.\"name\", '') as sector,");
            sql.append(" coalesce(cases_casejobs.\"name\", '') as job,");
            sql.append(" null::character varying(255) as program,");
            sql.append(" case_jobrunstatus.\"name\" as \"value\"");

            sql.append(" from cases.cases_casejobs");

            sql.append(" left outer join cases.cases");
            sql.append(" on cases.id = cases_casejobs.case_id");
          
            sql.append(" left outer join emf.georegions");
            sql.append(" on georegions.id = cases_casejobs.region_id");

            sql.append(" left outer join cases.case_jobrunstatus");
            sql.append(" on case_jobrunstatus.id = cases_casejobs.runstatus_id");

            sql.append(" left outer join emf.sectors");
            sql.append(" on sectors.id = cases_casejobs.sector_id");

            sql.append(" where cases_casejobs.case_id = " + caseId);
/*
SQL Pattern to follow...

--Summary Tab Data
select 
    --unique composite keys (needed when doing comparison join, need to match on these keys)
    13 as case_id, 
    null::integer as input_name_id, 
    null::integer as region_id, 
    null::integer as sector_id, 
    null::integer as program_id, 
    null::integer as case_job_id, 
    null::integer as envt_vars_id,
    null::integer as param_name_id,

    'Summary' as tab, 
    summary_tab."key" as "name", 
    null::character varying(255) as env_var,
    null::character varying(255) as region,
    null::character varying(255) as sector,
    null::character varying(255) as job,
    null::character varying(255) as program,
--  null::boolean as match,
    summary_tab."value"
from (
SELECT 'Name' as "key", "name" as "value"
FROM cases.cases
where id = 13
union all
SELECT 'Description' as "key", description as "value"
FROM cases.cases
where id = 13
union all 
SELECT 'Project' as "key", projects.name as "value"
FROM cases.cases 
inner join emf.projects
on projects.id = cases.project_id
where cases.id = 13
union all
SELECT 'Run Status' as "key", run_status as "value"
FROM cases.cases
where id = 13
union all
SELECT 'Is Final' as "key", is_final || '' as "value"
FROM cases.cases
where id = 13
union all 
SELECT 'Sectors' as "key",  string_agg(sectors.name, ',' ORDER BY sectors.name) as "value"
FROM cases.case_sectors
inner join emf.sectors
on sectors.id = case_sectors.sector_id
where case_sectors.case_id = 13
union all
SELECT 'Copied From' as "key", template_used as "value"
FROM cases.cases
where id = 13
union all
SELECT 'Last Modified By' as "key", users."name" as "value"
FROM cases.cases
inner join emf.users
on users.id = cases.user_id
where cases.id = 13
union all
SELECT 'Last Modified Date' as "key", cases.last_modified_date || '' as "value"
FROM cases.cases
where cases.id = 13
union all
SELECT 'Model & Version' as "key", model_to_runs."name" as "value"
FROM cases.cases
inner join cases.model_to_runs
on model_to_runs.id = cases.model_to_run_id
where cases.id = 13
union all
SELECT 'Modeling Region' as "key", regions."name" as "value"
FROM cases.cases
inner join emf.regions
on regions.id = cases.modeling_region_id
where cases.id = 13
union all 
SELECT 'Regions' as "key",  string_agg(georegions.name, ',' ORDER BY georegions.name) as "value"
FROM cases.case_regions
inner join emf.georegions
on georegions.id = case_regions.region_id
where case_regions.case_id = 13
union all
SELECT '# of met layers' as "key", cases.num_met_layers || '' as "value"
FROM cases.cases
where cases.id = 13
union all
SELECT '# of emission layers' as "key", cases.num_emissions_layers || '' as "value"
FROM cases.cases
where cases.id = 13
union all
SELECT 'Start Date & Time' as "key", cases.start_date || '' as "value"
FROM cases.cases
where cases.id = 13
union all
SELECT 'Downstream Model' as "key", air_quality_models."name" as "value"
FROM cases.cases
inner join cases.air_quality_models
on air_quality_models.id = cases.air_quality_model_id
where cases.id = 13
union all
SELECT 'Speciation' as "key", speciations."name" as "value"
FROM cases.cases
inner join cases.speciations
on speciations.id = cases.speciation_id
where cases.id = 13
union all
SELECT 'Meteorological Year' as "key", meteorlogical_years."name" as "value"
FROM cases.cases
inner join cases.meteorlogical_years
on meteorlogical_years.id = cases.meteorlogical_year_id
where cases.id = 13
union all
SELECT 'Base Year' as "key", emissions_years."name" as "value"
FROM cases.cases
inner join cases.emissions_years
on emissions_years.id = cases.emissions_year_id
where cases.id = 13
union all
SELECT 'Future Year' as "key", cases.future_year || '' as "value"
FROM cases.cases
where cases.id = 13
union all
SELECT 'End Date & Time' as "key", cases.end_date || '' as "value"
FROM cases.cases
where cases.id = 13
) summary_tab


--Inputs Tab Data
union all

select 
    --unique composite keys (needed when doing comparison join, need to match on these keys)
    cases_caseinputs.case_id, 
    cases_caseinputs.input_name_id, 
    cases_caseinputs.region_id, 
    cases_caseinputs.sector_id, 
    cases_caseinputs.program_id, 
    cases_caseinputs.case_job_id, 

    --not unique part of key but needed
    cases_caseinputs.envt_vars_id,
    null::integer as param_name_id,

    'Inputs' as tab, 
    inputnames."name", 
    input_envt_vars."name" as env_var,
    georegions."name" as region,
    coalesce(sectors."name", 'All sectors') as sector,
    coalesce(cases_casejobs."name", 'All jobs for sector') as job,
    programs.name as program,
--  null::boolean as match,
    datasets.name || ' [Version: ' || coalesce(versions.version || '', '') || ' DS Type: ' || coalesce(dataset_types.name, '') || ']' as "value"
--,*


from cases.cases_caseinputs

left outer join emf.datasets
on datasets.id = cases_caseinputs.dataset_id

left outer join emf.dataset_types
on dataset_types.id = cases_caseinputs.dataset_type_id

left outer join cases.cases_casejobs
on cases_casejobs.id = cases_caseinputs.case_job_id

left outer join cases.input_envt_vars 
on input_envt_vars.id = cases_caseinputs.envt_vars_id
  
left outer join cases.inputnames 
on inputnames.id = cases_caseinputs.input_name_id

left outer join cases.programs 
on programs.id = cases_caseinputs.program_id

left outer join emf.georegions 
on georegions.id = cases_caseinputs.region_id

left outer join emf.sectors 
on sectors.id = cases_caseinputs.sector_id

left outer join cases.subdirs 
on subdirs.id = cases_caseinputs.subdir_id

left outer join emf.versions 
on versions.id = cases_caseinputs.version_id
where cases_caseinputs.case_id = 13





--Parameters Tab Data
union all

select 

    --unique composite keys for this table (needed when doing comparison join, need to match on these keys)
    --case_id, param_name_id, env_vars_id, region_id, sector_id, case_job_id
    cases_parameters.case_id, 
    null::integer as input_name_id, 
    cases_parameters.region_id, 
    cases_parameters.sector_id, 
    cases_parameters.program_id, 
    cases_parameters.case_job_id, 
    cases_parameters.env_vars_id, 
    cases_parameters.param_name_id,


    'Parameters' as tab, 
    parameter_names."name", 
    parameter_env_vars."name" as env_var,
    georegions."name" as region,
    coalesce(sectors."name", 'All sectors') as sector,
    coalesce(cases_casejobs."name", 'All jobs for sector') as job,
    programs.name as program,
--  null::boolean as match,
    cases_parameters.env_value as "value"

from cases.cases_parameters

left outer join cases.cases 
on cases.id = cases_parameters.case_id

left outer join cases.parameter_env_vars 
on parameter_env_vars.id = cases_parameters.env_vars_id

left outer join cases.parameter_names 
on parameter_names.id = cases_parameters.param_name_id

left outer join cases.programs 
on programs.id = cases_parameters.program_id

left outer join emf.georegions 
on georegions.id = cases_parameters.region_id

left outer join emf.sectors 
on sectors.id = cases_parameters.sector_id

left outer join cases.value_types 
on value_types.id = cases_parameters.val_type_id

left outer join cases.cases_casejobs
on cases_casejobs.id = cases_parameters.case_job_id

where cases_parameters.case_id = 13


 */
        
        return sql;
    }
    
    private String buildGetCaseColumnValueSQL(int caseId, String columnLabel, String columnName) {
        return "SELECT '" + columnLabel + "' as \"key\", \"" + columnName + "\" || '' as \"value\" FROM cases.cases where id = " + caseId;
    }
    
    private String getCaseAbbreviation(Case caseObj) {
        return (caseObj != null ? caseObj.getAbbreviation().getName() : "");
    }
    
    public static void main(String[] args) {
        
        try {
            System.out.println(new SQLCompareCasesQuery(null).createCompareQuery(new int[] {7, 8}));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Case getCase(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return new CaseDAO().getCase(caseId, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Case, id = " + caseId);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
}
