package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class ApplyMeasureInSeriesControlStrategyInventoryOutput implements ControlStrategyInventoryOutput {

    protected ControlStrategy controlStrategy;

    protected DatasetCreator creator;

    protected TableFormat tableFormat;

    protected StatusDAO statusServices;

    protected User user;

    protected HibernateSessionFactory sessionFactory;

//    private DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    protected EmfDataset inputDataset;
    
    protected ControlStrategyResult controlStrategyResult;

    protected Datasource datasource;

    protected String namePrefix;
    
    public ApplyMeasureInSeriesControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, String namePrefix, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.controlStrategyResult = controlStrategyResult;
        this.inputDataset = controlStrategyResult.getInputDataset();
        this.user = user;
        this.sessionFactory = sessionFactory;
//        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.tableFormat = new FileFormatFactory(dbServer).tableFormat(inputDataset.getDatasetType());
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                dbServer.getEmissionsDatasource(), new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords()));
        this.statusServices = new StatusDAO(sessionFactory);
        this.namePrefix = namePrefix;
    }

    public void create() throws Exception {
        doCreateInventory(inputDataset, getDatasetTableName(inputDataset));
    }

    protected void doCreateInventory(EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {
            EmfDataset dataset = creator.addControlledInventoryDataset(creator.createControlledInventoryDatasetName(namePrefix, inputDataset), 
                    inputDataset, inputDataset.getDatasetType(), 
                    tableFormat, description(inputDataset));
            
            String outputInventoryTableName = getDatasetTableName(dataset);
            
            ControlStrategyResult result = getControlStrategyResult(controlStrategyResult.getId());
            try {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource);
            } catch (Exception e) {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource, true);
            }        

            setControlStrategyResultContolledInventory(result, dataset);
            updateVersion(dataset, dbServer, sessionFactory.getSession(), user);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }
    
    protected void updateVersion(EmfDataset dataset, DbServer dbsrv, Session session, User usr) throws Exception {
        Version version = version(dataset, dataset.getDefaultVersion());
        
        if (version == null)
            return;
        
        DatasetDAO dao = new DatasetDAO();
        version = dao.obtainLockOnVersion(usr, version.getId(), session);
        version.setNumberRecords((int)dao.getDatasetRecordsNumber(dbsrv, session, dataset, version));
        version.setCreator(usr);
        dao.updateVersionNReleaseLock(version, session);
    }

//    private void setandRunQASteps() throws EmfException {
//        try {
//            ControlStrategyResult result = getControlStrategyResult();
//            EmfDataset controlledDataset = (EmfDataset) result.getControlledInventoryDataset();
//            QAStepTask qaTask = new QAStepTask(controlledDataset, controlledDataset.getDefaultVersion(), user,
//                    sessionFactory, dbServerFactory);
//            qaTask.runSummaryQASteps(qaTask.getDefaultSummaryQANames());
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        }
//    }

    protected String description(EmfDataset inputDataset) {
        String startingDesc = inputDataset.getDescription() + "";
        if ((startingDesc.indexOf("FIPS,SCC") > 0) || (startingDesc.indexOf("\"FIPS\",") > 0))
        {
            //the columns desc is already included
            return startingDesc + "\n" + creator.getKeyValsAsHeaderString();
        }
        //the columns desc is not included
        return inputDataset.getDescription() 
        + "\n#DESC " + columnListForHeader() 
        + "\n" + creator.getKeyValsAsHeaderString();
    }

    //build comma-delimted list of column name
    protected String columnListForHeader() {
        String delimitedList = "";
        Column[] columns = tableFormat.cols();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name().toLowerCase();
            if (!columnName.equalsIgnoreCase("Record_Id")
                    && !columnName.equalsIgnoreCase("Dataset_Id")
                    && !columnName.equalsIgnoreCase("Version")
                    && !columnName.equalsIgnoreCase("Delete_Versions")
                    ) 
            delimitedList += (delimitedList.length() > 0 ? "," + "\"" + columnName + "\"" : "\"" + columnName + "\"");
        }
        return delimitedList;
    }

    protected ControlStrategyResult getControlStrategyResult(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            ControlStrategyResult result = dao.getControlStrategyResult(id, session);
            if (result == null)
                throw new EmfException("You have to run the control strategy to create control inventory output");
            return result;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    protected void addControlStrategyResult(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            int id = dao.add(result, session);
            result.setId(id);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveControlStrategyResult(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(result, session);
        } catch (Exception e) {
            throw new EmfException("Could not update control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void setControlStrategyResultContolledInventory(ControlStrategyResult result, EmfDataset controlledInventory) throws EmfException {
        result.setControlledInventoryDataset(controlledInventory);
        saveControlStrategyResult(result);
    }

    protected Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }
    
    protected String getDatasetTableName(Dataset dataset) throws EmfException {
        if (dataset == null) return "";
        InternalSource[] sources = dataset.getInternalSources();
        if (sources.length > 1) {
            throw new EmfException(
                    "At this moment datasets with multiple tables are not supported for creating a inventory output");
        }
        String tableName = sources[0].getTable();
        return tableName;
    }

    protected int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInZeroBasedMonth(controlStrategy.getInventoryYear(), month) : 31;
    }
    
    protected String detailDatasetTable(ControlStrategyResult result) throws EmfException {
        return getDatasetTableName(result.getDetailedResultDataset());
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource) throws EmfException {
        createControlledInventory(datasetId, inputTable, 
                detailResultTable, outputTable, 
                version, dataset, 
                datasource, false);
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) throws EmfException {
        String query = populateInventory(datasetId, inputTable, 
                detailResultTable, outputTable,
                version(inputDataset, controlStrategyResult.getInputDatasetVersion()), inputDataset, 
                datasource, missingColumns);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }

    private String populateInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) throws EmfException {
        VersionedQuery versionedQuery = new VersionedQuery(version);
        int month = inputDataset.applicableMonth();
        int noOfDaysInMonth = 31;
        boolean isMonthlyInventory = false;
        if (month != -1) {
            noOfDaysInMonth = getDaysInMonth(month);
            isMonthlyInventory = true;
        }
        String sql = "select ";
        String columnList = "";
        Column[] columns = tableFormat.cols();
        //flag indicating if we are doing replacement vs
        //add on controls., currently we only support replacement controls.
        boolean isReplacementControl = false;//TODO, still needs work
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
//                sql += "record_id";
//                columnList += "record_id";
            } else if (columnName.equalsIgnoreCase("dataset_id")) {
//              sql += "," + datasetId + " as dataset_id";
//              columnList += ",dataset_id";
                sql += datasetId + " as dataset_id";
                columnList += "dataset_id";
            } else if (columnName.equalsIgnoreCase("delete_versions")) {
                sql += ", '' as delete_versions";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("version")) {
                sql += ", 0 as version";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ceff")) {
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then (1- b.final_emissions / b.starting_emissions) * 100 else null::double precision end else ceff end as ceff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("avd_emis")) {
                    sql += ", case when b.source_id is not null then b.final_emissions / " + (month != -1 ? noOfDaysInMonth : "365") + " else avd_emis end as avd_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ann_emis")) {
                if (isMonthlyInventory)
                    sql += ", -9.0::double precision as ann_emis";
                else
                    sql += ", case when b.source_id is not null then b.final_emissions else ann_emis end as ann_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("reff")) {
                sql += ", case when b.source_id is not null then 100 else reff end as reff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("rpen")) {
                sql += ", case when b.source_id is not null then 100 else rpen end as rpen";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CONTROL_MEASURES")) {
                if (isReplacementControl)
                    sql += ", cm_abbrev_list as CONTROL_MEASURES";
                else
                    sql += ", case when " + (!missingColumns ? "control_measures" : "null") + " is null or length(" + (!missingColumns ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (!missingColumns ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                if (isReplacementControl)
                    sql += ", percent_reduction_list as PCT_REDUCTION";
                else
                    sql += ", case when " + (!missingColumns ? "pct_reduction" : "null") + " is null or length(" + (!missingColumns ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (!missingColumns ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CURRENT_COST")) {
                sql += ", annual_cost as CURRENT_COST";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CUMULATIVE_COST")) {
                if (isReplacementControl)
                    sql += ", case when b.source_id is not null then " + (!missingColumns ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!missingColumns ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
                else
                    sql += ", case when b.source_id is not null then " + (!missingColumns ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then cumulative_cost + coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!missingColumns ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
//                sql += ", case when " + (!missingColumns ? "cumulative_cost" : "null") + " is null and annual_cost is null then null::double precision else coalesce(" + (!missingColumns ? "cumulative_cost" : "null::double precision") + ", 0.0) + coalesce(annual_cost, 0) end as CUMULATIVE_COST";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(inputTable, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT source_id, "
        + "min(final_emissions) as final_emissions, "
        + "max(input_emis) as starting_emissions, "
        + "sum(annual_cost) as annual_cost, "
        + "public.concatenate_with_ampersand(cm_abbrev) as cm_abbrev_list, "
        + "public.concatenate_with_ampersand(percent_reduction::varchar) as percent_reduction_list "
        + "FROM (select source_id, input_emis, final_emissions, annual_cost, cm_abbrev, percent_reduction "
        + "        FROM " + qualifiedTable(detailResultTable, datasource)
        + "        order by source_id, apply_order "
        + "        ) tbl "
        + "    group by source_id ) as b "
        + "on inv.record_id = b.source_id"
        + " WHERE " + versionedQuery.query();
        sql = "INSERT INTO " + qualifiedTable(outputTable, datasource) + " (" + columnList + ") " + sql;
        if (DebugLevels.DEBUG_25()) 
            System.out.println(sql);
        return sql;
    }

    protected String qualifiedTable(String table, Datasource datasource) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }

    protected void failStatus(StatusDAO statusServices, String message) {
        String end = "Failed to create a controlled inventory for strategy "+controlStrategy.getName()+
           ": " + message;
        Status status = status(user, end);
        statusServices.add(status);
    }

    protected void startStatus(StatusDAO statusServices) {
        String start = "Creating controlled inventory of type '" + inputDataset.getDatasetType()
                + "' using control strategy '" + controlStrategy.getName() + "' for dataset '" + inputDataset.getName() + "'";
        Status status = status(user, start);
        statusServices.add(status);
    }

    protected Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Controlled Inventory");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    protected void createDetailedResultTableIndexes(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.create_strategy_detailed_result_table_indexes('" + getDatasetTableName(controlStrategyResult.getDetailedResultDataset()) + "');analyze emissions." + getDatasetTableName(controlStrategyResult.getDetailedResultDataset()) + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //
        } finally {
            //
        }
    }
}
