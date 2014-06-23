package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunQAStepTask {

    private QAStep[] qasteps;

    private User user;

    private StatusDAO statusDao;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private PooledExecutor threadPool;

    private String exportDirectory;
    private String []exportFiles = null;

    private boolean verboseStatusLogging = true;

    public RunQAStepTask(QAStep[] qaStep, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            String exportDirectory, boolean verboseStatusLogging, String[] exportFiles) {
        this(qaStep, user, 
            dbServerFactory, sessionFactory);
        this.exportDirectory = exportDirectory;
        this.exportFiles = exportFiles;
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public RunQAStepTask(QAStep[] qaStep, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.qasteps = qaStep;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.threadPool = createThreadPool();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public void run() throws EmfException {
        QAStep qaStep = null;
        String exportFile = null;
        try {
            for (int i = 0; i < qasteps.length; i++) {
                qaStep = qasteps[i];
                exportFile = exportFiles==null ? null : exportFiles[i];
                runSteps(qaStep, exportFile);
            }
        } catch (Exception e) {
            // need to catch the general exception in case there is a 
            // non-EMFException that is generated - else the user never gets a message
            e.printStackTrace();
            setStatus("Failed to run QA step " + qaStep.getName() + suffix(qaStep) + ". " + e.getMessage());
            throw new EmfException("Failed to run QA step : " + qaStep.getName() + suffix(qaStep));
        }
    }

    private void runSteps(QAStep qaStep, String exportFile) throws EmfException {
        String suffix = suffix(qaStep);
        prepare(suffix, qaStep);
        DbServer dbServer = dbServerFactory.getDbServer();
        long startTime;
        long endTime;
        try {
            startTime = System.currentTimeMillis();
            QAProgramRunner runQAProgram = qaProgramRunner(qaStep, dbServer);
            runQAProgram.run();
            endTime = System.currentTimeMillis();
            System.out.println("Ran QA step, " + qaStep.getName() + ", in " + ((endTime - startTime) / (1000))  + " secs");
        } finally { // add catch to deal with exception
            close(dbServer);
        }
        complete(suffix, qaStep);

        if (exportDirectory != null && exportDirectory.trim().length() != 0) {
            startTime = System.currentTimeMillis();
            ExportQAStep exportQATask = new ExportQAStep(qaStep, dbServerFactory, 
                    user, sessionFactory, 
                    threadPool, verboseStatusLogging);
            exportQATask.export(exportDirectory, exportFile, true);
            endTime = System.currentTimeMillis();
            System.out.println("Exported QA step, " + qaStep.getName() + ", in " + ((endTime - startTime) / (1000))  + " secs");
        }
    
    }

    private void close(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not close database connection." + e.getMessage());
        }
    }

    private QAProgramRunner qaProgramRunner(QAStep step, DbServer dbServer) throws EmfException {
        RunQAProgramFactory factory = new RunQAProgramFactory(step, dbServer, 
                sessionFactory);
        try {
            return factory.create();
        } catch (EmfException e) {
            throw new EmfException("Could not create the program runner");
        }
    }

    private void prepare(String suffixMsg, QAStep qastep) {
        if (verboseStatusLogging)
            setStatus("Started running QA step '" + qastep.getName() + suffixMsg);
    }

    private void complete(String suffixMsg, QAStep qastep) {
        if (verboseStatusLogging)
            setStatus("Completed running QA step '" + qastep.getName() + suffixMsg);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("RunQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix(QAStep qastep) {
        return "' for Version '" + versionName(qastep) + "' of Dataset '" + datasetName(qastep) + "'";
    }

    private String versionName(QAStep qastep) {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName(QAStep qastep) {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }
}
