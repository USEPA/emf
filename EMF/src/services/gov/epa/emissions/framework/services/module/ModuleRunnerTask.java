package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ModuleRunnerTask {

    private Module[] modules;

    private User user;

    private DatasetDAO datasetDAO;
    
    private StatusDAO statusDAO;

    private ModulesDAO modulesDAO;
    
    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private Datasource datasource;
    
    private PooledExecutor threadPool;

    private boolean verboseStatusLogging = true;

    public ModuleRunnerTask(Module[] modules, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            boolean verboseStatusLogging) {
        this(modules, user, dbServerFactory, sessionFactory);
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public ModuleRunnerTask(Module[] modules, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.modules = modules;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.datasource = dbServerFactory.getDbServer().getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.datasetDAO = new DatasetDAO(dbServerFactory);
        this.statusDAO = new StatusDAO(sessionFactory);
        this.modulesDAO = new ModulesDAO();
        this.threadPool = createThreadPool();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3); // terminate after 3 (unused) minutes

        return threadPool;
    }

    public void run() throws EmfException {
        for(Module module : modules) {
            runModule(module);
        }
    }

    public void runModule(Module module) throws EmfException {
        
        prepare("", module);
        
        ModuleTypeVersion moduleTypeVersion = module.getModuleTypeVersion();

        Date start = new Date();
        
        History history = new History();
        history.setModule(module);
        history.setStatus(History.STARTED);
        history.setCreator(user);
        history.setCreationDate(start);

        Map<String, HistoryDataset>   historyDatasets   = new HashMap<String, HistoryDataset>();
        Map<String, HistoryParameter> historyParameters = new HashMap<String, HistoryParameter>();
        
        String errorMessage;
        
        String logMessage = String.format("Module '%s' started by %s on %s",
                                          module.getName(), user.getName(),
                                          CustomDateFormat.format_yyyy_MM_dd_HHmmssSSS(start));
        history.addLogMessage(History.INFO, logMessage);
        
        Session session = sessionFactory.getSession();
        
        module = modulesDAO.obtainLockedModule(user, module, session);
        
        try {
            module.addModuleHistory(history);
            module = modulesDAO.update(module, session);
            
            String algorithm = moduleTypeVersion.getAlgorithm();
            history.setUserScript(algorithm);
            
            // create output datasets
            // replace all dataset place-holders in the algorithm
            for(ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
                if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                    if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                        // create output dataset
                        DatasetType datasetType = moduleTypeVersionDataset.getDatasetType();
                        SqlDataTypes types = dbServerFactory.getDbServer().getSqlDataTypes();
                        VersionedTableFormat versionedTableFormat = new VersionedTableFormat(datasetType.getFileFormat(), types);
                        String description = "New dataset created by the '" + module.getName() + "' module for the '" + moduleTypeVersionDataset.getPlaceholderName() + "' placeholder.";
                        DatasetCreator datasetCreator = new DatasetCreator(moduleDataset, user, sessionFactory, dbServerFactory, datasource);
                        EmfDataset dataset = datasetCreator.addDataset("mod", moduleDataset.getDatasetNamePattern(), datasetType, versionedTableFormat, description);
                        
                        InternalSource[] internalSources = dataset.getInternalSources();
                        if (internalSources.length != 1) {
                            errorMessage = String.format("Internal error: new dataset '%s' was created with %d internal sources (expected one).",
                                                         moduleDataset.getDatasetNamePattern(), internalSources.length);
                            throw new EmfException(errorMessage);
                        }
                        
                        logMessage = String.format("Created %s %s dataset for '%s' placeholder:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: 0",
                                                   ModuleDataset.NEW, ModuleTypeVersionDataset.OUT, moduleDataset.getPlaceholderName(),
                                                   internalSources[0].getType(), dataset.getName(), internalSources[0].getTable());
                        history.addLogMessage(History.INFO, logMessage);
                        
                        HistoryDataset historyDataset = new HistoryDataset();
                        historyDataset.setPlaceholderName(moduleDataset.getPlaceholderName());
                        historyDataset.setDatasetId(dataset.getId());
                        historyDataset.setVersion(0);
                        historyDataset.setHistory(history);
                        historyDatasets.put(moduleDataset.getPlaceholderName(), historyDataset);
                        
                        algorithm = replacePlaceholders(algorithm, moduleDataset, dataset, 0);
                    } else { // REPLACE
                        throw new EmfException("Not implemented yet."); // TODO
                    }
                } else { // IN or INOUT
                    EmfDataset dataset = datasetDAO.getDataset(sessionFactory.getSession(), moduleDataset.getDatasetId());
                    
                    InternalSource[] internalSources = dataset.getInternalSources();
                    if (internalSources.length != 1) {
                        errorMessage = String.format("Internal error: dataset '%s' has %d internal sources (expected one).",
                                                     dataset.getName(), internalSources.length);
                        throw new EmfException(errorMessage);
                    }
                    
                    logMessage = String.format("Using %s dataset for '%s' placeholder:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: %d",
                                               moduleTypeVersionDataset.getMode(), moduleDataset.getPlaceholderName(),
                                               internalSources[0].getType(), dataset.getName(), internalSources[0].getTable(), moduleDataset.getVersion());
                    history.addLogMessage(History.INFO, logMessage);
                    
                    HistoryDataset historyDataset = new HistoryDataset();
                    historyDataset.setPlaceholderName(moduleDataset.getPlaceholderName());
                    historyDataset.setDatasetId(dataset.getId());
                    historyDataset.setVersion(moduleDataset.getVersion());
                    historyDataset.setHistory(history);
                    historyDatasets.put(moduleDataset.getPlaceholderName(), historyDataset);
                    
                    algorithm = replacePlaceholders(algorithm, moduleDataset, dataset, moduleDataset.getVersion());
                }
            }
            history.setHistoryDatasets(historyDatasets);
            
            // replace global place-holders in the algorithm
            String startPattern = "\\$\\{\\s*";
            String separatorPattern = "\\s*\\.\\s*";
            String endPattern = "\\s*\\}";

            // Important: keep the list of valid placeholders in sync with
            //            gov.epa.emissions.framework.services.module.ModuleTypeVersion
            
            algorithm = Pattern.compile(startPattern + "user" + separatorPattern + "full_name" + endPattern, Pattern.CASE_INSENSITIVE)
                               .matcher(algorithm).replaceAll(user.getName());
    
            algorithm = Pattern.compile(startPattern + "user" + separatorPattern + "id" + endPattern, Pattern.CASE_INSENSITIVE)
                               .matcher(algorithm).replaceAll(user.getId() + "");
    
            algorithm = Pattern.compile(startPattern + "user" + separatorPattern + "account_name" + endPattern, Pattern.CASE_INSENSITIVE)
                               .matcher(algorithm).replaceAll(user.getUsername());
    
            algorithm = Pattern.compile(startPattern + "module" + separatorPattern + "name" + endPattern, Pattern.CASE_INSENSITIVE)
                               .matcher(algorithm).replaceAll(module.getName());
    
            algorithm = Pattern.compile(startPattern + "module" + separatorPattern + "id" + endPattern, Pattern.CASE_INSENSITIVE)
                               .matcher(algorithm).replaceAll(module.getId() + "");
    
            history.setUserScript(algorithm);

            // verify that the algorithm doesn't have any place-holders left
            Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(algorithm);
            if (matcher.find()) {
                int pos = matcher.start();
                String match = matcher.group();
                String message = String.format("Unrecognized placeholder %s at location %d.", match, start);
                throw new EmfException(message); 
            }
            
            // create outer block
            // declare all parameters, initialize IN and INOUT parameters
            String parameterDeclarations = "";
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                HistoryParameter historyParameter;
                ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
                if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    parameterDeclarations += "    " + moduleParameter.getParameterName() + " " + moduleTypeVersionParameter.getSqlParameterType() + ";\n";
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                } else { // IN or INOUT
                    parameterDeclarations += "    " + moduleParameter.getParameterName() + " " + moduleTypeVersionParameter.getSqlParameterType() + " := " + moduleParameter.getValue() + ";\n";
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), moduleParameter.getValue());
                }
                historyParameters.put(moduleParameter.getParameterName(), historyParameter);
            }
            history.setHistoryParameters(historyParameters);
            
            // return the values of all INOUT and OUT parameters as a result set
            String outputParameters = "";
            for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
                ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
                if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.INOUT) ||
                    moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    if (!outputParameters.isEmpty()) {
                        outputParameters += "UNION ALL\n";
                    }
                    outputParameters += "SELECT '" + moduleTypeVersionParameter.getParameterName() + "' AS name, CAST(" + moduleTypeVersionParameter.getParameterName() + " AS text) AS value";
                }
            }
            
            String userTimeStamp = user.getUsername() + "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());

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
                algorithm = "\nDO $" + userTag + "$\nBEGIN\n" + algorithm + "\nEND $" + userTag + "$;\n";
            } else {
                algorithm = "\nDO $" + userTag + "$\nDECLARE\n" + parameterDeclarations + "BEGIN\n" + algorithm + outputParameters + "\nEND $" + userTag + "$;\n" + selectOutputParameters;
            }
    
            // execute algorithm
            Statement statement = null;
            try {
                history.setUserScript(algorithm);
                history.setStatus(History.USER_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting user script (algorithm).");
                
                module = modulesDAO.update(module, session);
                
                statement = dbServerFactory.getDbServer().getConnection().createStatement();
                statement.execute(algorithm);
                // get the values of all INOUT and OUT parameters
                while(statement.getMoreResults()) {
                    ResultSet resultSet = statement.getResultSet();
                    while(resultSet.next()) {
                        String name = resultSet.getString(1);
                        String value = resultSet.getString(2);
                        historyParameters.get(name).setValue(value);
                    }
                }
            } catch (SQLException e) {
                // e.printStackTrace();
                // TODO save error to the current execution history record
                throw new EmfException("Failed to execute algorithm: " + e.getMessage());
            } catch (Exception e) {
                // e.printStackTrace();
                // TODO save error to the current execution history record
                throw new EmfException("Failed to execute algorithm: " + e.getMessage());
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                }
            }
            history.setStatus(History.COMPLETED);
            history.setResult(History.SUCCESS);
            
            history.addLogMessage(History.INFO, "User script completed successfully.");
            
            module = modulesDAO.update(module, session);
            complete(history.getResult(), module);
            
        } catch (EmfException e) {
            
            history.setStatus(History.COMPLETED);
            history.setResult(History.FAILED);
            history.setErrorMessage(e.getMessage());
            
            errorMessage = "User script failed:\n\n" + e.getMessage() + "\n\n" + getLineNumberedScript(history.getUserScript()) + "\n";
            
            history.addLogMessage(History.ERROR, errorMessage);
            
            module = modulesDAO.update(module, session);
            complete(history.getResult() + ": " + errorMessage, module);
        } finally {
            module = modulesDAO.releaseLockedModule(user, module, session);
        }
    }

    private String getLineNumberedScript(String script) {
        if (script == null)
            return "";
        
        StringBuilder lineNumberedScript = new StringBuilder();
        int charNumber = 1;
        int lineNumber = 1;
        String[] lines = script.split("\n");
        for(String line : lines) {
            if (lineNumber == 1)
                lineNumberedScript.append("Line  Char  Text\n");
            lineNumberedScript.append(String.format("%4d %5d  %s\n", lineNumber, charNumber, line));
            lineNumber++;
            charNumber += line.length() + 1;
        }
        return lineNumberedScript.toString();
    }
    
    private String replacePlaceholders(String algorithm, ModuleDataset moduleDataset, EmfDataset dataset, int version) throws EmfException {
        String result = algorithm;
        ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
        String separatorPattern = "\\s*\\.\\s*";
        String startPattern = "\\$\\{\\s*" + moduleDataset.getPlaceholderName() + separatorPattern;
        String endPattern = "\\s*\\}";

        InternalSource[] internalSources = dataset.getInternalSources();
        if (internalSources.length == 0) {
            throw new EmfException("Can't handle datasets with no internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + dataset.getName() + "').");
        } else if (internalSources.length > 1) {
            throw new EmfException("Can't handle datasets with multiple internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + dataset.getName() + "').");
        }

        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleTypeVersion
        
        result = Pattern.compile(startPattern + "dataset_name" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(dataset.getName());

        result = Pattern.compile(startPattern + "dataset_id" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(dataset.getId() + "");

        result = Pattern.compile(startPattern + "version" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(version + "");

        result = Pattern.compile(startPattern + "table_name" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll("emissions." + internalSources[0].getTable()); // FIXME hard-coded schema name

        result = Pattern.compile(startPattern + "mode" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(moduleTypeVersionDataset.getMode());

        if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
            result = Pattern.compile(startPattern + "output_method" + endPattern, Pattern.CASE_INSENSITIVE)
                            .matcher(result).replaceAll(moduleDataset.getOutputMethod());
        }

        return result;
    }

    private void close(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not close database connection." + e.getMessage());
        }
    }

    private void prepare(String suffixMsg, Module module) {
        if (verboseStatusLogging)
            setStatus("Started running module '" + module.getName() + "'." + suffixMsg);
    }

    private void complete(String suffixMsg, Module module) {
        if (verboseStatusLogging)
            setStatus("Completed running module '" + module.getName() + "'." + suffixMsg);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Module Runner");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
}
