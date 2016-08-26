package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ModuleRunnerTask {

    private Module[] modules;

    private User user;

    private DatasetDAO datasetDAO;
    
    private StatusDAO statusDao;

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
        this.statusDao = new StatusDAO(sessionFactory);
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
        
        ModuleTypeVersion moduleTypeVersion = module.getModuleTypeVersion();
        
        String algorithm = moduleTypeVersion.getAlgorithm();
        
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
                    algorithm = replacePlaceholders(algorithm, moduleDataset, dataset, 0);
                } else { // REPLACE
                    throw new EmfException("Not implemented yet."); // TODO
                }
            } else { // IN or INOUT
                EmfDataset dataset = datasetDAO.getDataset(sessionFactory.getSession(), moduleDataset.getDatasetId());
                algorithm = replacePlaceholders(algorithm, moduleDataset, dataset, moduleDataset.getVersion());
            }
        }

        // replace global place-holders in the algorithm
        String startPattern = "\\$\\{\\s*";
        String separatorPattern = "\\s*\\.\\s*";
        String endPattern = "\\s*\\}";
        
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

        // TODO verify that the algorithm doesn't have any place-holders left

        // create outer block
        // declare all parameters, initialize IN and INOUT parameters
        String parameterDeclarations = "";
        for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
            if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                parameterDeclarations += "    " + moduleTypeVersionParameter.getParameterName() + " " + moduleTypeVersionParameter.getSqlParameterType() + ";\n";
            } else { // IN or INOUT
                parameterDeclarations += "    " + moduleTypeVersionParameter.getParameterName() + " " + moduleTypeVersionParameter.getSqlParameterType() + " := " + moduleParameter.getValue() + ";\n";
            }
        }
        // return the values of all INOUT and OUT parameters as a result set
        String outputParameters = "";
        for(ModuleParameter moduleParameter : module.getModuleParameters().values()) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
            if (moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.INOUT) ||
                moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                if (!outputParameters.isEmpty()) {
                    outputParameters += "\nUNION ALL\n";
                }
                outputParameters += "SELECT '" + moduleTypeVersionParameter.getParameterName() + "' AS name, CAST(" + moduleTypeVersionParameter.getParameterName() + " AS text) AS value";
            }
        }
        if (!outputParameters.isEmpty()) {
            outputParameters = "\n\n-- output parameters result set\n\n" + outputParameters + ";\n";
        }

        if (parameterDeclarations.isEmpty()) {
            algorithm = "\nDO $algorithm$\nBEGIN\n" + algorithm + "\nEND $algorithm$;\n";
        } else {
            algorithm = "\nDO $algorithm$\nDECLARE\n" + parameterDeclarations + "BEGIN\n" + algorithm + "\n\n-- output parameters result set\n\n" + outputParameters + "\nEND $algorithm$;\n";
        }

        // execute algorithm
        Statement statement = null;
        try {
            statement = dbServerFactory.getDbServer().getConnection().createStatement();
            if (statement.execute(algorithm)) {
                // get the values of all INOUT and OUT parameters
                ResultSet resultSet = statement.getResultSet();
                while(resultSet.next()) {
                    String name = resultSet.getString(1);
                    String value = resultSet.getString(2);
                    // TODO save the output parameter values to the current execution history record
                }
            }
        } catch (SQLException e) {
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
            setStatus("Started running module '" + module.getName() + suffixMsg);
    }

    private void complete(String suffixMsg, Module module) {
        if (verboseStatusLogging)
            setStatus("Completed running module '" + module.getName() + suffixMsg);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("ModuleRunner");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }
}
