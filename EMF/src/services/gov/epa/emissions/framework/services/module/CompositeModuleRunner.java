package gov.epa.emissions.framework.services.module;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

class CompositeModuleRunner extends ModuleRunner {
    private Map<Integer, SubmoduleRunner> submoduleRunners; // the key is the submodule id 

    public CompositeModuleRunner(ModuleRunnerContext moduleRunnerContext) throws EmfException {
        super(moduleRunnerContext);
        Module module = getModule();
        if (!module.isComposite()) {
            throw new EmfException("Internal error: \"" + module.getName() + "\" is not a composite module");
        }
        submoduleRunners = new HashMap<Integer, SubmoduleRunner>();
        for (ModuleTypeVersionSubmodule submodule : module.getModuleTypeVersion().getModuleTypeVersionSubmodules().values()) {
            if (submodule.getModuleTypeVersion().isComposite()) {
                submoduleRunners.put(submodule.getId(), new CompositeSubmoduleRunner(moduleRunnerContext, this, submodule));
            } else {
                submoduleRunners.put(submodule.getId(), new SimpleSubmoduleRunner(moduleRunnerContext, this, submodule));
            }
        }
    }

    protected void execute() {
        Connection connection = getConnection();
        ModulesDAO modulesDAO = getModulesDAO();
        EntityManager entityManager = getEntityManager();
        
        Module module = getModule();
        ModuleTypeVersion moduleTypeVersion = getModuleTypeVersion();
        History history = getHistory();
        Map<String, HistoryParameter> historyParameters = history.getHistoryParameters();
        String userTimeStamp = getUserTimeStamp();
        String tempUserPassword = getTempUserPassword();

        String logMessage = "";
        String errorMessage = "";
        
        Statement statement = null;
        
        setFinalStatusMessage("");
        
        List<String> outputDatasetTables = new ArrayList<String>();

        try {
            createDatasets();
            
            // prepare all parameters
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
                HistoryParameter historyParameter;
                if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
                    if (moduleParameter.isSet()) {
                        historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), moduleParameter.getValue());
                        setInputParameter(moduleParameter.getParameterName(), moduleParameter.getValue());
                    } else {
                        historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                        setInputParameter(moduleParameter.getParameterName(), null);
                    }
                } else if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.INOUT)) {
                    if (moduleParameter.isSet()) {
                        historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), moduleParameter.getValue());
                        setInputParameter(moduleParameter.getParameterName(), moduleParameter.getValue());
                        setOutputParameter(moduleParameter.getParameterName(), moduleParameter.getValue());
                    } else {
                        historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                        setInputParameter(moduleParameter.getParameterName(), null);
                        setOutputParameter(moduleParameter.getParameterName(), null);
                    }
                } else { // OUT
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                    setOutputParameter(moduleParameter.getParameterName(), null);
                }
                historyParameters.put(moduleParameter.getParameterName(), historyParameter);
                logMessage = String.format("%s parameter: %s %s = %s",
                                           moduleTypeVersionParameter.getMode(),
                                           moduleTypeVersionParameter.getSqlParameterType(),
                                           moduleParameter.getParameterName(),
                                           moduleParameter.getValue());
                history.addLogMessage(History.INFO, logMessage);
            }
            
            // execute setup script
            
            String setupScript = getTempUserSetupScript(userTimeStamp, tempUserPassword) +
                                 getGrantPermissionsScript(userTimeStamp, outputDatasetTables);
            
            try {
                history.setSetupScript(setupScript);
                history.setStatus(History.SETUP_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting setup script.");
                
                history = modulesDAO.updateHistory(history, entityManager);

                statement = connection.createStatement();
                statement.execute(setupScript);
                
            } catch (Exception e) {
                throw new EmfException(SETUP_SCRIPT_ERROR + e.getMessage());
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
            
            // execute submodules
            history.setStatus(History.SUBMODULES);
            history.addLogMessage(History.INFO, "Started executing submodules.");
            history = modulesDAO.updateHistory(history, entityManager);
            
            TreeMap<Integer, SubmoduleRunner>  todoSubmoduleRunners = new TreeMap<Integer, SubmoduleRunner>();
            TreeMap<Integer, SubmoduleRunner> readySubmoduleRunners = new TreeMap<Integer, SubmoduleRunner>();
            TreeMap<Integer, SubmoduleRunner>  doneSubmoduleRunners = new TreeMap<Integer, SubmoduleRunner>();
            todoSubmoduleRunners.putAll(submoduleRunners);

            // pass input datasets to submodules
            for(ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersion.getModuleTypeVersionDatasetConnections().values()) {
                if (datasetConnection.getSourceSubmodule() != null)
                    continue;
                DatasetVersion datasetVersion = getInputDataset(datasetConnection.getSourcePlaceholderName());
                if (datasetConnection.getTargetSubmodule() == null) {
                    setOutputDataset(datasetConnection.getTargetPlaceholderName(), datasetVersion);
                } else {
                    SubmoduleRunner submoduleRunner = todoSubmoduleRunners.get(datasetConnection.getTargetSubmodule().getId());
                    submoduleRunner.setInputDataset(datasetConnection.getTargetPlaceholderName(), datasetVersion);
                }
                if (datasetVersion == null) {
                    logMessage = String.format("Passing dataset NULL version NULL from \"%s\" to \"%s\"",
                                               datasetConnection.getSourceName(), datasetConnection.getTargetName());
                } else {
                    logMessage = String.format("Passing dataset \"%s\" version %d from \"%s\" to \"%s\"",
                                               datasetVersion.getDataset().getName(), datasetVersion.getVersion(), datasetConnection.getSourceName(), datasetConnection.getTargetName());
                }
                history.addLogMessage(History.INFO, logMessage);
            }
            
            // pass input parameters to submodules
            for(ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersion.getModuleTypeVersionParameterConnections().values()) {
                if (parameterConnection.getSourceSubmodule() != null)
                    continue;
                String value = getInputParameter(parameterConnection.getSourceParameterName()); // could be null
                if (parameterConnection.getTargetSubmodule() == null) {
                    setOutputParameter(parameterConnection.getTargetParameterName(), value);
                } else {
                    SubmoduleRunner submoduleRunner = todoSubmoduleRunners.get(parameterConnection.getTargetSubmodule().getId());
                    submoduleRunner.setInputParameter(parameterConnection.getTargetParameterName(), value);
                }
                if (value == null) {
                    logMessage = String.format("Passing value NULL from \"%s\" to \"%s\"",
                                               parameterConnection.getSourceName(), parameterConnection.getTargetName());
                } else {
                    logMessage = String.format("Passing value \"%s\" from \"%s\" to \"%s\"",
                            value, parameterConnection.getSourceName(), parameterConnection.getTargetName());
                }
                history.addLogMessage(History.INFO, logMessage);
            }
            
            // while there are still submodules to run
            //   get the list of all submodules that are ready to run
            //   execute the ready submodules in the order of their id
            //   use connections to pass datasets and parameters to submodules and module
            while(doneSubmoduleRunners.size() < submoduleRunners.size()) {
                // check if any submodule runner in the todo list is ready
                List<Integer> readyList = new ArrayList<Integer>();
                for(Integer submoduleId : todoSubmoduleRunners.keySet()) {
                    SubmoduleRunner submoduleRunner = todoSubmoduleRunners.get(submoduleId); 
                    if (submoduleRunner.isReady()) {
                        readyList.add(submoduleId);
                        readySubmoduleRunners.put(submoduleId, submoduleRunner);
                    }
                }
                for(Integer submoduleId : readyList) {
                    todoSubmoduleRunners.remove(submoduleId);
                }
                
                if (readySubmoduleRunners.isEmpty()) {
                    throw new EmfException("Internal error: " + todoSubmoduleRunners.size() + " submodule(s) left to do but none are ready.");
                }
                
                // execute all ready submodules in ascending id order 
                for(Integer submoduleId : readySubmoduleRunners.keySet()) {
                    SubmoduleRunner submoduleRunner = readySubmoduleRunners.get(submoduleId);
                    try {
                        logMessage = String.format("Running submodule \"%s\" (ID=%d)\n", submoduleRunner.getPathNames(), submoduleRunner.getModuleTypeVersionSubmodule().getId());
                        history.addLogMessage(History.INFO, logMessage);
                        
                        submoduleRunner.run();
                        
                        logMessage = String.format("Submodule \"%s\" (ID=%d) finished with status %s and result %s.\n",
                                                    submoduleRunner.getPathNames(), submoduleRunner.getModuleTypeVersionSubmodule().getId(),
                                                    submoduleRunner.getHistorySubmodule().getStatus(), submoduleRunner.getHistorySubmodule().getResult());
                        history.addLogMessage(History.INFO, logMessage);
                        
                        if (!submoduleRunner.getHistorySubmodule().getStatus().equals(History.COMPLETED) ||
                            !submoduleRunner.getHistorySubmodule().getResult().equals(History.SUCCESS)) {
                            throw new EmfException(logMessage);
                        }
                            
                        // pass submodule output datasets to this module and other submodules in the todo list
                        for(ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersion.getModuleTypeVersionDatasetConnections().values()) {
                            ModuleTypeVersionSubmodule sourceSubmodule = datasetConnection.getSourceSubmodule();
                            if (sourceSubmodule == null || sourceSubmodule.getId() != submoduleId)
                                continue;
                            DatasetVersion submoduleOutputDatasetVersion = submoduleRunner.getOutputDataset(datasetConnection.getSourcePlaceholderName());
                            ModuleTypeVersionSubmodule targetSubmodule = datasetConnection.getTargetSubmodule();
                            if (targetSubmodule == null) {
                                DatasetVersion moduleOutputDatasetVersion = getOutputDataset(datasetConnection.getTargetPlaceholderName());
                                try {
                                    transferData(submoduleOutputDatasetVersion, moduleOutputDatasetVersion);
                                } catch (Exception e) {
                                    errorMessage = String.format("Failed to transfer data from source \"%s\" (dataset \"%s\" version %d) to target \"%s\" (dataset \"%s\" version %d): %s",
                                                                 datasetConnection.getSourceName(), submoduleOutputDatasetVersion.getDataset().getName(), submoduleOutputDatasetVersion.getVersion(),
                                                                 datasetConnection.getTargetName(), moduleOutputDatasetVersion.getDataset().getName(), moduleOutputDatasetVersion.getVersion(),
                                                                 e.getMessage());
                                    throw new EmfException(errorMessage);
                                }
                            } else {
                                SubmoduleRunner targetSubmoduleRunner = todoSubmoduleRunners.get(targetSubmodule.getId());
                                targetSubmoduleRunner.setInputDataset(datasetConnection.getTargetPlaceholderName(), submoduleOutputDatasetVersion);
                                logMessage = String.format("Passing dataset \"%s\" version %d from \"%s\" to \"%s\"",
                                                           submoduleOutputDatasetVersion.getDataset().getName(), submoduleOutputDatasetVersion.getVersion(),
                                                           datasetConnection.getSourceName(), datasetConnection.getTargetName());
                                history.addLogMessage(History.INFO, logMessage);
                            }
                        }
                        // TODO delete the unused output datasets (that are not connected to any targets)
                        
                        // pass submodule output parameters to this module and other submodules in the todo list
                        for(ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersion.getModuleTypeVersionParameterConnections().values()) {
                            ModuleTypeVersionSubmodule sourceSubmodule = parameterConnection.getSourceSubmodule();
                            if (sourceSubmodule == null || sourceSubmodule.getId() != submoduleId)
                                continue;
                            String submoduleOutputValue = submoduleRunner.getOutputParameter(parameterConnection.getSourceParameterName());
                            ModuleTypeVersionSubmodule targetSubmodule = parameterConnection.getTargetSubmodule();
                            if (targetSubmodule == null) {
                                setOutputParameter(parameterConnection.getTargetParameterName(), submoduleOutputValue);
                                historyParameters.get(parameterConnection.getTargetParameterName()).setValue(submoduleOutputValue);
                            } else {
                                SubmoduleRunner targetSubmoduleRunner = todoSubmoduleRunners.get(targetSubmodule.getId());
                                targetSubmoduleRunner.setInputParameter(parameterConnection.getTargetParameterName(), submoduleOutputValue);
                            }
                            logMessage = String.format("Passing value \"%s\" from \"%s\" to \"%s\"",
                                                       submoduleOutputValue, parameterConnection.getSourceName(), parameterConnection.getTargetName());
                            history.addLogMessage(History.INFO, logMessage);
                        }
                        
                    } catch (EmfException e) {
                        throw new EmfException("Failed to run submodule '" + submoduleRunner.getPathNames() + "': " + e.getMessage());
                    }
                    doneSubmoduleRunners.put(submoduleId, submoduleRunner);
                }
                readySubmoduleRunners.clear();
            }

            // TODO verify that all output parameters have been set
            
            history.setStatus(History.TEARDOWN_SCRIPT);
            executeTeardownScript(outputDatasetTables);
            
            history.setStatus(History.COMPLETED);
            history.setResult(History.SUCCESS);
            
            setFinalStatusMessage("Completed running module '" + module.getName() + "': " + history.getResult());
            
            history.addLogMessage(History.SUCCESS, getFinalStatusMessage());
            
            history = modulesDAO.updateHistory(history, entityManager);
            
        } catch (Exception e) {
            
            String eMessage = e.getMessage();
            
            history.setResult(History.FAILED);
            history.setErrorMessage(eMessage);

            if (eMessage.startsWith(SETUP_SCRIPT_ERROR)) {
                errorMessage = eMessage + "\n\n" + getLineNumberedScript(history.getSetupScript()) + "\n";
                try {
                    executeTeardownScript(outputDatasetTables);
                } catch (Exception e2) {
                    errorMessage += "\n\n" + e2.getMessage() + "\n\n" + getLineNumberedScript(history.getTeardownScript()) + "\n";
                }
            } else if (eMessage.startsWith(SUBMODULE_ERROR)) {
                errorMessage = eMessage + "\n";
                try {
                    executeTeardownScript(outputDatasetTables);
                } catch (Exception e2) {
                    errorMessage += "\n\n" + e2.getMessage() + "\n\n" + getLineNumberedScript(history.getTeardownScript()) + "\n";
                }
            } else if (eMessage.startsWith(TEARDOWN_SCRIPT_ERROR)) {
                errorMessage = eMessage + "\n\n" + getLineNumberedScript(history.getTeardownScript()) + "\n";
            } else {
                errorMessage = eMessage + "\n";
                try {
                    executeTeardownScript(outputDatasetTables);
                } catch (Exception e2) {
                    errorMessage += "\n\n" + e2.getMessage() + "\n\n" + getLineNumberedScript(history.getTeardownScript()) + "\n";
                }
            }
            
            setFinalStatusMessage("Completed running module '" + module.getName() + "': " + history.getStatus() + " " + history.getResult() + "\n\n" + errorMessage);
            
            history.addLogMessage(History.ERROR, getFinalStatusMessage());
            
            history = modulesDAO.updateHistory(history, entityManager);
            
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // ignore
                }
                statement = null;
            }
            
            Map<String, EmfDataset> datasets = new HashMap<String, EmfDataset>();
            collectTemporaryDatasets(datasets);
            
            if (!datasets.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for(String placeholderPathNames : datasets.keySet()) {
                    EmfDataset dataset = datasets.get(placeholderPathNames);
                    message.append(String.format("Deleting temporary dataset \"%s\"\nfor placeholder \"%s\"\n\n", dataset.getName(), placeholderPathNames));
                }
                
                history.addLogMessage(History.INFO, message.toString());
                history = modulesDAO.updateHistory(history, entityManager);
                
                try {
                    EmfDataset[] datasetsArray = datasets.values().toArray(new EmfDataset[]{});
                    deleteDatasets(datasetsArray);
                    history.addLogMessage(History.INFO, "Successfully deleted all temporary datasets.");
                    history = modulesDAO.updateHistory(history, entityManager);
                } catch (EmfException e) {
                    history.addLogMessage(History.INFO, "Failed to delete a temporary dataset:\n" + e.getMessage());
                    history = modulesDAO.updateHistory(history, entityManager);
                }
            }
        }
    }

    protected void collectTemporaryDatasets(Map<String, EmfDataset> datasets) {
        super.collectTemporaryDatasets(datasets);
        for(SubmoduleRunner submoduleRunner : submoduleRunners.values()) {
            submoduleRunner.collectTemporaryDatasets(datasets);
        }
    }
    
    public Map<Integer, SubmoduleRunner> getSubmoduleRunners() {
        return submoduleRunners;
    }

    public void setSubmoduleRunners(Map<Integer, SubmoduleRunner> submoduleRunners) {
        this.submoduleRunners = submoduleRunners;
    }
}
