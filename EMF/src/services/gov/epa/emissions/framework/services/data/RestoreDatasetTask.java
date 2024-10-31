package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.db.PostgresRestore;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.io.File;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RestoreDatasetTask implements Runnable {

    Log log = LogFactory.getLog(RestoreDatasetTask.class);

    private boolean windowsOS = false;
    private DbServerFactory dbServerFactory;
    private EntityManagerFactory entityManagerFactory;
    private DatasetDAO datasetDAO;
    private Integer datasetId;
    private EmfPropertiesDAO propertyDao;
    private StatusDAO statusDAO;
    private String username;

    public RestoreDatasetTask() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public RestoreDatasetTask(DbServerFactory dbServerFactory,
                                EntityManagerFactory entityManagerFactory,
                                Integer datasetId,
                                String username) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory =entityManagerFactory;
        this.datasetDAO = new DatasetDAO(dbServerFactory);
        this.propertyDao = new EmfPropertiesDAO();
        this.statusDAO = new StatusDAO(entityManagerFactory);
        this.datasetId = datasetId;
        this.username = username;
    }

    private EmfDataset getDataset() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            EmfDataset dataset = this.datasetDAO.getDataset(entityManager, datasetId.intValue());
            return dataset;
        } catch (RuntimeException e) {
            log.error("Could not get dataset with id=" + datasetId.intValue(), e);
            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
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
//            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
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

            //stop consolidated from being archived at this point, maybe in the future
            EmfDataset dataset = getDataset();
            InternalSource[] internalSources = dataset.getInternalSources();
            String[] tableNames = new String[internalSources.length];
            for (int i = 0; i < internalSources.length; i++) {
                tableNames[i] = internalSources[i].getTable();
            }

            //if just one table, see if its consolidated, if so then don't allow the archival
            if (tableNames.length == 1 && dbServer.getEmissionsDatasource().tableDefinition().isConsolidationTable(tableNames[0]))
                throw new EmfException("Not allowed to restore a dataset that shares a consolidated table.");

            //get table name to backup...
            String filePath = getProperty("postgres-backup-dir-for-emissions-schema") + File.separator + "ds" + File.separator + (Math.round(dataset.getId() / 1000) + 1) + File.separator + dataset.getId() + ".bck";

            setStatus("Started restoring dataset, " + dataset.getName() + ".");

            PostgresRestore postgresRestore = new PostgresRestore(getProperty("postgres-bin-dir"),
                    getProperty("postgres-db"),
                    getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"),
                    filePath);
            postgresRestore.restore();

            //analyze table to make sure indexes are applied
            dbServer.getEmissionsDatasource().tableDefinition().analyzeTable(tableNames[0]);

            setStatus("Restoring indexes on dataset, " + dataset.getName() + ".");

            //set dataset Status to "Archived"
            dataset.setStatus("Restored");
            entityManager.clear();
            this.datasetDAO.updateDSPropNoLocking(dataset, entityManager);

            setStatus("Completed restoring dataset, " + dataset.getName() + ".");

        } catch (Exception e) {
            setStatus("Error restoring dataset, " + e.getMessage() + ".");
//            log.error("Error restoring dataset", e);
            e.printStackTrace();
//            throw new EmfException(e.getMessage());
        } finally {
            if (entityManager != null) entityManager.close();
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                log.error("Error restoring dataset", e);
                e.printStackTrace();
//                throw new EmfException(e.getMessage());
            }
        }
    }
}
