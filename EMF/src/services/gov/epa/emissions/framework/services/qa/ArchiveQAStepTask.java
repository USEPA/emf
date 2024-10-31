package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.db.PostgresDump;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.io.File;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ArchiveQAStepTask implements Runnable {

    Log log = LogFactory.getLog(ArchiveQAStepTask.class);

    private boolean windowsOS = false;
    private DbServerFactory dbServerFactory;
    private EntityManagerFactory entityManagerFactory;
    private QADAO qaDAO;
    private Integer qaStepResultId;
    private EmfPropertiesDAO propertyDao;
    private StatusDAO statusDAO;
    private String username;

    public ArchiveQAStepTask() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public ArchiveQAStepTask(DbServerFactory dbServerFactory,
                              EntityManagerFactory entityManagerFactory,
                              Integer qaStepResultId,
                              String username) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory =entityManagerFactory;
        this.qaDAO = new QADAO();
        this.propertyDao = new EmfPropertiesDAO();
        this.qaStepResultId = qaStepResultId;
        this.statusDAO = new StatusDAO(entityManagerFactory);
        this.username = username;
    }

    private QAStepResult getQAStepResult() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return this.qaDAO.getQAStepResult(qaStepResultId, entityManager);
        } catch (RuntimeException e) {
            log.error("Could not get QA Step Result with id=" + qaStepResultId, e);
            throw new EmfException("Could not get QA Step Result with id=" + qaStepResultId);
        } finally {
            entityManager.close();
        }
    }

    private QAStep getQAStep(Integer qaStepId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return this.qaDAO.getQAStep(qaStepId, entityManager);
        } catch (RuntimeException e) {
            log.error("Could not get QA Step Result with id=" + qaStepResultId, e);
            throw new EmfException("Could not get QA Step Result with id=" + qaStepResultId);
        } finally {
            entityManager.close();
        }
    }

    private String getProperty(String name) {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            return propertyDao.getProperty(name, entityManager).getValue();
        } catch (Exception e) {
            e.printStackTrace();
//            throw new EmfException(e.getMessage());
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return null;
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(this.username);
        endStatus.setType("Archiving");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }

    public void run() {
        DbServer dbServer = null;
        EntityManager entityManager = null;
        try {

            dbServer = dbServerFactory.getDbServer();
            entityManager = entityManagerFactory.createEntityManager();

            QAStepResult qAStepResult = getQAStepResult();
            QAStep qAStep = getQAStep(qAStepResult.getQaStepId());
            String[] tableNames = new String[1];
            tableNames[0] = qAStepResult.getTable();

            //nothing to backup, go ahead and get out of here...
            if (tableNames[0] == null)
                return;

            //get table name to backup...
            String filePath = getProperty("postgres-backup-dir-for-emissions-schema") + File.separator + "qa" + File.separator + (Math.round(qaStepResultId / 1000) + 1) + File.separator + qaStepResultId + ".bck";

            //see if folder exists yet...
            File backupFolder = new File(filePath.substring(0,filePath.lastIndexOf(File.separator)));
            if (!backupFolder.exists()) {
                backupFolder.mkdir();
                backupFolder.setWritable(true, true);
                if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
                    Runtime.getRuntime().exec("CACLS " + filePath.substring(0, filePath.lastIndexOf(File.separator)) + " /E /G \"Users\":W");
                    Thread.sleep(1000); // for the system to refresh the file access permissions
                }
            }

            setStatus("Started archiving QA Step Result, " + qAStep.getName() + ".");

            PostgresDump postgresDump = new PostgresDump(getProperty("postgres-bin-dir"),
                    getProperty("postgres-db"),
                    getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"),
                    filePath,
                    dbServer.getEmissionsDatasource().getName(),
                    tableNames);
            postgresDump.dump();

            dbServer.getEmissionsDatasource().tableDefinition().dropTable(tableNames[0]);

            setStatus("Dropping table for QA Step Result, " + qAStep.getName() + ".");

            //set dataset Status to "Archived"
            qAStepResult.setTableCreationStatus("Archived");
            entityManager.clear();
            this.qaDAO.updateQAStepResult(qAStepResult, entityManager);

            setStatus("Completed archiving QA Step Result, " + qAStep.getName() + ".");

        } catch (Exception e) {
            setStatus("Error archiving QA Step Result, " + e.getMessage() + ".");
            e.printStackTrace();
//            throw new EmfException(e.getMessage());
        } finally {
            if (entityManager != null) entityManager.close();
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                log.error("Error archiving QA Step Result", e);
                e.printStackTrace();
//                throw new EmfException(e.getMessage());
            }
        }
    }
}
