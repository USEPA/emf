package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.CommonFileHeaderReader;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.outputs.QueueCaseOutput;
import gov.epa.emissions.framework.services.data.CountriesDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.IntendedUsesDAO;
import gov.epa.emissions.framework.services.data.KeywordsDAO;
import gov.epa.emissions.framework.services.data.ProjectsDAO;
import gov.epa.emissions.framework.services.data.RegionsDAO;
import gov.epa.emissions.framework.services.data.SectorsDAO;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ImportCaseOutputSubmitter;
import gov.epa.emissions.framework.tasks.ImportCaseOutputTask;
import gov.epa.emissions.framework.tasks.ImportClientSubmitter;
import gov.epa.emissions.framework.tasks.ImportSubmitter;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ManagedImportService {
    private static Log log = LogFactory.getLog(ManagedImportService.class);

    private static int numOfRunningThread = 0;

    private static Thread runningThread = null;

    private HibernateSessionFactory sessionFactory;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmmss");

    private ImportSubmitter importClientSubmitter = null;

    private ImportSubmitter importCaseOutputSubmitter = null;

    private ArrayList<Runnable> importTasks = new ArrayList<Runnable>();

    private ArrayList<Runnable> importOutputTasks = new ArrayList<Runnable>();

    private static int svcCount = 0;

    private static final String FOR_CLIENT = "client";

    private static final String FOR_OUTPUT = "output";

    private String svcLabel = null;

    private DbServerFactory dbServerFactory;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    public ManagedImportService(HibernateSessionFactory sessionFactory) {
        this(DbServerFactory.get(), sessionFactory);
    }

    public ManagedImportService(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;

        if (System.getProperty("IMPORT_EXPORT_TEMP_DIR") == null)
            setTempDirProperties();

        if (System.getProperty("MASS_STORAGE_ROOT") == null)
            setMassStorageProperties();

        if (DebugLevels.DEBUG_17())
            System.out.println("ManagedImportService: At the class initialization -- numOfRunningThread: "
                    + numOfRunningThread);
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private synchronized File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private synchronized boolean isNameUsed(String name) throws Exception {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();

        try {
            return dao.datasetNameUsed(name, session);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized String importDatasetsForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType) throws EmfException {
        registerSubmitter(FOR_CLIENT, folderPath, filenames); // initialies importClientSubmitter
        File path = validatePath(folderPath);
        Services services = services();

        try {
            for (int i = 0; i < filenames.length; i++)
                addTasks(folderPath, path, new String[] { filenames[i] }, filenames[i], user, datasetType, services);

            //if items are already being executed in the queue, then display a queue status message
            if (importClientSubmitter.getTaskManagerRunCount() > 0) {
                for (Runnable task : importTasks) {
//                for (int i = 0; i < tasks.size(); i++) {
                    ((ImportTask)task).setWaitingStatus();
                }
            }

            addTasksToSubmitter(importClientSubmitter, importTasks);
        } catch (Exception e) {
            setErrorMsgs(folderPath, e);
            throw new EmfException(e.getMessage());
        }

        return importClientSubmitter.getSubmitterId();
    }

    public synchronized String importDatasetForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType, String datasetName) throws EmfException {
        registerSubmitter(FOR_CLIENT, folderPath, filenames);
        File path = validatePath(folderPath);
        Services services = services();

        try {
            addTasks(folderPath, path, filenames, datasetName, user, datasetType, services);

            //if items are already being executed in the queue, then display a queue status message
            if (importClientSubmitter.getTaskManagerRunCount() > 0) {
                for (Runnable task : importTasks) {
//                for (int i = 0; i < tasks.size(); i++) {
                    ((ImportTask)task).setWaitingStatus();
                }
            }


            addTasksToSubmitter(importClientSubmitter, importTasks);
        } catch (Exception e) {
            e.printStackTrace();
            setErrorMsgs(folderPath, e);
            throw new EmfException(e.getMessage());
        }

        return importClientSubmitter.getSubmitterId();
    }

    private synchronized void registerSubmitter(String task, String folderPath, String[] filenames) {
        // The service instance (one per session) will have only one submitter for the type of service
        // Here the TaskManagerImportService has one reference to the ImportClientSubmitter
        ImportSubmitter submitter = null;

        if (task.equals(FOR_CLIENT)) {
            if (importClientSubmitter == null)
                importClientSubmitter = new ImportClientSubmitter();

            submitter = importClientSubmitter;
        } else if (task.equals(FOR_OUTPUT)) {
            if (importCaseOutputSubmitter == null)
                importCaseOutputSubmitter = new ImportCaseOutputSubmitter();

            submitter = importCaseOutputSubmitter;
        }

        TaskManagerFactory.getImportTaskManager().registerTaskSubmitter(submitter);
        logStartMessages(task, folderPath, filenames);
    }

    private void logStartMessages(String task, String folderPath, String[] filenames) {
        if (DebugLevels.DEBUG_9()) {
            System.out.println("ManagedImportService: " + task);
            System.out.println("ManagedImportService:import() called at: " + new Date());
            System.out.println(">>## In import service:import() " + myTag() + " for datasets: " + filenames.toString());
            System.out.println("FULL PATH= " + folderPath);
        }
    }

    private synchronized void addTasksToSubmitter(TaskSubmitter submitter, ArrayList<Runnable> importTasksList) {
        if (DebugLevels.DEBUG_11())
            System.out.println("Before importTaskSubmitter.addTasksToSubmitter # of elements in importTasks array= "
                    + importTasksList.size());

        // All importTasks have been created...so add to the submitter
        submitter.addTasksToSubmitter(importTasksList);

        // now that all tasks have been submitted remove them from from importTasks
        importTasksList.removeAll(importTasksList);

        log.info("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + submitter.getTaskCount());
        log.info("ManagedImportService:import() submitted all importTasks dropping out of loop");

        if (DebugLevels.DEBUG_9()) {
            System.out
                    .println("After importTaskSubmitter.addTasksToSubmitter and importTasks cleanout # of elements in eximTasks array= "
                            + importTasks.size());
            System.out.println("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + submitter.getTaskCount());
            System.out.println("ManagedImportService:import() exiting at: " + new Date());
        }
    }

    private synchronized void setErrorMsgs(String folderPath, Exception e) {
        // don't need to log messages about importing to existing file
        if (e.getMessage() != null && e.getMessage().indexOf("existing file") < 0
                && e.getMessage().indexOf("has been used") < 0)
            // don't need to log the error if it's just an existing file or dataset
            log.error("ERROR starting to import to folder: " + folderPath, e);
    }

    private synchronized void addTasks(String folder, File path, String[] filenames, String dsName, User user,
            DatasetType dsType, Services services) throws Exception {
        EmfDataset dataset = createDataset(folder, filenames[0], dsName, user, dsType, false);
        ImportTask task = new ImportTask(dataset, filenames, path, user, services, dbServerFactory, sessionFactory);

        importTasks.add(task);
    }

    private synchronized void addOutputTasks(User user, CaseOutput output, Services services, boolean useTaskManager)
            throws Exception {
        String folder = output.getPath();
        String pattern = output.getPattern();
        String fullPath = output.getDatasetFile();
        String datasetName = output.getDatasetName(); //.trim(); // TODO: JIZHEN1
        if ( datasetName != null) {
        	datasetName = datasetName.trim();
        	output.setDatasetName(datasetName);
        }
        String[] files = null;

        if ((folder == null || folder.trim().isEmpty()) && (fullPath == null || fullPath.trim().isEmpty()))
            throw new Exception("Error registering output: Please specify files to register case output "
                    + output.getName());

        if (fullPath != null && !fullPath.trim().isEmpty()) {
            // get folder from full path
            File singleFile = new File(fullPath);
            folder = singleFile.getParent();
            files = new String[] { singleFile.getName() };
        } else {
            // get files from pattern and folder
            files = getFilenamesFromPattern(folder, pattern);
        }

        File path = validatePath(folder);
        DatasetType type = getDsType(output.getDatasetType(), output);

        if (files.length > 1 && !type.isExternal())
            for (int i = 0; i < files.length; i++)
                // here we're making multiple datasets
                createOutputTask(type, datasetName, user, output, services, new String[] { files[i] }, path,
                        useTaskManager);
        else
            // this is to make one dataset
            createOutputTask(type, datasetName, user, output, services, files, path, useTaskManager);
    }

    private synchronized void createOutputTask(DatasetType type, String datasetName, User user, CaseOutput output,
            Services services, String[] files, File path, boolean useTaskManager) throws Exception {
        if (datasetName == null || datasetName.trim().isEmpty())
            datasetName = files[0];

        if (files.length > type.getMaxFiles() && type.getMaxFiles() != -1)
            throw new EmfException("Error registering output: Number of files (" + files.length
                    + ") exceeds limit for dataset type " + type.getName() + ".");

        CaseOutput localOuput = createNewCaseOutput(output);
        boolean nameSpecified = (localOuput.getName() != null && !localOuput.getName().trim().isEmpty());

        if (!nameSpecified)
            localOuput.setName(datasetName);

        EmfDataset dataset = createDataset(path.getAbsolutePath(), files[0], datasetName, user, type, true);

        if (DebugLevels.DEBUG_11()) {
            System.out
                    .println("Output name before create import task: " + (localOuput == null ? "" : output.getName()));
            System.out.println("Dataset name before create import task: " + dataset.getName());
        }

        ImportCaseOutputTask task = new ImportCaseOutputTask(localOuput, dataset, files, path, user, services,
                dbServerFactory, sessionFactory, useTaskManager);

        importOutputTasks.add(task);
    }

    private synchronized CaseOutput createNewCaseOutput(CaseOutput oldOutput) {
        CaseOutput newOutput = new CaseOutput(oldOutput.getName());
        newOutput.setCaseId(oldOutput.getCaseId());
        newOutput.setJobId(oldOutput.getJobId());

        return newOutput;
    }

    private synchronized DatasetType getDsType(String datasetType, CaseOutput output) throws EmfException {
        DatasetTypesDAO dao = new DatasetTypesDAO();
        DatasetType type = dao.get(datasetType, sessionFactory.getSession());

        if (type == null)
            throw new EmfException("Error registering output: Dataset type '" + datasetType
                    + "' does not exist for dataset " + output.getDatasetName() + ".");

        return type;
    }

    public synchronized String[] importDatasetsForCaseOutput(User user, CaseOutput[] outputs) throws EmfException {
        List<String> submitterIds = new ArrayList<String>();

        // here the files and path are for informational purposes (printing) only
        String[] files = new String[] { outputs[0].getDatasetFile() };
        registerSubmitter(FOR_OUTPUT, outputs[0].getPath(), files);

        Services services = services();

        Exception exception = null;

        for (CaseOutput output : outputs) {
            try {
                submitterIds.add(importDatasetForCaseOutput(user, output, services));
            } catch (Exception e) {
                log.error(e);
                exception = e;
            }
        }

        if (exception != null)
            throw new EmfException(exception.getMessage());

        return submitterIds.toArray(new String[0]);
    }

    public synchronized String importDatasetForCaseOutput(User user, CaseOutput output, Services services) 
            throws EmfException {
        String fileFolder = output.getPath();

        try {
            addOutputTasks(user, output, services, true);
            addTasksToSubmitter(importCaseOutputSubmitter, importOutputTasks);
        } catch (Exception e) {
            e.printStackTrace();
            setErrorMsgs(fileFolder, e);
            throw new EmfException(e.getMessage());
        }

        return importCaseOutputSubmitter.getSubmitterId();
    }

    private synchronized EmfDataset createDataset(String folder, String filename, String datasetName, User user,
            DatasetType datasetType, boolean forCaseOutput) throws Exception {
        EmfDataset dataset = new EmfDataset();
        File file = new File(folder, filename);

        String name = getCorrectedDSName(datasetName, datasetType);

        if (isNameUsed(name) && !forCaseOutput)
            throw new Exception("Dataset name " + name + " has been used.");

        CommonFileHeaderReader headerReader = new CommonFileHeaderReader(file);

        try {
            headerReader.readHeader();
        } catch (Exception e) {
            log.error("Error reading import file " + file.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            headerReader.close();
        }

        Date startDate = headerReader.getStartDate();
        Date endDate = headerReader.getEndDate();

        if ((startDate != null && !startDate.before(CustomDateFormat.parse_MMddyyyy("1/1/2200")))
                || (endDate != null && !endDate.before(CustomDateFormat.parse_MMddyyyy("1/1/2200")))) {
            log.warn("EMF_START_DATE: " + startDate + "; EMF_END_DATE: " + endDate);
            throw new EmfException(
                    "Invalid year or date format for EMF start/end date in file header (use MM/dd/YYYY hh:mm).");
        }
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);

        dataset.setCreator(user.getUsername());
        dataset.setCreatorFullName(user.getName());
        dataset.setDatasetType(datasetType);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(file.exists() ? new Date(file.lastModified()) : new Date());
        dataset.setAccessedDateTime(new Date());
        dataset.setStartDateTime(startDate);
        dataset.setStopDateTime(endDate);
        dataset.setTemporalResolution(headerReader.getTemporalResolution());

        return setDatasetProperties(folder, dataset, headerReader.getRegion(), headerReader.getProject(), headerReader
                .getSector(), headerReader.getCountry(), user);
    }

    private synchronized EmfDataset setDatasetProperties(String folder, EmfDataset dataset, String region,
            String project, String sector, String country, User user) {
        SectorsDAO sectorsDao = new SectorsDAO();
        ProjectsDAO projectsDao = new ProjectsDAO();
        RegionsDAO regionsDao = new RegionsDAO();
        IntendedUsesDAO intendedUsesDao = new IntendedUsesDAO();
        CountriesDAO countriesDao = new CountriesDAO();
        KeywordsDAO keywordsDAO = new KeywordsDAO();

        Session session = sessionFactory.getSession();
        String massStorageRoot = System.getProperty("MASS_STORAGE_ROOT");

        try {
            
            Project projectObj = null;
                
            if (project != null && !project.trim().isEmpty()) {
                projectObj = projectsDao.getProject(project, session);
            
            
                if (projectObj == null && user.isAdmin())
                    projectObj = projectsDao.addProject(new Project(project), session);
            
            
                if (projectObj == null)
                    log.warn("Project '" + project + "' cannot be added by user: " + user.getUsername());
            }
            
            dataset.setProject(projectObj);

            Region regionObj = regionsDao.getRegion(region, session);
            dataset.setRegion((regionObj == null && region != null) ? regionsDao.addRegion(new Region(region), session)
                    : regionObj);

            Country countryObj = countriesDao.getCountry(country, session);
            dataset.setCountry((countryObj == null && country != null) ? countriesDao.addCountry(new Country(country),
                    session) : countryObj);

            Sector sectorObj = sectorsDao.getSector(sector, session);

            if (sectorObj == null && sector != null && !sector.isEmpty())
                log.error("Sector " + sector + " does not exist in sectors table.");
            else
                dataset.setSectors(new Sector[] { sectorObj });

            if (massStorageRoot != null && folder.startsWith(massStorageRoot)) {
                Keyword massKey = new Keyword("MASS_STORAGE_LOCATION");
                Keyword loaded = keywordsDAO.add(massKey, session);
                KeyVal keyval = new KeyVal(loaded, folder);
                dataset.addKeyVal(keyval);
            }

            dataset.setIntendedUse(intendedUsesDao.getIntendedUse("public", sessionFactory.getSession()));
        } catch (HibernateException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            session.close();
        }

        return dataset;
    }

    private synchronized String getCorrectedDSName(String datasetName, DatasetType datasetType) {
        KeyVal[] keyVals = datasetType.getKeyVals();

        if (keyVals == null || keyVals.length == 0)
            return datasetName;

        String prefix = null;
        String suffix = null;

        for (KeyVal keyval : keyVals) {
            if (keyval.getName().equalsIgnoreCase("EXPORT_PREFIX"))
                prefix = keyval.getValue();

            if (keyval.getName().equalsIgnoreCase("EXPORT_SUFFIX"))
                suffix = keyval.getValue();

            if (prefix != null && suffix != null)
                break;
        }

        if (prefix != null && datasetName.startsWith(prefix))
            datasetName = datasetName.substring(prefix.length());

        if (suffix != null && datasetName.endsWith(suffix))
            datasetName = datasetName.substring(0, datasetName.length() - suffix.length());

        return datasetName;
    }

    public synchronized String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        try {
            File directory = new File(folder);
            FilePatternMatcher fpm = new FilePatternMatcher(directory, pattern);
            String[] allFilesInFolder = directory.list();
            String[] fileNamesForImport = fpm.matchingNames(allFilesInFolder);

            if (fileNamesForImport.length > 0)
                return fileNamesForImport;

            if (DebugLevels.DEBUG_11()) {
                System.out.println("ManagedImportService: File patterns passed: " + pattern);

                for (String file : fileNamesForImport)
                    System.out.println("ManagedImportService: File matched from the pattern: " + file);
            }

            throw new EmfException("No files found for pattern '" + pattern + "'");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad Folder+Pattern: " + folder + File.pathSeparator + pattern);
            throw new EmfException("No files found for pattern: " + pattern + " in folder " + folder);
        }
    }

    public String printStatusImportTaskManager() throws EmfException {
        return TaskManagerFactory.getImportTaskManager().getStatusOfWaitAndRunTable();
    }

    public Long getCaseOutputCount(Session session) {
        Long count = 0L;
        count = (Long) session.createQuery("select count(*) as total from " + QueueCaseOutput.class.getSimpleName())
                .uniqueResult();
        return count;
    }

    public synchronized void registerCaseOutputs(User user, CaseOutput[] outputs) throws EmfException {
        CaseDAO caseDao = new CaseDAO();
        Session session = sessionFactory.getSession();
        Exception exception = null;

        for (CaseOutput output : outputs) {
            try {
                QueueCaseOutput queueOutput = new QueueCaseOutput();
                queueOutput.poputlate(output);
                caseDao.addQueueCaseOutput(queueOutput, session);
            } catch (Exception e) {
                log.error(e);
                exception = e;
            }
        }

        try {
            if (ManagedImportService.numOfRunningThread == 0
                    || (ManagedImportService.runningThread != null && !ManagedImportService.runningThread.isAlive())) {
                executeCaseOutputRegistration(user);
            }
        } catch (HibernateException e) {
            log.error(e);
            exception = e;
        }

        if (session != null && session.isConnected())
            session.close();

        if (exception != null)
            throw new EmfException(exception.getMessage());
    }

    private synchronized void executeCaseOutputRegistration(final User user) {

        runningThread = new Thread(new Runnable() {
            public void run() {
                CaseDAO caseDao = null;
                Session session = null;

                try {
                    if (DebugLevels.DEBUG_17())
                        System.out.println("Current thread running the case output registration (id): "
                                + Thread.currentThread().getId());

                    caseDao = new CaseDAO();
                    session = sessionFactory.getSession();

                    List<QueueCaseOutput> caseOutputs = caseDao.getQueueCaseOutputs(session);
                    int numOutputs = caseOutputs.size();

                    while (numOutputs > 0) {

                        for (int i = 0; i < numOutputs; i++) {
                            if (DebugLevels.DEBUG_17())
                                System.out.println("Currently running the queued case output (id): "
                                        + caseOutputs.get(i).getId());

                            createNRunOutputTasks(user, caseOutputs.get(i));
                            removeQedOutput(caseOutputs.get(i), caseDao, session);
                        }

                        // Checking if new ones coming during the process
                        caseOutputs = caseDao.getQueueCaseOutputs(session);
                        numOutputs = caseOutputs.size();
                    }
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    ManagedImportService.numOfRunningThread = 0; // reset so that other thread can kick off

                    if (session != null && session.isConnected())
                        session.close();
                }
            } // end of run()
        }); // enf of new Thread

        if (DebugLevels.DEBUG_17()) {
            System.out.println("Ready to kick off the case output registration thread -- numOfRunningThread: "
                    + numOfRunningThread);
        }

        runningThread.start();
        numOfRunningThread++;

        if (DebugLevels.DEBUG_17())
            System.out.println("After kicking off the case output registration thread -- numOfRunningThread: "
                    + numOfRunningThread);
    }

    private void createNRunOutputTasks(final User user, QueueCaseOutput caseOutput) {
        try {
            createOutputTasksFromQ(user, caseOutput);
        } catch (Exception e) {
            log.error(e);
        }

        try {
            runOutputTasks();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void removeQedOutput(QueueCaseOutput qOutput, CaseDAO caseDao, Session session) {
        caseDao.removeQedOutput(qOutput, session);

        if (DebugLevels.DEBUG_17())
            System.out.println("qedOutput (id = " + qOutput.getId() + ") removed from QueueCaseOutput table.");

        session.flush();
        session.clear();
    }

    private void createOutputTasksFromQ(User user, QueueCaseOutput qoutput) throws Exception {
        importOutputTasks.removeAll(importOutputTasks);
        CaseOutput output = qoutput.convert2CaseOutput();
        Services services = services();

        addOutputTasks(user, output, services, false);
    }

    private void runOutputTasks() throws Exception {
        for (Iterator<Runnable> iter = importOutputTasks.iterator(); iter.hasNext();) {
            try {
                Runnable task = iter.next();
                task.run();
            } catch (Exception e) {
                log.error(e);
            }
        }

        importOutputTasks.removeAll(importOutputTasks); // make sure the list is cleared once all tasks done
    }

    private void setTempDirProperties() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null)
                System.setProperty("IMPORT_EXPORT_TEMP_DIR", eximTempDir.getValue());
        } finally {
            session.close();
        }
    }

    private void setMassStorageProperties() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty massStorageRoot = new EmfPropertiesDAO().getProperty("MASS_STORAGE_ROOT", session);

            if (massStorageRoot != null)
                System.setProperty("MASS_STORAGE_ROOT", massStorageRoot.getValue());
        } finally {
            session.close();
        }
    }

}
