package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresCOPYExport;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

public class RunQACaseReports implements Runnable {

    private StatusDAO statusDao;
    private User user;
    private HibernateSessionFactory sessionFactory;
    private Session session;
  
    private String exportDir;
   
    private Datasource datasource;
    private CaseDAO dao;
    private DbServer dbServer;
    private String reportSQL;
    private PostgresCOPYExport postgresCOPYExport;

    public RunQACaseReports(User user, DbServerFactory dbFactory, CaseDAO dao,
            HibernateSessionFactory sessionFactory,  String exportDir) {
        this.dao = dao;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.exportDir = exportDir;
        this.statusDao = new StatusDAO(sessionFactory);
        this.session = sessionFactory.getSession();
        this.postgresCOPYExport = new PostgresCOPYExport(dbServer);
    }


    public String validateAndBuildReportSQL(int[] caseIds, String gridName, 
            Sector[] sectors, String[] repDims, String whereClause) throws EmfException {
        String infoString = "";
        String selectSql = "";
        String headSelectSql = "";
        String tailSelectSql = "";
        String finalSql = " ";
        String annemisSql = "";
        Boolean useCounty = false;

        //reset report SQL, there could validation issues...
        this.reportSQL = "";

        String whereSql = whereClause.trim(); 
        if (whereSql.length() > 0)
            whereSql = whereSql.toLowerCase().startsWith("where")? 
                    whereSql:" where " + whereSql;

        List<String> repDimsList = Arrays.asList(repDims);
        if ( repDimsList.contains("County") ) 
            useCounty = true;


        // Set up the final select header
        if ( useCounty )
            headSelectSql += "Fips, ";
        for ( int i =0; i<repDims.length; i++) {
            headSelectSql += repDims[i] + ", ";
        }
        tailSelectSql = headSelectSql.substring(0, headSelectSql.length()-2);
        headSelectSql = tailSelectSql;
        
        setStatus("Started running QA case reports. ");
        
        try {
            String fipsRunningSelectList = "";
            String countyRunningSelectList = "";
            String stateRunningSelectList = "";
            String sectorRunningSelectList = "";
            String speciesRunningSelectList = "";

            for ( int i =0; i<caseIds.length; i++) {
                // 1. Get sectorList table name and dataset version
                Case caseQa = dao.getCase(caseIds[i], session);
                String caseAbbrev = caseQa.getAbbreviation().getName();
                String sectorListSql = new SQLAnnualReportQuery(sessionFactory).getSectorListTableQuery(caseIds[i], gridName);
                //String allRegionSectorListSql = new SQLAnnualReportQuery(sessionFactory).getSectorListTableQuery(caseIds[i], "");
                ResultSet rs = null;
                String tableName = "";
                Integer dsId;
                Integer version;
                String regName= gridName;

                if (DebugLevels.DEBUG_0())
                    System.out.println("Getting sectorlist table: '" +caseAbbrev + "'" + sectorListSql);
                try {
                    rs = datasource.query().executeQuery(sectorListSql);
                    if ( rs.next() ) { 
                        tableName = rs.getString(1);
                        dsId = rs.getInt(2);
                        version = rs.getInt(3);
                        regName = rs.getString(4);
                    }
                    else {
                        //log.error("No SECTORLIST file, case: \"" + caseAbbrev + "\"\n" );
                        //log.error("query -" + sectorListSql + "\n" );
                        infoString += "\n\"" + caseAbbrev + "\": No SECTORLIST file. \n";
                        continue;
                    }          

                    infoString += "\n";
                    infoString += caseAbbrev + ": \"" + regName + "\"\n";
                } catch (Exception e) {
                    e.printStackTrace();
                    if (session != null && session.isConnected())
                        session.close();  
                    setStatus("Error getting sectorlist table: '" +caseAbbrev + "'" + e.getMessage());
                    throw new EmfException("Error getting sectorlist table: '" +caseAbbrev + "'" + e.getMessage() );
                } finally {
                    if (rs != null)
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            //
                        }     
                }

                //2. Get sector names and its source cases from SECTORLIST file, 
                //   Save them to a map
                String sectorMergeSql="";
                HashMap<String, String> mapSectorCase = new HashMap<String, String>();

                try {
                    sectorMergeSql = new SQLAnnualReportQuery(sessionFactory).getSectorsCasesQuery(tableName, dsId, version);
                    if (DebugLevels.DEBUG_0())
                        System.out.println("Getting sectors to cases mappings from sectorlist table: '" 
                                +caseAbbrev + "'" +sectorMergeSql);

                    rs = datasource.query().executeQuery(sectorMergeSql);
                    while ( rs.next() ) { 
                        String cSector = rs.getString(1);
                        String sCase = rs.getString(2);
                        mapSectorCase.put(cSector, sCase);
                        if (DebugLevels.DEBUG_0())
                            System.out.println("mapping: " + cSector + "  " + sCase  );
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    setStatus("Error getting sectors to cases mappings: '" +caseAbbrev + "'" + e.getMessage());
                    throw new EmfException("Error getting sectors to cases mappings. '" +caseAbbrev + "'" + e.getMessage());
                } finally {
                    if (rs != null)
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            //
                        }
                }

                //3. Create SQL to get Annual Report Dataset table name
                String reportTableSql = "";
                String noRegReportTableSql = "";

                String[] sectorTables = new String[sectors.length];
                Integer[] dsIds =  new Integer[sectors.length];
                Integer[] dsVers = new Integer[sectors.length];
                String[] regionNames = new String[sectors.length];

                for ( int j = 0; j < sectors.length; j++ ) {
                    String sectorName = sectors[j].getName();
                    String secCaseAbbrev = mapSectorCase.get(sectorName);
                    if ( secCaseAbbrev != null ){
                        reportTableSql = new SQLAnnualReportQuery(sessionFactory).getReportTableQuery(sectorName, 
                                secCaseAbbrev, gridName, useCounty);
                        noRegReportTableSql = new SQLAnnualReportQuery(sessionFactory).getReportTableQuery(sectorName, 
                                secCaseAbbrev, "", useCounty);
                        if (DebugLevels.DEBUG_0())
                            System.out.println("Get report tables by sectors: " + reportTableSql);

                        rs = null;
                        try {
                            rs = datasource.query().executeQuery(reportTableSql);

                            if ( rs.next() ) { 
                                sectorTables[j] = rs.getString(1);
                                dsIds[j] = rs.getInt(2);
                                dsVers[j] = rs.getInt(3);
                                regionNames[j] = rs.getString(4);
                                infoString += "  " + sectorName + ": \"" + secCaseAbbrev + "\", \"" + regionNames[j] +"\"\n";
                            }
                            else {
                                rs = datasource.query().executeQuery(noRegReportTableSql);
                                if ( rs.next() ) { 
                                    sectorTables[j] = rs.getString(1);
                                    dsIds[j] = rs.getInt(2);
                                    dsVers[j] = rs.getInt(3);
                                    regionNames[j] = rs.getString(4);
                                    infoString += "  " + sectorName + ": \"" + secCaseAbbrev + "\", \"" + regionNames[j] +"\"\n";
                                }
                                else{
                                    //setStatus("No report table for case '" + caseAbbrev + "', sector '"+ sectorName + "'");
                                    infoString += "  " + sectorName + ": \"" + secCaseAbbrev + "\", No annual report. \n" ;
                                }
                            }            
                        } catch (SQLException e) {
                            if (session != null && session.isConnected())
                                session.close();  
                            e.printStackTrace();
                            setStatus("Error getting report table for case '" + caseAbbrev + "', sector '"+ sectorName + "'. " + e.getMessage());
                            throw new EmfException("Error getting report table for case '" 
                                    + caseAbbrev + "', sector '"+ sectorName + "'. " + e.getMessage());
                        } finally {
                            if (rs != null)
                                try {
                                    rs.close();
                                } catch (SQLException e) {
                                    //
                                }     
                        }
                    }
                    else
                        infoString += "  " + sectorName + ": Not in sectorlist table. \n";
                }
                //4. Extract info from Annual Report Dataset table

                String annualReportSql = new SQLAnnualReportQuery(sessionFactory).getSectorsReportsQuery(sectorTables, 
                        dsIds, dsVers, caseAbbrev, whereSql, useCounty );
                //System.out.println("annual Query -- " + annualReportSql);
                if ( ! annualReportSql.trim().isEmpty()) {

                    if ( ! finalSql.trim().isEmpty()){
                        finalSql += "  full join (" + annualReportSql + " ) as case" + i +
                        " on coalesce(case" + i + ".state, '')= coalesce(" + stateRunningSelectList + ", '') " +
                        " and coalesce(case" + i + ".sector, '')= coalesce(" + sectorRunningSelectList + ", '') " +
                        " and coalesce(case" + i + ".species, '')= coalesce(" + speciesRunningSelectList + ", '') ";

                        if (useCounty)
                            finalSql += " and coalesce(case" + i + ".fips, '')= coalesce(" + fipsRunningSelectList + ", '') " +
                            " and coalesce(case" + i + ".county, '')= coalesce(" + countyRunningSelectList + ", '') ";

                        fipsRunningSelectList += ",case" + i + ".fips";
                        countyRunningSelectList = ",case" + i + ".county";
                        stateRunningSelectList += ",case" + i + ".state";
                        sectorRunningSelectList += ",case" + i + ".sector";
                        speciesRunningSelectList += ",case" + i + ".species";

                        selectSql += ", case" + i + ".$$" ;
                    }
                    else {
                        finalSql += "  from (" + annualReportSql + " ) as case" + i ;
                        fipsRunningSelectList += "case" + i + ".fips";
                        countyRunningSelectList = "case" + i + ".county";
                        stateRunningSelectList += "case" + i + ".state";
                        sectorRunningSelectList += "case" + i + ".sector";
                        speciesRunningSelectList += "case" + i + ".species";
                        selectSql = "coalesce(case" + i + ".$$" ;
                    }
                    annemisSql += ", case"+ i + ".\"" + caseAbbrev + "\"";
                    headSelectSql += ", sum(\"" + caseAbbrev + "\") as \"" + caseAbbrev + "\"";
                }

            }
            if ( useCounty )
                selectSql = "select " + selectSql.replace("$$", "fips") + ") as fips, " +
                selectSql.replace("$$", "state") + ") as state, " +
                selectSql.replace("$$", "county") + ") as county, " +
                selectSql.replace("$$", "sector") + ") as sector, " +
                selectSql.replace("$$", "species") + ") as species " ;
            else
                selectSql = "select " + selectSql.replace("$$", "state") + ") as state, " +
                selectSql.replace("$$", "sector") + ") as sector, " +
                selectSql.replace("$$", "species") + ") as species " ; 
            selectSql = selectSql + annemisSql;

            if (  finalSql.trim().isEmpty()){
                if ( useCounty) {
                    setStatus("No county annual reports for selected cases. ");
                    throw new EmfException(infoString +"\n No county annual reports for selected cases. ");
                }
                setStatus("No county/state annual reports for selected cases. ");
                throw new EmfException(infoString + "\n No county/state annual reports for selected cases. ");
            }

            finalSql = "select " + headSelectSql + " from ( " + selectSql + finalSql + " ) as foo group by " + tailSelectSql;
            
            //store report SQL in module level field so its available to threaded run method...
            this.reportSQL = finalSql;

            if (DebugLevels.DEBUG_0())
                System.out.println("Final Query: " + this.reportSQL);
            
            return infoString;
             
        } catch (RuntimeException e) {
            e.printStackTrace();
            //setStatus("Error: Could not retrieve annual Reports: " + e.getMessage());
            throw new EmfException("Error: Could not retrieve annual Reports. \n" + e.getMessage());
        }
    }
    
    
    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("RunQACaseReports");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());
        statusDao.add(endStatus);
    }

    private String tempQAFilePath() throws EmfException {
        String separator = File.separator; 
  
        if (exportDir == null || exportDir.isEmpty())
            exportDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(exportDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
        {
            setStatus("Error: Import-export temporary folder does not exist or lacks write permissions: "
                    + exportDir);
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + exportDir);
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return exportDir + separator +  "runQACaseReports_" + timeStamp + ".csv"; // this is how exported file name was
    }


    public void run() {
        // NOTE Auto-generated method stub
        File localFile;
        try {
            localFile = new File(tempQAFilePath());

            postgresCOPYExport.export(this.reportSQL, localFile.getAbsolutePath());
        
            setStatus("Completed running QA case Reports.  Report was exported to " + localFile.getAbsolutePath() + ".  Total lines in report: " + countLines(localFile));
        
        
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            setStatus("Error running QA case Reports:  " + e.getMessage());
        } catch (ExporterException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            setStatus("Error running QA case Reports:  " + e.getMessage());
        } catch (IOException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            setStatus("Error running QA case Reports:  " + e.getMessage());
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private static long countLines(File file) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        try
        {
            while (br.readLine() != null)
            {
                count++;
            }
        }
        finally
        {
            br.close();
        }
        return count;
    }
}

  