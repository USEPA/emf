package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.db.PostgresDump;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import java.io.*;
import java.util.Date;

public class ArchiveDatasetTask implements Runnable {

    Log log = LogFactory.getLog(ArchiveDatasetTask.class);

    private boolean windowsOS = false;
    private DbServerFactory dbServerFactory;
    private HibernateSessionFactory sessionFactory;
    private DatasetDAO datasetDAO;
    private Integer datasetId;
    private EmfPropertiesDAO propertyDao;
    private StatusDAO statusDAO;
    private String username;

    public ArchiveDatasetTask() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public ArchiveDatasetTask(DbServerFactory dbServerFactory,
                              HibernateSessionFactory sessionFactory,
                              Integer datasetId,
                              String username) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory =sessionFactory;
        this.datasetDAO = new DatasetDAO(dbServerFactory);
        this.propertyDao = new EmfPropertiesDAO(sessionFactory);
        this.datasetId = datasetId;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.username = username;
    }

    private EmfDataset getDataset() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = this.datasetDAO.getDataset(session, datasetId.intValue());
            return dataset;
        } catch (RuntimeException e) {
            log.error("Could not get dataset with id=" + datasetId.intValue(), e);
            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
        } finally {
            session.close();
        }
    }

    private String getProperty(String name) {
        return propertyDao.getProperty(name).getValue();
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
        Session session = null;
        try {

            dbServer = dbServerFactory.getDbServer();
            session = sessionFactory.getSession();

            //stop consolidated from being archived at this point, maybe in the future
            EmfDataset dataset = getDataset();
            InternalSource[] internalSources = dataset.getInternalSources();
            String[] tableNames = new String[internalSources.length];
            for (int i = 0; i < internalSources.length; i++) {
                tableNames[i] = internalSources[i].getTable();
            }

            //if just one table, see if its consolidated, if so then don't allow the archival
            if (tableNames.length == 1 && dbServer.getEmissionsDatasource().tableDefinition().isConsolidationTable(tableNames[0]))
                throw new EmfException("Not allowed to archive a dataset that shares a consolidated table.");

            //get table name to backup...
            String filePath = getProperty("postgres-backup-dir-for-emissions-schema") + File.separator + "ds" + File.separator + (Math.round(dataset.getId() / 1000) + 1) + File.separator + dataset.getId() + ".bck";

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

            setStatus("Started archiving dataset, " + dataset.getName() + ".");

            PostgresDump postgresDump = new PostgresDump(getProperty("postgres-bin-dir"),
                    getProperty("postgres-db"),
                    getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"),
                    filePath,
                    dbServer.getEmissionsDatasource().getName(),
                    tableNames);
            postgresDump.dump();

            dbServer.getEmissionsDatasource().tableDefinition().dropTable(tableNames[0]);

            setStatus("Dropping table for dataset, " + dataset.getName() + ".");

            //set dataset Status to "Archived"
            dataset.setStatus("Archived");
            session.clear();
            this.datasetDAO.updateDSPropNoLocking(dataset, session);

            setStatus("Completed archiving dataset, " + dataset.getName() + ".");

        } catch (Exception e) {
            setStatus("Error archiving dataset, " + e.getMessage() + ".");
            e.printStackTrace();
//            throw new EmfException(e.getMessage());
        } finally {
            if (session != null) session.close();
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                log.error("Error archiving dataset", e);
                e.printStackTrace();
//                throw new EmfException(e.getMessage());
            }
        }
    }
}
