package gov.epa.emissions.framework.services.module;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

abstract class SubmoduleRunner extends ModuleRunner {
    private ModuleRunner parentModuleRunner;
    private ModuleTypeVersionSubmodule moduleTypeVersionSubmodule;
    private HistorySubmodule historySubmodule;
    
    private Date submoduleStartDate;
    
    private String path = null;
    private String pathNames = null;
    private int inputDatasetsCount = 0;
    private int inputParametersCount = 0;
    
    public SubmoduleRunner(ModuleRunnerContext moduleRunnerContext, ModuleRunner parentModuleRunner, ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        super(moduleRunnerContext);
        this.parentModuleRunner = parentModuleRunner;
        this.moduleTypeVersionSubmodule = moduleTypeVersionSubmodule;
        
        inputDatasetsCount = 0;
        for (ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleTypeVersionDatasets().values())
            if (!moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) // IN & INOUT
                inputDatasetsCount++;
        
        inputParametersCount = 0;
        for (ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleTypeVersionParameters().values())
            if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) // IN & INOUT
                inputParametersCount++;
    }

    protected void start() {
        submoduleStartDate = new Date();
    }

    protected void createSubmoduleHistory() {
        historySubmodule = new HistorySubmodule();
        historySubmodule.setHistory(getHistory());
        historySubmodule.setCreationDate(getStartDate());
        historySubmodule.setSubmodulePath(getPath());
        historySubmodule.setSubmodulePathNames(getPathNames());
        historySubmodule.setStatus(History.STARTED);
        String logMessage = String.format("Submodule '%s' started by %s on %s",
                                          historySubmodule.getSubmodulePathNames(), getUser().getName(),
                                          CustomDateFormat.format_yyyy_MM_dd_HHmmssSSS(getStartDate()));
        historySubmodule.addLogMessage(History.INFO, logMessage);
        getHistory().addHistorySubmodule(historySubmodule);
        getModulesDAO().updateHistory(getHistory(), getSession());
    }

    protected void stop() {
        Date submoduleStopDate = new Date();
        long durationSeconds = (submoduleStopDate.getTime() - submoduleStartDate.getTime()) / 1000;
        historySubmodule.setDurationSeconds((int)durationSeconds);
        historySubmodule = getModulesDAO().updateSubmodule(historySubmodule, getSession());
    }

    public void run() throws EmfException {
        start();
        createSubmoduleHistory();
        execute();
        stop();
    }

    protected String getNewInternalDatasetName(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        String tempName = getModule().getName() + " #" + getHistory().getRunId() +  " " + getPathNames();
        if (tempName.length() > 245) {
            tempName = getModule().getName() + " #" + getHistory().getRunId() + " ... / " + moduleTypeVersionSubmodule.getName() + " / " + moduleTypeVersionDataset.getPlaceholderName();
        }
        if (tempName.length() > 245) {
            tempName = getModule().getName() + " #" + getHistory().getRunId() + " " + moduleTypeVersionDataset.getPlaceholderName();
        }
        if (tempName.length() > 245) {
            tempName = getModule().getName() + " #" + getHistory().getRunId();
        }
        if (tempName.length() > 245) {
            tempName = tempName.substring(0, 245);
        }
        String name = "";
        EmfDataset dataset = null;
        do {
            name = tempName + " " + CustomDateFormat.format_HHMMSSSS(new Date()); 
            dataset = getDatasetDAO().getDataset(getSession(), tempName);
        } while (dataset != null);
        return name;
    }

    private void checkInternalDatasetReplacementRules(EmfDataset dataset, Module module, String placeholderPathNames) throws EmfException {
        if (!wasDatasetCreatedByModule(dataset, module, placeholderPathNames)) {
            throw new EmfException("Can't replace internal dataset \"" + dataset.getName() +
                                   "\" because it was not created by module \"" + module.getName() +
                                   "\" for the \"" + placeholderPathNames + "\" placeholder");
        }
    }
    
    protected void createDatasets() throws Exception {
        
        DbServer dbServer = getDbServer();
        User user = getUser();
        HibernateSessionFactory sessionFactory = getHibernateSessionFactory();
        DbServerFactory dbServerFactory = getDbServerFactory();
        Datasource datasource = getDatasource();
        Connection connection = getConnection();
        DatasetDAO datasetDAO = getDatasetDAO();
        Session session = getSession();
        Date startDate = getStartDate();
        
        Module module = getModule();
        Map<String, ModuleInternalDataset> moduleInternalDatasets = module.getModuleInternalDatasets();

        History history = getHistory();
        Map<String, HistoryInternalDataset> historyInternalDatasets = history.getHistoryInternalDatasets();
        
        ModuleTypeVersion moduleTypeVersion = moduleTypeVersionSubmodule.getModuleTypeVersion();
        
        String logMessage = "";
        String errorMessage = "";
        
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            if (!moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT))
                continue;
            String placeholderName = moduleTypeVersionDataset.getPlaceholderName();
            String placeholderPath = getPath(placeholderName);
            String placeholderPathNames = getPathNames(placeholderName);
            boolean keepInternalDataset = false;
            String internalDatasetName = "";
            if (moduleInternalDatasets.containsKey(placeholderPath)) {
                ModuleInternalDataset moduleInternalDataset = moduleInternalDatasets.get(placeholderPath);
                keepInternalDataset = moduleInternalDataset.getKeep();
                String datasetNamePattern = moduleInternalDataset.getDatasetNamePattern();
                if (datasetNamePattern == null || datasetNamePattern.trim().length() == 0) {
                    internalDatasetName = getNewInternalDatasetName(moduleTypeVersionDataset);
                } else {
                    internalDatasetName = getNewDatasetName(datasetNamePattern, user, startDate, history);
                }
            } else {
                keepInternalDataset = false;
                internalDatasetName = getNewInternalDatasetName(moduleTypeVersionDataset);
            }
            String persistence = keepInternalDataset ? "persistent" : "temporary";
            EmfDataset dataset = getDatasetDAO().getDataset(session, internalDatasetName);
            int versionNumber = 0;
            if (dataset == null) { // NEW
                DatasetType datasetType = moduleTypeVersionDataset.getDatasetType();
                SqlDataTypes types = dbServer.getSqlDataTypes();
                VersionedTableFormat versionedTableFormat = new VersionedTableFormat(datasetType.getFileFormat(), types);
                String description = "New internal dataset created by the '" + module.getName() + "' module for the '" + placeholderPathNames + "' placeholder.";
                DatasetCreator datasetCreator = new DatasetCreator(module, placeholderPathNames, user, sessionFactory, dbServerFactory, datasource);
                dataset = datasetCreator.addDataset("mod", internalDatasetName, datasetType, module.getIsFinal(), versionedTableFormat, description);
               
                InternalSource internalSource = getInternalSource(dataset);
                
                logMessage = String.format("Created new %s internal dataset for %s placeholder %s:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: %d",
                                           persistence, moduleTypeVersionDataset.getMode(), placeholderPathNames,
                                           internalSource.getType(), dataset.getName(), internalSource.getTable(), versionNumber);
                historySubmodule.addLogMessage(History.INFO, logMessage);

                setOutputDataset(placeholderName, new DatasetVersion(dataset, versionNumber, keepInternalDataset));
            } else { // REPLACE
                String datasetName = dataset.getName();
                
                checkInternalDatasetReplacementRules(dataset, module, placeholderPathNames);

                boolean must_unlock = false;
                if (!dataset.isLocked()) {
                    dataset = datasetDAO.obtainLocked(user, dataset, session);
                    must_unlock = true;
                } else if (!dataset.isLocked(user)) {
                    errorMessage = String.format("Could not replace internal dataset '%s' for placeholder '%s'. The dataset is locked by %s.",
                                                  datasetName, placeholderPathNames, dataset.getLockOwner());
                    throw new EmfException(errorMessage);
                }
                
                try {
                    DatasetCreator datasetCreator = new DatasetCreator(module, placeholderPathNames, user, sessionFactory, dbServerFactory, datasource);
                    datasetCreator.replaceDataset(session, connection, dataset, module.getIsFinal());
                } finally {
                    if (must_unlock)
                        dataset = datasetDAO.releaseLocked(user, dataset, session);
                }
                
                InternalSource internalSource = getInternalSource(dataset);
                
                logMessage = String.format("Replacing internal dataset for %s placeholder %s:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: %d",
                                           moduleTypeVersionDataset.getMode(), placeholderPathNames,
                                           internalSource.getType(), dataset.getName(), internalSource.getTable(), versionNumber);
                historySubmodule.addLogMessage(History.INFO, logMessage);
                
                setOutputDataset(placeholderName, new DatasetVersion(dataset, versionNumber, keepInternalDataset));
            }
            
            if (keepInternalDataset) {
                HistoryInternalDataset historyInternalDataset = new HistoryInternalDataset();
                historyInternalDataset.setHistory(history);
                historyInternalDataset.setPlaceholderPath(placeholderPath);
                historyInternalDataset.setPlaceholderPathNames(placeholderPathNames);
                historyInternalDataset.setDatasetId(dataset.getId());
                historyInternalDataset.setVersion(versionNumber);
                historyInternalDatasets.put(placeholderPath, historyInternalDataset);
            }
        }
        getModulesDAO().updateHistory(getHistory(), getSession());
    }

    public ModuleRunner getParentModuleRunner() {
        return parentModuleRunner;
    }

    public History getHistory() {
        return parentModuleRunner.getHistory();
    }

    public ModuleTypeVersionSubmodule getModuleTypeVersionSubmodule() {
        return moduleTypeVersionSubmodule;
    }

    public int getId() {
        return moduleTypeVersionSubmodule.getId();
    }

    public String getPath() {
        if (path == null) {
            path = parentModuleRunner.getPath();
            if (path == null || path.trim().isEmpty()) {
                path = "" + moduleTypeVersionSubmodule.getId();
            } else {
                path = path + "/" + moduleTypeVersionSubmodule.getId();
            }
        }
        return path;
    }

    public String getPathNames() {
        if (pathNames == null) {
            pathNames = parentModuleRunner.getPathNames();
            if (pathNames == null || pathNames.trim().isEmpty()) {
                pathNames = moduleTypeVersionSubmodule.getName();
            } else {
                pathNames = pathNames + " / " + moduleTypeVersionSubmodule.getName();
            }
        }
        return pathNames;
    }

    public HistorySubmodule getHistorySubmodule() {
        return historySubmodule;
    }

    public boolean isReady() throws EmfException {
        Map<String, DatasetVersion> inputDatasets = getInputDatasets();
        if (inputDatasets.size() < inputDatasetsCount) // quick check optimization
            return false;
        if (inputDatasets.size() > inputDatasetsCount)
            throw new EmfException("Internal error: two many input datasets (" + inputDatasets.size() + " instead of " + inputDatasetsCount + ")");
        Map<String, DatasetVersion> outputDatasets = getOutputDatasets();
        for (ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleTypeVersionDatasets().values()) {
            if (!moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) { // IN & INOUT
                if (!inputDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName()))
                    throw new EmfException("Internal error: the input dataset for " + moduleTypeVersionDataset.getPlaceholderName() + " IN/INOUT placeholder is missing");
            }
            if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.INOUT)) { // INOUT
                if (!outputDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName()))
                    throw new EmfException("Internal error: the output dataset for " + moduleTypeVersionDataset.getPlaceholderName() + " INOUT placeholder is missing");
            }
        }

        Map<String, String> inputParameters = getInputParameters();
        if (inputParameters.size() < inputParametersCount) // quick check optimization
            return false;
        if (inputParameters.size() > inputParametersCount)
            throw new EmfException("Internal error: two many input parameters (" + inputParameters.size() + " instead of " + inputParametersCount + ")");
        Map<String, String> outputParameters = getOutputParameters();
        for (ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleTypeVersionParameters().values()) {
            if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) { // IN & INOUT
                if (!inputParameters.containsKey(moduleTypeVersionParameter.getParameterName()))
                    throw new EmfException("Internal error: the input for " + moduleTypeVersionParameter.getParameterName() + " IN/INOUT parameter is missing");
            }
            if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.INOUT)) { // INOUT
                if (!outputParameters.containsKey(moduleTypeVersionParameter.getParameterName()))
                    throw new EmfException("Internal error: the output for " + moduleTypeVersionParameter.getParameterName() + " INOUT parameter is missing");
            }
        }
        
        return true;
    }

    protected void executeTeardownScript(List<String> outputDatasetTables) throws EmfException {
        Connection connection = getConnection();
        ModulesDAO modulesDAO = getModulesDAO();
        Session session = getSession();

        String teardownScript = getDenyPermissionsScript(getUserTimeStamp(), outputDatasetTables);

        Statement statement = null;
        try {
            historySubmodule.setTeardownScript(teardownScript);
            historySubmodule.setStatus(History.TEARDOWN_SCRIPT);
            
            historySubmodule.addLogMessage(History.INFO, "Starting teardown script.");
            
            historySubmodule = modulesDAO.updateSubmodule(historySubmodule, session);
            
            statement = connection.createStatement();
            statement.execute(teardownScript);
            
        } catch (Exception e) {
            // e.printStackTrace();
            // TODO save error to the current execution history record
            throw new EmfException(TEARDOWN_SCRIPT_ERROR + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // ignore
                }
                statement = null;
            }
        }
    }

    public Date getSubmoduleStartDate() {
        return submoduleStartDate;
    }
}
