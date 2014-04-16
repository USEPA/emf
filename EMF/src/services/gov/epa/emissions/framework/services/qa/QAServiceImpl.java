package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class QAServiceImpl implements QAService {

    private static Log LOG = LogFactory.getLog(QAServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private QADAO dao;
    
    private StatusDAO statusDAO; // = new StatusDAO(sessionFactory);

    private PooledExecutor threadPool;

    public QAServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public QAServiceImpl(HibernateSessionFactory sessionFactory) {
        this(sessionFactory, DbServerFactory.get());
    }

    public QAServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.threadPool = createThreadPool();
        dao = new QADAO();
        statusDAO = new StatusDAO(sessionFactory);
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStep[] results = dao.steps(dataset, session);
            return results;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Steps for dataset: " + dataset.getName(), e);
            throw new EmfException("Could not retrieve QA Steps for dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    public synchronized QAStepResult[] getQAStepResults(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult[] results = dao.qaRsults(dataset, session);
            return results;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Step Results for dataset: " + dataset.getName(), e);
            throw new EmfException("Could not retrieve QA Step Results for dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    public synchronized QAProgram[] getQAPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getQAPrograms(session);
        } catch (RuntimeException e) {
            LOG.error("Could not get QA Programs", e);
            throw new EmfException("Could not get QA Programs");
        } finally {
            session.close();
        }
    }

    public synchronized void runQAStep(QAStep step, User user) throws EmfException {
        DbServer dbServer = null;
        
        try {
            writeDBConnectionInfo("BEFORE RUN QASTEP ON DATASETID = " + step.getDatasetId());
            updateResultStatus(step, "In process");
            updateWitoutCheckingConstraints(new QAStep[] { step });
            checkRestrictions(step);
            dbServer = dbServerFactory.getDbServer();
            removeQAResultTable(step, dbServer);
        } catch (Exception e) {
            LOG.error("Error pre-processing QA step for running.", e);
            throw new EmfException(e.getMessage());
        }

        RunQAStep runner = new RunQAStep(new QAStep[] { step }, user, dbServerFactory, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Running QA Steps", runner));
        } catch (Exception e) {
            LOG.error("Error running in qa step-" + step.getName(), e);
            throw new EmfException("Error running in qa step-" + step.getName() + ":" + e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
                writeDBConnectionInfo("AFTER RUN QASTEP ON DATASETID = " + step.getDatasetId());
            } catch (Exception e) {
                LOG.error("Error closing DB server and logging DB connections.", e);
            }
        }

    }

    private void writeDBConnectionInfo(String prefix) throws EmfException {
        String os = System.getProperty("os.name").toUpperCase();

        if (!os.startsWith("WINDOWS")) {
            String logNumDBConnCmd = "ps aux | grep postgres | wc -l";
            InputStream inStream = RemoteCommand.executeLocal(logNumDBConnCmd);

            RemoteCommand.logRemoteStdout("Logged DB connections (" + prefix + ")", inStream);
            return;
        }

        if (os.startsWith("WINDOWS")) {
            String[] cmd = new String[3];
            
            if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
                cmd[0] = "command.com";
            } else {
                cmd[0] = "cmd.exe";
            }

            cmd[1] = "/C";
            cmd[2] = "tasklist /FI \"IMAGENAME eq postgres.exe\"";
            cmd = new String[] { cmd[0], cmd[1], cmd[2] };

            Process p;

            try {
                p = Runtime.getRuntime().exec(cmd);
            } catch (IOException e1) {
                LOG.error("Error logging DB connections.", e1);
                return;
            }

            InputStream instream = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
            int count = 0;

            try {
                while (reader.readLine() != null)
                    count++;
            } catch (Exception e) {
                LOG.error("Error in logging number of DB connections.", e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Error closing inputstream reader on logging DB connections.",e);
                }
            }

            LOG.warn("Logged DB connections (" + prefix + "): " + count);
        }
    }

    private synchronized void removeQAResultTable(QAStep step, DbServer dbServer) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            QAStepResult result = dao.qaStepResult(step, session);

            if (result == null)
                return;

            String table = result.getTable();

            if (table != null && !table.trim().isEmpty()) {
                TableCreator tableCreator = new TableCreator(dbServer.getEmissionsDatasource());

                if (tableCreator.exists(table.trim())) {
                    tableCreator.drop(table.trim());
                }
            }

            dao.removeQAStepResult(result, session);
        } catch (Exception e) {
            LOG.error("Cannot drop result table for QA step: " + step.getName(), e);
            throw new EmfException("Cannot drop result table for QA step: " + step.getName());
        } finally {
            try {
                session.close();

                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException(e.getMessage());
            }
        }
    }

    private synchronized void updateResultStatus(QAStep qaStep, String status) {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult result = dao.qaStepResult(qaStep, session);

            if (result == null)
                return;

            result.setTableCreationStatus(status);
            dao.updateQAStepResult(result, session);
        } finally {
            session.close();
        }
    }

    public synchronized void exportQAStep(QAStep step, User user, String dirName, String fileName, boolean overide, String rowFilter) throws EmfException {
        try {
            ExportQAStep exportQATask = new ExportQAStep(step, dbServerFactory, user, sessionFactory, threadPool, rowFilter);
            exportQATask.export(dirName, fileName, overide);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }
    }

    public synchronized void downloadQAStep(QAStep step, User user, String fileName, boolean overwrite, String rowFilter) throws EmfException {
        try {
            ExportQAStep exportQATask = new ExportQAStep(step, dbServerFactory, user, sessionFactory, threadPool, rowFilter);
            exportQATask.download(fileName, overwrite);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }
    }

    public synchronized void exportShapeFileQAStep(QAStep step, User user, String dirName,
            String fileName, boolean overide, ProjectionShapeFile projectionShapeFile, String rowFilter, PivotConfiguration pivotConfiguration) throws EmfException {
        try {
            ExportShapeFileQAStep exportQATask = new ExportShapeFileQAStep(step, dbServerFactory, user, sessionFactory,
                    threadPool, true);
            exportQATask.export(dirName, fileName, projectionShapeFile, overide, rowFilter, pivotConfiguration);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }
    }

    public synchronized void downloadShapeFileQAStep(QAStep step, User user, 
            String fileName, ProjectionShapeFile projectionShapeFile, 
            String rowFilter, PivotConfiguration pivotConfiguration, 
            boolean overwrite) throws EmfException {
        try {
            ExportShapeFileQAStep exportQATask = new ExportShapeFileQAStep(step, dbServerFactory, user, sessionFactory,
                    threadPool, true);
            exportQATask.download(fileName, projectionShapeFile, rowFilter, pivotConfiguration, overwrite);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }
    }

    private String getProperty(String propertyName) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, session);
            return property.getValue();
        } finally {
            session.close();
        }
    }

    private boolean findColumn(String actualColumnName, String columnNameToFind) {
        if (columnNameToFind.matches("(?i)(\"*)(" + actualColumnName + ")(\"*)"))
            return true;
        return false;
    }

    public boolean isShapefileCapable(QAStepResult stepResult) throws EmfException {
        DbServer dbServer = null;
        boolean hasLatitudeCol = false;
        boolean hasLongitudeCol = false;
        String[] validCountyFields = getProperty(EmfProperty.POSTGIS_COUNTY_FIELDS).split(",");
        String[] validStateFields = getProperty(EmfProperty.POSTGIS_STATE_FIELDS).split(",");
        String[] validLatitudeFields = getProperty(EmfProperty.POSTGIS_LATITUDE_FIELDS).split(",");
        String[] validLongitudeFields = getProperty(EmfProperty.POSTGIS_LONGITUDE_FIELDS).split(",");

        try {
            dbServer = dbServerFactory.getDbServer();
            Column[] columns = dbServer.getEmissionsDatasource().dataModifier().getColumns(stepResult.getTable());

            for (Column column : columns) {
                String colName = column.getName();
                // String colType = column.getColumnTypeName(i);

                for (String fipsField : validCountyFields) {
                    if (findColumn(colName, fipsField)) {
                        return true;
                    }
                }
                for (String fipsStField : validStateFields) {
                    if (findColumn(colName, fipsStField)) {
                        return true;
                    }
                }
                if (!hasLatitudeCol) {
                    for (String latitudeField : validLatitudeFields) {
                        if (findColumn(colName, latitudeField)) {
                            hasLatitudeCol = true;
                            break;
                        }
                    }
                }
                if (!hasLongitudeCol) {
                    for (String longitudeField : validLongitudeFields) {
                        if (findColumn(colName, longitudeField)) {
                            hasLongitudeCol = true;
                            break;
                        }
                    }
                }
                if (hasLatitudeCol && hasLongitudeCol)
                    return true;
            }
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return false;
    }

    private synchronized void checkRestrictions(QAStep step) throws EmfException {
        QAProgram program = step.getProgram();
        if (program == null)
            throw new EmfException("Please specify a runnable QA program before running (e.g., SQL)");
        String runClassName = program.getRunClassName();
        if ((runClassName == null) || (runClassName.trim().length() == 0))
            throw new EmfException("The program " + program.getName() + " cannot currently be run in the EMF");
    }

    //public synchronized void updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
    public synchronized QAStep[] updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
        
        updateIds(steps);
        updateSteps(steps);
        
        return steps;
    }

    private synchronized void updateIds(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.updateQAStepsIds(steps, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set the ids", e);
            throw new EmfException("Could not set the ids");
        } finally {
            session.close();
        }
    }

    private synchronized void updateSteps(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.update(steps, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update QA Steps", e);
            throw new EmfException("Could not update QA Steps");
        } finally {
            session.close();
        }
    }

    public synchronized QAStep update(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (dao.exists(step, session)) {
                throw new EmfException("The selected QA Step name is already in use");
            }
            dao.update(new QAStep[] { step }, session);
            return step;
        } catch (RuntimeException e) {
            LOG.error("Could not update QA Step", e);
            throw new EmfException("Could not update QA Step -" + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    public boolean getSameAsTemplate(QAStep step) throws EmfException{
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getSameAsTemplate(step, dbServer); 
        } catch (RuntimeException e) {
            LOG.error("Error in getting sameAstemplate", e);
            throw new EmfException("Error in getting sameAstemplate");
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized QAStepResult getQAStepResult(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult qaStepResult = dao.qaStepResult(step, session);
            if (qaStepResult != null)
                qaStepResult.setCurrentTable(isCurrentTable(qaStepResult, session));
            return qaStepResult;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Step Result", e);
            throw new EmfException("Could not retrieve QA Step Result");
        } finally {
            session.close();
        }
    }

    private synchronized boolean isCurrentTable(QAStepResult qaStepResult, Session session) {
        Version version = new Versions().get(qaStepResult.getDatasetId(), qaStepResult.getVersion(), session);
        Date versionDate = version.getLastModifiedDate();
        Date date = qaStepResult.getTableCreationDate();
        if (date == null || versionDate == null)
            return false;
        int value = date.compareTo(versionDate);
        if (value >= 0)
            return true;

        return false;

    }

    public synchronized QAProgram addQAProgram(QAProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.addQAProgram(program, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add QA Program " + program.getName() + ". ", e);
            throw new EmfException("Could not add QA Program " + program.getName() + ".");
        } finally {
            session.close();
        }
    }

    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getProjectionShapeFiles(session);
        } catch (RuntimeException e) {
            LOG.error("Could not get Projection Shape Files", e);
            throw new EmfException("Could not get Projection Shape Files");
        } finally {
            session.close();
        }
    }

    public void copyQAStepsToDatasets(User user, QAStep[] steps, int[] datasetIds, boolean replace) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO datasetDAO = new DatasetDAO();
        DbServer dbServer = dbServerFactory.getDbServer();
        // store locked datasets in this array, some could be null, if their locked by someone else
        EmfDataset[] datasets = new EmfDataset[datasetIds.length];
        String datasetNameList = "";
        try {
            // get lock first, if you can't then throw an error
            for (int i = 0; i < datasetIds.length; i++) {
                int datasetId = datasetIds[i];
                // get lock on dataset type so we can update it...
                EmfDataset dataset = datasetDAO.obtainLocked(user, datasetDAO.getDataset(session, datasetId), session);
                if (!dataset.isLocked(user))
                    throw new EmfException("Could not copy QA Steps to " + dataset.getName() + " its locked by "
                            + dataset.getLockOwner() + ".");
                datasets[i] = dataset;
            }
            int i = 0;
            for (EmfDataset dataset : datasets) {
                ++i;
                QAStep[] existingQaSteps = dao.steps(dataset, session);
                boolean exists = false;
                // add qa step to dataset
                for (QAStep step : steps) {
                    QAStep tmpStep = step;
                    if ( i != 0) {
                        tmpStep = new QAStep(step); 
                    }
                    exists = false;
                    // override applicable settings...
                    tmpStep.setDatasetId(dataset.getId());
                    tmpStep.setWho("");
                    tmpStep.setDate(null);
                    tmpStep.setStatus("Not Started");
                    // check if one with the same name already exists
                    for (QAStep existingQAStep : existingQaSteps) {
                        if (existingQAStep.getName().equals(step.getName())) {
                            exists = true;
                            // if replacing, then remove existing template
                            if (replace) {
                                removeQAResultTable(existingQAStep, dbServer);
                                dao.removeQAStep(existingQAStep, session);
                            }
                        }
                    }
                    // if not replacing, then add "Copy of " in front of the name,
                    // also make sure the new "Copy of " + name is not already used.
                    if (exists && !replace) {
                        String newName = "Copy of " + tmpStep.getName();
                        // check if one with the same name already exists
                        for (QAStep existingQAStep : existingQaSteps) {
                            if (existingQAStep.getName().equals(newName)) {
                                newName = "Copy of " + newName;
                            }
                        }
                        tmpStep.setName(newName);
                    }
                    dao.add(new QAStep[] { tmpStep }, session);
                }
                datasetNameList += (i > 1 ? ", " : "") + dataset.getName();
            }
            Status endStatus = new Status();
            endStatus.setUsername(user.getUsername());
            endStatus.setType("CopyQAStep");
            endStatus.setMessage("Copied " + steps.length + " QA Steps to Datasets: " + datasetNameList + ".");
            endStatus.setTimestamp(new Date());

            new StatusDAO(sessionFactory).add(endStatus);

        } catch (RuntimeException e) {
            LOG.error("Could not copy QA Steps to Datasets.", e);
            throw new EmfException("Could not copy QA Steps to Datasets. " + e.getMessage());
        } finally {
            // release lock
            for (EmfDataset dataset : datasets) {
                // release lock on datasets
                if (dataset != null)
                    datasetDAO.releaseLocked(user, dataset, session);
            }
            session.close();
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized void deleteQASteps(User user, QAStep[] steps, int datasetId) throws EmfException { //BUG3615
        
        try {
            DeleteQASteps task = new DeleteQASteps(steps, datasetId, dbServerFactory, user, sessionFactory, threadPool);
            task.delete();
        } catch (Exception e) {
            LOG.error("Could not delete QA steps", e);
            throw new EmfException("Could not delete QA steps: " + e.getMessage());
        }
        
        /*
        StatusDAO statusDAO = new StatusDAO(sessionFactory);
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("DeleteQASteps");
        status.setMessage("Start to delete QA Steps.");
        status.setTimestamp(new Date());
        statusDAO.add(status);
        
        Session session = sessionFactory.getSession();
        DatasetDAO datasetDAO = new DatasetDAO();
        EmfDataset dataset = datasetDAO.obtainLocked(user, datasetDAO.getDataset(session, datasetId), session);
        
        DbServer dbServer = dbServerFactory.getDbServer();
        Datasource emfDatasource = dbServer.getEmfDatasource();
        List<QAStep> stepsToBeDeleted = new ArrayList<QAStep>();
        int total = 0;
        String stepsReferenced = "";
        int numStepsReferenced = 0;
        String stepsFailedDependencyCheck = "";
        int numStepsFailedDependencyCheck = 0;
        String sql = "";
        for ( QAStep step : steps) {
            total++;
            sql = "select s.id, s.dataset_id, s.name from emf.qa_steps s where ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP[[.[.]](\\s*)\\\"CURRENT_DATASET\\\"(\\s*),(\\s*)\""; 
            sql += step.getName();
            sql += "\\\"(\\s*)[[.].]](.*)' and s.dataset_id = ";
            sql += step.getDatasetId() + ") ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP[[.[.]](\\s*)\\\"";
            sql += dataset.getName();
            sql += "\\\"(\\s*),(\\s*)\""; 
            sql += step.getName();
            sql += "\\\"(\\s*)[[.].]](.*)') ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP_VERSION[[.[.]](\\s*)\\\"CURRENT_DATASET\\\"(\\s*),(\\s*)\""; 
            sql += step.getName();
            sql += "\\\"(\\s*),(\\s*)([0-9]+)[[.].]](.*)' and s.dataset_id = ";
            sql += step.getDatasetId() + ") ";
            sql += " or ";
            sql += "(s.program_arguments ~* '(.*[[.$.]])DATASET_QASTEP[[.[.]](\\s*)\\\"";
            sql += dataset.getName();
            sql += "\\\"(\\s*),(\\s*)\"";
            sql += step.getName();
            sql += "\\\"(\\s*),(\\s*)([0-9]+)[[.].]](.*)') ";
            sql += ";";

            try {
                ResultSet rs = emfDatasource.query().executeQuery(sql);

                if ( rs.next()) { // not empty
                    stepsReferenced += "\"" + step.getName() + "\" referenced by QA step \"" + rs.getString("name") + "\" for dataset \""; 
                    EmfDataset qaDataset = datasetDAO.obtainLocked(user, datasetDAO.getDataset(session, rs.getInt("dataset_id")), session);
                    stepsReferenced += qaDataset.getName() + "\'";
                    while ( rs.next()) {
                        stepsReferenced += ", ";
                        stepsReferenced += " \"" + rs.getString("name") + "\" for dataset \""; 
                        qaDataset = datasetDAO.obtainLocked(user, datasetDAO.getDataset(session, rs.getInt("dataset_id")), session);
                        stepsReferenced += qaDataset.getName() + "\"";
                    }
                    stepsReferenced +=". ";
                    numStepsReferenced++;
                } else { //empty
                    stepsToBeDeleted.add(step);
                }
            }catch (SQLException e) {
                LOG.error( "Error when running query - " + sql + " - to check QA Step " + step.getName() + "'s dependences: ", e); 
                setStatus( user, "Error when running query - " + sql + " - to check QA Step " + step.getName() + "'s dependences: " + e.getMessage() + ". This step won't be deleted.");
                stepsFailedDependencyCheck += (numStepsFailedDependencyCheck==0 ? "" : ",") + step.getName();
                numStepsFailedDependencyCheck++;
            } catch (Exception e) {
                LOG.error( "Error when checking QA Step " + step.getName() + "'s dependences: ", e); 
                setStatus( user, "Error when checking QA Step " + step.getName() + "'s dependences: " + e.getMessage() + ". This step won't be deleted.");
                stepsFailedDependencyCheck += (numStepsFailedDependencyCheck==0 ? "" : ",") + step.getName();
                numStepsFailedDependencyCheck++;
            }
        }
        
        String msg = "";
        msg += stepsFailedDependencyCheck.equals("") ? "" : 
            "Failed to check " + numStepsFailedDependencyCheck + " steps' dependences - they will not be deleted: " + 
            stepsFailedDependencyCheck + ".";
        msg += msg.equals("") ? "" : "\n";
        msg += stepsReferenced.equals("") ? "" : 
            "" + numStepsReferenced + " steps are referenced by some other QA steps and will not be deleted: " + 
            stepsReferenced;
        msg += msg.equals("") ? "" : "\n";
        msg += stepsToBeDeleted.size() > 0 ? 
               "Start to delete QA steps..." :
               "No QA steps will be deleted.";
        setStatus( user, msg);
        
        int succeeded=0;
        int failed = 0;
        try {
            if (dbServer!=null && dbServer.isConnected()) {
                dbServer.disconnect();
            }
        } catch (Exception e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        for ( QAStep step : stepsToBeDeleted) {
            try {
                dbServer = dbServerFactory.getDbServer();
                this.removeQAResultTable(step, dbServer);
                try {
                    dao.deleteQAStep(step, session);
                    succeeded++;
                } catch (RuntimeException e) {
                    failed++;
                    LOG.error("Failed to delete QA Step " + step.getName(), e);
                    setStatus(user,"Failed to delete QA Step " + step.getName() + ": " + e.getMessage());
                }
            } catch (EmfException e) {
                failed++;
                LOG.error("Failed to remove result tables for QA Step " + step.getName(), e);
                setStatus(user,"Failed to remove result tables for QA Step " + step.getName() + ": " + e.getMessage());
            } catch ( Exception e) {
                failed++;
                LOG.error("Failed to delete QA Step " + step.getName(), e);
                setStatus(user,"Failed to delete " + step.getName() + ": " + e.getMessage());
            }
            
        }
        
        session.close();
        
        msg = "Completed deleting the QA steps: \n";
        msg += "Total number of steps: " + total + "\n"; 
        msg += numStepsFailedDependencyCheck==0 ? "" : 
               "Failed to check dependences for " + numStepsFailedDependencyCheck + " QA steps - not deleted.\n";
        msg += numStepsReferenced==0 ? "" :
               "" + numStepsReferenced + " QA Steps referenced by other QA steps - not deleted.\n";
        msg += succeeded==0 ? "" : 
               "Successfully deleted " + succeeded + " QA Steps\n";
        msg += failed==0 ? "" :
               "Failed to delete " + failed + " QA Steps\n";
        setStatus(user,msg);
        */
    }
    
//    private void setStatus( User user, String msg) {
//        Status status = new Status();
//        status.setUsername(user.getUsername());
//        status.setType("DeleteQASteps");
//        status.setMessage(msg);
//        status.setTimestamp(new Date());
//        statusDAO.add(status);
//    }
}