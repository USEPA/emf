package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class FastAnalysisTask {

    protected FastAnalysis fastAnalysis;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    private User user;

    private int recordCount;

    private StatusDAO statusDAO;

    private FastDAO fastAnalysisDAO;

    private DatasetCreator creator;

    private Keywords keywords;

    // private TableFormat tableFormat;

    protected List<FastAnalysisOutput> fastRunOutputList;

    public FastAnalysisTask(FastAnalysis fastRun, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory) throws EmfException {
        this.fastAnalysis = fastRun;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.fastAnalysisDAO = new FastDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(null, user, sessionFactory, dbServerFactory, datasource, keywords);
        this.fastRunOutputList = new ArrayList<FastAnalysisOutput>();
        // setup the strategy run
        setup();
    }

    private void setup() {
        //
    }

    protected FastAnalysisOutput createFastAnalysisOutput(FastAnalysisOutputType fastRunOutputType,
            EmfDataset outputDataset) throws EmfException {
        FastAnalysisOutput result = new FastAnalysisOutput();
        result.setFastAnalysisId(fastAnalysis.getId());
        result.setOutputDataset(outputDataset);
        // result.setInventoryDataset(inventory);
        // result.setInventoryDatasetVersion(inventoryVersion);

        result.setType(fastRunOutputType);
        result.setStartDate(new Date());
        result.setRunStatus("Start processing inventory dataset");

        // persist output
        saveFastAnalysisOutput(result);
        return result;
    }

    public FastAnalysisOutput createAnalysisGriddedDifferenceResultOutput() throws Exception {

        // setup result
        FastAnalysisOutput analysisGriddedDifferenceResultOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT + ".");

            EmfDataset analysisGriddedDifferenceResult = createAnalysisGriddedDifferenceResultDataset();

            analysisGriddedDifferenceResultOutput = createFastAnalysisOutput(
                    getFastAnalysisOutputType(FastAnalysisOutputType.GRIDDED_DIFFERENCE),
                    analysisGriddedDifferenceResult);

            populateAnalysisGriddedDifferenceResult(analysisGriddedDifferenceResultOutput);

            updateOutputDatasetVersionRecordCount(analysisGriddedDifferenceResultOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT + ".");

        } catch (EmfException ex) {
            runStatus = "Failed creating " + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } catch (Exception ex) {
            runStatus = "Failed creating " + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw new EmfException(runStatus, ex);
        } finally {
            if (analysisGriddedDifferenceResultOutput != null) {
                analysisGriddedDifferenceResultOutput.setCompletionDate(new Date());
                analysisGriddedDifferenceResultOutput.setRunStatus(runStatus);
                saveFastAnalysisOutput(analysisGriddedDifferenceResultOutput);
            }
        }

        return analysisGriddedDifferenceResultOutput;
    }

    public FastAnalysisOutput createAnalysisDomainDifferenceResultOutput(EmfDataset analysisGriddedDifferenceResult,
            int analysisGriddedDifferenceResultVersion) throws Exception {

        // setup result
        FastAnalysisOutput analysisDomainDifferenceResultOutput = null;
        String runStatus = "";

        // Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT + ".");

            EmfDataset analysisDomainDifferenceResult = createAnalysisDomainDifferenceResultDataset();

            analysisDomainDifferenceResultOutput = createFastAnalysisOutput(
                    getFastAnalysisOutputType(FastAnalysisOutputType.DOMAIN_DIFFERENCE), analysisDomainDifferenceResult);

            populateAnalysisDomainDifferenceResult(analysisGriddedDifferenceResult,
                    analysisGriddedDifferenceResultVersion, analysisDomainDifferenceResultOutput);

            updateOutputDatasetVersionRecordCount(analysisDomainDifferenceResultOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT + ".");

        } catch (EmfException ex) {
            runStatus = "Failed creating " + DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT + ". Exception = "
                    + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (analysisDomainDifferenceResultOutput != null) {
                analysisDomainDifferenceResultOutput.setCompletionDate(new Date());
                analysisDomainDifferenceResultOutput.setRunStatus(runStatus);
                saveFastAnalysisOutput(analysisDomainDifferenceResultOutput);
            }
        }

        return analysisDomainDifferenceResultOutput;
    }

    private FastAnalysisOutputType getFastAnalysisOutputType(String name) throws EmfException {
        FastAnalysisOutputType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = fastAnalysisDAO.getFastAnalysisOutputType(name, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get FAST analysis output type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private EmfDataset createAnalysisGriddedDifferenceResultDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT);
        return creator.addDataset("ds", fastAnalysis.getName() + "_"
                + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT, datasetType, new VersionedTableFormat(
                datasetType.getFileFormat(), dbServer.getSqlDataTypes()), "");

        // new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes())
    }

    private EmfDataset createAnalysisDomainDifferenceResultDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT);
        return creator.addDataset("ds", fastAnalysis.getName() + "_"
                + DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT, datasetType, new VersionedTableFormat(datasetType
                .getFileFormat(), dbServer.getSqlDataTypes()), "");
    }

    private EmfDataset getFastRunGriddedOutputDataset(FastRun fastRun) {
        EmfDataset dataset = null;
        for (FastRunOutput output : getFastRunOutputs(fastRun.getId())) {
            if (output.getOutputDataset().getDatasetType().getName().equals(
                    DatasetType.FAST_RUN_GRIDDED_OUTPUT)) {
                dataset = output.getOutputDataset();
            }
        }
        return dataset;
    }

    protected FastRunOutput[] getFastRunOutputs(int fastRunId) {
        FastRunOutput[] results = new FastRunOutput[] {};
        Session session = sessionFactory.getSession();
        try {
            results = fastAnalysisDAO.getFastRunOutputs(fastRunId, session).toArray(new FastRunOutput[0]);
        } finally {
            session.close();
        }
        return results;
    }

    private void populateAnalysisGriddedDifferenceResult(FastAnalysisOutput fastRunOutput) throws EmfException {

        String sql = "INSERT INTO "
                + qualifiedEmissionTableName(fastRunOutput.getOutputDataset())
                + " (dataset_id, version, sector, pollutant, x, y, sum_sens_total_cancer_risk, sum_base_total_cancer_risk, diff_sum_total_cancer_risk, sum_sens_pop_weighted_cancer_risk, sum_base_pop_weighted_cancer_risk, diff_sum_pop_weighted_cancer_risk, sum_sens_pop_weighted_air_quality, sum_base_pop_weighted_air_quality, diff_sum_pop_weighted_air_quality, sum_sens_air_quality, sum_base_air_quality, diff_sum_air_quality, sum_sens_emission, sum_base_emission, diff_sum_emission) "
                + "select "
                + fastRunOutput.getOutputDataset().getId()
                + " as dataset_id, 0 as version, "
                + " coalesce(post.sector, pre.sector) as sector,  "
                + " coalesce(post.pollutant, pre.pollutant) as pollutant, "
                + " coalesce(post.x, pre.x) as x, "
                + " coalesce(post.y, pre.y) as y, "
                + " sum(post.cancer_risk_per_person) as sum_sens_total_cancer_risk, "
                + " sum(pre.cancer_risk_per_person) as sum_base_total_cancer_risk, "
                + " sum(post.cancer_risk_per_person) - sum(pre.cancer_risk_per_person) as diff_sum_total_cancer_risk, "
                + " sum(post.population_weighted_cancer_risk) as sum_sens_pop_weighted_cancer_risk, "
                + " sum(pre.population_weighted_cancer_risk) as sum_base_pop_weighted_cancer_risk, "
                + " sum(post.population_weighted_cancer_risk) - sum(pre.population_weighted_cancer_risk) as diff_sum_pop_weighted_cancer_risk, "
                + " sum(post.population_weighted_air_quality) as sum_sens_pop_weighted_air_quality, "
                + " sum(pre.population_weighted_air_quality) as sum_base_pop_weighted_air_quality, "
                + " sum(post.population_weighted_air_quality) - sum(pre.population_weighted_air_quality) as diff_sum_pop_weighted_air_quality, "
                + " sum(post.air_quality) as sum_sens_air_quality, "
                + " sum(pre.air_quality) as sum_base_air_quality, "
                + " sum(post.air_quality) - sum(pre.air_quality) as diff_sum_air_quality, "
                + " sum(post.emission) as sum_sens_emission, " + " sum(pre.emission) as sum_base_emission, "
                + " sum(post.emission) - sum(pre.emission) as diff_sum_emission " + " from ( ";
        FastAnalysisRun[] fastAnalysisRuns = fastAnalysis.getBaselineRuns();
        int count = 0;
        for (FastAnalysisRun fastAnalysisRun : fastAnalysisRuns) {
            EmfDataset dataset = getFastRunGriddedOutputDataset(fastAnalysisRun.getFastRun());
            int versionNumber = 0;
            double adjustmentFactor = fastAnalysisRun.getAdjustmentFactor();
            Version inventoryVersion = version(dataset.getId(), versionNumber);
            VersionedQuery datasetVersionedQuery = new VersionedQuery(inventoryVersion, "aq");
            String datasetTableName = qualifiedEmissionTableName(dataset);
            sql += (count > 0 ? " union all " : "") + " select " + "     aq.sector, "
                    + "     aq.cmaq_pollutant as pollutant, " + "     aq.x, " + "     aq.y, " + "     "
                    + adjustmentFactor + "::double precision * cancer_risk_per_person as cancer_risk_per_person, "
                    + "     " + adjustmentFactor + "::double precision * total_cancer_risk as total_cancer_risk, "
                    + "     " + adjustmentFactor
                    + "::double precision * population_weighted_cancer_risk as population_weighted_cancer_risk, "
                    + "     " + adjustmentFactor
                    + "::double precision * population_weighted_air_quality as population_weighted_air_quality, "
                    + "     " + adjustmentFactor + "::double precision * aq.emission as emission, " + "     "
                    + adjustmentFactor + "::double precision * aq.air_quality as air_quality " + " from "
                    + datasetTableName + " aq " + " where " + datasetVersionedQuery.query() + " ";
            ++count;
        }
        sql += " ) as pre " + " " + " full join ( ";
        fastAnalysisRuns = fastAnalysis.getSensitivityRuns();
        count = 0;
        for (FastAnalysisRun fastAnalysisRun : fastAnalysisRuns) {
            EmfDataset dataset = getFastRunGriddedOutputDataset(fastAnalysisRun.getFastRun());
            int versionNumber = 0;
            double adjustmentFactor = fastAnalysisRun.getAdjustmentFactor();
            Version inventoryVersion = version(dataset.getId(), versionNumber);
            VersionedQuery datasetVersionedQuery = new VersionedQuery(inventoryVersion, "aq");
            String datasetTableName = qualifiedEmissionTableName(dataset);
            sql += (count > 0 ? " union all " : "") + " select " + "     aq.sector, "
                    + "     aq.cmaq_pollutant as pollutant, " + "     aq.x, " + "     aq.y, " + "     "
                    + adjustmentFactor + "::double precision * cancer_risk_per_person as cancer_risk_per_person, "
                    + "     " + adjustmentFactor + "::double precision * total_cancer_risk as total_cancer_risk, "
                    + "     " + adjustmentFactor
                    + "::double precision * population_weighted_cancer_risk as population_weighted_cancer_risk, "
                    + "     " + adjustmentFactor
                    + "::double precision * population_weighted_air_quality as population_weighted_air_quality, "
                    + "     " + adjustmentFactor + "::double precision * aq.emission as emission, " + "     "
                    + adjustmentFactor + "::double precision * aq.air_quality as air_quality " + " from "
                    + datasetTableName + " aq " + " where " + datasetVersionedQuery.query() + " ";
            ++count;
        }
        sql += " ) as post "
                + " on post.sector = pre.sector "
                + " and post.pollutant = pre.pollutant "
                + " and post.x = pre.x "
                + " and post.y = pre.y "
                + " "
                // + "--where coalesce(post.sector, pre.sector) = 'ptipm'  "
                // + "--and coalesce(post.pollutant, pre.pollutant) = 'PM2.5' "
                // + "  "
                + " group by coalesce(post.sector, pre.sector),  " + " coalesce(post.pollutant, pre.pollutant),  "
                + " coalesce(post.x, pre.x),  " + " coalesce(post.y, pre.y) "
                + " order by coalesce(post.sector, pre.sector),  " + " coalesce(post.pollutant, pre.pollutant),  "
                + " coalesce(post.x, pre.x),  " + " coalesce(post.y, pre.y) " + " ;";

        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to "
                    + DatasetType.FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT + " table" + "\n" + e.getMessage());
        }
    }

    private void populateAnalysisDomainDifferenceResult(EmfDataset analysisGriddedDifferenceResult,
            int analysisGriddedDifferenceResultVersion, FastAnalysisOutput fastRunOutput) throws EmfException {

        Version datasetVersion = version(analysisGriddedDifferenceResult.getId(),
                analysisGriddedDifferenceResultVersion);
        VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "aq");
        String datasetTableName = qualifiedEmissionTableName(analysisGriddedDifferenceResult);

        String sql = "INSERT INTO "
                + qualifiedEmissionTableName(fastRunOutput.getOutputDataset())
                + " (dataset_id, version, sector, pollutant, sum_sens_total_cancer_risk, sum_base_total_cancer_risk, diff_sum_total_cancer_risk, sum_sens_pop_weighted_cancer_risk, sum_base_pop_weighted_cancer_risk, diff_sum_pop_weighted_cancer_risk, sum_sens_pop_weighted_air_quality, sum_base_pop_weighted_air_quality, diff_sum_pop_weighted_air_quality, sum_sens_air_quality, sum_base_air_quality, diff_sum_air_quality, sum_sens_emission, sum_base_emission, diff_sum_emission) "
                + "select "
                + fastRunOutput.getOutputDataset().getId()
                + " as dataset_id, 0 as version, "
                + " sector,  "
                + " pollutant, "
                + " sum(sum_sens_total_cancer_risk) as sum_sens_total_cancer_risk, "
                + " sum(sum_base_total_cancer_risk) as sum_base_total_cancer_risk, "
                + " sum(sum_sens_total_cancer_risk) - sum(sum_base_total_cancer_risk) as diff_sum_total_cancer_risk, "
                + " sum(sum_sens_pop_weighted_cancer_risk) as sum_sens_pop_weighted_cancer_risk, "
                + " sum(sum_base_pop_weighted_cancer_risk) as sum_base_pop_weighted_cancer_risk, "
                + " sum(sum_sens_pop_weighted_cancer_risk) - sum(sum_base_pop_weighted_cancer_risk) as diff_sum_pop_weighted_cancer_risk, "
                + " sum(sum_sens_pop_weighted_air_quality) as sum_sens_pop_weighted_air_quality, "
                + " sum(sum_base_pop_weighted_air_quality) as sum_base_pop_weighted_air_quality, "
                + " sum(sum_sens_pop_weighted_air_quality) - sum(sum_base_pop_weighted_air_quality) as diff_sum_pop_weighted_air_quality, "
                + " sum(sum_sens_air_quality) as sum_sens_air_quality, "
                + " sum(sum_base_air_quality) as sum_base_air_quality, "
                + " sum(sum_sens_air_quality) - sum(sum_base_air_quality) as diff_sum_air_quality, "
                + " sum(sum_sens_emission) as sum_sens_emission, " + " sum(sum_base_emission) as sum_base_emission, "
                + " sum(sum_sens_emission) - sum(sum_base_emission) as diff_sum_emission " + " from "
                + datasetTableName + " aq " + " where " + datasetVersionedQuery.query() + " "
                + " group by sector, pollutant ";

        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to "
                    + DatasetType.FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT + " table" + "\n" + e.getMessage());
        }
    }

    public void run() throws EmfException {

        // get rid of strategy results
        deleteAnalysisOutputs();

        // run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {
            // process/load each input dataset
            // FastRunInventory[] fastRunInventories = fastRun.getInventories();
            //            
            // for (int i = 0; i < fastRunInventories.length; i++) {
            try {
                FastAnalysisOutput analysisGriddedDifferenceResultOutput = createAnalysisGriddedDifferenceResultOutput();

                createAnalysisDomainDifferenceResultOutput(analysisGriddedDifferenceResultOutput.getOutputDataset(), 0);

                recordCount = 0; // loader.getRecordCount();
                status = "Completed.";
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing : " + "" + ". " + e.getMessage();
                // setStatus(status);
            } finally {

                // see if there was an error, if so, make sure and propogate to the calling method.
                if (status.startsWith("Failed"))
                    throw new EmfException(status);

                // make sure somebody hasn't cancelled this run.
                if (isRunStatusCancelled()) {
                    status = "Cancelled. FAST Analysis run was cancelled: " + fastAnalysis.getName();
                    setStatus(status);
                    return;
                    // throw new EmfException("Strategy run was cancelled.");
                }
                //
            }
            // }

            // now create the measure summary result based on the results from the strategy run...
            // generateStrategyMeasureSummaryResult();

            // //now create the county summary result based on the results from the strategy run...
            // generateStrategyCountySummaryResult();

        } catch (Exception e) {
            status = "Failed. Error processing inventory";
            e.printStackTrace();
            throw new EmfException(e.getMessage(), e);
        } finally {
            // run any post processes
            try {
                afterRun();
                // updateVersionInfo();
            } catch (Exception e) {
                status = "Failed. Error processing inventory";
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

    private void beforeRun() {
        // for (FastRunInventory fastRunInventory : fastRun.getInventories()) {
        // //make sure inventory has indexes created...
        // makeSureInventoryDatasetHaveIndexes(fastRunInventory);
        // }
        // // NOTE Auto-generated method stub

    }

    protected void deleteAnalysisOutputs() throws EmfException {

        Session session = sessionFactory.getSession();
        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();

            // first get the datasets to delete
            EmfDataset[] datasets = fastAnalysisDAO.getFastAnalysisOutputDatasets(fastAnalysis.getId(), session);
            if (datasets != null) {
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        setStatus("The Fast analysis output dataset, " + dataset.getName()
                                + ", will not be deleted since you are not the creator.");
                    } else {
                        dsList.add(dataset);
                    }
                }
            }

            // get rid of old strategy results...
            removeFastAnalysisOutputs();

            // delete and purge datasets
            if (dsList != null && dsList.size() > 0) {
                fastAnalysisDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs.");
        } finally {
            session.close();
        }
    }

    private void saveFastAnalysisOutput(FastAnalysisOutput fastAnalysisOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastAnalysisDAO.updateFastAnalysisOutput(fastAnalysisOutput, session);
            // runQASteps(fastRunOutput);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private boolean isRunStatusCancelled() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return fastAnalysisDAO.getFastAnalysisRunStatus(fastAnalysis.getId(), session).equals("Cancelled");
        } catch (RuntimeException e) {
            throw new EmfException("Could not check if strategy run was cancelled.");
        } finally {
            session.close();
        }
    }

    private String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    private String emissionTableName(Dataset dataset) {
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

    private DatasetType getDatasetType(String name) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    private void updateOutputDatasetVersionRecordCount(FastAnalysisOutput fastAnalysisOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();

        try {
            EmfDataset result = fastAnalysisOutput.getOutputDataset();

            if (result != null) {
                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());

                if (version != null) {
//                    version.setCreator(user);
                    updateVersion(result, version, dbServer, session, dao);
                }
            }
        } catch (Exception e) {
            throw new EmfException("Cannot update result datasets (strategy id: "
                    + fastAnalysisOutput.getFastAnalysisId() + "). " + e.getMessage());
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

    private void removeFastAnalysisOutputs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastAnalysisDAO.removeFastAnalysisOutputs(fastAnalysis.getId(), session);
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

    public FastAnalysis getFastAnalysis() {
        return fastAnalysis;
    }

    private void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }

    public long getRecordCount() {
        return recordCount;
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("FASTAnalysis");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
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

}
