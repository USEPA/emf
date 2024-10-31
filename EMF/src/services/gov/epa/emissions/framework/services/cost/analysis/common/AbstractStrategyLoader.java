package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.other.StrategyMessagesFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public abstract class AbstractStrategyLoader implements StrategyLoader {

    protected ControlStrategy controlStrategy;
    
    protected double totalCost = 0.0;

    protected double totalReduction = 0.0;
    
//    private DecimalFormat decFormat;

    protected RecordGenerator recordGenerator;
    
    protected int recordCount = 0;
    
    protected Datasource datasource;
    
    protected DatasetCreator creator;
    
    protected EntityManagerFactory entityManagerFactory;
    
    protected User user;
    
    protected DbServerFactory dbServerFactory;
    
    protected DbServer dbServer;
    
    protected Keywords keywords;

    private DatasetType controlStrategyDetailedResultDatasetType;
    
    private StrategyResultType detailedStrategyResultType;
    
    private String filterForSourceQuery;

    protected int daysInMonth = 31; //useful only if inventory is monthly based and not yearly.

    protected int month = -1; //useful only if inventory is monthly based and not yearly.

    private StatusDAO statusDAO;
    
    protected ControlStrategyResult[] results;

    protected List<ControlStrategyResult> strategyMessageResultList = new ArrayList<ControlStrategyResult>();

    protected ControlStrategyDAO controlStrategyDAO;
    
    protected ControlStrategyResult strategyMessagesResult;

    public AbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            EntityManagerFactory entityManagerFactory, ControlStrategy controlStrategy) throws EmfException {
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.controlStrategy = controlStrategy;
//        this.decFormat = new DecimalFormat("0.###E0");
        this.keywords = new Keywords(new DataCommonsServiceImpl(entityManagerFactory).getKeywords());
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        // VERSIONS TABLE - completed - should throw exception if emission schema and versions table apear together
        this.creator = new DatasetCreator(controlStrategy, user, 
                entityManagerFactory, dbServerFactory,
                datasource, keywords);
        this.statusDAO = new StatusDAO(entityManagerFactory);
        this.controlStrategyDAO = new ControlStrategyDAO(dbServerFactory, entityManagerFactory);
        this.results = getControlStrategyResults();
    }

    //call this to process the input and create the output in a batch fashion
    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        return null;
    }

    //implement code that is specific to the strategy type
    abstract protected void doBatchInsert(ResultSet resultSet) throws Exception;

    public final void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }

    protected ControlStrategyResult createStrategyResult(EmfDataset inputDataset, int inputDatasetVersion) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inputDataset);
        result.setInputDatasetVersion(inputDatasetVersion);
        result.setDetailedResultDataset(createResultDataset(inputDataset));
        
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing dataset");

        //persist result
        addControlStrategyResult(result);
        return result;
    }

    protected void updateControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (DebugLevels.DEBUG_25())
                System.out.println("Strategy Result Type: " + strategyResult.getStrategyResultType());
            controlStrategyDAO.updateWithoutLock(strategyResult, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    protected void addControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (DebugLevels.DEBUG_25())
                System.out.println("Strategy Result Type: " + strategyResult.getStrategyResultType());
            controlStrategyDAO.add(strategyResult, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    protected void deleteDatasets(EmfDataset[] datasets) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            controlStrategyDAO.removeResultDatasets(datasets, user, entityManager, dbServer);
        } catch (RuntimeException e) {
            throw new EmfException("Could not delete control strategy result dataset(s): " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    protected StrategyResultType getDetailedStrategyResultType() throws EmfException {
        if (detailedStrategyResultType == null) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                detailedStrategyResultType = controlStrategyDAO.getDetailedStrategyResultType(entityManager);
            } catch (RuntimeException e) {
                throw new EmfException("Could not get detailed strategy result type");
            } finally {
                entityManager.close();
            }
        }
        return detailedStrategyResultType;
    }

    private EmfDataset createResultDataset(EmfDataset inputDataset) throws EmfException {
        DatasetType dsType = getControlStrategyDetailedResultDatasetType();
        return creator.addDataset("Strategy", "CSDR", 
                inputDataset, dsType,
                new VersionedTableFormat(dsType.getFileFormat(), dbServer.getSqlDataTypes()));
    }

    private EmfDataset createStrategyMessagesDataset() throws Exception {
      return creator.addDataset("DS", 
              DatasetCreator.createDatasetName(controlStrategy.getName() + "_strategy_msgs"), 
              getDatasetType("Strategy Messages (CSV)"), 
              new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
              strategyMessagesDatasetDescription());
    }

    private EmfDataset createStrategyMessagesDataset(EmfDataset inventory) throws Exception {
      return creator.addDataset("DS", 
              DatasetCreator.createDatasetName(inventory.getName() + "_strategy_msgs"), 
              getDatasetType("Strategy Messages (CSV)"), 
              new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
              strategyMessagesDatasetDescription());
    }
  
    protected EmfDataset createStrategyMessagesDataset(String namePrefix, EmfDataset inventory) throws Exception {
        return creator.addDataset("DS", 
                DatasetCreator.createDatasetName(namePrefix + "_" + inventory.getName() + "_strategy_msgs"), 
                getDatasetType("Strategy Messages (CSV)"), 
                new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
                strategyMessagesDatasetDescription());
      }
    
    private String strategyMessagesDatasetDescription() {
        return "#Strategy Messages\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
    }

    protected DatasetType getControlStrategyDetailedResultDatasetType() {
        if (controlStrategyDetailedResultDatasetType == null) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                controlStrategyDetailedResultDatasetType = new DatasetTypesDAO().get(DatasetType.strategyDetailedResultExtended, entityManager);
            } finally {
                entityManager.close();
            }
        }
        return controlStrategyDetailedResultDatasetType;
    }

    public final int getRecordCount() {
        return recordCount;
    }
    
    protected boolean inventoryHasTargetPollutant(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();

        String pollToMatch = controlStrategy.getTargetPollutant().getName();
        if (pollToMatch.equals("PM2_5")) pollToMatch = "PM2";

        String query = "SELECT 1 as Found "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + " and poll IN ("
            + "   SELECT DISTINCT p.name"
            + "   FROM emf.pollutants p"
            + "   JOIN emf.aggregrated_efficiencyrecords r"
            + "   ON r.pollutant_id = p.id"
            + "   WHERE p.name LIKE '%" + pollToMatch + "%'"
            + ")"
            + getFilterForSourceQuery() + " limit 1;";
        //System.out.println(System.currentTimeMillis() + " " + query);
        ResultSet rs = null;
        Statement statement = null;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
//            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                if (rs.getInt(1) > 0)
                    return true;
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /**/ }
                rs = null;
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { /**/ }
                statement = null;
            }
        }
        return false;
    }

    public void makeSureInventoryDatasetHasIndexes(Dataset dataset) {
        createIndexes(dataset);
    }

    private void createIndexes(Dataset dataset) {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = emissionTableName(dataset);
        setStatus("Started creating indexes on inventory, " 
                + dataset.getName() 
                + ".  Depending on the size of the dataset, this could take several minutes.");

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        //for orl inventories
        dataTable.addIndex(table, "fips", false);
        dataTable.addIndex(table, "plantid", false);
        dataTable.addIndex(table, "pointid", false);
        dataTable.addIndex(table, "stackid", false);
        dataTable.addIndex(table, "segment", false);
        dataTable.addIndex(table, "mact", false);
        dataTable.addIndex(table, "sic", false);

        dataTable.addIndex(table, "poll", false);
        dataTable.addIndex(table, "scc", false);
        dataTable.addIndex(table, "naics", false);
        
        //for flat file inventories
        dataTable.addIndex(table, "country_cd", false);
        dataTable.addIndex(table, "region_cd", false);
        dataTable.addIndex(table, "tribal_code", false);
        dataTable.addIndex(table, "facility_id", false);
        dataTable.addIndex(table, "unit_id", false);
        dataTable.addIndex(table, "rel_point_id", false);
        dataTable.addIndex(table, "process_id", false);
        dataTable.addIndex(table, "reg_codes", false);
        
        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    
        setStatus("Completed creating indexes on inventory, " 
                + dataset.getName() 
                + ".");
    }

    private String getFilterFromRegionDataset() throws EmfException {
        if (controlStrategy.getCountyDataset() == null) return "";
        String sqlFilter = "";
        String versionedQuery = new VersionedQuery(version(controlStrategy.getCountyDataset().getId(), controlStrategy.getCountyDatasetVersion())).query();
        String query = "SELECT distinct fips "
            + " FROM " + qualifiedEmissionTableName(controlStrategy.getCountyDataset()) 
            + " where " + versionedQuery;
        return sqlFilter.length() > 0 ? " and fips in (" + query + ")" : "" ;
    }

    protected void setResultTotalCostTotalReductionAndCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String pollToMatch = null;
        if (controlStrategy.getTargetPollutant() != null) {
            pollToMatch = controlStrategy.getTargetPollutant().getName();
            if (pollToMatch.equals("PM2_5")) pollToMatch = "PM2";
        }
        
        String query = "SELECT count(1) as record_count, round(sum(Annual_Cost)::numeric,2) as total_cost, " 
            + (controlStrategy.getTargetPollutant() == null || controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION)
                    ? "null::double precision" 
                    : "sum(case when poll LIKE '%" + pollToMatch + "%' then Eff_Emis_Reduction else null::double precision end)"
            ) + " as total_reduction "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
                controlStrategyResult.setRecordCount(recordCount);
                Double totalCost = rs.getDouble(2);
                if (!rs.wasNull())
                    controlStrategyResult.setTotalCost(totalCost);
                Double totalTargetPollutantReduction = rs.getDouble(3);
                if (!rs.wasNull())
                    controlStrategyResult.setTotalReduction(totalTargetPollutantReduction);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
    }

    protected void setResultCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT count(1) as record_count "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
                controlStrategyResult.setRecordCount(recordCount);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
    }

    public String getFilterForSourceQuery() throws EmfException {
        if (filterForSourceQuery == null) {
            String sqlFilter = getFilterFromRegionDataset();
            String filter = controlStrategy.getFilter();
            
            //get and build strategy filter...
            if (filter == null || filter.trim().length() == 0)
                sqlFilter = "";
            else 
                sqlFilter = " and (" + filter + ") "; 

            filterForSourceQuery = sqlFilter;
        }
        return filterForSourceQuery;
    }

    protected Version version(ControlStrategyInputDataset controlStrategyInputDataset) {
        return version(controlStrategyInputDataset.getInputDataset().getId(), controlStrategyInputDataset.getVersion());
    }

    private Version version(int datasetId, int version) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, entityManager);
        } finally {
            entityManager.close();
        }
    }

    protected String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    protected String qualifiedName(String table) throws EmfException {
     // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }

    protected int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInZeroBasedMonth(controlStrategy.getInventoryYear(), month) : 31;
    }
    
    protected double getEmission(double annEmis, double avdEmis) {
        return month != - 1 ? (avdEmis == 0.0 ? annEmis : avdEmis * daysInMonth) : annEmis;
    }
    
    protected void updateDataset(EmfDataset dataset) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            dao.updateWithoutLocking(dataset, entityManager);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not update dataset: " + dataset.getName());
        } finally {
            entityManager.close();
        }
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
    
    public ControlStrategyResult[] getControlStrategyResults() {
        if (results == null) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                results = controlStrategyDAO.getControlStrategyResults(controlStrategy.getId(), entityManager).toArray(new ControlStrategyResult[0]);
            } finally {
                entityManager.close();
            }
        }
        return results;
    }
    
    protected double getUncontrolledEmission(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();
        int month = controlStrategyInputDataset.getInputDataset().applicableMonth();
        int daysInMonth = getDaysInMonth(month);
        double uncontrolledEmission = 0.0D;

        String datsetTypeName = controlStrategyInputDataset.getInputDataset().getDatasetType().getName();
        boolean isFlatFileInventory = datsetTypeName.equals(DatasetType.FLAT_FILE_2010_POINT) 
            || datsetTypeName.equals(DatasetType.FLAT_FILE_2010_NONPOINT)
            || datsetTypeName.equals(DatasetType.ff10MergedInventory);
        String sqlAnnEmis = (isFlatFileInventory ? "ann_value" : (month != -1 ? "coalesce(" + daysInMonth + " * avd_emis, ann_emis)" : "ann_emis"));
        
        String pollToMatch = controlStrategy.getTargetPollutant().getName();
        if (pollToMatch.equals("PM2_5")) pollToMatch = "PM2";

        String query = "SELECT sum(" + sqlAnnEmis + ") "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + " and poll IN ("
            + "   SELECT DISTINCT p.name"
            + "   FROM emf.pollutants p"
            + "   JOIN emf.aggregrated_efficiencyrecords r"
            + "   ON r.pollutant_id = p.id"
            + "   WHERE p.name LIKE '%" + pollToMatch + "%'"
            + ")"
            + getFilterForSourceQuery() + ";";
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                uncontrolledEmission = rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return uncontrolledEmission;
    }

    protected void createDetailedResultTableIndexes(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.create_strategy_detailed_result_table_indexes('" + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + "');analyze emissions." + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + ";";
        
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    protected void removeControlStrategyResult(int resultId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), resultId, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            entityManager.close();
        }
    }
    
    public int getMessageDatasetRecordCount() {
        return strategyMessagesResult != null ? strategyMessagesResult.getRecordCount() : 0;
    }
    
    public ControlStrategyResult getStrategyMessagesResult() {
        return strategyMessagesResult;
    }

    public ControlStrategyResult[] getStrategyMessagesResults() {
        return strategyMessageResultList.toArray(new ControlStrategyResult[0]);
    }

    private StrategyResultType getStrategyMessagesResultType() throws EmfException {
        StrategyResultType resultType = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.strategyMessages, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            entityManager.close();
        }
        return resultType;
    }

    private DatasetType getDatasetType(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return new DatasetTypesDAO().get(name, entityManager);
        } finally {
            entityManager.close();
        }
    }

    public ControlStrategyResult createStrategyMessagesResult() throws Exception 
    {
        strategyMessagesResult = new ControlStrategyResult();
        strategyMessagesResult.setControlStrategyId(controlStrategy.getId());
//        result.setInputDataset(inventory);
//        result.setInputDatasetVersion(inventoryVersion);
        strategyMessagesResult.setDetailedResultDataset(createStrategyMessagesDataset());

        strategyMessagesResult.setStrategyResultType(getStrategyMessagesResultType());
        strategyMessagesResult.setStartTime(new Date());
        strategyMessagesResult.setRunStatus("Start processing strategy messages result");

        //persist result
        addControlStrategyResult(strategyMessagesResult);

        //add to list for later use...
        strategyMessageResultList.add(strategyMessagesResult);

        return strategyMessagesResult;
    }

    protected ControlStrategyResult createStrategyMessagesResult(EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        strategyMessagesResult = new ControlStrategyResult();
        strategyMessagesResult.setControlStrategyId(controlStrategy.getId());
        strategyMessagesResult.setInputDataset(inventory);
        strategyMessagesResult.setInputDatasetVersion(inventoryVersion);
        strategyMessagesResult.setDetailedResultDataset(createStrategyMessagesDataset(inventory));

        strategyMessagesResult.setStrategyResultType(getStrategyMessagesResultType());
        strategyMessagesResult.setStartTime(new Date());
        strategyMessagesResult.setRunStatus("Start processing strategy messages result");

        //persist result
        addControlStrategyResult(strategyMessagesResult);

        //add to list for later use...
        strategyMessageResultList.add(strategyMessagesResult);

        return strategyMessagesResult;
    }
    
    protected ControlStrategyResult createStrategyMessagesResult(String namePrefix, EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        strategyMessagesResult = new ControlStrategyResult();
        strategyMessagesResult.setControlStrategyId(controlStrategy.getId());
        strategyMessagesResult.setInputDataset(inventory);
        strategyMessagesResult.setInputDatasetVersion(inventoryVersion);
        strategyMessagesResult.setDetailedResultDataset(createStrategyMessagesDataset(namePrefix, inventory));

        strategyMessagesResult.setStrategyResultType(getStrategyMessagesResultType());
        strategyMessagesResult.setStartTime(new Date());
        strategyMessagesResult.setRunStatus("Start processing strategy messages result");

        //persist result
        addControlStrategyResult(strategyMessagesResult);

        //add to list for later use...
        strategyMessageResultList.add(strategyMessagesResult);

        return strategyMessagesResult;
    }
    
    protected void deleteStrategyMessageResult(ControlStrategyResult strategyMessagesResult) throws EmfException {
        //get rid of strategy results...
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            EmfDataset[] ds = controlStrategyDAO.getResultDatasets(controlStrategy.getId(), strategyMessagesResult.getId(), entityManager);
            
            //get rid of old strategy results...
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), strategyMessagesResult.getId(), entityManager);
            //delete and purge datasets
            controlStrategyDAO.removeResultDatasets(ds, user, entityManager, dbServer);

            //remove from list so its perused for later use...
            strategyMessageResultList.remove(strategyMessagesResult);

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove control strategy message result.");
        } finally {
            entityManager.close();
        }
    }

    protected void populateStrategyMessagesDataset(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult strategyMessagesResult, ControlStrategyResult detailedResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_strategy_messages("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + strategyMessagesResult.getId() + ", " + detailedResult.getId() + ");";
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            setStatus("Started populating Strategy Messages Output from inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            datasource.query().execute(query);
            setStatus("Completed populating Strategy Messages Output from inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
        } catch (SQLException e) {
            if (DebugLevels.DEBUG_25())
                System.out.println("SQLException populateStrategyMessagesDataset\n"  + e.getMessage());
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }
}