package gov.epa.emissions.framework.services.fast.shapefile;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresSQLToShapeFile;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.VersionedQuery;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportFastOutputToShapeFileTask implements Runnable {

//    private String user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportFastOutputToShapeFileTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;

    private String dirName;

    private DbServerFactory dbServerFactory;

    private String userName;

    private int datasetId;

    private int gridId;

    private int datasetVersion;
    
    public ExportFastOutputToShapeFileTask(int datasetId, int datasetVersion, int gridId, String userName, String dirName,
            String pollutant, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.gridId = gridId;
        this.dirName = dirName;
        this.userName = userName;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.dbServerFactory = dbServerFactory;
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
            
            
            PostgresSQLToShapeFile exporter = new PostgresSQLToShapeFile(dbServer);
            // Exporter exporter = new DatabaseTableCSVExporter(result.getTable(), dbServer.getEmissionsDatasource(),
            // batchSize(sessionFactory));
            
            //generate a file per sector
            for (String sector : getDatasetSectors(dataset, datasetVersion)) {
                for (String pollutant : getDatasetPollutants(dataset, datasetVersion, sector)) {
                    file = exportFile(this.dirName, dataset, sector, pollutant);
                    suffix = suffix();
                    prepare(suffix, dataset);
                    String sql = prepareSQLStatement(dataset, datasetVersion, grid, pollutant, sector);
                    exporter.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), getProperty("postgres-user"),
                            getProperty("pgsql2shp-info"), file.getAbsolutePath(), true, sql, null);
                    complete(suffix, dataset);
                }
            }
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

    private String getProperty(String propertyName) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, session);
            return property.getValue();
        } finally {
            session.close();
        }
    }

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

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String[] getDatasetSectors(EmfDataset dataset, Version datasetVersion) throws EmfException {
        DbServer dbServer = null;
        List<String> sectorList = new ArrayList<String>();
        try {
            dbServer = dbServerFactory.getDbServer();
            VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select distinct coalesce(sector, '') as sector from emissions." + emissionTableName(dataset)
                            + " as i where " + datasetVersionedQuery.query() + " order by sector ");

            while (rs.next()) {
                sectorList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new EmfException(e.getMessage(), e);
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return sectorList.toArray(new String[0]);
    }

    private String[] getDatasetPollutants(EmfDataset dataset, Version datasetVersion, String sector) throws EmfException {
        DbServer dbServer = null;
        DatasetType datasetType = dataset.getDatasetType();
        boolean hasCmaqPollutantColumn = false;
        boolean hasPollutantColumn = false;
        for (Column column : datasetType.getFileFormat().getColumns()) {
            if (column.getName().equalsIgnoreCase("cmaq_pollutant")) hasCmaqPollutantColumn = true;
            if (column.getName().equalsIgnoreCase("pollutant")) hasPollutantColumn = true;
        }
        List<String> pollutantList = new ArrayList<String>();
        if (!hasCmaqPollutantColumn && !hasPollutantColumn) return new String[0];
        try {
            dbServer = dbServerFactory.getDbServer();
            VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select distinct coalesce(" + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + ", '') as " + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + " from emissions." + emissionTableName(dataset)
                            + " as i where " + datasetVersionedQuery.query() + " and sector = '" + sector.replace("'","''") + "' order by " + (hasCmaqPollutantColumn ? "cmaq_pollutant" : "pollutant") + " ");

            while (rs.next()) {
                pollutantList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new EmfException(e.getMessage(), e);
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return pollutantList.toArray(new String[0]);
    }

    private String prepareSQLStatement(EmfDataset dataset, Version datasetVersion, Grid grid, String pollutant, String sector) throws ExporterException, EmfException {

        DbServer dbServer = null;
        boolean hasXCol = false;
        boolean hasYCol = false;
        boolean hasPollutantCol = false;
        boolean hasCMAQPollutantCol = false;
        // will hold unique list of column names, pqsql2shp doesn't like multiple columns with the same name...
        Map<String, String> cols = new HashMap<String, String>();
        Map<String, String> colAliases = new HashMap<String, String>();
        //fast run output columns to encounter
        colAliases.put("emission", "ems");
        colAliases.put("air_quality", "aq");
        colAliases.put("population_weighted_air_quality", "aqp");
        colAliases.put("cancer_risk_per_person", "cr");
        colAliases.put("total_cancer_risk", "tcr");
        colAliases.put("population_weighted_cancer_risk", "pw");
        colAliases.put("grid_cell_population", "gcp");
        colAliases.put("pct_population_in_grid_cell_to_model_domain", "pp");
        colAliases.put("cmaq_pollutant", "pollutant");
        
        //fast analysis output columns to encounter
        colAliases.put("sum_sens_emission", "ssems");
        colAliases.put("sum_base_emission", "sbems");
        colAliases.put("diff_sum_emission", "dsems");
        colAliases.put("sum_sens_air_quality", "ssaq");
        colAliases.put("sum_base_air_quality", "sbaq");
        colAliases.put("diff_sum_air_quality", "dsaq");
        colAliases.put("sum_sens_pop_weighted_air_quality", "ssaqp");
        colAliases.put("sum_base_pop_weighted_air_quality", "sbaqp");
        colAliases.put("diff_sum_pop_weighted_air_quality", "dsaqp");
        colAliases.put("sum_sens_pop_total_cancer_risk", "sstcr");
        colAliases.put("sum_base_pop_total_cancer_risk", "sbtcr");
        colAliases.put("diff_sum_pop_total_cancer_risk", "dstcr");
        colAliases.put("sum_sens_pop_weighted_cancer_risk", "sspw");
        colAliases.put("sum_base_pop_weighted_cancer_risk", "sbpw");
        colAliases.put("diff_sum_pop_weighted_cancer_risk", "dspw");

//        EMS AQ  AQP CR  TCR PW  GCP PP  URE
//        Emissions   AQcon   pop_weighted_AQ cancer_risk/person  total_cancer_risk   pop_weighted_cancer_risk    grid_cell_population    perc_pop_in_grid_cell_relative_to_modeling_domain   URE

        String colNames = "";
        String sql = "";
        String tableName = dataset.getInternalSources()[0].getTable();
        VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");

        try {
            // VERSIONS TABLE - Completed - throws exception if the following case is true
            if ("versions".equalsIgnoreCase(tableName.toLowerCase())) {
                throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
            }
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select * from emissions." + tableName + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            String colName = "";
            for (int i = 1; i <= columnCount; i++) {
                colName = md.getColumnName(i).toLowerCase();
                if (!cols.containsKey(colName)) {
                    cols.put(colName, colName);
                    colNames += (colNames.length() > 0 ? "," : "");
                    if (colName.equals("x")) {
                        colNames += "hor.x";
                    } else if (colName.equals("y")) {
                        colNames += "vert.y";
                    } else if (colName.equals("sector")) {
                        colNames += "'" + sector + "'::varchar(64) as sector";
                    } else if (colName.equals("pollutant")) {
                        colNames += "pollutant";
                    } else if (colName.equals("cmaq_pollutant")) {
                        colNames += "cmaq_pollutant";
                    } else {
                        colNames += colName + (!colAliases.containsKey(colName) ? "" : " as " + colAliases.get(colName));
                    }
                }

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
                throw new ExporterException("Dataset is missing applicable columns; x, y, and pollutant (or cmaq_polluant); needed to generate a shapefile.");
            
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
        sql = "select " + colNames + ", ST_translate(origin_grid.boxrep, hor.x * " + grid.getXcell() + ", vert.y * " + grid.getYcell() + ") As the_geom "
            + " from generate_series(0," + (grid.getNcols() - 1) + ") as hor(x) "
            + " cross join generate_series(0," + (grid.getNrows() - 1) + ") as vert(y) "
            + " cross join (SELECT ST_SetSRID(CAST('BOX(" + grid.getXcent() + " " + grid.getYcent() + "," + (grid.getXcent() + grid.getXcell()) + " " + (grid.getYcent() + grid.getYcell()) + ")' as box2d), 104307) as boxrep) as origin_grid "
            + " left outer join emissions." + tableName + " i "
            + " on i.x = hor.x + 1 "
            + " and i.y = vert.y + 1 "
            + " and " + datasetVersionedQuery.query() + " "
            + " and " + (hasPollutantCol ? "i.pollutant" : "i.cmaq_pollutant") + "='" + pollutant + "'"
            + " and i.sector='" + sector + "'"
            + " order by hor.x, vert.y ";
        
        System.out.println(sql);
    
        return sql;
    }

    private void prepare(String suffixMsg, EmfDataset dataset) {
        setStatus("Started exporting FAST output '" + dataset.getName() + "'" + suffixMsg);
    }

    private void complete(String suffixMsg, EmfDataset dataset) {
        setStatus("Completed exporting FAST output '" + dataset.getName() + "'" + suffixMsg);
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

    private String suffix() {
        return " for Version '" + "" + "' of Dataset '" + "" + "' to " + file.getAbsolutePath();
//        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to " + file.getAbsolutePath();
    }

//    private String versionName() {
//        Session session = sessionFactory.getSession();
//        try {
//            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
//        } finally {
//            session.close();
//        }
//    }
//
//    private String datasetName() {
//        Session session = sessionFactory.getSession();
//        try {
//            DatasetDAO dao = new DatasetDAO();
//            return dao.getDataset(session, qastep.getDatasetId()).getName();
//        } finally {
//            session.close();
//        }
//    }

    private File exportFile(String dirName, EmfDataset dataset, String sector, String pollutant) {
        return new File(dirName, getFileName(dataset, sector, pollutant));
    }

    private String getFileName(EmfDataset dataset, String sector, String pollutant) {
        String fileName = sector + "_" + pollutant + "_" + dataset.getName();
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
}
