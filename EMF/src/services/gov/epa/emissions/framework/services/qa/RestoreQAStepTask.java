package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.db.PostgresRestore;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.io.File;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RestoreQAStepTask implements Runnable {

    Log log = LogFactory.getLog(RestoreQAStepTask.class);

    private boolean windowsOS = false;
    private DbServerFactory dbServerFactory;
    private EntityManagerFactory entityManagerFactory;
    private QADAO qaDAO;
    private Integer qaStepResultId;
    private EmfPropertiesDAO propertyDao;
    private StatusDAO statusDAO;
    private String username;

    public RestoreQAStepTask() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public RestoreQAStepTask(DbServerFactory dbServerFactory,
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return propertyDao.getProperty(name, entityManager).getValue();
        } catch (RuntimeException e) {
            log.error("Could not get property with name=" + name, e);
//            throw new EmfException("Could not get QA Step Result with id=" + qaStepResultId);
        } finally {
            entityManager.close();
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

            //get table name to restore...
            String filePath = getProperty("postgres-backup-dir-for-emissions-schema") + File.separator + "qa" + File.separator + (Math.round(qaStepResultId / 1000) + 1) + File.separator + qaStepResultId + ".bck";

            setStatus("Started restoring QA Step Result, " + qAStep.getName() + ".");

            PostgresRestore postgresRestore = new PostgresRestore(getProperty("postgres-bin-dir"),
                    getProperty("postgres-db"),
                    getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"),
                    filePath);
            postgresRestore.restore();

            //analyze table to make sure indexes are applied
            dbServer.getEmissionsDatasource().tableDefinition().analyzeTable(tableNames[0]);

            setStatus("Restoring indexes on QA Step Result, " + qAStep.getName() + ".");

            //set QAStepResult Status to "Archived"
            qAStepResult.setTableCreationStatus("Restored");
            entityManager.clear();
            this.qaDAO.updateQAStepResult(qAStepResult, entityManager);

            setStatus("Completed restoring QA Step Result, " + qAStep.getName() + ".");

        } catch (Exception e) {
            setStatus("Error restoring QAStepResult, " + e.getMessage() + ".");
            e.printStackTrace();
//            throw new EmfException(e.getMessage());
        } finally {
            if (entityManager != null) entityManager.close();
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                log.error("Error restoring QAStepResult", e);
                e.printStackTrace();
//                throw new EmfException(e.getMessage());
            }
        }
    }
}
