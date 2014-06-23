package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

public class FastRunTask {

    protected FastRun fastRun;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    private User user;

    private int recordCount;

    private StatusDAO statusDAO;

    private FastDAO fastRunDAO;

    private DatasetCreator creator;

    private Keywords keywords;

    // private TableFormat tableFormat;

    protected List<FastRunOutput> fastRunOutputList;

    private EmfDataset invTableDataset;

    private int invTableDatasetVersion;

    private VersionedQuery invTableVersionedQuery;

    private String invTableTableName;

    private Grid domain;

    public FastRunTask(FastRun fastRun, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory) throws EmfException {
        this.fastRun = fastRun;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.fastRunDAO = new FastDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(null, user, sessionFactory, dbServerFactory, datasource, keywords);
        this.fastRunOutputList = new ArrayList<FastRunOutput>();
        this.invTableDataset = fastRun.getInvTableDataset();
        this.invTableDatasetVersion = fastRun.getInvTableDatasetVersion();
        this.invTableVersionedQuery = new VersionedQuery(version(invTableDataset.getId(), invTableDatasetVersion),
                "invtable");
        this.invTableTableName = qualifiedEmissionTableName(invTableDataset);
        this.domain = fastRun.getGrid();
        // setup the strategy run
        setup();
    }

    private void setup() {
        //
    }

    protected FastRunOutput createFastRunOutput(FastRunOutputType fastRunOutputType, EmfDataset outputDataset)
            throws EmfException {
        FastRunOutput result = new FastRunOutput();
        result.setFastRunId(fastRun.getId());
        result.setOutputDataset(outputDataset);
        // result.setInventoryDataset(inventory);
        // result.setInventoryDatasetVersion(inventoryVersion);

        result.setType(fastRunOutputType);
        result.setStartDate(new Date());
        result.setRunStatus("Start processing inventory dataset");

        // persist output
        saveFastRunOutput(result);
        return result;
    }

    public FastRunOutput createIntermediateInventoryOutput() throws Exception {

        // setup result
        FastRunOutput griddedSectorSCCPollOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_RUN_INTERMEDIATE_INVENTORY
                    + " dataset output.");
            DatasetType datasetType = getDatasetType(DatasetType.FAST_RUN_INTERMEDIATE_INVENTORY);
            EmfDataset griddedSectorSCCPollDataset = creator.addDataset("ds", fastRun.getAbbreviation() + "_"
                    + DatasetType.FAST_RUN_INTERMEDIATE_INVENTORY,
                    datasetType,
                    new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()), "");

            griddedSectorSCCPollOutput = createFastRunOutput(getFastRunOutputType(FastRunOutputType.INTERMEDIATE_INVENTORY),
                    griddedSectorSCCPollDataset);

            populateGriddedEmissionOutput(griddedSectorSCCPollDataset, new String[] {});

            updateOutputDatasetVersionRecordCount(griddedSectorSCCPollOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_RUN_INTERMEDIATE_INVENTORY
                    + " dataset output.");

        } catch (EmfException ex) {
            runStatus = "Failed creating " + DatasetType.FAST_RUN_INTERMEDIATE_INVENTORY
                    + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (griddedSectorSCCPollOutput != null) {
                griddedSectorSCCPollOutput.setCompletionDate(new Date());
                griddedSectorSCCPollOutput.setRunStatus(runStatus);
                saveFastRunOutput(griddedSectorSCCPollOutput);
            }
        }

        return griddedSectorSCCPollOutput;
    }

    public FastRunOutput createIntermediateAirQualityOutput(FastGriddedCMAQPollutantAirQualityEmissionResult[] results)
            throws Exception {
        // setup result
        FastRunOutput intermediateAirQualityOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_RUN_INTERMEDIATE_AIR_QUALITY + " dataset output.");

            EmfDataset intermediateAirQualityDataset = createIntermediateAirQualityDataset(results);

            intermediateAirQualityOutput = createFastRunOutput(
                    getFastRunOutputType(FastRunOutputType.INTERMEDIATE_AIR_QUALITY),
                    intermediateAirQualityDataset);

            // populateGriddedEmissionOutput(griddedSummaryEmissionAQDataset, new String[] {});

            updateOutputDatasetVersionRecordCount(intermediateAirQualityOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_RUN_INTERMEDIATE_AIR_QUALITY + " dataset output.");

        } catch (EmfException ex) {
            runStatus = "Failed creating " + DatasetType.FAST_RUN_INTERMEDIATE_AIR_QUALITY + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (intermediateAirQualityOutput != null) {
                intermediateAirQualityOutput.setCompletionDate(new Date());
                intermediateAirQualityOutput.setRunStatus(runStatus);
                saveFastRunOutput(intermediateAirQualityOutput);
            }
        }

        return intermediateAirQualityOutput;
    }

    public FastRunOutput createGriddedOutput(
            FastGriddedCMAQPollutantAirQualityEmissionResult[] results) throws Exception {

        // setup result
        FastRunOutput griddedOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_RUN_GRIDDED_OUTPUT + " dataset output.");

            EmfDataset griddedOutputDataset = createGriddedOutputDataset(results);

            griddedOutput = createFastRunOutput(
                    getFastRunOutputType(FastRunOutputType.GRIDDED_OUTPUT),
                    griddedOutputDataset);

            // populateGriddedEmissionOutput(griddedDetailedEmissionAQDataset, new String[] {});

            updateOutputDatasetVersionRecordCount(griddedOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_RUN_GRIDDED_OUTPUT + " dataset output.");

        } catch (EmfException ex) {
            ex.printStackTrace();
            runStatus = "Failed creating " + DatasetType.FAST_RUN_GRIDDED_OUTPUT + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (griddedOutput != null) {
                griddedOutput.setCompletionDate(new Date());
                griddedOutput.setRunStatus(runStatus);
                saveFastRunOutput(griddedOutput);
            }
        }

        return griddedOutput;
    }

    public FastRunOutput createDomainOutput(EmfDataset griddedOutputDataset) throws Exception {

        // setup result
        FastRunOutput domainOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_RUN_DOMAIN_OUTPUT + " dataset output.");

            EmfDataset domainOutputDataset = createDomainOutputDataset();

            domainOutput = createFastRunOutput(
                    getFastRunOutputType(FastRunOutputType.DOMAIN_OUTPUT),
                    domainOutputDataset);

            populateDomainOutput(domainOutputDataset, griddedOutputDataset);

            updateOutputDatasetVersionRecordCount(domainOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_RUN_DOMAIN_OUTPUT + " dataset output.");

        } catch (EmfException ex) {
            ex.printStackTrace();
            runStatus = "Failed creating " + DatasetType.FAST_RUN_DOMAIN_OUTPUT + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (domainOutput != null) {
                domainOutput.setCompletionDate(new Date());
                domainOutput.setRunStatus(runStatus);
                saveFastRunOutput(domainOutput);
            }
        }

        return domainOutput;
    }

    public void run() throws EmfException {

        // get rid of run outputs
        deleteRunOutputs();

        // run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        // String status = "";
        try {

            FastRunOutput intermediateInventoryOutput = createIntermediateInventoryOutput();

            calculateAQ(intermediateInventoryOutput.getOutputDataset());

            // now create the measure summary result based on the results from the strategy run...
            // generateStrategyMeasureSummaryResult();

            // //now create the county summary result based on the results from the strategy run...
            // generateStrategyCountySummaryResult();

        } catch (Exception e) {
            // status = "Failed. Error processing inventory";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            // run any post processes
            try {
                afterRun();
                // updateVersionInfo();
            } catch (Exception e) {
                // status = "Failed. Error processing inventory";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                disconnectDbServer();
            }
        }
    }

    private void afterRun() {
        // NOTE Auto-generated method stub

    }

    private void beforeRun() throws EmfException {
        for (FastRunInventory fastRunInventory : fastRun.getInventories()) {
            // make sure inventory has indexes created...
            makeSureInventoryDatasetHaveIndexes(fastRunInventory);
        }
        // NOTE Auto-generated method stub

    }

    protected void deleteRunOutputs() throws EmfException {

        Session session = sessionFactory.getSession();
        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();

            // first get the datasets to delete
            EmfDataset[] datasets = fastRunDAO.getOutputDatasets(fastRun.getId(), session);
            if (datasets != null) {
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        setStatus("The Fast run output dataset, " + dataset.getName()
                                + ", will not be deleted since you are not the creator.");
                    } else {
                        dsList.add(dataset);
                    }
                }
            }

            // get rid of old strategy results...
            removeFastRunOutputs();

            // delete and purge datasets
            if (dsList != null && dsList.size() > 0) {
                fastRunDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs.");
        } finally {
            session.close();
        }
    }

    protected int getDaysInMonth(int year, int month) {
        return month != -1 ? DateUtil.daysInZeroBasedMonth(year, month) : 31;
    }

    public void makeSureInventoryDatasetHaveIndexes(FastRunInventory fastRunInventory) throws EmfException {
        String query = "SELECT public.create_orl_table_indexes('"
                + emissionTableName(fastRunInventory.getDataset()).toLowerCase() + "');analyze "
                + qualifiedEmissionTableName(fastRunInventory.getDataset()).toLowerCase() + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            // e.printStackTrace();
            // supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    protected boolean isRunStatusCancelled() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return fastRunDAO.getFastRunRunStatus(fastRun.getId(), session).equals("Cancelled");
        } catch (RuntimeException e) {
            throw new EmfException("Could not check if strategy run was cancelled.");
        } finally {
            session.close();
        }
    }

    protected int getRecordCount(EmfDataset dataset) throws EmfException {
        String query = "SELECT count(1) as record_count " + " FROM " + qualifiedEmissionTableName(dataset);
        ResultSet rs = null;
        Statement statement = null;
        int recordCount = 0;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
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
        }
        return recordCount;
    }

    private double getDomainPopulationCount(EmfDataset dataset, Grid grid) throws EmfException {
        String query = "SELECT sum(totalpop) as totalpop " + " FROM " + qualifiedEmissionTableName(dataset) + " "
                + " WHERE col between 1 and " + grid.getNcols() + "::integer and row between 1 and " + grid.getNrows()
                + "::integer ";
        ResultSet rs = null;
        Statement statement = null;
        double populationCount = 0;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                populationCount = rs.getInt(1);
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
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
        }
        return populationCount;
    }

    protected String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }

    protected FastRunOutputType getFastRunOutputType(String name) throws EmfException {
        FastRunOutputType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = fastRunDAO.getFastRunOutputType(name, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    protected EmfDataset getDataset(int id) {
        EmfDataset dataset = null;
        Session session = sessionFactory.getSession();
        try {
            dataset = new DatasetDAO().getDataset(session, id);
        } finally {
            session.close();
        }
        return dataset;
    }

    protected DatasetType getDatasetType(String name) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    protected FastRunOutput[] getFastRunOutputs() {
        FastRunOutput[] results = new FastRunOutput[] {};
        Session session = sessionFactory.getSession();
        try {
            results = fastRunDAO.getFastRunOutputs(fastRun.getId(), session).toArray(new FastRunOutput[0]);
        } finally {
            session.close();
        }
        return results;
    }

    protected void saveFastRunOutput(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRunOutput(fastRunOutput, session);
            // runQASteps(fastRunOutput);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveFastRun(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRun(fastRun, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save sector scenario: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveFastRunSummaryOutput(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRunOutput(fastRunOutput, session);
        } catch (Exception e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void updateOutputDatasetVersionRecordCount(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();

        try {
            EmfDataset result = fastRunOutput.getOutputDataset();

            if (result != null) {
                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());

                if (version != null) {
//                    version.setCreator(user);
                    updateVersion(result, version, dbServer, session, dao);
                }
            }
        } catch (Exception e) {
            throw new EmfException("Cannot update result datasets (strategy id: " + fastRunOutput.getFastRunId()
                    + "). " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session, DatasetDAO dao)
            throws Exception {
        version = dao.obtainLockOnVersion(user, version.getId(), session);
        version.setNumberRecords((int) dao.getDatasetRecordsNumber(dbServer, session, dataset, version));
        dao.updateVersionNReleaseLock(version, session);
    }

    private void removeFastRunOutputs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.removeFastRunResults(fastRun.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

    // private void removeFastRunOutput(int resultId) throws EmfException {
    // Session session = sessionFactory.getSession();
    // try {
    // controlStrategyDAO.removeFastRunOutput(fastRun.getId(), resultId, session);
    // } catch (RuntimeException e) {
    // throw new EmfException("Could not remove previous control strategy result(s)");
    // } finally {
    // session.close();
    // }
    // }

    public FastRun getFastRun() {
        return fastRun;
    }

    protected void runQASteps(FastRunOutput fastRunOutput) {
        // EmfDataset resultDataset = (EmfDataset)fastRunOutput.getDetailedResultDataset();
        if (recordCount > 0) {
            // runSummaryQASteps(resultDataset, 0);
        }
        // excuteSetAndRunQASteps(inputDataset, fastRun.getDatasetVersion());
    }

    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
        // 11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step
        // templates...
        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
        if (qaStepTemplates != null) {
            String[] qaStepTemplateNames = new String[qaStepTemplates.length];
            for (int i = 0; i < qaStepTemplates.length; i++)
                qaStepTemplateNames[i] = qaStepTemplates[i].getName();
            qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, ""/* fastRun.getExportDirectory() */, null);
        }
    }

    protected void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }

    public long getRecordCount() {
        return recordCount;
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("FastRun");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }

    public String getFilterForSourceQuery() {
        String filterForSourceQuery = "";
        String sqlFilter = "";
        String filter = "";

        // get and build strategy filter...
        if (filter == null || filter.trim().length() == 0)
            sqlFilter = "";
        else
            sqlFilter = " and (" + filter + ") ";

        filterForSourceQuery = sqlFilter;
        return filterForSourceQuery;
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

    // private void cleanMappingDataset(EmfDataset mappingDataset) throws EmfException {
    // ResultSet rs = null;
    // Connection connection = null;
    // Statement statement = null;
    // String mappingDatasetQualifiedEmissionTableName = qualifiedEmissionTableName(mappingDataset);
    //        
    // // boolean hasRpenColumn = hasColName("rpen",mappingDataset.getDatasetType().getFileFormat());
    // // boolean hasMactColumn = hasColName("mact",(FileFormatWithOptionalCols) formatUnit.fileFormat());
    // // boolean hasSicColumn = hasColName("sic",(FileFormatWithOptionalCols) formatUnit.fileFormat());
    // // boolean hasCpriColumn = hasColName("cpri",(FileFormatWithOptionalCols) formatUnit.fileFormat());
    // // boolean hasPrimaryDeviceTypeCodeColumn = hasColName("primary_device_type_code",(FileFormatWithOptionalCols)
    // formatUnit.fileFormat());
    // boolean hasSectorColumn = hasColName("sector", mappingDataset.getDatasetType().getFileFormat());
    // try {
    // // first lets clean up "" values and convert them to null values...
    //       
    //
    // //check to see if -9 even shows for any of the columns in the inventory
    // String sql = "select 1 "
    // + " from " + mappingDatasetQualifiedEmissionTableName
    // + " where dataset_id = " + mappingDataset.getId()
    // + " and ("
    // + " trim(eecs) = '' or strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0"
    // + " or trim(mact) = '' or strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0"
    // + " or trim(naics) = '' or strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0"
    // + " or trim(scc) = '' or strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0"
    // + (hasSectorColumn ?
    // " or trim(sector) = '' or strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0" : "")
    // + ") limit 1;";
    //            
    // connection = datasource.getConnection();
    // statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    // rs = statement.executeQuery(sql);
    // boolean foundNegative9 = false;
    // while (rs.next()) {
    // foundNegative9 = true;
    // }
    //
    // if (foundNegative9) {
    // sql = "update " + mappingDatasetQualifiedEmissionTableName
    // +
    // " set eecs = case when trim(eecs) = '' then null::character varying(10) when strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0 then trim(eecs) else eecs end "
    // +
    // "     ,mact = case when trim(mact) = '' then null::character varying(4) when strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0 then trim(mact) else mact end "
    // +
    // "     ,scc = case when trim(scc) = '' then null::character varying(10) when strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0 then trim(scc) else scc end "
    // +
    // "     ,naics = case when trim(naics) = '' then null::character varying(6) when strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0 then trim(naics) else naics end "
    // + (hasSectorColumn ?
    // "     ,sector = case when trim(sector) = '' then null::character varying(64) when strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0 then trim(sector) else sector end "
    // : "")
    // + " where dataset_id = " + mappingDataset.getId()
    // + " and ("
    // + " trim(eecs) = '' or strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0"
    // + " or trim(mact) = '' or strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0"
    // + " or trim(naics) = '' or strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0"
    // + " or trim(scc) = '' or strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0"
    // + (hasSectorColumn ?
    // " or trim(sector) = '' or strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0" : "")
    // + ");";
    //                
    //                
    // // sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then
    // null::character varying(4) else sic end
    //                
    // statement.execute(sql);
    // statement.execute("vacuum " + mappingDatasetQualifiedEmissionTableName);
    // statement.close();
    // }
    // } catch (Exception exc) {
    // // NOTE: this closes the db server for other importers
    // // try
    // // {
    // // if ((connection != null) && !connection.isClosed()) connection.close();
    // // }
    // // catch (Exception ex)
    // // {
    // // throw ex;
    // // }
    // // throw exc;
    // throw new EmfException(exc.getMessage());
    // } finally {
    // if (rs != null) {
    // try {
    // rs.close();
    // } catch (SQLException e) { /**/
    // }
    // rs = null;
    // }
    // if (statement != null) {
    // try {
    // statement.close();
    // } catch (SQLException e) { /**/
    // }
    // statement = null;
    // }
    // }
    //
    // }

    protected boolean hasColName(String colName, XFileFormat fileFormat) {
        Column[] cols = fileFormat.cols();
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name()))
                hasIt = true;

        return hasIt;
    }

    private EmfDataset populateGriddedEmissionOutput(EmfDataset griddedSectorSCCPollDataset, String[] sectors)
            throws EmfException {
        String sqlTemp = "";
        String sql2 = "";

        for (FastRunInventory fastRunInventory : fastRun.getInventories()) {
            EmfDataset dataset = fastRunInventory.getDataset();
            if (dataset.getDatasetTypeName().equals(DatasetType.orlPointInventory)) {
                sqlTemp = buildSQLSelectForORLPointDataset(dataset, fastRunInventory.getVersion());
                if (sqlTemp.length() > 0)
                    sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
            }
        }

        DbServer dbServer = dbServerFactory.getDbServer();
        Connection con = dbServer.getConnection();
        Statement statement = null;
        try {

            EmfDataset speciesMapping = fastRun.getSpeciesMapppingDataset();

            VersionedQuery speciesMappingVersionedQuery = new VersionedQuery(version(speciesMapping.getId(), 0), "fsm");
            String speciesMappingTableName = qualifiedEmissionTableName(speciesMapping);
            String griddedSectorSCCPollTableName = qualifiedEmissionTableName(griddedSectorSCCPollDataset);

            sql2 = "INSERT INTO "
                    + griddedSectorSCCPollTableName
                    + " (dataset_id, delete_versions, version, sector, cmaq_pollutant, inventory_pollutant, x, y, emission, factor, transfer_coefficient) \nselect "
                    + griddedSectorSCCPollDataset.getId()
                    + "::integer as dataset_id, '' as delete_versions, 0 as version, fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, sum(emis), coalesce(fsm.factor, 1.0) as factor, fsm.transfer_coeff as emis \n"
                    + "from ( \n" + sql2;
            sql2 += ") summary \n";
            sql2 += " inner join "
                    + speciesMappingTableName
                    + " fsm \n on fsm.sector = summary.sector \n and fsm.inventory_pollutant = summary.poll \n and summary.scc like coalesce(case when coalesce(fsm.scc, '') = '' then null else fsm.scc end, summary.scc) \n";
            sql2 += " where " + speciesMappingVersionedQuery.query();
            sql2 += " group by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff order by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y; ";

            /*
             * sql2 =
             * "create table test.fast_emis_by_cmaq_inventory_poll as \nselect fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff, sum(emis) as emis \n"
             * + "from ( \n" + sql2; sql2 += ") summary \n"; sql2 +=
             * " inner join emissions.DS_fast_species_mapping_1604915993 fsm \n on fsm.sector = summary.sector \n and fsm.inventory_pollutant = summary.poll \n and summary.scc like coalesce(fsm.scc, summary.scc) \n"
             * ; sql2 +=
             * " group by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y, fsm.factor, fsm.transfer_coeff order by fsm.sector, fsm.cmaq_pollutant, fsm.inventory_pollutant, x, y; "
             * ;
             */

            long timing = System.currentTimeMillis();

            statement = con.createStatement();
            timing = System.currentTimeMillis();
            statement.execute(sql2);
            System.out.println("time to populate fast_emis_by_cmaq_inventory_poll dataset = "
                    + (System.currentTimeMillis() - timing));
            creator.updateVersionZeroRecordCount(griddedSectorSCCPollDataset);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query \n" + e.getMessage(), e);
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
        return griddedSectorSCCPollDataset;
    }

    private void populateDomainOutput(EmfDataset domainOutputDataset, EmfDataset griddedOutputDataset) throws EmfException {
        String sql2 = "";
        
        DbServer dbServer = dbServerFactory.getDbServer();
        Connection con = dbServer.getConnection();
        Statement statement = null;
        try {
        
            String colNameList = "dataset_id, delete_versions, version";
            for (Column column : domainOutputDataset.getDatasetType().getFileFormat().getColumns()) {
                colNameList += ", " + column.getName();
            }
        
            VersionedQuery griddedOutputVersionedQuery = new VersionedQuery(version(griddedOutputDataset.getId(), 0));
            String griddedOutputTableName = qualifiedEmissionTableName(griddedOutputDataset);
        
            String domainOutputTableName = qualifiedEmissionTableName(domainOutputDataset);
        
            sql2 = "INSERT INTO "
            + domainOutputTableName
            + " (" + colNameList + ") \nselect "
            + domainOutputDataset.getId()
            + "::integer as dataset_id, '' as delete_versions, 0 as version,"
            + "sector,cmaq_pollutant,sum(emission) as emission, sum(population_weighted_cancer_risk) as population_weighted_cancer_risk,sum(population_weighted_air_quality) as population_weighted_air_quality "
            + " from " + griddedOutputTableName + " "
            + " where " + griddedOutputVersionedQuery.query() 
            + " group by sector, cmaq_pollutant "
            + " order by sector, cmaq_pollutant;";
            
            statement = con.createStatement();
            statement.execute(sql2);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query \n" + e.getMessage(), e);
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

    private String buildSQLSelectForORLPointDataset(EmfDataset orlPointDataset, int versionNumber) throws EmfException {
        // Sector sector = griddedSCCDataset.getSectors()[0];
        // String tableName = griddedSCCDataset.getInternalSources()[0].getTable();
        String sql = "";
        Sector sector = null;
        if (orlPointDataset.getSectors() != null && orlPointDataset.getSectors().length > 0)
            sector = orlPointDataset.getSectors()[0];
        if (sector == null)
            throw new EmfException("Dataset " + orlPointDataset.getName() + " is missing the sector.");
        String tableName = qualifiedEmissionTableName(orlPointDataset);
        if (sector == null)
            throw new EmfException("Dataset " + orlPointDataset.getName() + " is missing the sector.");
        VersionedQuery versionedQuery = new VersionedQuery(version(orlPointDataset.getId(), versionNumber), "inv");
        sql = "select '"
                + sector.getName().replace("'", "''")
                + "'::varchar(64) as sector, inv.scc, invtable.name as poll, sum(invtable.factor * inv.ann_emis) as emis, ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getXcent()
                + ") / "
                + domain.getXcell()
                + ") as x, ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getYcent() + ") / " + domain.getYcell() + ") as y from " + tableName + " inv inner join "
                + invTableTableName
                + " invtable on invtable.cas = inv.poll where coalesce(inv.ann_emis,0.0) <> 0.0 and "
                + versionedQuery.query() + " and " + invTableVersionedQuery.query();
        sql += " and ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getXcent() + ") / " + domain.getXcell() + ") between 1 and " + domain.getNcols() + " ";
        sql += " and ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getYcent() + ") / " + domain.getYcell() + ") between 1 and " + domain.getNrows() + " ";
        sql += " group by ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getXcent()
                + ") / "
                + domain.getXcell()
                + "), ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - "
                + domain.getYcent() + ") / " + domain.getYcell() + "), inv.scc, invtable.name \n";
//System.out.println(sql);
        return sql;
    }

    private void calculateAQ(EmfDataset intermediateInventory) throws Exception {
        // get transfer coefficients and put into a HashMap for later use.
        Map<String, AQTransferCoefficient> transferCoefficientMap = getTransferCoefficients();

        // Create list of FastCMAQResult objects for calculating air quality
        List<FastGriddedCMAQPollutantAirQualityEmissionResult> results = new ArrayList<FastGriddedCMAQPollutantAirQualityEmissionResult>();
        long timing = System.currentTimeMillis();
        System.out.println("load up conc values for grid " + timing);
        String intermediateInventoryTableName = qualifiedEmissionTableName(intermediateInventory);
        VersionedQuery intermediateInventoryVersionedQuery = new VersionedQuery(version(
                intermediateInventory.getId(), 0), "fo");
        String query = "select fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant, fo.x, fo.y, fo.factor, fo.emission, fo.transfer_coefficient from "
                + intermediateInventoryTableName
                + " as fo where "
                + intermediateInventoryVersionedQuery.query()
                + " order by fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant;";
        DbServer dbServer = dbServerFactory.getDbServer();
        Connection con = dbServer.getConnection();
        ResultSet rs = null;
        Statement statement = null;
        int noCols = this.domain.getNcols();
        int noRows = this.domain.getNrows();
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            int y, x;
            double emissionValue;
            float factor;
            String sector = "";
            String cmaqPollutant = "";
            String inventoryPollutant = "";
            String prevSector = "";
            String prevCmaqPollutant = "";
            String prevInventoryPollutant = "";
            String transferCoeff = "";
            FastGriddedCMAQPollutantAirQualityEmissionResult fastCMAQResult = null;
            // FastCMAQInventoryPollutantResult fastCMAQInventoryPollutantResult;

            double[][] emission = new double[noCols][noRows];
            FastGriddedInventoryPollutantAirQualityEmissionResult result = null;
            while (rs.next()) {
                sector = rs.getString(1);
                cmaqPollutant = rs.getString(2);
                inventoryPollutant = rs.getString(3);
                x = rs.getInt(4);
                y = rs.getInt(5);
                factor = rs.getFloat(6);
                emissionValue = rs.getDouble(7);
                transferCoeff = rs.getString(8);
                // if (fastCMAQResultMap.containsKey(sector + "_" + cmaqPollutant)) {
                // fastCMAQResult = fastCMAQResultMap.get(sector + "_" + cmaqPollutant);
                // } else {
                // fastCMAQResult = new FastCMAQResult(sector, cmaqPollutant);
                // // fastCMAQInventoryPollutantResult = new FastCMAQInventoryPollutantResult();
                // }

                if (!sector.equals(prevSector) || !cmaqPollutant.equals(prevCmaqPollutant)) {
                    if (result != null) {
                        result.setEmission(emission);
                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
                        // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
                        results.add(fastCMAQResult);
                    }
                    emission = new double[noCols][noRows];
                    fastCMAQResult = new FastGriddedCMAQPollutantAirQualityEmissionResult(sector, cmaqPollutant);
                    result = new FastGriddedInventoryPollutantAirQualityEmissionResult(inventoryPollutant, factor,
                            transferCoeff, this.domain.getNcols(), noRows);
                } else if (!inventoryPollutant.equals(prevInventoryPollutant)) {
                    if (result != null) {
                        result.setEmission(emission);
                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
                    }
                    emission = new double[noCols][noRows];
                    result = new FastGriddedInventoryPollutantAirQualityEmissionResult(inventoryPollutant, factor,
                            transferCoeff, this.domain.getNcols(), noRows);
                }

                prevSector = sector;
                prevCmaqPollutant = cmaqPollutant;
                prevInventoryPollutant = inventoryPollutant;
                emission[x - 1][y - 1] = emissionValue;

                // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
            }
            // get last item in there too.
            result.setEmission(emission);
            fastCMAQResult.addCmaqInventoryPollutantResults(result);
            // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
            results.add(fastCMAQResult);
            rs.close();
            rs = null;
            statement.close();
            statement = null;
            con.close();
            con = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { //
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { //
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { //
                }
                con = null;
            }
        }
        System.out.println("finished loading up conc values for grid = " + (System.currentTimeMillis() - timing));
        timing = System.currentTimeMillis();

        // store grid info...
        float xcent = domain.getXcent(), ycent = domain.getYcent(), delx = domain.getXcell(), dely = domain.getYcell();

        for (FastGriddedCMAQPollutantAirQualityEmissionResult cMAQResult : results) {
            String sector = cMAQResult.getSector();
            System.out.println("result sector = " + sector + ", pollutant = " + cMAQResult.getCmaqPollutant());
            for (FastGriddedInventoryPollutantAirQualityEmissionResult result : cMAQResult
                    .getCmaqInventoryPollutantResults()) {
                double[][] emission = result.getEmission();
                double[][] airQuality = null;//new double[noCols][noRows];

                if (sector.equals("ptnonipm") || sector.equals("ptipm") || sector.equals("point")
                        || sector.equals("othpt"))
                    sector = "point";
                else
                    sector = "all nonpoint";

                System.out.println("start to calc affect for each cell on every other cell "
                        + System.currentTimeMillis());

                AQTransferCoefficient transferCoefficient = transferCoefficientMap.get(sector.toLowerCase() + "_"
                        + result.getTranferCoefficient().toLowerCase());
                if (transferCoefficient == null) 
                    break;
                //this can now be defined since there is a transfer coefficient to use...
                airQuality = new double[noCols][noRows];
                double beta1 = transferCoefficient.getBeta1();
                double beta2 = transferCoefficient.getBeta2();

                for (int x = 1; x <= noCols; x++) {
                    for (int y = 1; y <= noRows; y++) {
                        for (int xx = 1; xx <= noCols; xx++) {
                            for (int yy = 1; yy <= noRows; yy++) {

                                // DISTANCE must be in kilometers not meters

                                airQuality[x - 1][y - 1] = airQuality[x - 1][y - 1]
                                        + beta1
                                        * emission[xx - 1][yy - 1]
                                        / (1 + Math.exp(Math.pow(Math.pow(Math.pow(Math
                                                .abs((yy * dely + ycent + 0.5 * dely) / 1000
                                                        - (y * dely + ycent + 0.5 * dely) / 1000), 2.0)
                                                + Math.pow(Math.abs((xx * delx + xcent + 0.5 * delx) / 1000
                                                        - (x * delx + xcent + 0.5 * delx) / 1000), 2.0), 0.5), beta2)));
                            }
                        }
                    }
                }
                result.setAirQuality(airQuality);
            }
            System.out.println("finished calc affect for each cell on every other cell " + System.currentTimeMillis());
        }
        System.out.println("time to calculate aq concentrations = " + (System.currentTimeMillis() - timing));

        createIntermediateAirQualityOutput(results.toArray(new FastGriddedCMAQPollutantAirQualityEmissionResult[0]));

        FastRunOutput griddedOutput = createGriddedOutput(results.toArray(new FastGriddedCMAQPollutantAirQualityEmissionResult[0]));

        createDomainOutput(griddedOutput.getOutputDataset());

    }

    private Map<String, AQTransferCoefficient> getTransferCoefficients() throws EmfException {
        Map<String, AQTransferCoefficient> transferCoefficientMap = new HashMap<String, AQTransferCoefficient>();// map
        ResultSet rs = null;
        Statement statement = null;
        DbServer dbServer = dbServerFactory.getDbServer();
        Connection con = dbServer.getConnection();

        EmfDataset transferCoefficients = fastRun.getTransferCoefficientsDataset();

        VersionedQuery transferCoefficientsVersionedQuery = new VersionedQuery(version(transferCoefficients.getId(),
                fastRun.getTransferCoefficientsDatasetVersion()));
        String transferCoefficientsTableName = qualifiedEmissionTableName(transferCoefficients);

        System.out.println("load up transfer coefficients into a HashMap " + System.currentTimeMillis());
        String query = "select sector, type, b1, b2 from " + transferCoefficientsTableName + " where "
                + transferCoefficientsVersionedQuery.query() + ";";
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                String sector = rs.getString(1).toLowerCase();
                String type = rs.getString(2).toLowerCase();
                transferCoefficientMap.put(sector + "_" + type, new AQTransferCoefficient(sector, type, rs
                        .getDouble(3), rs.getDouble(4)));
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
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
        System.out.println("finished loading transfer coefficients into a HashMap " + System.currentTimeMillis());
        return transferCoefficientMap;
    }

    private EmfDataset createDomainOutputDataset() throws Exception {
        DatasetType datasetType = getDatasetType(DatasetType.FAST_RUN_DOMAIN_OUTPUT);
        return creator.addDataset("ds", fastRun.getAbbreviation() + "_"
                + DatasetType.FAST_RUN_DOMAIN_OUTPUT, datasetType, new VersionedTableFormat(datasetType
                .getFileFormat(), dbServer.getSqlDataTypes()), "");
    }
    
    private EmfDataset createGriddedOutputDataset(FastGriddedCMAQPollutantAirQualityEmissionResult[] results)
            throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        DatasetType datasetType = getDatasetType(DatasetType.FAST_RUN_GRIDDED_OUTPUT);
        EmfDataset dataset = creator.addDataset("ds", fastRun.getAbbreviation() + "_"
                + DatasetType.FAST_RUN_GRIDDED_OUTPUT,
                datasetType, new VersionedTableFormat(
                        datasetType.getFileFormat(), dbServer.getSqlDataTypes()), "");
        // EmfDataset dataset = addDataset(datasetName,
        // getDatasetType(DatasetType.fastGriddedSummaryEmissionAirQuality),
        // new VersionedTableFormat(new GriddedSummaryEmissionAirQualityResultFileFormat(dbServer.getSqlDataTypes()),
        // dbServer.getSqlDataTypes()), "");
        String colNameList = "dataset_id, delete_versions, version";
        for (Column column : datasetType.getFileFormat().getColumns()) {
            colNameList += ", " + column.getName();
        }
        VersionedQuery cancerRiskVersionedQuery = new VersionedQuery(version(fastRun.getCancerRiskDataset().getId(),
                fastRun.getCancerRiskDatasetVersion()), "ure");
        String cancerRiskTableName = qualifiedEmissionTableName(fastRun.getCancerRiskDataset());
        VersionedQuery domainPopulationVersionedQuery = new VersionedQuery(version(fastRun.getDomainPopulationDataset()
                .getId(), fastRun.getDomainPopulationDatasetVersion()), "grid");
        String domainPopulationTableName = qualifiedEmissionTableName(fastRun.getDomainPopulationDataset());

        double populationCount = getDomainPopulationCount(fastRun.getDomainPopulationDataset(), fastRun.getGrid());

        int datasetId = dataset.getId();
        String tableName = qualifiedEmissionTableName(dataset);
        Connection con = dbServer.getConnection();
        con.setAutoCommit(false);
        Statement statement = null;
        try {
            statement = con.createStatement();
            // long timing = System.currentTimeMillis();

            int counter = 0;
            for (FastGriddedCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
                String sector = cmaqResult.getSector();
                String pollutant = cmaqResult.getCmaqPollutant();
                // System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
                double[][] emission = cmaqResult.getEmission();
                double[][] airQuality = cmaqResult.getAirQuality();
                
                for (int x = 1; x <= this.domain.getNcols(); x++) {
                    for (int y = 1; y <= this.domain.getNrows(); y++) {
                        ++counter;
                        statement
                                .addBatch("INSERT INTO "
                                        + tableName
                                        + " (" + colNameList + ") \nselect "
                                        + datasetId
                                        + "::integer as dataset_id, '' as delete_versions, 0 as version,'"
                                        + sector
                                        + "','"
                                        + pollutant
                                        + "',"
                                        + x
                                        + ","
                                        + y
                                        + ","
                                        + emission[x - 1][y - 1]
                                        + ","
                                        + (airQuality != null ? airQuality[x - 1][y - 1] : "null") + "::double precision"
                                        + ", "
                                        + (airQuality != null ? airQuality[x - 1][y - 1] : "null") + "::double precision"
                                        + " * totalpop / "
                                        + populationCount
                                        + "::double precision as pop_weighted_aq, "
                                        + (airQuality != null ? airQuality[x - 1][y - 1] : "null") + "::double precision"
                                        + " * cancer_risk_ure as cancer_risk_per_person, "
                                        + (airQuality != null ? airQuality[x - 1][y - 1] : "null") + "::double precision"
                                        + " * cancer_risk_ure * totalpop as total_cancer_risk, "
                                        + (airQuality != null ? airQuality[x - 1][y - 1] : "null") + "::double precision"
                                        + " * totalpop / "
                                        + populationCount
                                        + "::double precision * cancer_risk_ure as pop_weighted_cancer_risk, totalpop as grid_cell_population, totalpop / "
                                        + populationCount
                                        + "::double precision * 100.0 as pct_population_in_grid_cell_to_model_domain, cancer_risk_ure as ure from (select 1) as foo left outer join "
                                        + domainPopulationTableName + " grid on grid.row = " + y + " and grid.col = "
                                        + x + " and " + domainPopulationVersionedQuery.query() + " left outer join "
                                        + cancerRiskTableName + " ure on ure.cmaq_pollutant = '" + pollutant + "' and "
                                        + cancerRiskVersionedQuery.query() + ";");
                    }
                }
                if (counter > 20000) {
                    counter = 0;
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
            con.commit();

            // now lets update the other fields...
            // POPULATION_WEIGHTED_AIR_QUALITY, CANCER_RISK_PER_PERSON,
            // TOTAL_CANCER_RISK, POPULATION_WEIGHTED_CANCER_RISK,
            // GRID_CELL_POPULATION, PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN,
            // URE

            // VersionedQuery versionedQuery = new VersionedQuery(version(dataset, 0), "aq");
            // statement.execute("update " + tableName + " "
            // + "set POPULATION_WEIGHTED_AIR_QUALITY = air_quality * totalpop / 6349855.90000001, "
            // + "CANCER_RISK_PER_PERSON = air_quality * cancer_risk_ure, "
            // + "TOTAL_CANCER_RISK = air_quality * cancer_risk_ure * totalpop, "
            // + "POPULATION_WEIGHTED_CANCER_RISK = air_quality * totalpop / 6349855.90000001 * cancer_risk_ure, "
            // + "GRID_CELL_POPULATION = totalpop, "
            // + "PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN = totalpop / 6349855.90000001 * 100.0, "
            // + "URE = cancer_risk_ure "
            // + "from " + tableName + " aq "
            // + "left outer join emissions.DS_4km_Detroit_Pop_776499559 grid "
            // + "on grid.row = aq.y "
            // + "and grid.col = aq.x "
            // + "left outer join emissions.DS_fast_cancer_risk_ure_1493480478 ure "
            // + "on ure.cmaq_pollutant = aq.cmaq_pollutant "
            // + "where " + versionedQuery.query());

            // TOTAL_CANCER_RISK, POPULATION_WEIGHTED_CANCER_RISK,
            // GRID_CELL_POPULATION, PCT_POPULATION_IN_GRID_CELL_TO_MODEL_DOMAIN,
            // URE);
        } catch (Exception ex) {
            ex.printStackTrace();
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
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dataset;
    }

    private EmfDataset createIntermediateAirQualityDataset(FastGriddedCMAQPollutantAirQualityEmissionResult[] results)
            throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        DatasetType datasetType = getDatasetType(DatasetType.FAST_RUN_INTERMEDIATE_AIR_QUALITY);
        EmfDataset dataset = creator.addDataset("ds", fastRun.getAbbreviation() + "_"
                + DatasetType.FAST_RUN_INTERMEDIATE_AIR_QUALITY,
                datasetType, new VersionedTableFormat(
                        datasetType.getFileFormat(), dbServer
                                .getSqlDataTypes()), "");
        String colNameList = "dataset_id, delete_versions, version";
        for (Column column : datasetType.getFileFormat().getColumns()) {
            colNameList += ", " + column.getName();
        }
        int datasetId = dataset.getId();
        String tableName = qualifiedEmissionTableName(dataset);
        Connection con = dbServer.getConnection();
        con.setAutoCommit(false);
        Statement statement = null;
        try {
            statement = con.createStatement();
            // long timing = System.currentTimeMillis();

            int counter = 0;

            for (FastGriddedCMAQPollutantAirQualityEmissionResult cmaqResult : results) {
                String sector = cmaqResult.getSector();
                String pollutant = cmaqResult.getCmaqPollutant();
                // System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
                for (FastGriddedInventoryPollutantAirQualityEmissionResult result : cmaqResult
                        .getCmaqInventoryPollutantResults()) {
                    double[][] emission = result.getEmission();
                    double[][] airQuality = result.getAirQuality();
                    String inventoryPollutant = result.getPollutant();
                    for (int x = 1; x <= this.domain.getNcols(); x++) {
                        for (int y = 1; y <= this.domain.getNrows(); y++) {
                            ++counter;
                            statement
                                    .addBatch("INSERT INTO "
                                            + tableName
                                            + " (" + colNameList + ") \nselect "
                                            + datasetId
                                            + "::integer as dataset_id, '' as delete_versions, 0 as version,'" + sector
                                            + "','" + pollutant + "','" + inventoryPollutant + "'," + x + "," + y + ","
                                            + result.getAdjustmentFactor() + ",'" + result.getTranferCoefficient()
                                            + "'," + emission[x - 1][y - 1] + "," + (airQuality != null ? airQuality[x - 1][y - 1] : "null::double precision") + ";");
                        }
                    }
                    if (counter > 20000) {
                        counter = 0;
                        statement.executeBatch();
                    }
                }
            }
            statement.executeBatch();
            con.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
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
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dataset;
    }

}
