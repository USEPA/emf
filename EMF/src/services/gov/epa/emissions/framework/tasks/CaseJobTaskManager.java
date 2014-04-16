package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseJobTask;
import gov.epa.emissions.framework.services.casemanagement.PersistedWaitTask;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CaseJobTaskManager implements TaskManager {
    private static Log log = LogFactory.getLog(CaseJobTaskManager.class);

    private static CaseDAO caseDAO = null;

    private static StatusDAO statusDAO = null;

    private static CaseJobTaskManager ref;

    private static int refCount = 0;

    private final int poolSize = 4;

    private final int maxPoolSize = 4;

    private final long keepAliveTime = 60;

    private static ArrayList<TaskSubmitter> submitters = new ArrayList<TaskSubmitter>();

    private static ThreadPoolExecutor threadPool = null;

    // PBQ is the queue for submitting jobs
    private static BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<Runnable>();

    private static ArrayBlockingQueue<Runnable> threadPoolQueue = new ArrayBlockingQueue<Runnable>(5);

    private static Hashtable<String, Task> runTable = new Hashtable<String, Task>();

    private static Hashtable<String, Task> waitTable = new Hashtable<String, Task>();

    private static Timer timer;

    private static HibernateSessionFactory sessionFactory;

    public static synchronized int getSizeofTaskQueue() {
        return taskQueue.size();
    }

    public static synchronized int getSizeofWaitTable() {
        return waitTable.size();
    }

    public static synchronized int getSizeofRunTable() {
        return runTable.size();
    }

    public static synchronized void shutDown() {
        if (DebugLevels.DEBUG_1())
            System.out.println("Shutdown called on Task Manager");
        taskQueue.clear();
        threadPoolQueue.clear();
        threadPool.shutdownNow();
    }

    public static synchronized void removeTask(Runnable task) {
        taskQueue.remove(task);
    }

    public static synchronized void removeTasks(ArrayList<?> tasks) {
        taskQueue.removeAll(tasks);
    }

    public static synchronized void registerTaskSubmitter(TaskSubmitter ts) {
        submitters.add(ts);
    }

    public static synchronized void deregisterSubmitter(TaskSubmitter ts) {
        if (DebugLevels.DEBUG_1())
            System.out.println("DeREGISTERED SUBMITTER: " + ts.getSubmitterId() + " Confirm task count= "
                    + ts.getTaskCount());
        submitters.remove(ts);
    }

    public synchronized void finalize() throws Throwable {
        if (DebugLevels.DEBUG_0())
            System.out.println("Finalizing TaskManager # of taskmanagers= " + refCount);

        shutDown();
    }

    // clone not supported needs to be added
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private CaseJobTaskManager(HibernateSessionFactory sessionFactory) {
        super();
        log.info("CaseJobTaskManager constructor");
        if (DebugLevels.DEBUG_0())
            System.out.println("CaseJob Task Manager created @@@@@ THREAD ID: " + Thread.currentThread().getId());

        refCount++;
        if (DebugLevels.DEBUG_4())
            System.out.println("CaseJobTask Manager created refCount= " + refCount);
        if (DebugLevels.DEBUG_4())
            System.out.println("Priority Blocking queue created? " + !(taskQueue == null));

        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, threadPoolQueue);
        if (DebugLevels.DEBUG_4())
            System.out.println("ThreadPool created? " + !(threadPool == null));
        if (DebugLevels.DEBUG_4())
            System.out.println("Initial # of jobs in Thread Pool: " + threadPool.getPoolSize());

        CaseJobTaskManager.sessionFactory = sessionFactory;
        caseDAO = new CaseDAO(sessionFactory);
        statusDAO = new StatusDAO(sessionFactory);

        // FIXME: Remove the next line after statusDAO is used in this class
        if (DebugLevels.DEBUG_9())
            System.out.println("Dummy: " + statusDAO.getClass().getName());
    }

    public static synchronized CaseJobTaskManager getCaseJobTaskManager(HibernateSessionFactory sessionFactory) {
        if (ref == null)
            ref = new CaseJobTaskManager(sessionFactory);
        return ref;
    }

    /**
     * Add a CaseJobTask to the PBQ
     * 
     * @throws EmfException
     */
    public static synchronized void addTask(CaseJobTask task) throws EmfException {
        if (DebugLevels.DEBUG_0())
            System.out.println("IN CaseJobTaskManager::add task= " + task.getTaskId() + " for job= " + task.getJobId());
        taskQueue.add(task);
        if (DebugLevels.DEBUG_0())
            System.out.println("IN CaseJobTaskManager size of task Queue= " + taskQueue.size());

        // Process the TaskQueue
        processTaskQueue();

    }

    public static synchronized void addTasks(ArrayList<Runnable> tasks) throws EmfException {

        if (DebugLevels.DEBUG_0())
            System.out.println("IN CaseJobTaskManager number of tasks received= " + tasks.size());
        taskQueue.addAll(tasks);
        if (DebugLevels.DEBUG_0())
            System.out.println("IN CaseJobTaskManager size of task Queue= " + taskQueue.size());

        synchronized (runTable) {
            if (threadPool.getCorePoolSize() - runTable.size() > 0) {
                processTaskQueue();
            }
        }// synchronized
    }

    public static synchronized void callBackFromThread(String taskId, String submitterId, String status, String[] msgs,
            String[] msgTypes, boolean regHistory) throws EmfException {
        if (DebugLevels.DEBUG_2())
            System.out.println("CaseJobTaskManager::callBackFromThread  refCount= " + refCount);
        if (DebugLevels.DEBUG_2())
            System.out.println("%%%% CaseJobTaskManager reports that Task# " + taskId + " for submitter= "
                    + submitterId + " completed with status= " + status + " and message= " + msgs);

        if (DebugLevels.DEBUG_0())
            System.out.println("***BELOW*** In callback the sizes are ***BELOW***");
        if (DebugLevels.DEBUG_0())
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
        if (DebugLevels.DEBUG_0())
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
        if (DebugLevels.DEBUG_0())
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());
        if (DebugLevels.DEBUG_0())
            System.out.println("***ABOVE*** In callback the sizes are shown ***ABOVE***");

        if (updateRunStatus(taskId, status, msgs, msgTypes, regHistory))
            synchronized (waitTable) {
                waitTable.remove(taskId);
            }

        if (DebugLevels.DEBUG_0())
            System.out.println("After Task removed Size of RUN TABLE: " + getSizeofRunTable());

        processTaskQueue();
    }

    private static boolean updateRunStatus(String taskId, String status, String[] msgs, String[] msgTypes,
            boolean regHistory) throws EmfException {
        System.out.println("CaseJobTaskManager::updateRunStatus: " + taskId + " status= " + status);

        CaseJobTask cjt = null;
        String jobStatus = "";
        CaseJob caseJob = null;
        boolean toRemove = false;

        try {

            if ((status.equals("completed")) || (status.equals("failed"))) {
                System.out.println("CaseJobTaskManager::updateRunStatus: " + status);
                synchronized (runTable) {
                    cjt = (CaseJobTask) runTable.get(taskId);

                    if (!(cjt == null)) {
                        System.out.println("Details of CJT: " + cjt.getJobName());
                        runTable.remove(taskId);
                        if (DebugLevels.DEBUG_0())
                            System.out
                                    .println("updateRunStatus Before remove from runTable on completed or failed job in thread");

                        // Now remove the task from the persisted wait table
                        caseDAO.removePersistedTask(new PersistedWaitTask(cjt.getJobId(), cjt.getCaseId(), cjt
                                .getUser().getId()));
                        if (DebugLevels.DEBUG_0())
                            System.out
                                    .println("updateRunStatus After remove from runTable on completed or failed job in thread");

                    }
                }

                if (cjt == null) {
                    // CaseJobTask was null because it was not a running job therefore get from waitTable
                    System.out
                            .println("CaseJobTask was null because it was not a running job therefore get from waitTable");
                    synchronized (waitTable) {
                        cjt = (CaseJobTask) waitTable.get(taskId);

                        if (cjt == null) {
                            System.out
                                    .println("Job task (ID: " + taskId + ") has already been removed from waitTable.");
                            toRemove = false;
                        } else {
                            System.out.println("Details of CJT: " + cjt.getJobName());
                            System.out.println("CaseJobTask Id for failed exports = " + cjt.getJobId());
                            System.out.println("CaseJobTask Id for failed exports = " + cjt.getTaskId());
                            System.out.println("Size of the waitTable before remove: " + waitTable.size());
                            // waitTable.remove(taskId);
                            toRemove = true;
                            if (DebugLevels.DEBUG_0())
                                System.out
                                        .println("updateRunStatus Before remove from runTable on completed or failed job in thread");

                            if (DebugLevels.DEBUG_0())
                                System.out
                                        .println("updateRunStatus After remove from runTable on completed or failed job in thread");
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Size of the waitTable after remove: " + waitTable.size());

                            // Now remove the task from the persisted wait table
                            caseDAO.removePersistedTask(new PersistedWaitTask(cjt.getJobId(), cjt.getCaseId(), cjt
                                    .getUser().getId()));
                        }
                    }
                }
            }

            System.out.println("After removal from Run Table is the CJT null? " + (cjt == null));

            if (status.equals("export failed")) {
                System.out.println("CaseJobTaskManager::updateRunStatus:  export failed");
                synchronized (waitTable) {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("Export Failed");
                    cjt = (CaseJobTask) waitTable.get(taskId);
                    if (DebugLevels.DEBUG_9()) {
                        System.out.println("Details of CJT: " + cjt.getJobName());
                        System.out.println("CaseJobTask Id for failed exports = " + cjt.getJobId());
                        System.out.println("CaseJobTask Id for failed exports = " + cjt.getTaskId());
                        System.out.println("Size of the waitTable before remove: " + waitTable.size());
                    }
                    // waitTable.remove(taskId);
                    toRemove = true;
                    if (DebugLevels.DEBUG_0())
                        System.out
                                .println("updateRunStatus Before remove from runTable on completed or failed job in thread");

                    // Now remove the task from the persisted wait table
                    caseDAO.removePersistedTask(new PersistedWaitTask(cjt.getJobId(), cjt.getCaseId(), cjt.getUser()
                            .getId()));
                    if (DebugLevels.DEBUG_0())
                        System.out
                                .println("updateRunStatus After remove from runTable on completed or failed job in thread");
                    if (DebugLevels.DEBUG_9()) {
                        System.out.println("CJTM::updateRunStatus return from removedPersistedTasks 3 (exportFailed)");
                        System.out.println("Size of the waitTable after remove: " + waitTable.size());
                    }
                }
            }

            if (status.equals("export succeeded")) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("CaseJobTaskManager::updateRunStatus:  export success");
                synchronized (waitTable) {
                    cjt = (CaseJobTask) waitTable.get(taskId);
                    if (DebugLevels.DEBUG_9())
                        System.out.println("Details of CJT: " + cjt.getJobName());
                }
            }

            if (DebugLevels.DEBUG_9())
                if (DebugLevels.DEBUG_9())
                    System.out.println("CJTM::updateRunStatus is casejobtask null? " + (cjt == null));

            // if this was a forced failed due to dependencies failing the casejobtask needs to come from
            // the wait queue since cjt will be null
            // if (cjt == null){
            // synchronized (waitTable) {
            // cjt = (CaseJobTask) waitTable.get(taskId);
            // }
            //                
            // }

            if (DebugLevels.DEBUG_9())
                System.out.println("Before getJobId is the CJT null? " + (cjt == null));

            // update the run status in the Case_CaseJobs
            int jid = cjt.getJobId();

            if (DebugLevels.DEBUG_9()) {
                System.out.println("Before getJobId jid= " + jid);
                System.out.println("after getJobId is the CJT null? " + (cjt == null));
            }

            caseJob = caseDAO.getCaseJob(jid);
            JobRunStatus jrs = caseJob.getRunstatus();

            String currentJobStatus;

            if (jrs == null) {
                currentJobStatus = "Failed";
            } else {
                currentJobStatus = jrs.getName();
            }

            if (DebugLevels.DEBUG_9())
                System.out.println("For JOB NAME= " + caseJob.getName() + "Incoming status flag= " + status
                        + ", Current database jobstatus= " + caseJob.getRunstatus().getName());

            if (DebugLevels.DEBUG_9())
                if (DebugLevels.DEBUG_9())
                    System.out.println("In CaseJobTaskManager::updateRunStatus for jobId= " + jid
                            + " Is the CaseJob null? " + (caseJob == null));

            boolean toUpdate = false;

            if (status.equals("completed") && (currentJobStatus.equalsIgnoreCase("Waiting"))) {
                if (DebugLevels.DEBUG_9())
                    System.out
                            .println("CaseJobTaskManager::updateRunStatus:  thread Status is completed current=Waiting set jobStatus=Submitted");
                jobStatus = "Submitted";
                caseJob.setIdInQueue(cjt.getQId());
                caseJob.setRunStartDate(new Date());
                toUpdate = true;
            }

            if (status.equals("failed")) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("CaseJobTaskManager::updateRunStatus:  jobStatus is Failed jobStatus=Failed");
                jobStatus = "Failed";
                caseJob.setRunCompletionDate(new Date());
                toUpdate = true;
            }

            if (status.equals("export succeeded")) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("CaseJobTaskManager::updateRunStatus:  export Succeeded jobStatus=Waiting");
                jobStatus = "Waiting";
                caseJob.setRunStartDate(new Date());
                toUpdate = true;
            }

            if (status.equals("export failed")) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("CaseJobTaskManager::updateRunStatus:  export FAILED jobStatus=FAILED");
                jobStatus = "Failed";
                caseJob.setRunStartDate(new Date());
                toUpdate = true;
            }

            if (toUpdate)
                updateJobWithHistory(msgs, msgTypes, regHistory, jobStatus, caseJob);
        } catch (Exception e) {
            if (DebugLevels.DEBUG_9())
                System.out.println("^^^^^^^^^^^^^^");
            e.printStackTrace();

            if (DebugLevels.DEBUG_9())
                System.out.println("^^^^^^^^^^^^^^");
            throw new EmfException(e.getMessage());
        }

        return toRemove;
    }

    private static void updateJobWithHistory(String[] msgs, String[] msgTypes, boolean regHistory, String jobStatus,
            CaseJob caseJob) {
        JobRunStatus jrStat = caseDAO.getJobRunStatuse(jobStatus);
        caseJob.setRunstatus(jrStat);
        caseJob.setRunLog(msgs[msgs.length - 1]);
        caseDAO.updateCaseJob(caseJob);

        if (regHistory) {
            for (int i = 0; i < msgs.length; i++) {
                if (msgs != null && !msgs[i].isEmpty()) {
                    JobMessage jobMsg = new JobMessage();
                    jobMsg.setCaseId(caseJob.getCaseId());
                    jobMsg.setJobId(caseJob.getId());
                    jobMsg.setMessageType(msgTypes[i]);
                    jobMsg.setMessage(msgs[i]);
                    jobMsg.setStatus(jrStat.getName());
                    jobMsg.setReceivedTime(new Date());
                    caseDAO.add(jobMsg);
                }
            }
        }
    }

    public static synchronized void processTaskQueue() throws EmfException {
        testAndSetWaitingTasksDependencies();

        // FIXME: Dump the waitTable below. Remove after testing
        synchronized (waitTable) {
            Collection<Task> all = waitTable.values();
            Iterator<Task> iter = all.iterator();
            while (iter.hasNext()) {
                CaseJobTask cjt = (CaseJobTask) iter.next();
                boolean readyF = cjt.isReady();
                boolean expSucceed = cjt.isExportsSuccess();
                boolean dependSet = cjt.isDependenciesSet();
                String name = cjt.getJobName();
                String dump = "[" + readyF + "/" + expSucceed + "/" + dependSet + "/" + "]";
                String mesg = "CJT::processTaskQueue for CJT/Job= " + name + " Flags Ready/exports/depends are " + dump;
                if (DebugLevels.DEBUG_9())
                    System.out.println(mesg);
            }

        }// synch waitTable
        // FIXME: Dump the waitTable above. Remove after testing

        int threadsAvail = -99;

        if (DebugLevels.DEBUG_0()) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(">>>>> BEGIN CaseJobTaskManager::processTaskQueue() *** " + new Date());
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());
            System.out.println("Before processing the taskQueue");
            System.out.println("SIZE OF TASKQUEUE: " + getSizeofTaskQueue());
        }

        boolean done = false;
        while (!done) {
            if (getSizeofTaskQueue() == 0) {
                if (DebugLevels.DEBUG_0())
                    System.out.println("#tasks in taskQueue == 0?? Breaking out of taskQueue TEST loop: ");
                done = true;
            } else {
                if (DebugLevels.DEBUG_0())
                    System.out.println("Before Peek into taskQueue: " + getSizeofTaskQueue());
                if (taskQueue.peek() != null) {
                    if (DebugLevels.DEBUG_0())
                        System.out.println("Peak into taskQueue has an object in head: " + getSizeofTaskQueue());

                    try {
                        CaseJobTask nextTask = (CaseJobTask) taskQueue.take();
                        if (DebugLevels.DEBUG_9())
                            System.out
                                    .println("&&&&& In CaseJobTaskManager::processQueue pop the queue the type of TASK objects nextTask is : "
                                            + nextTask.getClass().getName());

                        // number of threads available before inspecting the priority blocking queue
                        synchronized (threadPool) {
                            // threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
                            threadsAvail = threadPool.getCorePoolSize() - runTable.size();

                        }// synchronized
                        if (DebugLevels.DEBUG_0())
                            System.out.println("Number of threads available before taskQueue PDQ: " + threadsAvail);

                        if (threadsAvail == 0) {
                            synchronized (waitTable) {
                                waitTable.put(nextTask.getTaskId(), nextTask);

                                // create a persisted wait task
                                PersistedWaitTask pwTask = new PersistedWaitTask(nextTask.getJobId(), nextTask
                                        .getCaseId(), nextTask.getUser().getId());
                                // Register the newly added waitTask to the persisted wait Task Table
                                caseDAO.addPersistedTask(pwTask);

                            }// synchronized
                        } else {
                            if (nextTask.isReady()) {

                                // add to runTable
                                synchronized (runTable) {
                                    runTable.put(nextTask.getTaskId(), nextTask);
                                }// synchronized

                                // runTask and decrement threadsAvail
                                threadPool.execute(nextTask);
                                // synchronized (threadPool) {
                                // threadsAvail--;
                                // }// synchronized
                            } else {
                                synchronized (waitTable) {
                                    waitTable.put(nextTask.getTaskId(), nextTask);

                                    // create a persisted wait task
                                    PersistedWaitTask pwTask = new PersistedWaitTask(nextTask.getJobId(), nextTask
                                            .getCaseId(), nextTask.getUser().getId());
                                    // Register the newly added waitTask to the persisted wait Task Table
                                    caseDAO.addPersistedTask(pwTask);
                                }// synchronized
                            }

                        }
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }// more tasks in taskQueue

        }// while not done

        if (DebugLevels.DEBUG_0()) {
            System.out.println("After processing the taskQueue");
            System.out.println("Size of PBQ taskQueue: " + getSizeofTaskQueue());
            System.out.println("Size of WAIT TABLE: " + getSizeofWaitTable());
            System.out.println("Size of RUN TABLE: " + getSizeofRunTable());
            System.out.println("Before processing the WAIT TABLE");
        }

        // Copy the waitTable to tempWaitTable
        Collection<Task> waitTasks = null;
        Hashtable<String, Task> tempWaitTable = new Hashtable<String, Task>();
        synchronized (waitTable) {
            waitTasks = waitTable.values();
            Iterator<Task> copyIter = waitTasks.iterator();
            while (copyIter.hasNext()) {
                Task copyTask = copyIter.next();
                if (DebugLevels.DEBUG_9())
                    System.out
                            .println("&&&&& In CaseJobTaskManager::processQueue process waitTasks the type of TASK object: "
                                    + copyTask.getClass().getName());

                tempWaitTable.put(copyTask.getTaskId(), copyTask);
            }
        }// synchronized (waitTable)

        // Reuse the waittasks collection
        waitTasks = tempWaitTable.values();

        if (DebugLevels.DEBUG_0())
            System.out.println("Number of waitTasks acquired from waitTable: " + waitTasks.size());

        // iterate over the tasks in the waitTable and find as many that can
        // be run in all available threads

        Iterator<Task> iter = waitTasks.iterator();
        while (iter.hasNext()) {

            // number of threads available before inspecting the waiting list
            synchronized (threadPool) {
                threadsAvail = threadPool.getCorePoolSize() - runTable.size();
            }

            if (DebugLevels.DEBUG_0())
                System.out.println("Number of threads available before waiting list-table: " + threadsAvail);

            if (threadsAvail > 0) {
                CaseJobTask tsk = (CaseJobTask) iter.next();

                if (DebugLevels.DEBUG_9()) {
                    System.out
                            .println("&&&&& In CaseJobTaskManager::processQueue run a waitTasks the type of TASK object: "
                                    + tsk.getClass().getName());
                    System.out.println(tsk.taskId);
                }

                if (DebugLevels.DEBUG_0()) {
                    System.out.println("Is the caseJobTask null? " + (tsk == null));
                    System.out.println("Is this task ready? " + tsk.isReady());
                }

                // look at this waitTable element and see if it isReady()==true
                if (tsk.isReady()) {
                    if (DebugLevels.DEBUG_0()) {
                        System.out.println("WAIT TABLE Before Moving Task from WAIT to RUN: " + waitTable.size());
                        System.out.println("RUN TABLE Before Moving Task from WAIT to RUN: " + runTable.size());
                        System.out.println("#THREADS Before Moving Task from WAIT to RUN: " + threadsAvail);
                    }

                    // remove from waitTable
                    synchronized (waitTable) {
                        waitTable.remove(tsk.getTaskId());

                    }// synchronized (wait Table)

                    // add to runTable
                    synchronized (runTable) {
                        runTable.put(tsk.getTaskId(), tsk);
                    }// synchronized (runTable)

                    // runTask and decrement threadsAvail
                    synchronized (threadPool) {
                        threadPool.execute(tsk);
                    }// synchronized (wait Table)

                    synchronized (threadPool) {
                        threadsAvail = threadPool.getCorePoolSize() - runTable.size();
                    }

                    if (DebugLevels.DEBUG_0()) {
                        System.out.println("WAITTABLE After Moving Task from WAIT to RUN: " + waitTable.size());
                        System.out.println("RUNTABLE After Moving Task from WAIT to RUN: " + runTable.size());
                        System.out.println("#THREADS After Moving Task from WAIT to RUN: " + threadsAvail);
                    }
                }

            } else {
                if (DebugLevels.DEBUG_0())
                    System.out.println("#THREADS == 0?? Breaking out of WAIT TEST loop: " + threadsAvail);
                break;

            }
        }// while wait Tasks iterator

        if (DebugLevels.DEBUG_0()) {
            System.out.println(">>>>> END CaseJobTaskManager::processTaskQueue() *** " + new Date());
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    public static void callBackFromExportJobSubmitter(String cjtId, String status, String mesg) throws EmfException {
        CaseJobTask cjt = null;

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseJobTaskManager::callBackFromExportJobSubmitter for caseJobTask= " + cjtId
                    + " status= " + status + " and message= " + mesg);
        synchronized (waitTable) {
            if (DebugLevels.DEBUG_9())
                System.out.println("Size of the wait Table: " + waitTable.size());
            cjt = (CaseJobTask) waitTable.get(cjtId);
            if (DebugLevels.DEBUG_9())
                System.out.println("For cjtId=" + cjtId + " Is the CaseJobTaksk null? " + (cjt == null));
        }

        if (cjt != null) {

            if (status.equals("completed")) {
                cjt.setExportsSuccess(true);
                if (updateRunStatus(cjtId, "export succeeded", new String[] { mesg }, new String[] { "i" }, false))
                    synchronized (waitTable) {
                        waitTable.remove(cjtId);
                    }
            }

            if (status.equals("failed")) {
                cjt.setExportsSuccess(false);
                if (updateRunStatus(cjtId, "export failed", new String[] { mesg }, new String[] { "i" }, false))
                    synchronized (waitTable) {
                        waitTable.remove(cjtId);
                    }
            }

        }

        processTaskQueue();

    }

    /**
     * Loop over all the waiting tasks. Test and set the dependencies of each task in the waitTable
     * 
     */
    private static synchronized void testAndSetWaitingTasksDependencies() throws EmfException {

        // Get all the waiting CaseJobTasks in the waitTable
        Collection<Task> allWaitTasks = waitTable.values();
        Iterator<Task> iter = allWaitTasks.iterator();
        List<String> tasks2Remove = new ArrayList<String>();

        // Loop over all the waiting CaseJobTasks tasks
        while (iter.hasNext()) {

            CaseJobTask cjt = (CaseJobTask) iter.next();
            if (DebugLevels.DEBUG_9())
                System.out.println("In CJTM: testAndSetWaitingTasksDependencies job (" + cjt.getJobName()
                        + ") has Ready Flag= " + cjt.isReadyFlag);
            if (DebugLevels.DEBUG_9())
                System.out.println("In CJTM: testAndSetWaitingTasksDependencies job (" + cjt.getJobName()
                        + ") has ExportsSuccess Flag= " + cjt.isExportsSuccess());
            if (DebugLevels.DEBUG_9())
                System.out.println("In CJTM: testAndSetWaitingTasksDependencies job (" + cjt.getJobName()
                        + ") has DependenciesSet Flag= " + cjt.isDependenciesSet());

            // For this CaseJobTask if dependencies have not been set yet
            if (DebugLevels.DEBUG_9())
                System.out.println("CJTM::testAndSetWaitingTasksDependencies Testing dependencies for job: "
                        + cjt.getJobName());
            if (!(cjt.isDependenciesSet())) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("Are dependencies flag was false. So test and set/reset");

                // get the caseJob
                CaseJob caseJob = caseDAO.getCaseJob(cjt.getJobId());
                // get the dependents of this caseJob
                DependentJob[] dependJobs = caseJob.getDependentJobs();

                // the caseJob has no dependencies therefore set to True
                if ((dependJobs == null) || (dependJobs.length == 0)) {
                    if (DebugLevels.DEBUG_9())
                        System.out
                                .println("CJTM::testAndSetWaitingTasksDependencies no dependent jobs so set the flag to true");

                    cjt.setDependenciesSet(true);
                    if (DebugLevels.DEBUG_9())
                        System.out
                                .println("CJTM::testAndSetWaitingTasksDependencies is dependent jobs flag set to true? "
                                        + cjt.isDependenciesSet());

                } else {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("job (" + cjt.getJobName() + ") has dependencies that are not met.");
                    // this CaseJob has dependents
                    // ArrayList<CaseJob> allDependentCaseJobs = new ArrayList<CaseJob>();

                    int nonFinalD = 0;
                    int foNSD = 0;
                    int compD = 0;

                    int TotalD = dependJobs.length;

                    // FIXME: BELOW FOR DEBUG ONLY REMOVE AFTER TESTING
                    // nonFinal/foNSD/compD/Total
                    String tempFlag = "[" + nonFinalD + "/" + foNSD + "/" + compD + "/" + TotalD + "]";
                    if (DebugLevels.DEBUG_9())
                        System.out
                                .println("CJTM::testAndSetWaitingTasksDependencies The flags nonFinal/foNSD/compD/Total = "
                                        + tempFlag);
                    // FIXME: ABOVE FOR DEBUG ONLY REMOVE AFTER TESTING

                    // Loop over dependetJobs and add the corresponding CaseJob to the allDependentCaseJobs
                    for (int i = 0; i < dependJobs.length; i++) {
                        DependentJob dJob = dependJobs[i];
                        CaseJob dcj = caseDAO.getCaseJob(dJob.getJobId());
                        if (DebugLevels.DEBUG_9())
                            System.out.println("dependent job: " + dcj.getName());
                        JobRunStatus jrs = dcj.getRunstatus();
                        String status = null;

                        if (jrs == null) {
                            status = "Failed";
                        } else {
                            status = jrs.getName();
                        }

                        if (DebugLevels.DEBUG_9())
                            System.out.println("dependent job status: " + status);

                        if ((status.equalsIgnoreCase("Not Started")) || (status.equalsIgnoreCase("Failed"))) {
                            foNSD++;
                        } else if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Quality Assured")) {
                            compD++;
                        } else {
                            nonFinalD++;
                        }

                        // allDependentCaseJobs.add(dcj);

                        // FIXME: BELOW FOR DEBUG ONLY REMOVE AFTER TESTING
                        // nonFinal/foNSD/compD/Total
                        tempFlag = "[" + nonFinalD + "/" + foNSD + "/" + compD + "/" + TotalD + "]";
                        if (DebugLevels.DEBUG_9())
                            System.out
                                    .println("CJTM::testAndSetWaitingTasksDependencies The flags nonFinal/foNSD/compD/Total = "
                                            + tempFlag);
                        // FIXME: ABOVE FOR DEBUG ONLY REMOVE AFTER TESTING

                    }// for dependentJobs

                    // If none of the jobs are in a non-Final state
                    if (nonFinalD == 0) {
                        if (DebugLevels.DEBUG_9())
                            System.out.println("All my dependents are in a final state");
                        // all our jobs are in a final=completed state
                        if (compD == TotalD) {
                            if (DebugLevels.DEBUG_9())
                                System.out.println("All my dependents are COMPLETED");

                            cjt.setDependenciesSet(true);
                        } else {
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Some of the dependent jobs have failed or not started for job= "
                                        + cjt.getJobName());

                            // We have a parent job that has atleast one dependent job that has failed or
                            // not started therefore send an error message to the user's status window
                            // log a failed jobstatus to the casejobs table
                            // and remove the corresponding casejobtask from the waitTable
                            User user = caseJob.getUser();

                            // set the CaseJob jobstatus (casejob table) to Failed
                            if (updateRunStatus(cjt.getTaskId(), "failed", new String[] { "" }, new String[] { "i" },
                                    false))
                                tasks2Remove.add(cjt.getTaskId());

                            String message = "Job name= " + cjt.getJobName()
                                    + " failed due to at least one dependent jobs state = Failed or Not Started";

                            // set the status in the user's status window
                            setStatus(user, statusDAO, message);

                            // now remove the job with bad dependencies from the waitTable
                            synchronized (waitTable) {
                                if (DebugLevels.DEBUG_9())
                                    System.out.println("Size of waitTable before remove: " + waitTable.size());
                                // waitTable.remove(cjt.getTaskId());
                                tasks2Remove.add(cjt.getTaskId());
                                if (DebugLevels.DEBUG_9())
                                    System.out.println("Size of waitTable after remove: " + waitTable.size());
                            }
                        }// some of the dependent jobs failed
                    }// none of the jobs are in a non-Final state

                }// CJT had dependents
            }// cjt dependencies was false
        }// loop over all waiting tasks

        // NOTE: can't remove these tasks while iterating through them
        synchronized (waitTable) {
            for (Iterator<String> iterator = tasks2Remove.iterator(); iterator.hasNext();)
                waitTable.remove(iterator.next());
        }

    }// testAndSetWaitingTasksDependencies

    protected static synchronized void setStatus(User user, StatusDAO statusServices, String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.add(endStatus);
    }

    /**
     * There was a change in the status of a waiting job so process the Queue
     */
    public static synchronized void callBackFromJobRunServer() throws EmfException {
        if (DebugLevels.DEBUG_9())
            System.out.println("EMF CMD CLIENT SENT A COMPLETED or FAILED STATUS BACK FROM THE RUNNING JOB: "
                    + new Date());
        try {
            int timeWait = 10; // Set the time delay to 10 seconds
            timer = new Timer();

            if (DebugLevels.DEBUG_9())
                System.out.println("Set timer and waiting for " + timeWait + " seconds before processing the queue");

            timer.schedule(new WaitDelay(), timeWait * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("Error in callback received from the Job Run Server");
        }
    }

    public static synchronized String getStatusOfWaitAndRunTable() throws EmfException {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseJobTaskManger::getStatusOfWaitAndRunTable");

        String mesg;

        mesg = createStatusMessage();

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseJobTaskManger::getStatusOfWaitAndRunTable Returning the message: " + mesg);

        return mesg;
    }

    private static synchronized String createStatusMessage() throws EmfException {
        try {
            StringBuffer sbuf = new StringBuffer();
            Iterator<Task> iter;
            String labels;

            Collection<Task> waitingTasks = waitTable.values();
            Collection<Task> runningTasks = runTable.values();

            labels = "\n=======================================\n";
            sbuf.append(labels);
            labels = "Status of the CaseJobTaskManager\n\n";
            sbuf.append(labels);
            labels = "Tasks in the Wait Table\n";
            sbuf.append(labels);
            labels = "JobId,JobName,CaseId,CaseName,UserId,Ready?\n";
            sbuf.append(labels);

            if (waitingTasks.size() == 0) {
                labels = "There are no tasks in the CaseJobTaskManager WaitTable\n";
                sbuf.append(labels);

            } else {
                iter = waitingTasks.iterator();

                while (iter.hasNext()) {
                    CaseJobTask cjt = (CaseJobTask) iter.next();
                    String cjtStatus = cjt.getJobId() + "," + cjt.getJobName() + "," + cjt.getCaseId() + ","
                            + cjt.getCaseName() + "," + cjt.getUser().getId() + "," + cjt.isReady() + "\n";
                    sbuf.append(cjtStatus);
                }
            }

            labels = "=======================================\n";
            sbuf.append(labels);
            labels = "Tasks in the CaseJobTaskManager RunTable\n";
            sbuf.append(labels);
            labels = "JobId,JobName,CaseId,CaseName,UserId\n";
            sbuf.append(labels);

            if (runningTasks.size() == 0) {
                labels = "There are no tasks in the CaseJobTaskManager RunTable\n";
                sbuf.append(labels);

            } else {

                iter = runningTasks.iterator();
                while (iter.hasNext()) {
                    CaseJobTask cjt = (CaseJobTask) iter.next();
                    String cjtStatus = cjt.getJobId() + "," + cjt.getJobName() + "," + cjt.getCaseId() + ","
                            + cjt.getCaseName() + "," + cjt.getUser().getId() + "\n";
                    sbuf.append(cjtStatus);
                }

            }

            labels = "=======================================\n";
            sbuf.append(labels);

            return sbuf.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System error in CaseJobTaskManager" + ex.getMessage());
        }
    }

    public static synchronized void cancelJob(int jobId, User user) throws EmfException {
        CaseJob caseJob = caseDAO.getCaseJob(jobId);
        String status = caseJob.getRunstatus().getName();
        String host = (caseJob.getHost() == null ? "localhost" : caseJob.getHost().getName());

        try {
            if (status == null) 
                throw new EmfException("Job status is null.");

            log.warn("User '" + user.getUsername() + "' tried to cancel job '" + caseJob.getName() + "' " + " on " + status
                    + " stage @" + new Date());

            if (status.equalsIgnoreCase("Submitted") || status.equalsIgnoreCase("Running"))
                remoteCancel(user, caseJob, host);

            if (status.equalsIgnoreCase("Waiting"))
                cancelTempTables(user, caseJob, host);

            if (status.equalsIgnoreCase("Exporting")) {
                boolean found = TaskManagerFactory.getExportTaskManager().cancelExports2Job(jobId, user);

                if (!found)
                    cancelTempTables(user, caseJob, host);
            }
        } finally {
            updateCancelStatus(user, caseJob, status);
            testAndSetWaitingTasksDependencies();
        }
    }

    private static void cancelTempTables(User user, CaseJob job, String host) throws EmfException {
        // 1. remove it from persisted tasks table so it won't start if service crashes
        // 2. lock wait table and remove it from wait table
        // 3a. check job status one more time
        // 3b. if status is 'Submitted' or 'Running', then remoteCncel()
        try {
            caseDAO
                    .removePersistedTask(new PersistedWaitTask(job.getId(), job.getCaseId(), job.getRunJobUser()
                            .getId()));
        } catch (Exception e) {
            log.warn("Persisted job (" + job.getName() + "): not found in db table.");
        }

        boolean found = findNRemove(job, waitTable, user);

        if (found)
            return;

        CaseJob fresh = caseDAO.getCaseJob(job.getId());
        JobRunStatus status = fresh.getRunstatus();

        if (status != null && status.getName().equalsIgnoreCase("Waiting")) {
            try {
                Thread.sleep(5000); // stop for 5 seconds
            } catch (InterruptedException e) {
                // no-op
            }

            fresh = caseDAO.getCaseJob(job.getId());
            status = fresh.getRunstatus();
        }

        if (status.getName().equalsIgnoreCase("Submitted") || status.getName().equalsIgnoreCase("Running"))
            remoteCancel(user, fresh, host);
    }

    private static boolean findNRemove(CaseJob job, Hashtable<String, Task> tempTable, User user) {
        boolean found = false;
        
        if (tempTable == null || tempTable.size() == 0)
            return found;

        synchronized (tempTable) {
            Collection<Task> allTasks = tempTable.values();
            Iterator<Task> iter = allTasks.iterator();
            CaseJobTask cjt = null;

            while (iter.hasNext()) {
                cjt = (CaseJobTask) iter.next();

                if (cjt.getJobId() == job.getId() && cjt.getUser().getId() == job.getRunJobUser().getId())
                    found = true;

                if (user.isAdmin() && job.getId() == cjt.getJobId())
                    found = true;

                if (found)
                    break;
            }

            if (found && cjt != null)
                tempTable.remove(cjt.getTaskId());
        }

        return found;
    }

    private static void remoteCancel(User user, CaseJob caseJob, String host) throws EmfException {
        String qid = caseJob.getIdInQueue();

        if (host.equalsIgnoreCase("localhost")) {
            updateCancelStatus(user, caseJob, caseJob.getRunstatus().getName());
            throw new EmfException("Please cancel the job command manually.");
        }

        if (qid == null || qid.trim().isEmpty())
            return;

        String command = getQueCommand(qid, host);

        if (DebugLevels.DEBUG_14())
            System.out.println("CANCEL JOBS: " + command);

        InputStream inStream = RemoteCommand.execute(user.getUsername(), host, command);
        String outTitle = "stdout from (" + host + "): " + command;
        RemoteCommand.logRemoteStdout(outTitle, inStream);
    }

    private static void updateCancelStatus(User user, CaseJob caseJob, String status) {
        String[] msgs = new String[] { "Job canceled from '" + status + "' state by user '" + user.getUsername() + "'." };
        String[] msgTypes = new String[] { "i" };
        String cancelStatus = "Failed";
        updateJobWithHistory(msgs, msgTypes, true, cancelStatus, caseJob);
    }

    private static String getQueCommand(String qid, String hostName) throws EmfException {
        Session session = CaseJobTaskManager.sessionFactory.getSession();

        if (!hostName.equals("localhost")) {
            int firstDot = hostName.indexOf(".");
            hostName = hostName.substring(0, firstDot);
        }

        try {
            EmfProperty command = new EmfPropertiesDAO().getProperty("CANCEL_JOB_COMMAND_" + hostName.toUpperCase(),
                    session);

            if (command == null || command.getValue().isEmpty())
                throw new EmfException("Can't get cancel job command from db table (emf.properties)");

            return command.getValue() + " " + qid;
        } finally {
            session.close();
        }
    }
}
