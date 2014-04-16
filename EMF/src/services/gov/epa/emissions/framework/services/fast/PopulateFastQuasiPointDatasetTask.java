package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class PopulateFastQuasiPointDatasetTask implements Runnable {

    private static final Log LOG = LogFactory.getLog(PopulateFastQuasiPointDatasetTask.class);

    private User user;

    private FastDataset fastDataset;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private StatusDAO statusDAO;
    
    private DatasetDAO datasetDAO;
    
    public PopulateFastQuasiPointDatasetTask(User user, FastDataset fastDataset,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.user = user;
        this.fastDataset = fastDataset;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.datasetDAO = new DatasetDAO(dbServerFactory);
    }

    public void run() {
        try {

            FastNonPointDataset fastNonPointDataset = fastDataset.getFastNonPointDataset();
            
            //do the work here...
            setStatus("Started populating FAST quasi point inventory, '" + fastDataset.getDataset().getName() + "'");
            
            populatePointInvFromSMOKEGriddedSCCRpt(fastNonPointDataset.getBaseNonPointDataset(), fastNonPointDataset.getBaseNonPointDatasetVersion(), fastNonPointDataset.getGriddedSMKDataset(), fastNonPointDataset.getGriddedSMKDatasetVersion(), fastNonPointDataset.getInvTableDataset(), fastNonPointDataset.getInvTableDatasetVersion(), fastDataset.getDataset(), fastNonPointDataset.getGrid());
            
            updateDatasetVersionRecordCount(fastDataset.getDataset());

            createIndexes(fastDataset.getDataset());
            
            setStatus("Finished populating FAST quasi point inventory, '" + fastDataset.getDataset().getName() + "'");

        
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not create inventory output. " + e.getMessage());
        } 
    }
    
    private void createIndexes(EmfDataset dataset) {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            
            DataTable dataTable = new DataTable(dataset, dbServer.getEmissionsDatasource());
            String table = dataset.getInternalSources()[0].getTable();

            //ALWAYS create indexes for these core columns...
            dataTable.addIndex(table, "record_id", true);
            dataTable.addIndex(table, "dataset_id", false);
            dataTable.addIndex(table, "version", false);
            dataTable.addIndex(table, "delete_versions", false);

            dataTable.addIndex(table, "fips", false);
            dataTable.addIndex(table, "poll", false);
            dataTable.addIndex(table, "scc", false);
            dataTable.addIndex(table, "plantid", false);
            dataTable.addIndex(table, "pointid", false);
            dataTable.addIndex(table, "stackid", false);
            dataTable.addIndex(table, "segment", false);
            dataTable.addIndex(table, "mact", false);
            dataTable.addIndex(table, "sic", false);

            //finally analyze the table, so the indexes take affect immediately, 
            //NOT when the SQL engine gets around to analyzing eventually
            dataTable.analyzeTable(table);
        } catch (Exception e) {
            //suppress all errors
            e.printStackTrace();
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    protected void updateDatasetVersionRecordCount(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            
            if (dataset != null) {
                Version version = datasetDAO.getVersion(session, dataset.getId(), dataset.getDefaultVersion());
                
                if (version != null) {
                    version.setCreator(user);
                    updateVersion(dataset, version, dbServer, session);
                }
            }
        } catch (Exception e) {
            throw new EmfException("Cannot update dataset (dataset id: " + dataset.getId() + "). " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session) throws Exception {
        version = datasetDAO.obtainLockOnVersion(user, version.getId(), session);
        version.setNumberRecords((int)datasetDAO.getDatasetRecordsNumber(dbServer, session, dataset, version));
        datasetDAO.updateVersionNReleaseLock(version, session);
    }

    private Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    private void populatePointInvFromSMOKEGriddedSCCRpt(EmfDataset baseInv, int baseInvVersion, 
            EmfDataset smokeRpt, int smokeRptVersion, 
            EmfDataset invTable, int invTableVersion, 
            EmfDataset newInv, Grid grid) throws EmfException, Exception {
        String baseInvTableName = baseInv.getInternalSources()[0].getTable();
//        String smokeRptTableName = smokeRpt.getInternalSources()[0].getTable();
        String newInvTableName = newInv.getInternalSources()[0].getTable();
        String invTableTableName = invTable.getInternalSources()[0].getTable();
        int newInvDatasetId = newInv.getId();
        VersionedQuery baseVersionedQuery = new VersionedQuery(version(baseInv, baseInvVersion), "inv");
//      VersionedQuery smokeRptVersionedQuery = new VersionedQuery(version(smokeRpt, smokeRptVersion), "smk");
        VersionedQuery invTableVersionedQuery = new VersionedQuery(version(invTable, invTableVersion), "invtable");

        String sql = "select ";
        String columnList = "";

        DbServer dbServer = dbServerFactory.getDbServer();
        TableFormat newTableFormat = new FileFormatFactory(dbServer).tableFormat(newInv.getDatasetType());
        TableFormat baseInvTableFormat = new FileFormatFactory(dbServer).tableFormat(baseInv.getDatasetType());
        Column[] columns = newTableFormat.cols();
        Datasource datasource = dbServer.getEmissionsDatasource();
        Connection con = dbServer.getConnection();
        Statement statement = null;
        makeSureInventoryDatasetHasIndexes(baseInvTableName, datasource);
        try {
            String pointEWKT = "POINT(' || (" + grid.getXcent() + " + " + grid.getXcell() + " * (smk.x - 0.5)) || ' ' || (" + grid.getYcent() + " + " + grid.getYcell() + " * (smk.y - 0.5)) || ')')";
            for (int i = 0; i < columns.length; i++) {
                String columnName = columns[i].name();
                if (columnName.equalsIgnoreCase("record_id")) {
    //                sql += "record_id";
    //                columnList += "record_id";
                } else if (columnName.equalsIgnoreCase("dataset_id")) {
    //              sql += "," + datasetId + " as dataset_id";
    //              columnList += ",dataset_id";
                    sql += newInvDatasetId + "::integer as dataset_id";
                    columnList += "dataset_id";
                } else if (columnName.equalsIgnoreCase("delete_versions")) {
                    sql += ", '' as delete_versions";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("version")) {
                    sql += ", 0 as version";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("plantid")) {
                    sql += ", smk.x || '' as plantid";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("pointid")) {
                    sql += ", smk.y || '' as pointid";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("stackid")) {
                    sql += ", smk.x || '' as stackid";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("segment")) {
                    sql += ", smk.y || '' as segment";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("poll")) {
                    sql += ", coalesce(invtable.cas, smk.poll) as poll";
                    columnList += "," + columnName;
    //                invtable.cas
                } else if (columnName.equalsIgnoreCase("avd_emis")) {
                    sql += ", smk.emis "
//                        + " / (inv.ann_emis * coalesce(invtable.factor, 1.0)) * (select sum(inv2.ann_emis * invtable2.factor) as total_emis" 
//                        + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
//                        + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
//                        + " on invtable2.cas = inv2.poll "
//                        + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
//                        + " and invtable2.factor = 1.0"
//                        + " where " + baseVersionedQuery.query().replace("inv", "inv2") + ")"
                        + " / 365 as avd_emis";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("ann_emis")) {
                    sql += ", smk.emis "
//                        + " / (inv.ann_emis * coalesce(invtable.factor, 1.0)) * (select sum(inv2.ann_emis * invtable2.factor) as total_emis" 
//                        + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
//                        + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
//                        + " on invtable2.cas = inv2.poll "
//                        + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
//                        + " and invtable2.factor = 1.0"
//                        + " where " + baseVersionedQuery.query().replace("inv", "inv2") + ") "
                        + " as ann_emis";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("xloc")) {
                    
                    sql += ", public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104307;" + pointEWKT + ",104308)) as x_loc";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("yloc")) {
                    sql += ", public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104307;" + pointEWKT + ",104308)) as y_loc";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("cpri") && hasColName("PRIMARY_DEVICE_TYPE_CODE", baseInvTableFormat)) {
                    sql += ", PRIMARY_DEVICE_TYPE_CODE::integer as cpri";
                    columnList += "," + columnName;
                } else if (columnName.equalsIgnoreCase("CSEC") && hasColName("SECONDARY_DEVICE_TYPE_CODE", baseInvTableFormat)) {
                    sql += ", SECONDARY_DEVICE_TYPE_CODE::integer as CSEC";
                    columnList += "," + columnName;
                } else {
                    sql += "," + (hasColName(columnName, baseInvTableFormat) ? "inv." : "newinv.") + columnName;
                    columnList += "," + columnName;
                }
    //            public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lat, public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104307;POINT(' || (1044000.0 + 4000.0 * (aq.x - 0.5)) || ' ' || (252000.0 + 4000.0 * (aq.y - 0.5)) || ')'),104308)) as lon            
            }
            sql += " FROM " + qualifiedTable(baseInvTableName, datasource) + " as inv"
                + " inner join " + qualifiedTable(invTableTableName, datasource)
                + " as invtable"
                + " on invtable.cas = inv.poll"
                + " and " + invTableVersionedQuery.query()
                + " and invtable.factor = 1.0"
    //            + " inner join ("
    //            + " select inv2.fips, inv2.scc, invtable2.name as smoke_name, sum(inv2.ann_emis * invtable2.factor) as total_emis" 
    //            + " from " + qualifiedTable(baseInvTableName, datasource) + " as inv2"
    //            + " inner join " + qualifiedTable(invTableTableName, datasource) + " as invtable2 "
    //            + " on invtable2.cas = inv2.poll "
    //            + " and " + invTableVersionedQuery.query().replace("invtable", "invtable2")
    //            + " and invtable2.factor = 1.0"
    //            + " where " + baseVersionedQuery.query().replace("inv", "inv2")
    //            + " and inv2.fips in ('26049','26065','26075','26087','26091','26093','26099','26115','26125','26147','26155','26161','26163','39043','39051','39095','39123','39173')"
    //            + " group by inv2.fips, inv2.scc, invtable2.name"
    //            + " order by inv2.fips, inv2.scc, invtable2.name"
    //            + ") as total" 
    //            + " on total.fips = inv.fips and total.scc = inv.scc and total.smoke_name = invtable.name"
                + " inner join ("
                + buildSQLSelectForSMOKEGriddedSCCRpt(smokeRpt, smokeRptVersion, grid)
                + ") as smk" 
                + " on inv.fips = smk.fips and inv.scc = smk.scc and invtable.name = smk.poll" 
                + " left outer join " + qualifiedTable(newInvTableName, datasource) + " as newinv "
                + " on 1 = 0"
                + " WHERE " + baseVersionedQuery.query();
            sql = "INSERT INTO " + qualifiedTable(newInvTableName, datasource) + " (" + columnList + ") \n" + sql;
//            System.out.println(sql);
            statement = con.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query \n" + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
            if (dbServer != null) {
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                dbServer = null;
            }
        }
    }

    private String buildSQLSelectForSMOKEGriddedSCCRpt(EmfDataset griddedSCCDataset, int griddedSCCDatasetVersionNumber,
            Grid grid) throws EmfException {
        String sql = "";
        if (griddedSCCDataset.getSectors() == null || griddedSCCDataset.getSectors().length == 0)
            throw new EmfException("Dataset " + griddedSCCDataset.getName() + " is missing the sector.");
        Sector sector = griddedSCCDataset.getSectors()[0];
        String tableName = griddedSCCDataset.getInternalSources()[0].getTable();
        if (sector == null)
            throw new EmfException("Dataset " + griddedSCCDataset.getName() + " is missing the sector.");
        VersionedQuery griddedSCCDatasetVersionedQuery = new VersionedQuery(version(griddedSCCDataset, griddedSCCDatasetVersionNumber));
        boolean isMonthly = sector.getName().equals("nonroad") || sector.getName().equals("onroad");
        List<String> pollutantColumns = getDatasetPollutantColumns(griddedSCCDataset);
        for (int i = 0; i < pollutantColumns.size(); i++) {
            String pollutant = pollutantColumns.get(i);
            sql += (i > 0 ? " union all " : "") + "select '" + sector.getName().replace("'", "''")
                    + "'::varchar(64) as sector, substring(region,2) as fips, scc, '" + pollutant.toUpperCase() + "' as poll, sum("
                    + (!isMonthly ? "" : "365 * ") + pollutant + ") as emis, x_cell::integer as x, y_cell::integer as y from emissions."
                    + tableName + " where  " + griddedSCCDatasetVersionedQuery.query()
                    + " and x_cell::integer between 1 and " + grid.getNcols() + " and y_cell::integer between 1 and " + grid.getNrows() + " and coalesce(" + pollutant
                    + ",0.0) <> 0.0 group by x_cell::integer, y_cell::integer, substring(region,2), scc \n";
        }
        return sql;
    }

    private List<String> getDatasetPollutantColumns(EmfDataset dataset) throws EmfException {
        List<String> pollutantColumnList = new ArrayList<String>();
        ResultSet rs;
        ResultSetMetaData md;
        Statement statement = null;
        DbServer dbServer = dbServerFactory.getDbServer();
        Connection con = dbServer.getConnection();
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery("select * from emissions." + dataset.getInternalSources()[0].getTable()
                    + " where 1 = 0;");
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        try {
            for (int i = 1; i < md.getColumnCount(); i++) {
                String columnName = md.getColumnName(i);
                int columnType = md.getColumnType(i);
                // ignore these columns, we really just want the pollutant/specie columns
                if (!columnName.equalsIgnoreCase("x_cell") && !columnName.equalsIgnoreCase("y_cell")
                        && !columnName.equalsIgnoreCase("source_id") && !columnName.equalsIgnoreCase("region")
                        && !columnName.equalsIgnoreCase("scc") && !columnName.equalsIgnoreCase("scc2")
                        && !columnName.equalsIgnoreCase("record_id") && !columnName.equalsIgnoreCase("dataset_id")
                        && !columnName.equalsIgnoreCase("version") && !columnName.equalsIgnoreCase("delete_versions")
                        && !columnName.equalsIgnoreCase("road") && !columnName.equalsIgnoreCase("link")
                        && !columnName.equalsIgnoreCase("veh_type") && columnType == Types.DOUBLE)
                    pollutantColumnList.add(columnName);
            }
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
            if (dbServer != null) {
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                dbServer = null;
            }
            
        }

        return pollutantColumnList;

    }
    
    private boolean hasColName(String colName, TableFormat fileFormat) {
        Column[] cols = fileFormat.cols();
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

    private String qualifiedTable(String table, Datasource datasource) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }

    private void makeSureInventoryDatasetHasIndexes(String datasetTableName, Datasource datasource) throws EmfException {
        String query = "SELECT public.create_orl_table_indexes('" + datasetTableName.toLowerCase() + "');analyze " + qualifiedTable(datasetTableName, datasource).toLowerCase() + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    private void close(DbServer dbServer) {
        try {
            if (dbServer != null)
                dbServer.disconnect();
        } catch (Exception e) {
            LOG.error("Could not close database connection." + e.getMessage());
        }
    }

    private void setStatus(String message) {
        statusDAO.add(createStatus(user, message));
    }


    private Status createStatus(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("FAST Dataset");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    public boolean shouldProceed() throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
//            for (int i = 0; i < controlStrategyResults.length; i++) {
//                if (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)
//                        || controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory)) {
//                    Dataset detailedResultDataset = controlStrategyResults[i].getDetailedResultDataset();
//                    if (detailedResultDataset == null)
//                        throw new EmfException("You should run the control strategy first before creating the inventory, input inventory - " + controlStrategyResults[i].getInputDataset().getName());
//                    String detailResultTableName = detailedResultDataset.getInternalSources()[0].getTable();
//                    int totalRows = dbServer.getEmissionsDatasource().tableDefinition().totalRows(detailResultTableName);
//                    if (totalRows == 0) {
//                        throw new EmfException(
//                                "Control Strategy Result does not have any data in the table. Control inventory is not created, input inventory - " + controlStrategyResults[i].getInputDataset().getName());
//                    }
//                }
//            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
        return true;
    }
}