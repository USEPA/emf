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
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.InfrastructureException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.SecureRandom;
import java.math.BigInteger;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.hibernate.Session;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

// import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ModuleRunnerTask {

    private Module[] modules;

    private User user;

    private DatasetDAO datasetDAO;
    
    private StatusDAO statusDAO;

    private ModulesDAO modulesDAO;
    
    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private Datasource datasource;
    
    // private PooledExecutor threadPool;

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
        this.dbServerFactory = dbServerFactory;
        this.datasource = dbServerFactory.getDbServer().getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.datasetDAO = new DatasetDAO(dbServerFactory);
        this.statusDAO = new StatusDAO(sessionFactory);
        this.modulesDAO = new ModulesDAO();
        // this.threadPool = createThreadPool();
        UserDAO userDAO = new UserDAO();
        this.user = userDAO.get(user.getId(), sessionFactory.getSession());
    }

//    private synchronized PooledExecutor createThreadPool() {
//        PooledExecutor threadPool = new PooledExecutor(20);
//        threadPool.setMinimumPoolSize(1);
//        threadPool.setKeepAliveTime(1000 * 60 * 3); // terminate after 3 (unused) minutes
//
//        return threadPool;
//    }

    public void run() throws EmfException {
        for(Module module : modules) {
            runModule(module);
        }
    }

    private String getVersionWhereFilter(Connection connection, int datasetId, int version, String table_alias) throws EmfException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String query = String.format("SELECT public.build_version_where_filter(%d, %d, '%s')", datasetId, version, table_alias);
            if (statement.execute(query)) {
                // get the return value
                ResultSet resultSet = statement.getResultSet();
                while(resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            throw new EmfException(String.format("Failed to get the version where filter for dataset_id %d version %d: %s", datasetId, version, e.getMessage()));
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
        throw new EmfException(String.format("Failed to get the version where filter for dataset_id %d version %d", datasetId, version));
    }
    
    private void createView(final StringBuilder viewName, final StringBuilder viewDefinition,
                            Connection connection, ModuleDataset moduleDataset, EmfDataset emfDataset, int version) throws EmfException {
        viewName.setLength(0);
        viewDefinition.setLength(0);
        
        ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
        String mode = moduleTypeVersionDataset.getMode().toLowerCase();
        
        viewName.append(moduleDataset.getPlaceholderName() + "_" + mode + "_view");
        
        int datasetId = emfDataset.getId();
        
        InternalSource[] internalSources = emfDataset.getInternalSources();
        if (internalSources.length == 0) {
            throw new EmfException("Can't handle datasets with no internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + emfDataset.getName() + "').");
        } else if (internalSources.length > 1) {
            throw new EmfException("Can't handle datasets with multiple internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + emfDataset.getName() + "').");
        }
        String schema = EmfDbServer.EMF_EMISSIONS_SCHEMA; // FIXME hard-coded schema name
        String tableName = internalSources[0].getTable();

        StringBuilder colNames = new StringBuilder();
        StringBuilder newColNames = new StringBuilder();
        String datasetIdColName = "dataset_id";
        String versionColName = "version";
        for(String col : internalSources[0].getCols()) {
            if (col.toLowerCase().equals("record_id"))
                continue;
            if (col.toLowerCase().equals("dataset_id")) {
                datasetIdColName = col;
                continue;
            }
            if (col.toLowerCase().equals("version")) {
                versionColName = col;
                continue;
            }
            if (col.toLowerCase().equals("delete_versions"))
                continue;
            if (colNames.length() > 0)
                colNames.append(", ");
            colNames.append(col);
            if (newColNames.length() > 0)
                newColNames.append(", ");
            newColNames.append("NEW." + col);
        }
        
        String versionWhereFilter = getVersionWhereFilter(connection, datasetId, version, "ds");
        
        viewDefinition.append(String.format("    CREATE TEMP VIEW %s (%s) AS\n" +
                                            "       SELECT %s\n" +
                                            "       FROM %s.%s ds\n" +
                                            "       WHERE %s;\n\n",
                                            viewName.toString(), colNames.toString(),
                                            colNames.toString(),
                                            schema, tableName,
                                            versionWhereFilter));
        
        if (!mode.equals(ModuleTypeVersionDataset.IN.toLowerCase())) {
            viewDefinition.append(String.format("    CREATE RULE %s_insert AS ON INSERT TO %s\n" +
                                                "    DO INSTEAD\n" +
                                                "    INSERT INTO %s.%s\n" +
                                                "        (%s, %s, %s)\n" +
                                                "    VALUES\n" +
                                                "        (%d, %d, %s)\n" +
                                                "    RETURNING\n" +
                                                "        %s;\n\n",
                                                viewName.toString(), viewName.toString(),
                                                schema, tableName,
                                                datasetIdColName, versionColName, colNames.toString(),
                                                datasetId, version, newColNames.toString(),
                                                colNames.toString()));
        }
    }

    private String getSetupScript(String tempUserName, String tempUserPassword, List<String> outputDatasetTables) {
        String setupScript =
            // TODO use encrypted password
            "CREATE USER ${temp_user} WITH CONNECTION LIMIT 1 UNENCRYPTED PASSWORD '${temp_password}';\n\n" +
                    
            "GRANT CONNECT, TEMPORARY ON DATABASE \"EMF\" TO ${temp_user};\n\n" +

            "REVOKE CREATE ON SCHEMA public FROM ${temp_user};\n\n" +

            "GRANT USAGE ON SCHEMA cases, emf, emissions, fast, modules, reference, sms TO ${temp_user};\n" +
            "GRANT SELECT, REFERENCES ON ALL TABLES IN SCHEMA cases, emf, emissions, fast, modules, reference, sms TO ${temp_user};\n" +
            "GRANT USAGE ON ALL SEQUENCES IN SCHEMA cases, emf, emissions, fast, modules, reference, sms TO ${temp_user};\n" +
            "GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA cases, emf, emissions, fast, modules, reference, sms TO ${temp_user};\n\n";

            // TODO grant USAGE permissions for all procedural languages, domains, and types

        for(String outputDatasetTable : outputDatasetTables)
            setupScript += "GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON TABLE " + outputDatasetTable + " TO ${temp_user};\n";
        if (!outputDatasetTables.isEmpty())
            setupScript += "\n";
        
        // create new temporary schema with the same name as the temporary user
        // the default search path will be the new temporary schema followed by public
        setupScript += "CREATE SCHEMA AUTHORIZATION ${temp_user};\n";
        
        setupScript = Pattern.compile("\\$\\{temp_user\\}", Pattern.CASE_INSENSITIVE)
                             .matcher(setupScript).replaceAll(tempUserName);

        setupScript = Pattern.compile("\\$\\{temp_password\\}", Pattern.CASE_INSENSITIVE)
                             .matcher(setupScript).replaceAll(tempUserPassword);

        return setupScript;
    }
    
    private String getTeardownScript(String tempUserName) {
        String teardownScript =
            "DROP OWNED BY ${temp_user} CASCADE;\n" +
            "DROP USER ${temp_user};\n";

        return Pattern.compile("\\$\\{temp_user\\}", Pattern.CASE_INSENSITIVE)
                      .matcher(teardownScript).replaceAll(tempUserName);
    }

    private Connection getUserConnection(String username, String password) throws InfrastructureException, SQLException { 
        BasicDataSource basicDataSource = (BasicDataSource)new DataSourceFactory().get();
        String jdbcDriverClassName = basicDataSource.getDriverClassName();
        String jdbcURL = basicDataSource.getUrl();

//        Properties connectionProperties = new Properties();
//        connectionProperties.setProperty("ApplicationName", "Module Runner");
//        connectionProperties.setProperty("logUnclosedConnections", "true");
//        connectionProperties.setProperty("loglevel", "2"); // org.postgresql.Driver.DEBUG (2)
        
        DriverManagerDataSource ds = new DriverManagerDataSource();
//        ds.setConnectionProperties(connectionProperties);
        ds.setDriverClassName(jdbcDriverClassName); 
        ds.setUsername(username); 
        ds.setPassword(password);
        ds.setUrl(jdbcURL + "&ApplicationName=Module%20Runner&logUnclosedConnections=true&loglevel=2");
        
        return ds.getConnection();
    }
    
    private void runModule(Module module) throws EmfException {
        
        prepare("", module);

        String finalStatusMessage = "";
        
        final String    SETUP_SCRIPT_ERROR = "Failed to execute setup script.\n";
        final String     USER_SCRIPT_ERROR = "Failed to execute user script (algorithm).\n";
        final String TEARDOWN_SCRIPT_ERROR = "Failed to execute teardown script.\n";
        
        ModuleTypeVersion moduleTypeVersion = module.getModuleTypeVersion();

        Date start = new Date();
        String timeStamp = CustomDateFormat.format_HHMMSSSS(start);
        String userTimeStamp = user.getUsername() + "_" + timeStamp;

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

        DbServer dbServer = null;
        Connection connection = null;
        Statement statement = null;
        
        try {
            dbServer = dbServerFactory.getDbServer();
            connection = dbServer.getConnection();
            
            module = modulesDAO.obtainLockedModule(user, module, session);
            if (!module.isLocked(user)) {
                throw new EmfException("Failed to lock module " + module.getName());
            }
            
            module.addModuleHistory(history);
            module = modulesDAO.update(module, session);
            
            String algorithm = moduleTypeVersion.getAlgorithm();
            history.setUserScript(algorithm);
            
            String datasetTablesSchema = EmfDbServer.EMF_EMISSIONS_SCHEMA; // FIXME hard-coded schema name
            
            StringBuilder viewDefinitions = new StringBuilder();
            
            List<String> outputDatasetTables = new ArrayList<String>();
            
            // create output datasets
            // create views for all datasets
            // replace all dataset placeholders in the algorithm
            for(ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
                if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                    if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                        DatasetType datasetType = moduleTypeVersionDataset.getDatasetType();
                        SqlDataTypes types = dbServer.getSqlDataTypes();
                        VersionedTableFormat versionedTableFormat = new VersionedTableFormat(datasetType.getFileFormat(), types);
                        String description = "New dataset created by the '" + module.getName() + "' module for the '" + moduleTypeVersionDataset.getPlaceholderName() + "' placeholder.";
                        DatasetCreator datasetCreator = new DatasetCreator(moduleDataset, user, sessionFactory, dbServerFactory, datasource);
                        String newDatasetName = getNewDatasetName(moduleDataset, start, history);
                        EmfDataset dataset = datasetCreator.addDataset("mod", newDatasetName, datasetType, versionedTableFormat, description);
                        
                        InternalSource[] internalSources = dataset.getInternalSources();
                        if (internalSources.length != 1) {
                            errorMessage = String.format("Internal error: new dataset '%s' was created with %d internal sources (expected one).",
                                                         newDatasetName, internalSources.length);
                            throw new EmfException(errorMessage);
                        }
                        
                        int version = 0;
                        
                        logMessage = String.format("Created %s %s dataset for '%s' placeholder:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: %d",
                                                   moduleDataset.getOutputMethod(), moduleTypeVersionDataset.getMode(), moduleDataset.getPlaceholderName(),
                                                   internalSources[0].getType(), dataset.getName(), internalSources[0].getTable(), version);
                        history.addLogMessage(History.INFO, logMessage);

                        outputDatasetTables.add(datasetTablesSchema + "." + internalSources[0].getTable());
                        
                        StringBuilder viewName = new StringBuilder();
                        StringBuilder viewDefinition = new StringBuilder();
                        createView(viewName, viewDefinition, connection, moduleDataset, dataset, version);
                        viewDefinitions.append(viewDefinition);
                       
                        HistoryDataset historyDataset = new HistoryDataset();
                        historyDataset.setPlaceholderName(moduleDataset.getPlaceholderName());
                        historyDataset.setDatasetId(dataset.getId());
                        historyDataset.setVersion(version);
                        historyDataset.setHistory(history);
                        historyDatasets.put(moduleDataset.getPlaceholderName(), historyDataset);
                        
                        algorithm = replaceDatasetPlaceholders(algorithm, moduleDataset, dataset, version, viewName.toString());
                    } else { // REPLACE
                        EmfDataset dataset = datasetDAO.getDataset(sessionFactory.getSession(), moduleDataset.getDatasetId());
                        if (dataset == null) {
                            errorMessage = String.format("Failed to find dataset with ID %d for placeholder '%s'.",
                                                         moduleDataset.getDatasetId(), moduleDataset.getPlaceholderName());
                            throw new EmfException(errorMessage);
                        }
                        String datasetName = dataset.getName();
                        
                        if (!dataset.isLocked()) {
                            dataset = datasetDAO.obtainLocked(user, dataset, session);
                        } else if (!dataset.isLocked(user)) {
                            errorMessage = String.format("Could not replace dataset '%s' for placeholder '%s'. The dataset is locked by %s.",
                                                          datasetName, moduleDataset.getPlaceholderName(), dataset.getLockOwner());
                            throw new EmfException(errorMessage);
                        }
                        
                        DatasetCreator datasetCreator = new DatasetCreator(moduleDataset, user, sessionFactory, dbServerFactory, datasource);
                        datasetCreator.replaceDataset(session, connection, dataset);
                        
                        InternalSource[] internalSources = dataset.getInternalSources();
                        if (internalSources.length != 1) {
                            errorMessage = String.format("Internal error: dataset '%s' has %d internal sources (expected one).",
                                                         datasetName, internalSources.length);
                            throw new EmfException(errorMessage);
                        }
                        
                        int version = 0;
                        
                        logMessage = String.format("Replacing %s dataset for '%s' placeholder:\n  * dataset type: '%s'\n  * dataset name: '%s'\n  * table name: '%s'\n  * version: %d",
                                                   moduleDataset.getPlaceholderName(), moduleTypeVersionDataset.getMode(),
                                                   internalSources[0].getType(), dataset.getName(), internalSources[0].getTable(), version);
                        history.addLogMessage(History.INFO, logMessage);
                        
                        outputDatasetTables.add(datasetTablesSchema + "." + internalSources[0].getTable());
                        
                        StringBuilder viewName = new StringBuilder();
                        StringBuilder viewDefinition = new StringBuilder();
                        createView(viewName, viewDefinition, connection, moduleDataset, dataset, version);
                        viewDefinitions.append(viewDefinition);
                       
                        HistoryDataset historyDataset = new HistoryDataset();
                        historyDataset.setPlaceholderName(moduleDataset.getPlaceholderName());
                        historyDataset.setDatasetId(dataset.getId());
                        historyDataset.setVersion(version);
                        historyDataset.setHistory(history);
                        historyDatasets.put(moduleDataset.getPlaceholderName(), historyDataset);
                        
                        algorithm = replaceDatasetPlaceholders(algorithm, moduleDataset, dataset, version, viewName.toString());
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
                    
                    if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.INOUT)) {
                        outputDatasetTables.add(datasetTablesSchema + "." + internalSources[0].getTable());
                    }
                    
                    StringBuilder viewName = new StringBuilder();
                    StringBuilder viewDefinition = new StringBuilder();
                    createView(viewName, viewDefinition, connection, moduleDataset, dataset, moduleDataset.getVersion());
                    viewDefinitions.append(viewDefinition);
                   
                    HistoryDataset historyDataset = new HistoryDataset();
                    historyDataset.setPlaceholderName(moduleDataset.getPlaceholderName());
                    historyDataset.setDatasetId(dataset.getId());
                    historyDataset.setVersion(moduleDataset.getVersion());
                    historyDataset.setHistory(history);
                    historyDatasets.put(moduleDataset.getPlaceholderName(), historyDataset);
                    
                    algorithm = replaceDatasetPlaceholders(algorithm, moduleDataset, dataset, moduleDataset.getVersion(), viewName.toString());
                }
            }
            history.setHistoryDatasets(historyDatasets);
            
            // replace global placeholders in the algorithm
            algorithm = replaceGlobalPlaceholders(algorithm, module, start, history);
            
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
                if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                    parameterDeclarations += "    " + parameterTimeStamp + " " + moduleTypeVersionParameter.getSqlParameterType() + ";\n";
                    historyParameter = new HistoryParameter(history, moduleParameter.getParameterName(), null);
                } else { // IN or INOUT
                    parameterDeclarations += "    " + parameterTimeStamp + " " + moduleTypeVersionParameter.getSqlParameterType() + " := " + moduleParameter.getValue() + ";\n";
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
                    outputParameters += "SELECT '" + moduleTypeVersionParameter.getParameterName() + "' AS name, CAST(" + parameterTimeStamp + " AS text) AS value";
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
            
            // Generate random password by choosing 30 * 5 = 150 bits from a cryptographically
            // secure random bit generator and encoding them in base-32.
            // 128 bits is considered to be cryptographically strong.
            // Each digit in a base 32 number can encode 5 bits, so 150 bits results in 30 characters.
            // This encoding is compact and efficient, with 5 random bits per character.
            String tempUserPassword = new BigInteger(32 * 5, new SecureRandom()).toString(32);

            String setupScript = getSetupScript(userTimeStamp, tempUserPassword, outputDatasetTables);
            
            try {
                history.setSetupScript(setupScript);
                history.setStatus(History.SETUP_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting setup script.");
                
                module = modulesDAO.update(module, session);

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
            
            // TODO create new connection and login as the temporary user

            Connection userConnection = null; 
            try {
                history.setUserScript(algorithm);
                history.setStatus(History.USER_SCRIPT);
                
                history.addLogMessage(History.INFO, "Starting user script (algorithm).");
                
                module = modulesDAO.update(module, session);
                
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
                        historyParameters.get(name).setValue(value);
                    }
                }
                
                // TODO update the record counts for the output datasets
                
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

                // execute teardown script

                String teardownScript = getTeardownScript(userTimeStamp);

                try {
                    history.setTeardownScript(teardownScript);
                    history.setStatus(History.TEARDOWN_SCRIPT);
                    
                    history.addLogMessage(History.INFO, "Starting teardown script.");
                    
                    module = modulesDAO.update(module, session);
                    
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
            
            history.setStatus(History.COMPLETED);
            history.setResult(History.SUCCESS);
            
            finalStatusMessage = "Completed running module '" + module.getName() + "': " + history.getResult();
            
            history.addLogMessage(History.SUCCESS, finalStatusMessage);
            
            module = modulesDAO.update(module, session);
            
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
            
            finalStatusMessage = "Completed running module '" + module.getName() + "': " + history.getResult() + "\n\n" + errorMessage;
            
            history.addLogMessage(History.ERROR, finalStatusMessage);
            
            module = modulesDAO.update(module, session);
            
        } finally {
            try {
                module = modulesDAO.releaseLockedModule(user, module, session);
            } catch (Exception e) {
                // ignore
            }
            
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // ignore
                }
                statement = null;
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                connection = null;
            }
            
            close(dbServer);
            
            session.close();
        }

        complete(finalStatusMessage);
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
    
    private String getNewDatasetName(ModuleDataset moduleDataset, Date date, History history) throws EmfException {
        String datasetName = moduleDataset.getDatasetNamePattern();
        datasetName = replaceGlobalPlaceholders(datasetName, moduleDataset.getModule(), date, history);
        assertNoPlaceholders(datasetName, "$");
        return datasetName;
    }
    
    private String replaceGlobalPlaceholders(String text, Module module, Date date, History history) {
        // replace global placeholders in the algorithm
        String startPattern = "\\$\\{\\s*";
        String separatorPattern = "\\s*\\.\\s*";
        String endPattern = "\\s*\\}";

        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleTypeVersion

        text = Pattern.compile(startPattern + "user" + separatorPattern + "full_name" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(user.getName());

        text = Pattern.compile(startPattern + "user" + separatorPattern + "id" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(user.getId() + "");

        text = Pattern.compile(startPattern + "user" + separatorPattern + "account_name" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(user.getUsername());

        text = Pattern.compile(startPattern + "module" + separatorPattern + "name" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(module.getName());

        text = Pattern.compile(startPattern + "module" + separatorPattern + "id" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(module.getId() + "");

        text = Pattern.compile(startPattern + "module" + separatorPattern + "final" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(module.getIsFinal() ? "Final" : "");
  
        text = Pattern.compile(startPattern + "run" + separatorPattern + "id" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(history.getRunId() + "");

        text = Pattern.compile(startPattern + "run" + separatorPattern + "date" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(CustomDateFormat.format_MM_DD_YYYY(date));

        text = Pattern.compile(startPattern + "run" + separatorPattern + "time" + endPattern, Pattern.CASE_INSENSITIVE)
                      .matcher(text).replaceAll(CustomDateFormat.format_HHmmssSSS(date));

        return text;
    }

    private String replaceDatasetPlaceholders(String algorithm, ModuleDataset moduleDataset, EmfDataset dataset, int version, String viewName) throws EmfException {
        String result = algorithm;
        ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
        String startPattern = "\\$\\{\\s*" + moduleDataset.getPlaceholderName();
        String separatorPattern = "\\s*\\.\\s*";
        String endPattern = "\\s*\\}";

        InternalSource[] internalSources = dataset.getInternalSources();
        if (internalSources.length == 0) {
            throw new EmfException("Can't handle datasets with no internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + dataset.getName() + "').");
        } else if (internalSources.length > 1) {
            throw new EmfException("Can't handle datasets with multiple internal sources (module '" + moduleDataset.getModule().getName() + "', dataset '" + dataset.getName() + "').");
        }

        String datasetTablesSchema = EmfDbServer.EMF_EMISSIONS_SCHEMA; // FIXME hard-coded schema name
        
        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleTypeVersion

        // new simplified syntax
        result = Pattern.compile(startPattern + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(viewName);

        // old syntax
        result = Pattern.compile(startPattern + separatorPattern + "dataset_name" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(dataset.getName());

        result = Pattern.compile(startPattern + separatorPattern + "dataset_id" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(dataset.getId() + "");

        result = Pattern.compile(startPattern + separatorPattern + "version" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(version + "");

        result = Pattern.compile(startPattern + separatorPattern + "table_name" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(datasetTablesSchema + "." + internalSources[0].getTable());

        result = Pattern.compile(startPattern + separatorPattern + "view" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(viewName);

        result = Pattern.compile(startPattern + separatorPattern + "mode" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(moduleTypeVersionDataset.getMode());

        if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
            result = Pattern.compile(startPattern + separatorPattern + "output_method" + endPattern, Pattern.CASE_INSENSITIVE)
                            .matcher(result).replaceAll(moduleDataset.getOutputMethod());
        }

        return result;
    }

    private String replaceParameterPlaceholders(String algorithm, ModuleParameter moduleParameter, String timeStamp) throws EmfException {
        String result = algorithm;
        ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
        String parameterName = moduleParameter.getParameterName();
        String parameterTimeStamp = parameterName + "_" + timeStamp; 
        String startPattern = "\\#\\{\\s*" + parameterName;
        String separatorPattern = "\\s*\\.\\s*";
        String endPattern = "\\s*\\}";

        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleTypeVersion

        result = Pattern.compile(startPattern + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(parameterTimeStamp);

        result = Pattern.compile(startPattern + separatorPattern + "sql_type" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(moduleTypeVersionParameter.getSqlParameterType());

        result = Pattern.compile(startPattern + separatorPattern + "mode" + endPattern, Pattern.CASE_INSENSITIVE)
                        .matcher(result).replaceAll(moduleTypeVersionParameter.getMode());

        if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionDataset.OUT)) {
            result = Pattern.compile(startPattern + separatorPattern + "input_value" + endPattern, Pattern.CASE_INSENSITIVE)
                            .matcher(result).replaceAll(moduleParameter.getValue());
        }

        return result;
    }

    private void assertNoPlaceholders(String text, String placeholderMark) throws EmfException {
        String startPattern = "\\" + placeholderMark + "\\{\\s*";
        String endPattern = "\\s*\\}";

        Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(text);
        if (matcher.find()) {
            int pos = matcher.start();
            String match = matcher.group();
            int lineNumber = findLineNumber(text, pos);
            String message = String.format("Unrecognized placeholder %s at line %d location %d.", match, pos, lineNumber);
            throw new EmfException(message); 
        }
    }
    
    // returns line number for character position (first line number is 1)
    private int findLineNumber(String text, int characterPosition) {
        String subtext = text.substring(0, characterPosition);
        return subtext.length() - subtext.replace("\n", "").length() + 1;
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

    private void complete(String finalStatusMessage) {
        if (verboseStatusLogging)
            setStatus(finalStatusMessage);
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
