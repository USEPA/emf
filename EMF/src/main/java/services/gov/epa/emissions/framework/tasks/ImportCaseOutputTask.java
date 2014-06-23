package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.external.AbstractExternalFilesImporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ImporterFactory;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ImportCaseOutputTask extends Task {

    @Override
    public boolean isEquivalent(Task task) { // NOTE: needs to verify definition of equality
        ImportCaseOutputTask importTask = (ImportCaseOutputTask) task;

        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())) {
            return true;
        }

        return false;
    }

    private static Log log = LogFactory.getLog(ImportCaseOutputTask.class);

    protected Importer importer;

    protected EmfDataset dataset;

    private CaseOutput output;

    protected String[] files;

    protected ExternalSource[] extSrcs;

    protected HibernateSessionFactory sessionFactory;

    protected double numSeconds;

    protected DatasetDAO datasetDao;

    private CaseDAO caseDao;

    private File path;

    private DbServerFactory dbServerFactory;

    private boolean useTaskManager;

    public ImportCaseOutputTask(CaseOutput output, EmfDataset dataset, String[] files, File path, User user,
            Services services, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            boolean useTaskManager) {
        super();
        createId();

        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());

        this.user = user;
        this.files = files;
        this.path = path;
        this.dataset = dataset;
        this.output = output;
        this.useTaskManager = useTaskManager;
        this.statusServices = services.getStatus();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.datasetDao = new DatasetDAO(dbServerFactory);
        this.caseDao = new CaseDAO(sessionFactory);
    }

    public void run() {
        if (DebugLevels.DEBUG_1()) {
            System.out.println(">>## ImportTask:run() " + taskId + " for dataset: " + this.dataset.getName());
        }
        if (DebugLevels.DEBUG_1())
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1())
            if (DebugLevels.DEBUG_1())
                System.out.println("Task# " + taskId + " running");

        DbServer dbServer = null;
        boolean isDone = false;
        String errorMsg = "";

        try {
            dbServer = dbServerFactory.getDbServer();
            ImporterFactory importerFactory = new ImporterFactory(dbServer);
            Importer importer = importerFactory.createVersioned(dataset, path, files);
            long startTime = System.currentTimeMillis();

            prepare(dbServer);
            importer.run();

            if (dataset.isExternal() && importer instanceof VersionedImporter) {
                Importer extImporter = ((VersionedImporter) importer).getWrappedImporter();
                extSrcs = ((AbstractExternalFilesImporter) extImporter).getExternalSources();
            }
            
            //update dataset version record count (only for internal sources) and creator, 
            Version version = version(dataset.getId(), dataset.getDefaultVersion());
            if (!dataset.isExternal()) {
                version.setNumberRecords(getNumOfRecords(dataset, version));
            }
            version.setCreator(user);
            updateVersionNReleaseLock(version);

            numSeconds = (System.currentTimeMillis() - startTime) / 1000;
            complete("Imported");
            isDone = true;
        } catch (Exception e) {
            errorMsg += e.getMessage();

            // this doesn't give the full path for some reason
            logError("File(s) import failed for user (" + user.getUsername() + ") at " + new Date().toString() + " -- "
                    + filesList(), e);

            removeDataset(dataset);

            Session session = sessionFactory.getSession();

            try {
                caseDao.removeCaseOutputs(user, new CaseOutput[] { output }, true, session);
            } catch (EmfException e1) {
                errorMsg += System.getProperty("line.separator") + e1.getMessage();
                log.error(errorMsg, e1);
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        } finally {
            try {
                if (isDone)
                    addCompletedStatus();
                else
                    addFailedStatus(errorMsg);
            } catch (Exception e2) {
                log.error("Error setting outputs status.", e2);
            } finally {
                try {
                    if (dbServer != null && dbServer.isConnected())
                        dbServer.disconnect();
                } catch (Exception e) {
                    log.error("Error closing database connection.", e);
                }
            }
        }
    }

    private void prepare(DbServer dbServer) throws Exception {
        addStartStatus();

        Session session = sessionFactory.getSession();
        CaseOutput existedOutput = null;

        try {
            existedOutput = caseDao.getCaseOutput(output, session);

            if (existedOutput != null) {
                EmfDataset dataset = datasetDao.getDataset(session, existedOutput.getDatasetId());
                datasetDao.deleteDatasets(new EmfDataset[] { dataset }, dbServer, session);
            }
        } catch (Exception e) {
            log.error("Error deleting dataset - " + e.getMessage());
        } finally {
            try {
                if (existedOutput != null)
                    caseDao.removeCaseOutputs(new CaseOutput[] { existedOutput }, session);
                caseDao.add(output);
                dataset.setStatus("Started import");
                addDataset();
            } catch (Exception e) {
                log.error("Error deleting dataset.", e);
                throw e;
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    private void complete(String status) {
        String message = "Case output " + output.getName() + " registered successfully.";

        dataset.setStatus(status);
        updateDataset(dataset);
        updateOutput(status, message);
    }

    private void updateOutput(String status, String message) {
        Session session = sessionFactory.getSession();

        try {
            output.setDatasetId(datasetDao.getDataset(session, dataset.getName()).getId());
            output.setStatus(status);
            output.setMessage(message);
            caseDao.updateCaseOutput(output, session);
        } catch (Exception e) {
            log.error("Error updating case output " + output.getName() + ". ", e);
        } finally {
            session.close();
        }
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

    protected void addDataset() throws EmfException {
        Session session = sessionFactory.getSession();
        String name = dataset.getName(); // TODO: JIZHEN1
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);

        try {
            if (datasetDao.datasetNameUsed(name, session)) {
                name += "_" + CustomDateFormat.format_yyyy_MM_dd_HHmmssSS(new Date());
                
                newName = name;
                if ( newName != null) {
                    newName = newName.trim();
                } else {
                    throw new EmfException("Dataset name is null");
                }
                dataset.setName(newName);
            }

            session.clear();
            datasetDao.add(dataset, session);
        } catch (Exception e) {
            log.error("Error adding new dataset: " + name, e);
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    protected void updateDataset(EmfDataset dataset) {
        Session session = sessionFactory.getSession();

        try {
            if (dataset.isExternal() && extSrcs != null && extSrcs.length > 0) {
                for (int i = 0; i < extSrcs.length; i++)
                    extSrcs[i].setDatasetId(dataset.getId());

                datasetDao.addExternalSources(extSrcs, session);
            }

            datasetDao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    protected void removeDataset(EmfDataset dataset) {
        Session session = sessionFactory.getSession();

        try {
            datasetDao.remove(dataset, session);
        } catch (Exception e) {
            logError("Could not get remove Dataset - " + dataset.getName(), e);
        } finally {
            session.close();
        }
    }

    protected void addStartStatus() {
        setStatus("started", "Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "
                + files[0] + output.getName() + " " + caseDao.toString());
    }

    protected void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] "
                + " in " + numSeconds + " seconds from " + files[0]; // TODO: add batch size to message once
        // available
        setStatus("completed", message);
    }

    private void addFailedStatus(String errorMsg) {
        setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + errorMsg);
    }

    protected void setStatus(String status, String message) {
        output.setStatus(status);
        output.setMessage(message);

        if (DebugLevels.DEBUG_4()) {
            System.out.println("ImportTaskManager = " + ImportTaskManager.getImportTaskManager());
            System.out.println("taskId = " + taskId);
            System.out.println("submitterId = " + submitterId);
            System.out.println("status = " + status);
            System.out.println("current thread = " + Thread.currentThread());
            System.out.println("message = " + message);
        }

        if (this.useTaskManager)
            ImportTaskManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(),
                    message);
        else 
            setStatus(message);
    }
    
    private synchronized void setStatus(String message) {
        if ( message.toLowerCase().contains("fail") || message.toLowerCase().contains("error")){
            Status endStatus = new Status();
            endStatus.setUsername(user.getUsername());
            endStatus.setType("CaseOutputImport");
            endStatus.setMessage(message);
            endStatus.setTimestamp(new Date());

            statusServices.add(endStatus);
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
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }

}
