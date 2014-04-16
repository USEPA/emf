package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.cost.ControlStrategyInventoryOutputTask;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QAProgramRunner;
import gov.epa.emissions.framework.services.qa.QueryToString;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class CaseServiceImpl implements CaseService {
    private static Log log = LogFactory.getLog(CaseServiceImpl.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    private CaseDAO dao;

    private PooledExecutor threadPool;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbFactory;

    private ManagedCaseService caseService;
    
    private CaseAssistanceService assistanceService;
    
    public CaseServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public CaseServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbFactory) {
        this.sessionFactory = sessionFactory;
        this.dbFactory = dbFactory;
        this.dao = new CaseDAO();
        
        if (DebugLevels.DEBUG_0())
            System.out.println("CaseServiceImpl::getCaseService  Is sessionFactory null? " + (sessionFactory == null));

        myTag();
        if (DebugLevels.DEBUG_0())
            System.out.println(myTag());
        threadPool = createThreadPool();

    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    @Override
    protected void finalize() throws Throwable {
        this.sessionFactory = null;
        this.dbFactory = null;
        this.dao = null;
        
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private synchronized ManagedCaseService getCaseService() {
        log.info("CaseServiceImpl::getCaseService");

        if (caseService == null) {
            try {

                caseService = new ManagedCaseService(dbFactory, sessionFactory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.caseService;
    }

    private synchronized CaseAssistanceService getCaseAssistanceService() {
        log.info("CaseServiceImpl::getCaseAssistanceService");

        if (assistanceService == null) {
            try {
                assistanceService = new CaseAssistanceService(sessionFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.assistanceService;
    }
    
    public Case addCase(User user, Case element) throws EmfException {
        return getCaseService().addCase(user, element);
    }

    public void removeCase(Case caseObj) throws EmfException {
        getCaseService().removeCase(caseObj);
    }

    public Case[] getCases() throws EmfException {
        return getCaseService().getCases();
    }
    
    public Case[] getCases(String nameContains) throws EmfException {
        return getCaseService().getCases(nameContains);
    }
    
    public Case reloadCase(int caseId) throws EmfException {
        return getCaseService().getCase(caseId);
    }
    
    public Case getCaseFromName(String name) throws EmfException {
        return getCaseService().getCaseFromName(name);
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        return getCaseService().getAbbreviations();
    }

    public Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException {
        return getCaseService().addAbbreviation(abbr);
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        return getCaseService().getAirQualityModels();
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        return getCaseService().getCaseCategories();
    }

    public CaseCategory addCaseCategory(CaseCategory element) throws EmfException {
        return getCaseService().addCaseCategory(element);
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        return getCaseService().getEmissionsYears();
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        return getCaseService().getMeteorlogicalYears();
    }

    public Speciation[] getSpeciations() throws EmfException {
        return getCaseService().getSpeciations();
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        return getCaseService().obtainLocked(owner, element);
    }

    public Case releaseLocked(User owner, Case locked) throws EmfException {
        return getCaseService().releaseLocked(owner, locked);
    }

    public Case updateCase(Case element) throws EmfException {
        return getCaseService().updateCase(element);
    }

    public InputName[] getInputNames() throws EmfException {
        return getCaseService().getInputNames();
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        return getCaseService().getInputEnvtVars();
    }

    public CaseProgram[] getPrograms() throws EmfException {
        return getCaseService().getPrograms();
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        return getCaseService().getModelToRuns();
    }

    public InputName addCaseInputName(InputName name) throws EmfException {
        return getCaseService().addCaseInputName(name);
    }

    public CaseProgram addProgram(CaseProgram program) throws EmfException {
        return getCaseService().addProgram(program);
    }

    public InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        return getCaseService().addInputEnvtVar(inputEnvtVar);
    }

    public ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        return getCaseService().addModelToRun(model);
    }

    public SubDir[] getSubDirs() throws EmfException {
        return getCaseService().getSubDirs();

    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        return getCaseService().addSubDir(subdir);
    }

    public CaseInput addCaseInput(User user, CaseInput input) throws EmfException {
        return getCaseService().addCaseInput(user, input, false);
    }
    
    public void addCaseInputs(User user, int caseId, CaseInput[] inputs) throws EmfException {
        getCaseService().addCaseInputs(user, caseId, inputs);
    }

    public void updateCaseInput(User user, CaseInput input) throws EmfException {
        getCaseService().updateCaseInput(user, input);
    }

    public void removeCaseInputs(User user, CaseInput[] inputs) throws EmfException {
        getCaseService().removeCaseInputs(user, inputs);
    }

    public void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset) throws EmfException {
        getCaseService().removeCaseOutputs(user, outputs, deleteDataset);
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        return getCaseService().getCaseInputs(caseId);
    }
    
    public CaseInput[] getCaseInputs(int caseId, int[] jobIds) throws EmfException {
        return getCaseService().getCaseInputs(caseId, jobIds);
    }
    
    public CaseInput[] getCaseInputs(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return getCaseService().getCaseInputs(pageSize, caseId, sector, envNameContains, showAll);
    }
    
    public void copyCaseObject(int[] toCopy, User user) throws EmfException {
        getCaseService().copyCaseObject(toCopy, user);
    }

    public CaseJob addCaseJob(User user, CaseJob job) throws EmfException {
        return getCaseService().addCaseJob(user, job, false);
    }
    
    public void addCaseJobs(User user, int caseId, CaseJob[] jobs) throws EmfException {
        getCaseService().addCaseJobs(user, caseId, jobs);
    }

    public CaseJob[] getCaseJobs(int caseId) throws EmfException {
        return getCaseService().getCaseJobs(caseId);
    }

    public CaseJob getCaseJob(int jobId) throws EmfException {
        return getCaseService().getCaseJob(jobId);
    }
    
//    public Sector[] getSectorsUsedbyJobs(int caseId) throws EmfException {
//        return getCaseService().getSectorsUsedbyJobs(caseId);
//    }

    public JobRunStatus[] getJobRunStatuses() throws EmfException {
        return getCaseService().getJobRunStatuses();
    }

    public Executable[] getExecutables(int casejobId) throws EmfException {
        return getCaseService().getExecutables(casejobId);
    }

    public void removeCaseJobs(User user, CaseJob[] jobs) throws EmfException {
        getCaseService().removeCaseJobs(user, jobs);
    }

    public void updateCaseJob(User user, CaseJob job) throws EmfException {
        getCaseService().updateCaseJob(user, job);
    }
    
    public void updateCaseJobStatus(CaseJob job) throws EmfException {
        getCaseService().updateCaseJobStatus(job);
    }
    
    public void saveCaseJobFromClient(User user, CaseJob job) throws EmfException {
        getCaseService().saveCaseJobFromClient(user, job);
    }

    public Host[] getHosts() throws EmfException {
        return getCaseService().getHosts();
    }

    public Host addHost(Host host) throws EmfException {
        return getCaseService().addHost(host);
    }

    /*
     * The String re
     */
    public String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        try {
            if (DebugLevels.DEBUG_0())
                System.out.println("CaseServiceImpl::runJobs called at  " + new Date());
            if (DebugLevels.DEBUG_0())
                System.out.println("Called CaseServiceImpl::runJobs with Integer[] jobIds size of array= "
                        + jobIds.length);
            if (DebugLevels.DEBUG_0())
                System.out.println("runJobs for caseId=" + caseId + " and submitted by User= " + user.getUsername());
            for (int i = 0; i < jobIds.length; i++) {
                if (DebugLevels.DEBUG_0())
                    System.out.println(i + ": " + jobIds[i]);
            }
            if (DebugLevels.DEBUG_6())
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            // submit the list of CaseJobs to the ManagedService
            if (DebugLevels.DEBUG_14())
                System.out.println("Start submitting jobs. " + new Date());

            String msg = getCaseService().submitJobs(jobIds, caseId, user);

            if (DebugLevels.DEBUG_14())
                System.out.println("Jobs submitted. " + new Date());

            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    public void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        getCaseService().updateCaseParameter(user, parameter);
    }

    public Executable addExecutable(Executable exe) throws EmfException {
        return getCaseService().addExecutable(exe);
    }

    public ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        return getCaseService().addParameterEnvVar(envVar);
    }

    public ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        return getCaseService().getParameterEnvVars();
    }

    public ValueType addValueType(ValueType type) throws EmfException {
        return getCaseService().addValueType(type);
    }

    public ValueType[] getValueTypes() throws EmfException {
        return getCaseService().getValueTypes();
    }

    public ParameterName addParameterName(ParameterName name) throws EmfException {
        return getCaseService().addParameterName(name);
    }

    public ParameterName[] getParameterNames() throws EmfException {
        return getCaseService().getParameterNames();
    }

    public CaseParameter addCaseParameter(User user, CaseParameter param) throws EmfException {
        return getCaseService().addCaseParameter(user, param, false);
    }
    
    public void addCaseParameters(User user, int caseID, CaseParameter[] params) throws EmfException {
        getCaseService().addCaseParameters(user, caseID, params);
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var) throws EmfException {
        return getCaseService().getCaseParameter(caseId, var);
    }
    
    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        return getCaseService().getCaseParameters(caseId);
    }
    
    public CaseParameter[] getCaseParameters(int caseId, int[] jobIds) throws EmfException {
        return getCaseService().getCaseParameters(caseId, jobIds);
    }
    
    public CaseParameter[] getCaseParameters(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return getCaseService().getCaseParameters(pageSize, caseId, sector, envNameContains, showAll);
    }

    public void removeCaseParameters(User user, CaseParameter[] params) throws EmfException {
        getCaseService().removeCaseParameters(user, params);
    }

    public void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {
        if (DebugLevels.DEBUG_0())
            System.out
                    .println("CaseServiceImpl::exportCaseInputs Total inputs size for export= " + caseInputIds.length);
        getCaseService().exportCaseInputs(user, caseInputIds, purpose);
    }

    public void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose) throws EmfException {
        if (DebugLevels.DEBUG_0())
            System.out.println("CaseServiceImpl::exportCaseInputsWithOverwrite Total inputs size for export= "
                    + caseInputIds.length);
        getCaseService().exportCaseInputsWithOverwrite(user, caseInputIds, purpose);

    }

    // ****************************************************************

    // ****************************
    public void submitJob(CaseJob job, User user, Case jobCase) throws EmfException {
        /**
         * runs the job.
         * 
         * Creates a job run file which sets up the environment including input files and parameters for the run script
         * defines the run script, creates a log file, exports the inputs, and then executes it.
         */

        if (false)
            throw new EmfException("");
        // FIXME: block commenst for test of Jobsubmitter
        /*
         * String executionStr = null; String logFileName = null; // Job run file name File ofile = this.getOfile(job); //
         * Create job script writeJobFile(job, user, ofile); // get log file for job script try { logFileName =
         * getLog(ofile); } catch (Exception e) { e.printStackTrace(); throw new EmfException(e.getMessage()); }
         * 
         */
        // FIXME: block ABOVE comments for test of Jobsubmitter
        // Export all inputs
        // List<CaseInput> inputs = caseService.getAllJobInputs(job);
        // if (DebugLevels.DEBUG_0()) System.out.println("Number of inputs for this job: " + inputs.size());
        // FIXME: Need to flesh this string out
        // purpose = "Used by CaseName and JobName"
        String purpose = "Used by job: " + job.getName() + " of Case: " + jobCase.getName();

        if (DebugLevels.DEBUG_6())
            System.out.println("User: " + user.getName());
        if (DebugLevels.DEBUG_6())
            System.out.println("Purpose: " + purpose);
        if (DebugLevels.DEBUG_6())
            System.out.println("Job: " + job.getName());
        if (DebugLevels.DEBUG_6())
            System.out.println("Job Case: " + jobCase.getName());
        // if (DebugLevels.DEBUG_6())
        // System.out.println("Case Inputs null? : " + (inputs == null));

        // pass the inputs to the exportJobSubmitter
        // String caseJobSubmitterId = exportService.exportForJob(user, inputs, purpose, job, jobCase);

        // if (DebugLevels.DEBUG_6())
        // System.out.println("Submitter Id for case job:" + caseJobSubmitterId);

        // FIXME: Commented out for job submitter
        // Create an execution string and submit job to the queue,
        // if the key word $EMF_JOBLOG is in the queue options,
        // replace w/ log file
        // String queueOptions = job.getQueOptions();
        // String queueOptionsLog = queueOptions.replace("$EMF_JOBLOG", logFileName);
        // if (queueOptionsLog.equals("")) {
        // executionStr = ofile.toString();
        // } else {
        // executionStr = queueOptionsLog + " " + ofile;
        // }
        //
        // /*
        // * execute the job script Note if hostname is localhost this is done locally w/o ssh and stdout and stderr is
        // * redirected to the log. This redirect is currently shell specific (should generalize) if hostname is not
        // * localhost it is through ssh
        // */
        // String username = user.getUsername();
        // String hostname = job.getHost().getName();
        // if (hostname.equals("localhost")) {
        // // execute on local machine
        // executionStr = executionStr + " " + this.runRedirect + " " + logFileName;
        // log.warn("Local Job execution string: " + executionStr);
        // RemoteCommand.executeLocal(executionStr);
        // } else {
        // // execute on remote machine and log stdout
        // InputStream inStream = RemoteCommand.execute(username, hostname, executionStr);
        // String outTitle = "stdout from (" + hostname + "): " + executionStr;
        // RemoteCommand.logStdout(outTitle, inStream);
        // }

    }

    // ****************************

    public void runJobs(CaseJob[] jobs, User user) throws EmfException {

        // need to create this user obj as final b.c. passed to threading
        // final User localUser = user;
        if (false)
            throw new EmfException("");
        // fill in access to jobTaskManager-- for now just loop over the
        // the jobs
        // for (CasseJob job : jobs) {
        // try {
        // Case jobCase = getCase(job.getCaseId());
        //
        // // set status as submitted and run the individual job
        // // setStatus(localUser, "Job " + job.getName() + " submitted for case " + jobCase + ".", "Run Job");
        // submitJob(job, user, jobCase);
        // } catch (Exception e) {
        // log.error("Could not run case job " + job.getName() + ".", e);
        // throw new EmfException(e.getMessage());
        // }
        //
        // }

        if (DebugLevels.DEBUG_0())
            System.out.println("Called CaseServiceImpl::runJobs with CaseJob[] size of CaseJobs= " + jobs.length);
        System.out.println("Called CaseServiceImpl::runJobs with CaseJob[] size of CaseJobs= " + jobs.length);
    }

    // for command line client
    public int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        return getCaseService().recordJobMessage(message, jobKey);
    }

    //NOTE: not used any more
    public int recordJobMessage(JobMessage[] msgs, String[] keys) throws EmfException {
        int msgLength = msgs.length;
        int returnVal = 0;

        if (msgs.length != keys.length) {
            throw new EmfException("Error recording job messages: Number of job messages (" + msgs.length
                    + ") doesn't match number of job keys (" + keys.length + ")");
        }

        for (int i = 0; i < msgLength; i++)
            returnVal = recordJobMessage(msgs[i], keys[i]);

        return returnVal;
    }

    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        return getCaseService().getJobMessages(caseId, jobId);
    }

    public CaseJob[] getAllValidJobs(int jobId, int caseId) throws EmfException {
        return getCaseService().getAllValidJobs(jobId, caseId);
    }

    public CaseJob[] getDependentJobs(int jobId) throws EmfException {
        return getCaseService().getDependentJobs(jobId);
    }

    public int[] getJobIds(int caseId, String[] jobNames) throws EmfException {
        return getCaseService().getJobIds(caseId, jobNames);
    }

    public String restoreTaskManagers() throws EmfException {
        return getCaseService().restoreTaskManagers();
    }

    public String printStatusCaseJobTaskManager() throws EmfException {
        return getCaseService().printStatusCaseJobTaskManager();
    }

    public Case[] getCases(CaseCategory category) {
        return getCaseService().getCases(category);
    }
    
    public Case[] getCases(CaseCategory category, String nameContains) throws EmfException {
        if (nameContains == null || nameContains.trim().length() == 0 || nameContains.trim().equals("*"))
           return getCaseService().getCases(category);
      
        return getCaseService().getCases(category, nameContains);     
    }

    public String validateJobs(Integer[] jobIDs) throws EmfException {
        if (DebugLevels.DEBUG_14())
            System.out.println("Start validating jobs. " + new Date());
        String msg = getCaseService().validateJobs(jobIDs);
        if (DebugLevels.DEBUG_14())
            System.out.println("Finished validating jobs. " + new Date());

        return msg;
    }

    public CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        return getCaseService().getCaseOutputs(caseId, jobId);
    }

    public void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        getCaseService().registerOutputs(outputs, jobKeys);
    }

    public synchronized Case updateCaseWithLock(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (!dao.canUpdate(caseObj, session))
                throw new EmfException("the case name is already in use");

            Case caseWithSameAbbr = dao.getCaseFromAbbr(caseObj.getAbbreviation(), session);

            if (caseWithSameAbbr != null && caseWithSameAbbr.getId() != caseObj.getId())
                throw new EmfException("the same case abbreviation has been used by another case: "
                        + caseWithSameAbbr.getName());

            Case caseWithLock = dao.updateWithLock(caseObj, session);

            return caseWithLock;
            // return dao.getById(csWithLock.getId(), session);
        } catch (Exception e) {
            if (e instanceof ConstraintViolationException) {
                throw new EmfException("Please check duplication: " + e.getLocalizedMessage() + ".");
            }
            
            log.error("Could not update Case: " + caseObj, e);
            throw new EmfException("Could not update Case: " + e.getMessage() + ".");
        }  finally {
            if (session != null && session.isConnected())
                session.close();
        }

    }

    public void updateCaseOutput(User user, CaseOutput output) throws EmfException {
        getCaseService().updateCaseOutput(user, output);
    }

    public void removeMessages(User user, JobMessage[] msgs) throws EmfException {
        getCaseService().removeMessages(user, msgs);

    }

    public CaseOutput addCaseOutput(User user, CaseOutput output) throws EmfException {
        return getCaseService().addCaseOutput(user, output);
    }

    
    public AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException {
        return getCaseService().addAirQualityModel(airQModel);
    }

    
    public EmissionsYear addEmissionsYear(EmissionsYear emissYear) throws EmfException {
        return getCaseService().addEmissionYear(emissYear);
    }
    
    public MeteorlogicalYear addMeteorologicalYear(MeteorlogicalYear metYear) throws EmfException {
        return getCaseService().addMeteorologicalYear(metYear);
    }

    
    public Speciation addSpeciation(Speciation speciation) throws EmfException {
        return getCaseService().addSpeciation(speciation);
    }
    
    public String getJobStatusMessage(int caseId) {
        return getCaseService().getJobStatusMessage(caseId);
    }

    public String[] getAllCaseNameIDs() throws EmfException {
        return getCaseService().getAllCaseNameIDs();
    }

    public Case mergeCases(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup, Case sensitivityCase) throws EmfException {
        return getCaseService().mergeCases(user, parentCaseId, templateCaseId, jobIds, jobGroup, sensitivityCase);
    }
    
    public Case addSensitivity2Case(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup, Case sensitivityCase, GeoRegion region) throws EmfException {
        return getCaseService().addSensitivity2Case(user, parentCaseId, templateCaseId, jobIds, jobGroup, sensitivityCase, region);
    }
    
    public String checkParentCase(Case caseObj) throws EmfException {
        return getCaseService().checkParentCase(caseObj);
    }

    public String validateNLInputs(int caseId) throws EmfException {
        return getCaseService().validateNLInputs(caseId);
    }

    public String validateNLParameters(int caseId) throws EmfException {
        return getCaseService().validateNLParameters(caseId);
    }

    public Case[] getSensitivityCases(int parentCaseId) throws EmfException {
        return getCaseService().getSensitivityCases(parentCaseId);
    }

    public String[] getJobGroups(int caseId) throws EmfException {
        return getCaseService().getJobGroups(caseId);
    }
    
    public Case[] getCasesThatInputToOtherCases(int caseId) {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCasesThatInputToOtherCases(caseId, session).toArray(new Case[0]);
        } finally {
            session.close();
        }
    }
    
    public Case[] getCasesThatOutputToOtherCases(int caseId) {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCasesThatOutputToOtherCases(caseId, session).toArray(new Case[0]);
        } finally {
            session.close();
        }
    }

    public void printCase(String folder, int caseId) throws EmfException {
        getCaseService().printCase(folder, caseId);
    }
    
    public String[] printLocalCase(int caseId) throws EmfException {
        return getCaseService().printLocalCase(caseId);
    }

    public Case[] getCasesByOutputDatasets(int[] datasetIds) {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCasesByOutputDatasets(datasetIds, session).toArray(new Case[0]);
        } finally {
            session.close();
        }
    }

    public Case[] getCasesByInputDataset(int datasetId) {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCasesByInputDataset(datasetId, session).toArray(new Case[0]);
        } finally {
            session.close();
        }
    }
    
    public void importCase(String folder, String[] files, User user) throws EmfException {
        getCaseAssistanceService().importCase(folder, files, user);
    }

    public String loadCMAQCase(String path, int jobId, int caseId, User user) throws EmfException {
        try {
            return getCaseAssistanceService().loadCMAQCase(path, jobId, caseId, user);
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    public int cancelJobs(int[] jobIds, User user) throws EmfException {
        return getCaseService().cancelJobs(jobIds, user);
    }
    
    public String[] isGeoRegionUsed(int caseId, GeoRegion[] grids) throws EmfException{
        return getCaseService().isGeoRegionUsed(caseId, grids);
    }
    
    public String isGeoRegionInSummary(int caseId, GeoRegion[] grids) throws EmfException{
        return getCaseService().isGeoRegionInSummary(caseId, grids);
    }

    public String getCaseComparisonResult(int[] caseIds) throws EmfException {
        DbServer dbServer = dbFactory.getDbServer();
        try {
            return new QueryToString(dbServer, new SQLCompareCasesQuery(sessionFactory).createCompareQuery(caseIds), ",").toString();
        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve case comparison result: " + e.getMessage(), e);
        } catch (ExporterException e) {
            throw new EmfException("Could not retrieve case comparison result: " + e.getMessage(), e);
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("ManagedCaseService: error closing db server. " + e.getMessage());
            }
        }
    }
    
    public synchronized String getCaseQaReports(User user, int[] caseIds, String gridName, 
            Sector[] sectors, String[] repDims, String whereClause, String serverDir) throws EmfException {
        // get dataset ids by using dataset names derived from gridName, case abbrev, 
        // sectors in case sectorlist input, state/county, speciation, and so on 
        //int[] datasetIds = getDatasetIds()
        try {
            long startTime = System.currentTimeMillis();
            
            RunQACaseReports runQACaseReports = new RunQACaseReports(user, dbFactory, dao, 
                    sessionFactory, serverDir);
            long endTime = System.currentTimeMillis();
            System.out.println("Ran QA Case Report in " + ((endTime - startTime) / (1000))  + " secs");
            String validationIssues = runQACaseReports.validateAndBuildReportSQL(caseIds, gridName, sectors, 
                    repDims, whereClause);

            threadPool.execute(new GCEnforcerTask("Run Case Output Comparison Report", runQACaseReports));
            return validationIssues;
        } catch (Exception e) {
            log.error("Error running Case Output Comparison Report", e);
            throw new EmfException(e.getMessage());
        }
    }
    
}
