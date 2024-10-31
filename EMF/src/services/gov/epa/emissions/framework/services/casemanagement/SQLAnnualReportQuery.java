package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SQLAnnualReportQuery {

    private EntityManagerFactory entityManagerFactory;
    
    public SQLAnnualReportQuery(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
    
    public String getSectorListTableQuery(int caseId, String gridName) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select ");
        sql.append(" internal_sources.table_name, " );
        sql.append(" cases_caseinputs.dataset_id, ");
        sql.append(" versions.version, ");
        sql.append(" coalesce(emf.georegions.name, ' ') as gridname" );
        sql.append(" FROM ");
        sql.append(" emf.internal_sources " );
        sql.append(" inner join cases.cases_caseinputs ");
        sql.append(" on internal_sources.dataset_id = cases_caseinputs.dataset_id  ");
        sql.append(" inner join cases.input_envt_vars  ");
        sql.append(" on input_envt_vars.id = cases_caseinputs.envt_vars_id  AND input_envt_vars.name = 'SECTORLIST' ");
        sql.append(" inner join emf.versions on cases_caseinputs.version_id = versions.id ");
        sql.append(" inner join cases.cases on cases.id = cases_caseinputs.case_id  AND cases.id = " + caseId );

        sql.append(" LEFT outer join emf.georegions ");
        sql.append(" on  cases_caseinputs.region_id = emf.georegions.id  " );
    
        sql.append(" AND georegions.name = '" + gridName + "' " );
       
        return sql.toString();
    }
    
    public String getSectorsCasesQuery(String tableName, Integer dsId, Integer versionId) throws EmfException {
        StringBuilder sql = new StringBuilder();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        Version version = version(entityManager, dsId, versionId );
        String datasetVersionedQuery = new VersionedQuery(version).query();
//        for (int i = 0; i < caseIds.length; ++i) {
//            if ( i > 0 ) sql.append( " union all " );
        sql.append(" select " );
        sql.append(" sector, " );
        sql.append(" sectorcase ");
        sql.append(" from " + qualifiedName(tableName) );
        sql.append(" where " +datasetVersionedQuery );
              
        return sql.toString();
    }
    
    public String getReportTableQuery(String sector, String mapSectorCase, 
            String gridName, Boolean useCounty) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select ");
        sql.append(" internal_sources.table_name, " );
        sql.append(" outputs.dataset_id, ");
        sql.append(" datasets.default_version, ");
        sql.append(" georegions.name" );
        //            sql.append( caseAbbrev + " as caseAbbrev" );
        sql.append(" FROM ");
        sql.append(" emf.internal_sources, " );
        sql.append(" cases.outputs, ");
        sql.append(" cases.cases_casejobs, ");
        sql.append(" cases.cases, ");
        sql.append(" emf.datasets, ");
        sql.append(" emf.georegions ");       
        sql.append(" WHERE ");
        sql.append(" cases.id = outputs.case_id " );
        sql.append(" AND cases_casejobs.id = outputs.job_id ");
        sql.append(" AND outputs.dataset_id = datasets.id ");
        sql.append(" AND internal_sources.dataset_id = datasets.id ");
        sql.append(" AND cases.abbreviation_id = ( select id from cases.case_abbreviations where name = '" + mapSectorCase + "')");
        sql.append(" AND cases_casejobs.sector_id = (select id from emf.sectors where name = '" + sector + "')");
        if ( ! gridName.trim().isEmpty()){
            sql.append(" AND cases_casejobs.region_id = emf.georegions.id" );  
            sql.append(" AND georegions.name = '" + gridName + "' " );
        }  
        else
            sql.append(" AND cases_casejobs.region_id = emf.georegions.id " );
  
        if ( ! useCounty )
            sql.append(" AND datasets.dataset_type in " +
            "(select id from emf.dataset_types where name = 'Smkmerge report state annual summary (CSV)' " +
            " or name = 'Smkmerge report county annual summary (CSV)')"
                     );
        else
            sql.append(" AND datasets.dataset_type = " +
            "(select id from emf.dataset_types where name = 'Smkmerge report county annual summary (CSV)')");

        return sql.toString();
    }
    
    public String getSectorsReportsQuery(String[] tableNames, Integer[] dsIds, 
            Integer[] dsVers, String caseAbbrev, String whereClause, Boolean useCounty ) throws EmfException {
        StringBuilder sql = new StringBuilder();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String colString = "";
        if ( useCounty )
            colString = " select fips, state, county, sector, species, ann_emis as \"" + caseAbbrev + "\"";
        else
            colString = " select state, sector, species, ann_emis as \"" + caseAbbrev +"\"";
        for (int i = 0; i < tableNames.length; i++) {
            // skip the loop if a sector is not in the current case
            if ( tableNames[i] != null && dsIds[i] !=null) {
                Version version = version(entityManager, dsIds[i], dsVers[i] );            
                String datasetVersionedQuery = new VersionedQuery(version).query();
                if ( sql.toString().contains("select")) sql.append( " union all " );
                sql.append(colString);
                sql.append(" from " + qualifiedName(tableNames[i]) );
                if ( whereClause.trim().isEmpty())
                    sql.append(" where " + datasetVersionedQuery );
                else {
                    sql.append("  " + whereClause);
                    sql.append(" AND " + datasetVersionedQuery ); 
                }
            }   
        }
        return sql.toString();
    }
    
//    private String getCaseAbbreviation(Case caseObj) {
//        return (caseObj != null ? caseObj.getAbbreviation().getName() : "");
//    }
    
    public static void main(String[] args) {
        
//        try {
//            //System.out.println(new SQLAnnualReportQuery(null).createCompareQuery(new int[] {7, 8}));
//        } catch (EmfException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
    }

//    private Case getCase(int caseId) throws EmfException {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        try {
//            return new CaseDAO().getCase(caseId, entityManager);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get Case, id = " + caseId);
//        } finally {
//            if (entityManager != null)
//                entityManager.close();
//        }
//    }
    
    private Version version(int datasetId, int version) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, entityManager);
        } finally {
            entityManager.close();
        }
    }
    
    private Version version(EntityManager entityManager, int datasetId, int version) {
        Versions versions = new Versions();
        return versions.get(datasetId, version, entityManager);
    }
    
    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return "emissions." + table;
    }

}
