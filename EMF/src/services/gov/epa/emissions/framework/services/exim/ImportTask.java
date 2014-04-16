package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.external.AbstractExternalFilesImporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ImportTaskManager;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ImportTask extends Task {
    
    @Override
    public boolean isEquivalent(Task task) { //NOTE: needs to verify definition of equality
        ImportTask importTask = (ImportTask) task;
        
        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())){
            return true;
        }
        
        return false;
    }

    private static Log log = LogFactory.getLog(ImportTask.class);

    protected Importer importer;

    protected EmfDataset dataset;

    protected String[] files;

    protected HibernateSessionFactory sessionFactory;

    protected double numSeconds;
    
    protected ExternalSource[] extSrcs;
    
    protected DatasetDAO dao;

    private File path;

    private DbServerFactory dbServerFactory;

    public ImportTask(EmfDataset dataset, String[] files, File path, User user, Services services,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        super();
        createId();
        
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());
        
        this.user = user;
        this.files = files;
        this.dataset = dataset;
        this.statusServices = services.getStatus();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.dao = new DatasetDAO(dbServerFactory);
        this.path = path;
    }

    public void run() {
        if (DebugLevels.DEBUG_1()) {
            log.debug(">>## ImportTask:run() " + taskId + " for dataset: " + this.dataset.getName());
        }
        if (DebugLevels.DEBUG_1()) {
            log.debug("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());
        }
            

        if (DebugLevels.DEBUG_1())
            if (DebugLevels.DEBUG_1())
                log.debug("Task# " + taskId + " running");
        
        Session session = null;
        DbServer dbServer = null;
        boolean isDone = false;
        String errorMsg = "";
        
        try {
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 1 ===");
            dbServer = dbServerFactory.getDbServer();
            ImporterFactory importerFactory = new ImporterFactory(dbServer);
            Importer importer = importerFactory.createVersioned(dataset, path, files);
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 02 ===");
            
            prepare(session);
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 03 ===");
            
            importer.run();
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 04 ===");
            
            if (dataset.isExternal() && importer instanceof VersionedImporter) {
                Importer extImporter = ((VersionedImporter)importer).getWrappedImporter();
                extSrcs = ((AbstractExternalFilesImporter)extImporter).getExternalSources();
            }
            
            //update dataset version record count (only for internal sources) and creator, 
            Version version = version(dataset.getId(), dataset.getDefaultVersion());
            if (!dataset.isExternal()) {
                version.setNumberRecords(getNumOfRecords(dataset, version));
            }
            version.setCreator(user);
            updateVersionNReleaseLock(version);

            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            complete(session, "Imported");
            isDone = true;
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 2 ===");
            
        } catch (Exception e) {
            errorMsg += e.getMessage();
            // this doesn't give the full path for some reason
            logError("File(s) import failed for user (" + user.getUsername() + ") at " + new Date().toString() + " -- " + filesList(), e);
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 3 ===");
            
            removeDataset(dataset); // TODO: JIZHEN BUG3316 internal sources of the dataset
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 4 ===");
            
        } finally {
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 5 ===");
            
            try {
                if (isDone) {
                    addCompletedStatus();
                    session.flush();
                } else 
                    addFailedStatus(errorMsg);
            } catch (Exception e2) {
                log.error("Error setting outputs status.", e2);
            } finally {
                try {
                    if (session != null && session.isConnected()) 
                        session.close();
                    
                    if (dbServer != null && dbServer.isConnected())
                        dbServer.disconnect();
                } catch (Exception e) {
                    log.error("Error closing database connection.", e);
                }
            }
            
            if (DebugLevels.DEBUG_1())
                log.debug("  >=== 6 ===");
            
        }
    }

    private Version version(int datasetId, int versionNumber) throws EmfException {
        DataServiceImpl dataServiceImpl = new DataServiceImpl(dbServerFactory, sessionFactory);
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            Version version = versions.get(datasetId, versionNumber, session);
            version = dataServiceImpl.obtainedLockOnVersion(user, version.getId());
            return version;

        } finally {
            session.close();
        }
    }

    private void updateVersionNReleaseLock(Version locked) throws EmfException {
        DataServiceImpl dataServiceImpl = new DataServiceImpl(dbServerFactory, sessionFactory);
        try {
            dataServiceImpl.updateVersionNReleaseLock(locked);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

    }
    public synchronized int getNumOfRecords(EmfDataset dataset, Version version) throws EmfException {
        DataServiceImpl dataServiceImpl = new DataServiceImpl(dbServerFactory, sessionFactory);
        try {
            InternalSource[] internalSources = dataset.getInternalSources();
            
            return dataServiceImpl.getNumOfRecords("emissions." + internalSources[0].getTable(), version, "");
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

    }

    protected void prepare(Session session) throws EmfException {
        addStartStatus();
        dataset.setStatus("Started import");
        addDataset(dataset, session);
    }

    protected void complete(Session session, String status) {
        dataset.setStatus(status);
        updateDataset(dataset, session);
    }

    protected String filesList() {
        StringBuffer fileList = new StringBuffer();
        fileList.append("Path: " + path.getAbsolutePath() + "; File(s): ");

        if (files.length > 0)
            for (int i = 0; i < files.length; i++)
                fileList.append(files[i] + ", ");
        
        String ret = fileList.toString();
        int idx = ret.lastIndexOf(",");
        
        return idx > 0 ? ret.substring(0, idx) : ret;
    }

    protected void addDataset(EmfDataset dataset, Session session) throws EmfException {
        try {
            if (dao.datasetNameUsed(dataset.getName(), session))
                throw new EmfException("The selected Dataset name is already in use");
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        }

        dao.add(dataset, session);
    }

    protected void updateDataset(EmfDataset dataset, Session session) {
        try {
            if (dataset.isExternal() && extSrcs != null && extSrcs.length > 0) {
                for (int i = 0; i < extSrcs.length; i++)
                    extSrcs[i].setDatasetId(dataset.getId());
                
                dao.addExternalSources(extSrcs, session);
            }
            
            dao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
        }
    }

    protected void removeDataset(EmfDataset dataset) {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(dataset, session);
            session.close();
        } catch (Exception e) {
            logError("Could not get remove Dataset - " + dataset.getName(), e);
        }
    }

    protected void addStartStatus() {
        setStatus("started", "Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "+
                files[0]);
    }

    protected void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] " 
               + " in " + numSeconds+" seconds from "+ files[0]; //TODO: add batch size to message once available
        setStatus("completed", message);
    }
    
    private void addFailedStatus(String errorMsg) {
        setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + errorMsg);
    }

    protected void setStatus(String status, String message) {
        ImportTaskManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(), message);
    }

    public void setWaitingStatus() {

        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage("Dataset, " + dataset.getName() + ", has been added to the Dataset Import Queue.  There queue is currently busy importing other datasets.");
        endStatus.setTimestamp(new Date());

        statusServices.add(endStatus);
}

    protected void logError(String messge, Exception e) {
        log.error(messge, e);
    }
    
    public EmfDataset getDataset() {
        return this.dataset;
    }
    
    public Importer getImporter() {
        return this.importer;
    }
    
    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1())
            log.debug(">>>> Destroying object: " + createId());
        super.finalize();
    }

}
