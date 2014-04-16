package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.ManagedCaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.CopyTaskManager;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CopyTask extends Task {
    
    @Override
    public boolean isEquivalent(Task task) { //NOTE: needs to verify definition of equality
        CopyTask copyTask = (CopyTask) task;
        
        if (this.copyToCase.getName().equalsIgnoreCase(copyTask.getCase().getName())){
            return true;
        }
        
        return false;
    }

    private static Log log = LogFactory.getLog(ImportTask.class);

    //private Importer importer;

    private Case copyToCase;

    private HibernateSessionFactory sessionFactory;
    
    //private LoggingServiceImpl loggingService;

    private double numSeconds;  
    
    private CaseDAO dao;

    private DbServerFactory dbServerFactory;
    
    private ManagedCaseService caseService;

    public CopyTask(Case copyCase, User user, Services services,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory, 
            ManagedCaseService caseService) {
        super();
        createId();
        
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + createId());
        
        this.user = user;
        this.copyToCase = copyCase;
        this.statusServices = services.getStatus();
//        this.loggingService = services.getLoggingService();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.caseService = caseService;
        this.dao = new CaseDAO(sessionFactory);
    }

    public void run() {
        if (DebugLevels.DEBUG_1())
            System.out.println(">>## CopyTask:run() " + taskId + " for case: " + this.copyToCase.getName());
        if (DebugLevels.DEBUG_1())
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1())
            if (DebugLevels.DEBUG_1())
                System.out.println("Task# " + taskId + " running");
        
        Session session = null;
        DbServer dbServer = null;
        boolean isDone = false;
        String errorMsg = "";
        
        try {
            dbServer = dbServerFactory.getDbServer();
            
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            
            addStartStatus();
            copySingleCaseObj(copyToCase, user);
            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            //complete(session, "Imported");
            isDone = true;
        } catch (Exception e) {
            errorMsg += e.getMessage();
            // this doesn't give the full path for some reason
            logError("Copy failed for user (" + user.getUsername() + ") at " + new Date().toString() + " -- " , e);
            //removeCase(copyToCase);
        } finally {
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
        }
    }


    public Case getCase(){
        return this.copyToCase;
    }
//
//    private void updateVersionNReleaseLock(Version locked) throws EmfException {
//        DataServiceImpl dataServiceImpl = new DataServiceImpl(dbServerFactory, sessionFactory);
//        try {
//            dataServiceImpl.updateVersionNReleaseLock(locked);
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        } finally {
//            //
//        }

//    }

//    protected void prepare(Session session) throws EmfException {
//        addStartStatus();
//        //copyCase.sets.setStatus("Started import");
//        addCase(copyToCase, session);
//    }

//    protected void complete(Session session, String status) {
//        c.setStatus(status);
//        updateDataset(dataset, session);
//    }


    protected void addCase(Case case1, Session session) throws EmfException {
        try {
            if (dao.nameUsed(case1.getName(), Case.class, session))
                throw new EmfException("The selected Dataset name is already in use");
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        }

        dao.add(case1, session);
    }

//    private void removeCase(Case case1) {
//        try {
//            Session session = sessionFactory.getSession();
//            dao.remove(case1, session);
//            session.close();
//        } catch (Exception e) {
//            logError("Could not get remove Case - " + case1.getName(), e);
//        }
//    }

    private void addStartStatus() {
        setStatus("started", "Started copy of " + copyToCase.getName() + " [" + copyToCase.getCaseCategory() + "] ");
    }

    private void addCompletedStatus() {
        String message = "Completed copy of " + copyToCase.getName() + " [" + copyToCase.getCaseCategory() + "] " 
               + " in " + numSeconds+" seconds "; //TODO: add batch size to message once available
        setStatus("completed", message);
    }
    
    private void addFailedStatus(String errorMsg) {
        setStatus("failed", "Failed copy of " + copyToCase.getName() + ". Reason: " + errorMsg);
    }

    private void setStatus(String status, String message) {
        CopyTaskManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(), message);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }
    
//    public Case copyCase() {
//        return this.copyToCase;
//    }
//    
//    public Importer getImporter() {
//        return this.importer;
//    }
//    
    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }
    
    private synchronized void copySingleCaseObj(Case toCopy, User user) throws Exception {
        Case copied = (Case) DeepCopy.copy(toCopy);
        copied.setName(getUniqueNewName("Copy of " + toCopy.getName()));
        copied.setTemplateUsed(toCopy.getName());
        copied.setAbbreviation(null);
        copied.setLastModifiedBy(user);
        copied.setLastModifiedDate(new Date());
        copied.setProject(null);
        Case loaded = addCopiedCase(copied, user);
        copyCaseJobs(toCopy.getId(), loaded.getId(), user); // copy job first for references in input and parameter
        copyCaseInputs(user, toCopy.getId(), loaded.getId());
        copyCaseParameters(user, toCopy.getId(), loaded.getId());

        Session session = sessionFactory.getSession();

        try {

            // NOTE: Verify why locked?
            // NOTE: it could be being edited by other user, but you still want to copy it
            if (loaded.isLocked())
                dao.forceReleaseLocked(loaded, session);
        } finally {
            session.close();
        }

        //return loaded;
    }
    
    private String getUniqueNewName(String name) {
        Session session = sessionFactory.getSession();
        List<String> names = new ArrayList<String>();

        try {
            List<Case> allCases = dao.getCases(session);

            for (Iterator<Case> iter = allCases.iterator(); iter.hasNext();) {
                Case caseObj = iter.next();
                if (caseObj.getName().startsWith(name)) {
                    names.add(caseObj.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all cases.\n" + e.getMessage());
        } finally {
            session.close();
        }

        if (names.size() == 0)
            return name;

        return name + " " + getSequence(name, names);
    }
    
    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            // NOTE: Assume one case won't be copied 10000 times.
            // This is farely safe assuming the random number do not duplicate.
            return Math.abs(new Random().nextInt()) % 10000;
        }
    }
    
    private synchronized Case addCopiedCase(Case element, User user) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            return dao.update(locked, session);
        } catch (RuntimeException e) {
            log.error("Could not add case " + element, e);
            throw new EmfException("Could not add case " + element);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    private synchronized void copyCaseJobs(int toCopyCaseId, int copiedCaseId, User user) throws Exception {
        CaseJob[] tocopy = caseService.getCaseJobs(toCopyCaseId);
        CaseJob[] copiedJobs = new CaseJob[tocopy.length];

        for (int i = 0; i < tocopy.length; i++)
            copiedJobs[i] = copySingleJob(tocopy[i], copiedCaseId, user);

        CaseJob[] depencyUpdated = resetDependentJobIds(copiedJobs, tocopy);

        try {
            for (CaseJob job : depencyUpdated)
                dao.updateCaseJob(job);
        } catch (Exception e) {
            log.error("Cannot update copied jobs with their dependent jobs.", e);
        }
    }
    
    private synchronized void copyCaseInputs(User user, int origCaseId, int copiedCaseId) throws Exception {
        CaseInput[] tocopy = caseService.getCaseInputs(origCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleInput(user, tocopy[i], copiedCaseId);
    }  

    private synchronized CaseInput copySingleInput(User user, CaseInput input, int copiedCaseId) throws Exception {
        CaseInput copied = (CaseInput) DeepCopy.copy(input);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(input.getCaseJobID(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setCaseJobID(copiedJob.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            session.close();
        }

        return caseService.addCaseInput(user, copied, true);
    }
    
    
    private synchronized void copyCaseParameters(User user, int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseParameter[] tocopy = caseService.getCaseParameters(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleParameter(user, tocopy[i], copiedCaseId);
    }

    private synchronized CaseJob copySingleJob(CaseJob job, int copiedCaseId, User user) throws Exception {
        job.setRunCompletionDate(new Date());
        job.setRunStartDate(new Date());
        CaseJob copied = (CaseJob) DeepCopy.copy(job);
        copied.setCaseId(copiedCaseId);
        copied.setJobkey(null); // jobkey supposedly generated when it is run
        copied.setRunstatus(null);
        copied.setRunLog(null);
        copied.setRunStartDate(null);
        copied.setRunCompletionDate(null);
        copied.setRunJobUser(null); // no running user at this time
        copied.setUser(user); // the user who makes the copy should be the owner of the copy
        copied.setIdInQueue(null);

        return caseService.addCaseJob(user, copied, true);
    }

    private synchronized CaseParameter copySingleParameter(User user, CaseParameter parameter, int copiedCaseId)
    throws Exception {
        CaseParameter copied = (CaseParameter) DeepCopy.copy(parameter);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(parameter.getJobId(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setJobId(copiedJob.getId());
            }
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

        return caseService.addCaseParameter(user, copied, true);
    }
    
    private synchronized CaseJob[] resetDependentJobIds(CaseJob[] copiedJobs, CaseJob[] toCopyJobs) {
        HashMap<String, String> origJobMap = new HashMap<String, String>();
        HashMap<String, String> copiedJobMap = new HashMap<String, String>();

        int size = copiedJobs.length;

        for (int i = 0; i < size; i++) {
            origJobMap.put(toCopyJobs[i].getId() + "", toCopyJobs[i].getName());
            copiedJobMap.put(copiedJobs[i].getName(), copiedJobs[i].getId() + "");
        }

        for (int j = 0; j < size; j++) {
            DependentJob[] depJobs = copiedJobs[j].getDependentJobs();
            ArrayList jobsToKeep = new ArrayList(); // AME: added per Qun

            if (depJobs != null && depJobs.length > 0) {
                for (int k = 0; k < depJobs.length; k++) {
                    String jobName = origJobMap.get(depJobs[k].getJobId() + "");
                    String jobId = copiedJobMap.get(jobName);
                    int id = 0;

                    try {
                        id = Integer.parseInt(jobId);
                        depJobs[k].setJobId(id);
                        jobsToKeep.add(depJobs[k]);
                    } catch (Exception e) {
                        // NOTE: discard the dependency if the job depended on doesn't exist.
                    }

                }
            }
            DependentJob[] remainingJobs = new DependentJob[jobsToKeep.size()];
            jobsToKeep.toArray(remainingJobs);
            copiedJobs[j].setDependentJobs(remainingJobs);
        }

        return copiedJobs;
    }

}
