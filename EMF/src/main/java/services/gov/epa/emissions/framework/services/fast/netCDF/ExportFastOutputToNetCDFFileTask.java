package gov.epa.emissions.framework.services.fast.netCDF;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDAO;
import gov.epa.emissions.framework.services.fast.Grid;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportFastOutputToNetCDFFileTask implements Runnable {

//    private String user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportFastOutputToNetCDFFileTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;

    private String dirName;

    private DbServerFactory dbServerFactory;

    private String userName;

    private int datasetId;

    private int gridId;

    private int datasetVersion;

    private boolean windowsOS;

    public ExportFastOutputToNetCDFFileTask(int datasetId, int datasetVersion, int gridId, String userName, String dirName,
            String pollutant, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
//        super(getDataset(datasetId), dbServerFactory.getDbServer(), getDataset(datasetId).getDatasetType().getFileFormat(), new NonVersionedDataFormatFactory(), 0);
        
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.gridId = gridId;
        this.dirName = dirName;
        this.userName = userName;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.dbServerFactory = dbServerFactory;
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }
    
    public void run() {
        String suffix = "";
        DbServer dbServer = dbServerFactory.getDbServer();
        EmfDataset dataset = getDataset(datasetId);
        try {
            Version datasetVersion = version(this.datasetId, this.datasetVersion);
            Grid grid = getGrid(gridId);

            //do some basic validation...
            if (dataset == null)
                throw new ExporterException("Dataset doesn't exist, dataset id = " + this.datasetId);
            if (datasetVersion == null)
                throw new ExporterException("Dataset version doesn't exist, dataset version = " + this.datasetVersion);
            if (grid == null)
                throw new ExporterException("Grid doesn't exist, dataset version = " + this.gridId);
            validateDir(this.dirName);
            //make sure it has the relevant columns in order to make the Net CDF file
            validateDatasetCanBeConverted(dataset);
            
            //build file first, the suffix status message string needs the file absolute path...
            this.file = exportFile(this.dirName, dataset);
            suffix = getSuffix(dataset);
            prepare(suffix);
            export(this.file);
            complete(suffix);
            
//            PostgresSQLToShapeFile exporter = new PostgresSQLToShapeFile(dbServer);
//            // Exporter exporter = new DatabaseTableCSVExporter(result.getTable(), dbServer.getEmissionsDatasource(),
//            // batchSize(sessionFactory));
//            
//            //generate a file per sector
//            for (String sector : getDatasetSectors(dataset, datasetVersion)) {
//                for (String pollutant : getDatasetPollutants(dataset, datasetVersion, sector)) {
//                    file = exportFile(this.dirName, dataset, sector, pollutant);
//                    suffix = suffix();
//                    prepare(suffix, dataset);
//                    String sql = prepareSQLStatement(dataset, datasetVersion, grid, pollutant, sector);
//                    exporter.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), getProperty("postgres-user"),
//                            getProperty("pgsql2shp-info"), file.getAbsolutePath(), sql, null);
//                    complete(suffix, dataset);
//                }
//            }
        } catch (Exception e) {
            logError("Failed to export dataset : " + dataset.getName() + suffix, e);
            setStatus("Failed to export dataset " + dataset.getName() + suffix + ". Reason: " + e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

//    private String getProperty(String propertyName) {
//        Session session = sessionFactory.getSession();
//        try {
//            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, session);
//            return property.getValue();
//        } finally {
//            session.close();
//        }
//    }

    private EmfDataset getDataset(int datasetId) {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetDAO().getDataset(session, datasetId);
        } finally {
            session.close();
        }
    }

    private Grid getGrid(int gridId) {
        Session session = sessionFactory.getSession();
        try {
            return new FastDAO().getGrid(session, gridId);
        } finally {
            session.close();
        }
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }

//    private String emissionTableName(Dataset dataset) {
//        InternalSource[] internalSources = dataset.getInternalSources();
//        return internalSources[0].getTable().toLowerCase();
//    }

//    private String[] getDatasetSectors(EmfDataset dataset, Version datasetVersion) throws EmfException {
//        DbServer dbServer = null;
//        List<String> sectorList = new ArrayList<String>();
//        try {
//            dbServer = dbServerFactory.getDbServer();
//            VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
//            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
//                    "select distinct coalesce(sector, '') as sector from emissions." + emissionTableName(dataset)
//                            + " as i where " + datasetVersionedQuery.query() + " order by sector ");
//
//            while (rs.next()) {
//                sectorList.add(rs.getString(1));
//            }
//        } catch (SQLException e) {
//            throw new EmfException(e.getMessage(), e);
//        } finally {
//            if (dbServer != null)
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//        }
//        return sectorList.toArray(new String[0]);
//    }

//    private String[] getDatasetPollutants(EmfDataset dataset, Version datasetVersion, String sector) throws EmfException {
//        DbServer dbServer = null;
//        DatasetType datasetType = dataset.getDatasetType();
//        boolean hasCmaqPollutantColumn = false;
//        boolean hasPollutantColumn = false;
//        for (Column column : datasetType.getFileFormat().getColumns()) {
//            if (column.getName().equalsIgnoreCase("cmaq_pollutant")) hasCmaqPollutantColumn = true;
//            if (column.getName().equalsIgnoreCase("pollutant")) hasPollutantColumn = true;
//        }
//        List<String> pollutantList = new ArrayList<String>();
//        if (!hasCmaqPollutantColumn && !hasPollutantColumn) return new String[0];
//        try {
//            dbServer = dbServerFactory.getDbServer();
//            VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
//            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
//                    "select distinct coalesce(" + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + ", '') as " + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + " from emissions." + emissionTableName(dataset)
//                            + " as i where " + datasetVersionedQuery.query() + " and sector = '" + sector.replace("'","''") + "' order by " + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + " ");
//
//            while (rs.next()) {
//                pollutantList.add(rs.getString(1));
//            }
//        } catch (SQLException e) {
//            throw new EmfException(e.getMessage(), e);
//        } finally {
//            if (dbServer != null)
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//        }
//        return pollutantList.toArray(new String[0]);
//    }

    class HeaderColumnItem {
        public HeaderColumnItem(String columnName, String columnUnits, String columnAbbreviation,
                String columnDescription) {
            super();
            this.columnName = columnName;
            this.columnUnits = columnUnits;
            this.columnAbbreviation = columnAbbreviation;
            this.columnDescription = columnDescription;
        }
        public String columnName;
        public String columnUnits;
        public String columnAbbreviation;
        public String columnDescription;
        
    }

    private HeaderColumnItem createHeaderColumnItem(String columnName, String columnUnits, String columnAbbreviation,
            String columnDescription) {
        return new HeaderColumnItem(columnName, columnUnits, columnAbbreviation,
                columnDescription);
    }
//    private String prepareSQLStatement(EmfDataset dataset, Version datasetVersion, Grid grid, String pollutant, String sector) throws ExporterException {
//
//        DbServer dbServer = null;
//        boolean hasXCol = false;
//        boolean hasYCol = false;
//        boolean hasPollutantCol = false;
//        boolean hasCMAQPollutantCol = false;
//        // will hold unique list of column names, pqsql2shp doesn't like multiple columns with the same name...
//        Map<String, String> cols = new HashMap<String, String>();
//        Map<String, HeaderColumnItem> headerColumnItems = new HashMap<String, HeaderColumnItem>();
//        
//        //common to Fast Runs and Analyses
//        headerColumnItems.put("x", createHeaderColumnItem("x", "UNITS", "POLID", "DESC"));
//        headerColumnItems.put("y", createHeaderColumnItem("y", "NA", "NA", "NA"));
//        headerColumnItems.put("sector", createHeaderColumnItem("sector", "NA", "NA", "NA"));
//        headerColumnItems.put("cmaq_pollutant", createHeaderColumnItem("pollutant", "NA", "NA", "NA"));
//        headerColumnItems.put("pollutant", createHeaderColumnItem("pollutant", "NA", "NA", "NA"));
//        headerColumnItems.put("emission", createHeaderColumnItem("Emission (ton/yr)", "ton/yr", "EMS", "Emissions"));
//        headerColumnItems.put("air_quality", createHeaderColumnItem("AQ conc (ug/m3)", "ug/m3", "AQ", "AQcon"));
//
//        //fast run output columns to encounter
//        headerColumnItems.put("population_weighted_air_quality", createHeaderColumnItem("Population weighted AQ", "mg/m3", "aqp", "pop_weighted_AQ"));
//        headerColumnItems.put("cancer_risk_per_person", createHeaderColumnItem("cancer risk/person", "NA", "cr", "cancer_risk/person"));
//        headerColumnItems.put("total_cancer_risk", createHeaderColumnItem("total cancer risk", "NA", "tcr", "total_cancer_risk"));
//        headerColumnItems.put("population_weighted_cancer_risk", createHeaderColumnItem("pop. weighted cancer risk", "NA", "pw", "pop_weighted_cancer_risk"));
//        headerColumnItems.put("grid_cell_population", createHeaderColumnItem("grid cell population", "NA", "gcp", "grid_cell_population"));
//        headerColumnItems.put("pct_population_in_grid_cell_to_model_domain", createHeaderColumnItem("% population in grid cell relative to modeling domain", "Percent", "pp", "perc_pop_in_grid_cell_relative_to_modeling_domain"));
//        headerColumnItems.put("ure", createHeaderColumnItem("URE", "NA", "ure", "URE"));
//        
//        //fast analysis output columns to encounter
//        headerColumnItems.put("sum_sens_emission", createHeaderColumnItem("Sum Sensitivity Emission (ton/yr)", "ton/yr", "ssems", "Sum Sensitivity Emissions"));
//        headerColumnItems.put("sum_base_emission", createHeaderColumnItem("Sum Baseline Emission (ton/yr)", "ton/yr", "sbems", "Sum Baseline Emissions"));
//        headerColumnItems.put("diff_sum_emission", createHeaderColumnItem("Difference Sum Emission (ton/yr)", "ton/yr", "dsems", "Difference Sum Emissions"));
//        headerColumnItems.put("sum_sens_air_quality", createHeaderColumnItem("Sum Sensitivity AQ conc (ug/m3)", "ug/m3", "ssaq", "Sum Sensitivity AQcon"));
//        headerColumnItems.put("sum_base_air_quality", createHeaderColumnItem("Sum Baseline AQ conc (ug/m3)", "ug/m3", "sbaq", "Sum Baseline AQcon"));
//        headerColumnItems.put("diff_sum_air_quality", createHeaderColumnItem("Difference Sum AQ conc (ug/m3)", "ug/m3", "dsaq", "Difference Sum AQcon"));
//        headerColumnItems.put("sum_sens_pop_weighted_air_quality", createHeaderColumnItem("Sum Sensitivity Population weighted AQ", "mg/m3", "ssaqp", "sum_sensitivity_pop_weighted_AQ"));
//        headerColumnItems.put("sum_base_pop_weighted_air_quality", createHeaderColumnItem("Sum Baseline Population weighted AQ", "mg/m3", "sbaqp", "sum_baseline_pop_weighted_AQ"));
//        headerColumnItems.put("diff_sum_pop_weighted_air_quality", createHeaderColumnItem("Difference Sum Population weighted AQ", "mg/m3", "dsaqp", "difference_sum_pop_weighted_AQ"));
//        headerColumnItems.put("sum_sens_pop_total_cancer_risk", createHeaderColumnItem("Sum Sensitivity total cancer risk", "NA", "sstcr", "sum_sensitivity_total_cancer_risk"));
//        headerColumnItems.put("sum_base_pop_total_cancer_risk", createHeaderColumnItem("Sum Baseline total cancer risk", "NA", "sbtcr", "sum_baseline_total_cancer_risk"));
//        headerColumnItems.put("sum_sens_pop_total_cancer_risk", createHeaderColumnItem("Difference Sum total cancer risk", "NA", "dstcr", "difference_sum_total_cancer_risk"));
//
//        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Sum Sensitivity pop. weighted cancer risk", "NA", "sspw", "sum_sensitivity_pop_weighted_cancer_risk"));
//        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Sum Baseline pop. weighted cancer risk", "NA", "sbpw", "sum_baseline_pop_weighted_cancer_risk"));
//        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Difference Sum pop. weighted cancer risk", "NA", "dspw", "difference_sum_pop_weighted_cancer_risk"));
//
////        EMS AQ  AQP CR  TCR PW  GCP PP  URE
////        Emissions   AQcon   pop_weighted_AQ cancer_risk/person  total_cancer_risk   pop_weighted_cancer_risk    grid_cell_population    perc_pop_in_grid_cell_relative_to_modeling_domain   URE
//
//        String colNames = "";
//        String sql = "";
//        String tableName = dataset.getInternalSources()[0].getTable();
//        VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
//
//        try {
//            dbServer = dbServerFactory.getDbServer();
//            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
//                    "select * from emissions." + tableName + " where 1 = 0");
//            ResultSetMetaData md = rs.getMetaData();
//            int columnCount = md.getColumnCount();
//            String colName = "";
//            for (int i = 1; i <= columnCount; i++) {
//                colName = md.getColumnName(i).toLowerCase();
//                if (!cols.containsKey(colName)) {
//                    cols.put(colName, colName);
//                    colNames += (colNames.length() > 0 ? "," : "");
//                    if (colName.equals("x")) {
//                        colNames += "hor.x";
//                    } else if (colName.equals("y")) {
//                        colNames += "vert.y";
//                    } else if (colName.equals("sector")) {
//                        colNames += "'" + sector + "'::varchar(64) as sector";
//                    } else if (colName.equals("pollutant")) {
//                        colNames += "pollutant";
//                    } else if (colName.equals("cmaq_pollutant")) {
//                        colNames += "cmaq_pollutant";
//                    } else {
//                        colNames += colName + (!headerColumnItems.containsKey(colName) ? "" : " as " + headerColumnItems.get(colName));
//                    }
//                }
//
//                if (colName.equals("x")) {
//                    hasXCol = true;
//                } else if (colName.equals("y")) {
//                    hasYCol = true;
//                } else if (colName.equals("pollutant")) {
//                    hasPollutantCol = true;
//                } else if (colName.equals("cmaq_pollutant")) {
//                    hasCMAQPollutantCol = true;
//                }
//            }
//
//            if (!hasXCol || !hasYCol || (!hasPollutantCol && !hasCMAQPollutantCol)) 
//                throw new ExporterException("Dataset is missing applicable columns; x, y, and pollutant (or cmaq_polluant); needed to generate a shapefile.");
//            
//        } catch (SQLException e) {
//            throw new ExporterException(e.getMessage());
//        } finally {
//            if (dbServer != null)
//                try {
//                    dbServer.disconnect();
//                } catch (Exception e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                }
//        }
//        sql = "select " + colNames + ", ST_translate(origin_grid.boxrep, hor.x * " + grid.getXcell() + ", vert.y * " + grid.getYcell() + ") As the_geom "
//            + " from generate_series(0," + (grid.getNcols() - 1) + ") as hor(x) "
//            + " cross join generate_series(0," + (grid.getNrows() - 1) + ") as vert(y) "
//            + " cross join (SELECT ST_SetSRID(CAST('BOX(" + grid.getXcent() + " " + grid.getYcent() + "," + (grid.getXcent() + grid.getXcell()) + " " + (grid.getYcent() + grid.getYcell()) + ")' as box2d), 104307) as boxrep) as origin_grid "
//            + " left outer join emissions." + tableName + " i "
//            + " on i.x = hor.x + 1 "
//            + " and i.y = vert.y + 1 "
//            + " and " + datasetVersionedQuery.query() + " "
//            + " and " + (hasPollutantCol ? "i.pollutant" : "i.cmaq_pollutant") + "='" + pollutant + "'"
//            + " and i.sector='" + sector + "'"
//            + " order by hor.x, vert.y ";
//        
//        System.out.println(sql);
//    
//        return sql;
//    }
//    
    private void validateDatasetCanBeConverted(EmfDataset dataset) throws ExporterException {

        DbServer dbServer = null;
        boolean hasXCol = false;
        boolean hasYCol = false;
        boolean hasPollutantCol = false;
        boolean hasCMAQPollutantCol = false;

        String tableName = dataset.getInternalSources()[0].getTable();

        try {
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select * from emissions." + tableName + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            String colName = "";
            for (int i = 1; i <= columnCount; i++) {
                colName = md.getColumnName(i).toLowerCase();
                if (colName.equals("x")) {
                    hasXCol = true;
                } else if (colName.equals("y")) {
                    hasYCol = true;
                } else if (colName.equals("pollutant")) {
                    hasPollutantCol = true;
                } else if (colName.equals("cmaq_pollutant")) {
                    hasCMAQPollutantCol = true;
                }
            }

            if (!hasXCol || !hasYCol || (!hasPollutantCol && !hasCMAQPollutantCol)) 
                throw new ExporterException("Dataset is missing applicable columns; x, y, and pollutant (or cmaq_polluant); needed to generate a Net CDF file.");
            
        } catch (SQLException e) {
            throw new ExporterException(e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
    
    }
    
    private void prepare(String suffixMsg) {
        setStatus("Started exporting FAST output Net CDF File" + suffixMsg);
    }

    private void complete(String suffixMsg) {
        setStatus("Completed exporting FAST output Net CDF File" + suffixMsg);
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(userName);
        endStatus.setType("ExportFASTOutput");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String getSuffix(EmfDataset dataset) {
        return " for Version '" + versionName() + "' of Dataset '" + dataset.getName() + "' to " + file.getAbsolutePath();
    }

    private String versionName() {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(this.datasetId, this.datasetVersion, session).getName();
        } finally {
            session.close();
        }
    }

    private File exportFile(String dirName, EmfDataset dataset) {
        return new File(dirName, getFileName(dataset));
    }

    private String getFileName(EmfDataset dataset) {
        String fileName = dataset.getName();
        for (int i = 0; i < fileName.length(); i++) {
            if (!Character.isLetterOrDigit(fileName.charAt(i))) {
                fileName = fileName.replace(fileName.charAt(i), '_');
            }
        }

        return fileName;
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }




    private void setProperties() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty batchSize = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null)
                System.setProperty("IMPORT_EXPORT_TEMP_DIR", eximTempDir.getValue());

            if (batchSize != null)
                System.setProperty("EXPORT_BATCH_SIZE", batchSize.getValue());
        } finally {
            session.close();
        }
    }
    public void export(File file) throws ExporterException {
        // TBD: make this use the new temp dir
        setProperties();
        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR");

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new ExporterException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir + "");

        Random rando = new Random();
        long id = Math.abs(rando.nextInt());

        String separator = System.getProperty("file.separator");
        String dataFileName = tempDir + separator + file.getName() + id + ".dat";
        String headerFileName = tempDir + separator + file.getName() + id + ".hed";
        File dataFile = new File(dataFileName);
        File headerFile = new File(headerFileName);

        // use one statement and connection object for all operations, this we can easily clean them up....
        Connection connection = null;
        Statement statement = null;
        DbServer dbServer = dbServerFactory.getDbServer();

        EmfDataset dataset = getDataset(datasetId);
        Datasource datasource = dbServer.getEmissionsDatasource();
        try {
            createNewFile(dataFile);
            writeHeader(headerFile, dataset);

            String originalQuery = "SELECT * FROM emissions." + dataset.getInternalSources()[0].getTable();//getQueryString(dataset, datasource);
            String query = getColsSpecdQueryString(dataset, originalQuery, datasource);
            String writeQuery = getWriteQueryString(dataFileName, query, dataset);

            // log.warn(writeQuery);

            connection = datasource.getConnection();

            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            executeQuery(statement, writeQuery);
            concatFiles(file, headerFileName, dataFileName);
            setExportedLines(originalQuery, statement);
        } catch (Exception e) {
            e.printStackTrace();
            // NOTE: this closes the db server for other exporters
            // try {
            // if ((connection != null) && !connection.isClosed())
            // connection.close();
            // } catch (Exception ex) {
            // ex.printStackTrace();
            // throw new ExporterException(ex.getMessage());
            // }
            throw new ExporterException(e.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
                // if (connection != null)
                // connection.close();
            } catch (SQLException e) {
                //
            }
                if (dbServer != null)
                    try {
                        dbServer.disconnect();
                    } catch (Exception e) {
                        // NOTE Auto-generated catch block
                        e.printStackTrace();
                    }
            
            if (dataFile.exists())
                dataFile.delete();
            if (headerFile.exists())
                headerFile.delete();
        }
    }

    private Map<String, String> getTableCols(Dataset dataset, Datasource datasource) throws EmfException {
        ResultSet rs = null;
        Map<String, String> cols = new HashMap<String, String>();
        InternalSource source = dataset.getInternalSources()[0];
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions.versions".equalsIgnoreCase(qualifiedTable) ) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTable + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
                cols.put(md.getColumnName(i).toLowerCase(), md.getColumnName(i).toLowerCase());
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }

        return cols;
    }

    private void createNewFile(File file) throws Exception {
        try {
            if (windowsOS) {
                // AME: Updates for EPA's system
                file.createNewFile();
                Runtime.getRuntime().exec("CACLS " + file.getAbsolutePath() + " /E /G \"Users\":W");
                file.setWritable(true, false);
                Thread.sleep(1000); // for the system to refresh the file access permissions
            }
            // for now, do nothing from Linux
        } catch (IOException e) {
            throw new ExporterException("Could not create export file: " + file.getAbsolutePath());
        }
    }

    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }

    protected void writeHeader(File file, Dataset dataset) throws Exception {
        PrintWriter writer = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));

        try {
            writeHeaders(writer, dataset);
        } finally {
            writer.close();
        }
    }

    protected void writeHeaders(PrintWriter writer, Dataset dataset) {
//        String header = "";//dataset.getDescription();
//        String cr = System.getProperty("line.separator");

        final String headerColumnGridIdentifier = "GRID";
        final String headerColumnNameIdentifier = "DATA";
        final String headerColumnUnitIdentifier = "UNITS";
        final String headerColumnAbbreviationIdentifier = "POLID";
        final String headerColumnDescriptionIdentifier = "DESC";
        String headerColumnNames = "";
        String headerColumnUnits = "";
        String headerColumnAbbreviations = "";
        String headerColumnDescriptions = "";
        Grid grid = getGrid(this.gridId);
        Map<String, HeaderColumnItem> headerColumnItems = new HashMap<String, HeaderColumnItem>();
        
        //common to Fast Runs and Analyses
        headerColumnItems.put("x", createHeaderColumnItem("x", "NA", "x", "x coordinate grid cell number"));
        headerColumnItems.put("y", createHeaderColumnItem("y", "NA", "y", "y coordinate grid cell number"));
        headerColumnItems.put("sector", createHeaderColumnItem("sector", "NA", "sector", "sector"));
        headerColumnItems.put("cmaq_pollutant", createHeaderColumnItem("pollutant", "NA", "pollutant", "pollutant"));
        headerColumnItems.put("pollutant", createHeaderColumnItem("pollutant", "NA", "pollutant", "pollutant"));
        headerColumnItems.put("emission", createHeaderColumnItem("Emission (ton/yr)", "ton/yr", "EMS", "Emissions"));
        headerColumnItems.put("air_quality", createHeaderColumnItem("AQ conc (ug/m3)", "ug/m3", "AQ", "AQcon"));

        //fast run output columns to encounter
        headerColumnItems.put("population_weighted_air_quality", createHeaderColumnItem("Population weighted AQ", "mg/m3", "aqp", "pop_weighted_AQ"));
        headerColumnItems.put("cancer_risk_per_person", createHeaderColumnItem("cancer risk/person", "NA", "cr", "cancer_risk/person"));
        headerColumnItems.put("total_cancer_risk", createHeaderColumnItem("total cancer risk", "NA", "tcr", "total_cancer_risk"));
        headerColumnItems.put("population_weighted_cancer_risk", createHeaderColumnItem("pop. weighted cancer risk", "NA", "pw", "pop_weighted_cancer_risk"));
        headerColumnItems.put("grid_cell_population", createHeaderColumnItem("grid cell population", "NA", "gcp", "grid_cell_population"));
        headerColumnItems.put("pct_population_in_grid_cell_to_model_domain", createHeaderColumnItem("% population in grid cell relative to modeling domain", "Percent", "pp", "perc_pop_in_grid_cell_relative_to_modeling_domain"));
        headerColumnItems.put("ure", createHeaderColumnItem("URE", "NA", "ure", "URE"));
        
        //fast analysis output columns to encounter
        headerColumnItems.put("sum_sens_emission", createHeaderColumnItem("Sum Sensitivity Emission (ton/yr)", "ton/yr", "ssems", "Sum Sensitivity Emissions"));
        headerColumnItems.put("sum_base_emission", createHeaderColumnItem("Sum Baseline Emission (ton/yr)", "ton/yr", "sbems", "Sum Baseline Emissions"));
        headerColumnItems.put("diff_sum_emission", createHeaderColumnItem("Difference Sum Emission (ton/yr)", "ton/yr", "dsems", "Difference Sum Emissions"));
        headerColumnItems.put("sum_sens_air_quality", createHeaderColumnItem("Sum Sensitivity AQ conc (ug/m3)", "ug/m3", "ssaq", "Sum Sensitivity AQcon"));
        headerColumnItems.put("sum_base_air_quality", createHeaderColumnItem("Sum Baseline AQ conc (ug/m3)", "ug/m3", "sbaq", "Sum Baseline AQcon"));
        headerColumnItems.put("diff_sum_air_quality", createHeaderColumnItem("Difference Sum AQ conc (ug/m3)", "ug/m3", "dsaq", "Difference Sum AQcon"));
        headerColumnItems.put("sum_sens_pop_weighted_air_quality", createHeaderColumnItem("Sum Sensitivity Population weighted AQ", "mg/m3", "ssaqp", "sum_sensitivity_pop_weighted_AQ"));
        headerColumnItems.put("sum_base_pop_weighted_air_quality", createHeaderColumnItem("Sum Baseline Population weighted AQ", "mg/m3", "sbaqp", "sum_baseline_pop_weighted_AQ"));
        headerColumnItems.put("diff_sum_pop_weighted_air_quality", createHeaderColumnItem("Difference Sum Population weighted AQ", "mg/m3", "dsaqp", "difference_sum_pop_weighted_AQ"));
        headerColumnItems.put("sum_sens_pop_total_cancer_risk", createHeaderColumnItem("Sum Sensitivity total cancer risk", "NA", "sstcr", "sum_sensitivity_total_cancer_risk"));
        headerColumnItems.put("sum_base_pop_total_cancer_risk", createHeaderColumnItem("Sum Baseline total cancer risk", "NA", "sbtcr", "sum_baseline_total_cancer_risk"));
        headerColumnItems.put("sum_sens_pop_total_cancer_risk", createHeaderColumnItem("Difference Sum total cancer risk", "NA", "dstcr", "difference_sum_total_cancer_risk"));

        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Sum Sensitivity pop. weighted cancer risk", "NA", "sspw", "sum_sensitivity_pop_weighted_cancer_risk"));
        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Sum Baseline pop. weighted cancer risk", "NA", "sbpw", "sum_baseline_pop_weighted_cancer_risk"));
        headerColumnItems.put("sum_sens_pop_weighted_cancer_risk", createHeaderColumnItem("Difference Sum pop. weighted cancer risk", "NA", "dspw", "difference_sum_pop_weighted_cancer_risk"));

        for (Column column : dataset.getDatasetType().getFileFormat().cols()) {
            String columnName = column.getName().toLowerCase();
            HeaderColumnItem headerColumnItem = headerColumnItems.get(columnName);
            headerColumnNames += (headerColumnNames.length() > 0 ? "," : "") + (headerColumnItem != null ? headerColumnItem.columnName : columnName);
            headerColumnUnits += (headerColumnUnits.length() > 0 ? "," : "") + (headerColumnItem != null ? headerColumnItem.columnUnits : "NA");
            headerColumnAbbreviations += (headerColumnAbbreviations.length() > 0 ? "," : "") + (headerColumnItem != null ? headerColumnItem.columnAbbreviation : columnName);
            headerColumnDescriptions += (headerColumnDescriptions.length() > 0 ? "," : "") + (headerColumnItem != null ? headerColumnItem.columnDescription : columnName);
        }

        //TBD:  write out grid header... some of this will be hardcoded for now
        //DET4k_36X45 1044000.0 252000.0 4000.0 4000.0 36 45 1 LAMBERT meters 33.0 45.0 -97.0 -97.0 40.0
        String headerGrid = grid.getAbbreviation() + " " + grid.getXcent() + " " + grid.getYcent() + " " + grid.getXcell() + " " + grid.getYcell() + " " + grid.getNcols() + " " + grid.getNrows() + " " + grid.getNthik() + " " + "LAMBERT" + " " + "meters 33.0 45.0 -97.0 -97.0 40.0";
        writer.println("#" + headerColumnGridIdentifier + " " + headerGrid);

        writer.println("#" + headerColumnNameIdentifier + " " + headerColumnNames);
        writer.println("#" + headerColumnUnitIdentifier + " " + headerColumnUnits);
        writer.println("#" + headerColumnAbbreviationIdentifier + " " + headerColumnAbbreviations);
        writer.println("#" + headerColumnDescriptionIdentifier + " " + headerColumnDescriptions);
    }

    private void executeQuery(Statement statement, String writeQuery) throws SQLException {
        // Statement statement = null;

        try {
            // statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.execute(writeQuery);
        } catch (Exception e) {
            log.error("Error executing query: " + writeQuery + ".", e);
            throw new SQLException(e.getMessage());
        } finally {
            // if (statement != null)
            // statement.close();
        }
    }

    private void concatFiles(File file, String headerFile, String dataFile) throws Exception {
        String[] cmd = null;

        if (windowsOS) {
            // System.out.println("copy " + headerFile + " + " + dataFile + " " + file.getAbsolutePath() + " /Y");
            cmd = getCommands("copy " + headerFile + " + " + dataFile + " " + file.getAbsolutePath() + " /Y");
        } else {
            String cmdString = "cat " + headerFile + " " + dataFile + " > " + file.getAbsolutePath();
            cmd = new String[] { "sh", "-c", cmdString };
        }

        Process p = Runtime.getRuntime().exec(cmd);
        int errorLevel = p.waitFor();

        if (errorLevel > 0)
            throw new Exception("Concatinating header and ORL data to file " + file.getAbsolutePath() + " failed.");
    }

    private String[] getCommands(String command) {
        String[] cmd = new String[3];
        String os = System.getProperty("os.name");

        if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            cmd[0] = "command.com";
        } else {
            cmd[0] = "cmd.exe";
        }

        cmd[1] = "/C";
        cmd[2] = command;

        return cmd;
    }

    private String getColsSpecdQueryString(Dataset dataset, String originalQuery, Datasource datasource) throws EmfException {
        String selectColsString = "SELECT ";
        Column[] cols = dataset.getDatasetType().getFileFormat().cols();
        Map<String, String> tableColsMap = getTableCols(dataset, datasource);
        int numCols = cols.length;

        for (int i = 0; i < numCols; i++) {
            String colName = cols[i].name().toLowerCase();
            // make sure you only include columns that exist in the table, new columns could have been
            // added to the ORL file format...
            selectColsString += (tableColsMap.containsKey(colName) ? colName : "null as " + colName) + ",";
        }

        selectColsString = selectColsString.substring(0, selectColsString.length() - 1);

        return selectColsString + " " + getSubString(originalQuery, "FROM", false);
    }

    private String getWriteQueryString(String dataFile, String query, Dataset dataset) {
        String withClause = " WITH NULL '' CSV FORCE QUOTE " + getNeedQuotesCols(dataset);

        return "COPY (" + query + ") to '" + putEscape(dataFile) + "'" + withClause;
    }

    private String getNeedQuotesCols(Dataset dataset) {
        String colNames = "";
        Column[] cols = dataset.getDatasetType().getFileFormat().cols();
        int numCols = cols.length;

        for (int i = 0; i < numCols; i++) {
            String colType = cols[i].sqlType().toUpperCase();

            if (colType.startsWith("VARCHAR") || colType.startsWith("TEXT"))
                colNames += cols[i].name() + ",";
        }

        return (colNames.length() > 0) ? colNames.substring(0, colNames.length() - 1) : colNames;
    }

    public void setExportedLines(String originalQuery, Statement statement) throws SQLException {
        Date start = new Date();

        // Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
        // ResultSet.CONCUR_READ_ONLY);
        String fromClause = getSubString(originalQuery, "FROM", false);
        String queryCount = "SELECT COUNT(\"dataset_id\") " + getSubString(fromClause, "ORDER BY", true);
        ResultSet rs = statement.executeQuery(queryCount);
        rs.next();
        rs.getLong(1);
        statement.close();

        Date ended = new Date();
        double lapsed = (ended.getTime() - start.getTime()) / 1000.00;

        if (lapsed > 5.0)
            log.warn("Time used to count exported data lines(second): " + lapsed);
    }

    private String getSubString(String origionalString, String mark, boolean beforeMark) {
        int markIndex = origionalString.indexOf(mark);

        if (markIndex < 0)
            return origionalString;

        if (beforeMark)
            return origionalString.substring(0, markIndex);

        return origionalString.substring(markIndex);
    }
}
