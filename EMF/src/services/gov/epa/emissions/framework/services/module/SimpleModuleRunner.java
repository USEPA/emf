package gov.epa.emissions.framework.services.module;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

class SimpleModuleRunner extends ModuleRunner {

    public SimpleModuleRunner(ModuleRunnerContext moduleRunnerContext) throws EmfException {
        super(moduleRunnerContext);
        Module module = moduleRunnerContext.getModule();
        if (module.isComposite()) {
            throw new EmfException("Internal error: \"" + module.getName() + "\" is a composite module");
        }
    }

    protected void execute() {
        DbServer dbServer = getDbServer();
        User user = getUser();
        Connection connection = getConnection();
        DatasetDAO datasetDAO = getDatasetDAO();
        ModulesDAO modulesDAO = getModulesDAO();
        Session session = getSession();
        
        Module module = getModule();
        ModuleTypeVersion moduleTypeVersion = getModuleTypeVersion();
        History history = getHistory();
        Map<String, HistoryParameter> historyParameters = history.getHistoryParameters();
        Date startDate = getStartDate();
        String timeStamp = getTimeStamp();
        String userTimeStamp = getUserTimeStamp();
        String tempUserPassword = getTempUserPassword();

        String errorMessage = "";
        
        Statement statement = null;
        
        setFinalStatusMessage("");
        
        try {
            String algorithm = moduleTypeVersion.getAlgorithm();
            history.setUserScript(algorithm);

            createDatasets();
            
            String datasetTablesSchema = EmfDbServer.EMF_EMISSIONS_SCHEMA; // FIXME hard-coded schema name
            
            StringBuilder viewDefinitions = new StringBuilder();
            
            List<Version> outputDatasetVersions = new ArrayList<Version>();
            List<String> outputDatasetTables = new ArrayList<String>();
            
            // create views for all datasets
            // replace all dataset placeholders in the algorithm
            for(ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
                StringBuilder viewName = new StringBuilder();
                StringBuilder viewDefinition = new StringBuilder();
                DatasetVersion datasetVersion = null;
                if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                    datasetVersion = getOutputDataset(moduleDataset.getPlaceholderName());
                } else { // IN or INOUT
                    datasetVersion = getInputDataset(moduleDataset.getPlaceholderName());
                }
                EmfDataset dataset = datasetVersion.getDataset();
                int versionNumber = datasetVersion.getVersion();
                InternalSource internalSource = getInternalSource(dataset);
                if (!moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.IN)) {
                    Version version = getVersion(dataset, versionNumber, session);
                    outputDatasetVersions.add(version);
                    outputDatasetTables.add(datasetTablesSchema + "." + internalSource.getTable());
                }
                createView(viewName, viewDefinition, connection, module, moduleTypeVersionDataset, dataset, versionNumber);
                viewDefinitions.append(viewDefinition);
                algorithm = replaceDatasetPlaceholders(algorithm, moduleDataset, dataset, versionNumber, viewName.toString());
            }
            
            // replace global placeholders in the algorithm
            algorithm = replaceGlobalPlaceholders(algorithm, user, startDate, history);
            
            history.setUserScript(algorithm);

            // verify that the algorithm doesn't have any dataset or global placeholders left
            assertNoPlaceholders(algorithm, "$");

            // replace all parameter placeholders
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                algorithm = replaceParameterPlaceholders(algorithm, moduleParameter, timeStamp);
            }
            
            history.setUserScript(algorithm);

            // verify that the algorithm doesn't have any parameter placeholders left
            assertNoPlaceholders(algorithm, "#");

            // create outer block
            // declare all parameters, initialize IN and INOUT parameters
            String parameterDeclarations = "";
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                String parameterTimeStamp = moduleParameter.getParameterName() + "_" + timeStamp;
                HistoryParameter historyParameter;
                ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
                String sqlParameterType = moduleTypeVersionParameter.getSqlParameterType();
                if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    parameterDeclarations += "    " + parameterTimeStamp + " " + sqlParameterType + ";\n";
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                } else { // IN or INOUT
                    parameterDeclarations += "    " + parameterTimeStamp + " " + sqlParameterType + " := CAST('" + moduleParameter.getValue() + "' AS " + sqlParameterType + ");\n";
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), moduleParameter.getValue());
                }
                historyParameters.put(moduleParameter.getParameterName(), historyParameter);
            }
            history.setHistoryParameters(historyParameters);
            
            // return the values of all INOUT and OUT parameters as a result set
            String outputParameters = "";
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                String parameterTimeStamp = moduleParameter.getParameterName() + "_" + timeStamp; 
                ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
                if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.IN)) {
                    if (!outputParameters.isEmpty()) {
                        outputParameters += "UNION ALL\n";
                    }
                    outputParameters += "SELECT '" + moduleTypeVersionParameter.getParameterName() + "' AS name, CAST(" + parameterTimeStamp + " AS text) AS value\n";
                }
            }
            
            String outputParametersTableName = "";
            String selectOutputParameters = "";
            if (!outputParameters.isEmpty()) {
                outputParametersTableName = "output_parameters_" + userTimeStamp;
                String outputParametersTable = "DROP TABLE IF EXISTS " + outputParametersTableName + ";\n" +
                                               "CREATE TEMPORARY TABLE " + outputParametersTableName + " AS\n";
                outputParameters = "\n\n-- output parameters result set\n\n" + outputParametersTable + outputParameters + ";\n";
                selectOutputParameters = "\nSELECT * FROM " + outputParametersTableName + ";\n\n" +
                                         "DROP TABLE IF EXISTS " + outputParametersTableName + ";\n\n";
            }
    
            String userTag = userTimeStamp + "_user_script";

            if (parameterDeclarations.isEmpty()) {
                algorithm = "\nDO $" + userTag + "$\nBEGIN\n" + viewDefinitions + algorithm + "\nEND $" + userTag + "$;\n";
            } else {
                algorithm = "\nDO $" + userTag + "$\nDECLARE\n" + parameterDeclarations + "BEGIN\n" + viewDefinitions + algorithm + outputParameters + "\nEND $" + userTag + "$;\n" + selectOutputParameters;
            }
    
            // execute setup script
            
            String setupScript = getTempUserSetupScript(userTimeStamp, tempUserPassword) +
                                 getGrantPermissionsScript(userTimeStamp, outputDatasetTables);
            
            try {
                history.setSetupScript(setupScript);
                history.setStatus(History.SETUP_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting setup script.");
                
                history = modulesDAO.updateHistory(history, session);

                statement = connection.createStatement();
                statement.execute(setupScript);
                
            } catch (Exception e) {
                // e.printStackTrace();
                // TODO save error to the current execution history record
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
            
            // execute algorithm
            
            // create new connection and login as the temporary user

            Connection userConnection = null; 
            try {
                history.setUserScript(algorithm);
                history.setStatus(History.USER_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting user script (algorithm).");
                
                history = modulesDAO.updateHistory(history, session);
                
                userConnection = getUserConnection(userTimeStamp, tempUserPassword);
                userConnection.setAutoCommit(true);
                statement = userConnection.createStatement();
                statement.execute(algorithm);

                // get the values of all INOUT and OUT parameters
                while(statement.getMoreResults()) {
                    ResultSet resultSet = statement.getResultSet();
                    while(resultSet.next()) {
                        String name = resultSet.getString(1);
                        String value = resultSet.getString(2);
                        
                        setOutputParameter(name, value);
                        
                        historyParameters.get(name).setValue(value);
                    }
                }

                // TODO verify that all output parameters have been set
                
                // update the record counts for the output datasets
                if (outputDatasetVersions.size() > 0) {
                    history.addLogMessage(History.INFO, "Updating the number of records for the OUT and INOUT datasets:");
                    for(Version version : outputDatasetVersions) {
                        EmfDataset dataset = datasetDAO.getDataset(session, version.getDatasetId());
                        int recordCount = updateVersion(dataset, version, dbServer, session, datasetDAO, user);
                        String message = String.format("Dataset \"%s\" version %d has %d records.", dataset.getName(), version.getVersion(), recordCount);
                        history.addLogMessage(History.INFO, message);
                    }
                }
                
                history.addLogMessage(History.INFO, "User script (algorithm) completed successfully.");
                
            } catch (Exception e) {
                // e.printStackTrace();
                // TODO save error to the current execution history record
                throw new EmfException(USER_SCRIPT_ERROR + e.getMessage());
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                    statement = null;
                }

                if (userConnection != null) {
                    userConnection.close();
                    userConnection = null;
                }
            }
            
            executeTeardownScript(outputDatasetTables);
            
            history.setStatus(History.COMPLETED);
            history.setResult(History.SUCCESS);
            
            setFinalStatusMessage("Completed running module '" + module.getName() + "': " + history.getResult());
            
            history.addLogMessage(History.SUCCESS, getFinalStatusMessage());
            
        } catch (Exception e) {
            
            String eMessage = e.getMessage();
            
            history.setStatus(History.COMPLETED);
            history.setResult(History.FAILED);
            history.setErrorMessage(eMessage);

            if (eMessage.startsWith(SETUP_SCRIPT_ERROR)) {
                errorMessage = eMessage + "\n\n" + getLineNumberedScript(history.getSetupScript()) + "\n";
            } else if (eMessage.startsWith(USER_SCRIPT_ERROR)) {
                errorMessage = eMessage + "\n\n" + getLineNumberedScript(history.getUserScript()) + "\n";
            } else if (eMessage.startsWith(TEARDOWN_SCRIPT_ERROR)) {
                errorMessage = eMessage + "\n\n" + getLineNumberedScript(history.getTeardownScript()) + "\n";
            } else {
                errorMessage = eMessage;
            }
            
            setFinalStatusMessage("Completed running module '" + module.getName() + "': " + history.getResult() + "\n\n" + errorMessage);
            
            history.addLogMessage(History.ERROR, getFinalStatusMessage());
            
        } finally {
            history = modulesDAO.updateHistory(history, session);
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
    
}
