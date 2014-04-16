package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.exim.ExportTask;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportTaskManager implements TaskManager {
    private static Log log = LogFactory.getLog(ExportTaskManager.class);

    private static ExportTaskManager ref;

    private static int refCount = 0;

    private static HibernateSessionFactory sessionFactory;

    private final int poolSize;

    private final int maxPoolSize;

    private final long keepAliveTime = 60;

    private static ArrayList<TaskSubmitter> submitters = new ArrayList<TaskSubmitter>();

    private static ThreadPoolExecutor threadPool = null;

    // PBQ is the queue for submitting jobs
    private static BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<Runnable>();

    private ArrayBlockingQueue<Runnable> threadPoolQueue = new ArrayBlockingQueue<Runnable>(5);

    private static Hashtable<String, Task> runTable = new Hashtable<String, Task>();

    private static Hashtable<String, Task> waitTable = new Hashtable<String, Task>();

    public static synchronized int getSizeofTaskQueue() {
        return taskQueue.size();
    }

    public static synchronized int getSizeofWaitTable() {
        return waitTable.size();
    }

    public static synchronized int getSizeofRunTable() {
        return runTable.size();
    }

    public synchronized void shutDown() {
        if (DebugLevels.DEBUG_1())
            System.out.println("Shutdown called on Task Manager");
        taskQueue.clear();
        threadPoolQueue.clear();
        threadPool.shutdownNow();
        sessionFactory = null;
    }

    public synchronized void removeTask(Runnable task) {
        taskQueue.remove(task);
    }

    public synchronized void removeTasks(ArrayList<?> tasks) {
        taskQueue.removeAll(tasks);
    }

    public synchronized void registerTaskSubmitter(TaskSubmitter ts) {
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
        // consumer.finalize();
        super.finalize();
    }

    // clone not supported needs to be added
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // Singleton factory method
    public static synchronized ExportTaskManager getExportTaskManager() {
        if (ref == null)
            ref = new ExportTaskManager();
        return ref;
    }

    // The constructor
    private ExportTaskManager() {
        super();

        sessionFactory = HibernateSessionFactory.get();

        if (DebugLevels.DEBUG_14())
            System.out
                    .println("*****HibernateSessionFactory in ExportTaskManager is null? " + (sessionFactory == null));

        this.poolSize = runningThreadSize();
        this.maxPoolSize = poolSize;

        log.info("ExportTaskManager");
        log.warn("ExportTaskManager thread pool size: " + poolSize + ".");

        if (DebugLevels.DEBUG_9())
            System.out.println("Export Task Manager created @@@@@ THREAD ID: " + Thread.currentThread().getId());

        refCount++;

        if (DebugLevels.DEBUG_9())
            System.out.println("Task Manager created refCount= " + refCount);
        if (DebugLevels.DEBUG_9())
            System.out.println("Priority Blocking queue created? " + !(taskQueue == null));

        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, threadPoolQueue);

        if (DebugLevels.DEBUG_9())
            System.out.println("ThreadPool created? " + !(threadPool == null));

        if (DebugLevels.DEBUG_9())
            System.out.println("Initial # of jobs in Thread Pool: " + threadPool.getPoolSize());
    }

    public synchronized void addTasks(ArrayList<Runnable> tasks) {
        taskQueue.addAll(tasks);

        synchronized (runTable) {
            if (runTable.size() == 0) {
                processTaskQueue();
            }
        }// synchronized
    }

    public static synchronized void processTaskQueue() {
        int threadsAvail = -99;
        try {
            if (DebugLevels.DEBUG_9()) {
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                System.out.println("*** BEGIN ExportTaskManager::processTaskQueue() *** " + new Date());
                System.out.println("Size of PBQ taskQueue: " + taskQueue.size());
                System.out.println("Size of WAIT TABLE: " + waitTable.size());
                System.out.println("Size of RUN TABLE: " + runTable.size());
                System.out.println("Number of tasks left in queue: " + getSizeofTaskQueue());
                System.out.println("# of tasks in Thread Pool size: " + threadPool.getPoolSize());
                System.out.println("Active Thread Count= " + threadPool.getActiveCount());
                System.out.println("Core pool size: " + threadPool.getCorePoolSize());
                System.out.println("Maximum pool size= " + threadPool.getMaximumPoolSize());
                System.out.println("Threads available for processing= "
                        + (threadPool.getCorePoolSize() - threadPool.getPoolSize()));
                System.out.println("Completed task count: " + threadPool.getCompletedTaskCount());
                System.out.println("TASK COUNT: " + threadPool.getTaskCount());
                System.out.println("ACTIVE TASK COUNT: " + threadPool.getActiveCount());
                System.out.println("SIZE OF waiting list-table: " + waitTable.size());
            }

            if (DebugLevels.DEBUG_9()) {
                System.out.println("Number of waitTasks acquired from waitTable: " + waitTable.values().size());
                System.out.println("Number of tasks acquired from runTable: " + runTable.size());
            }

            // iterate over the tasks in the waitTable and find as many that can
            // be run in all available threads
            Collection<Task> waitTasks = waitTable.values();
            Iterator<Task> iter = waitTasks.iterator();

            while (iter.hasNext()) {
                // number of threads available before inspecting the waiting list
                // synchronized (threadPool) {
                // threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
                threadsAvail = threadPool.getCorePoolSize() - runTable.size();
                if (DebugLevels.DEBUG_9())
                    System.out.println("Number of threads available before waiting list-table: " + threadsAvail);
                // }

                if (threadsAvail > 0) {
                    ExportTask tsk = (ExportTask) iter.next();
                    if (DebugLevels.DEBUG_9())
                        System.out
                                .println("&&&&& In ExportTaskManager::processQueue threadsAvail so pop a task to run the type of TASK objects coming in are: "
                                        + tsk.getClass().getName());

                    // look at this waitTable element and see if it is exportEquivalent (same Absolute Path)
                    // to any of the tasks currently in the runTable
                    // synchronized (waitTable) {
                    if (notEqualsToAnyRunTask(tsk)) {
                        if (DebugLevels.DEBUG_9())
                            System.out.println("WAIT TABLE Before Moving Task from WAIT to RUN: " + waitTable.size());
                        if (DebugLevels.DEBUG_9())
                            System.out.println("RUN TABLE Before Moving Task from WAIT to RUN: " + runTable.size());
                        if (DebugLevels.DEBUG_9())
                            System.out.println("#THREADS Before Moving Task from WAIT to RUN: " + threadsAvail);

                        // remove from waitTable
                        waitTable.remove(tsk.getTaskId());

                        // add to runTable
                        runTable.put(tsk.getTaskId(), tsk);

                        // runTask and decrement threadsAvail
                        threadPool.execute(tsk);
                        // threadsAvail--;

                        if (DebugLevels.DEBUG_9())
                            System.out.println("WAITTABLE After Moving Task from WAIT to RUN: " + waitTable.size());
                        if (DebugLevels.DEBUG_9())
                            System.out.println("RUNTABLE After Moving Task from WAIT to RUN: " + runTable.size());
                        if (DebugLevels.DEBUG_9())
                            System.out.println("#THREADS After Moving Task from WAIT to RUN: " + threadsAvail);

                    }
                    // }// synchronized (...)

                } else {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("#THREADS == 0?? Breaking out of WAIT TEST loop: " + threadsAvail);
                    break;
                }

            } // iterating over waiting tasks

            if (DebugLevels.DEBUG_9())
                System.out.println("SIZE OF TASKQUEUE: " + getSizeofTaskQueue());
            boolean done = false;
            while (!done) {
                if (taskQueue.size() == 0) {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("#tasks in taskQueue == 0?? Breaking out of taskQueue TEST loop: ");
                    done = true;
                } else {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("Before Peak into taskQueue: " + taskQueue.size());
                    if (taskQueue.peek() != null) {
                        if (DebugLevels.DEBUG_9())
                            System.out.println("Peak into taskQueue has an object in head: " + taskQueue.size());

                        try {
                            Task tp = (Task) taskQueue.peek();
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Task Class Name: " + tp.getClass().getName());
                            Task tt = (Task) taskQueue.take();
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Task Class Name: " + tt.getClass().getName());
                            ExportTask nextTask = (ExportTask) tt;
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Processing the PBQ taskId: " + tt.getTaskId());
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Processing the PBQ submitterId: " + tt.getSubmitterId());

                            // ExportTask nextTask = (ExportTask) taskQueue.take();
                            // number of threads available before inspecting the priority blocking queue
                            // synchronized (threadPool) {
                            // threadsAvail = threadPool.getCorePoolSize() - threadPool.getActiveCount();
                            threadsAvail = threadPool.getCorePoolSize() - runTable.size();

                            // }// synchronized
                            if (DebugLevels.DEBUG_9())
                                System.out.println("Number of threads available before taskQueue PDQ: " + threadsAvail);

                            if (threadsAvail == 0) {
                                // synchronized (waitTable) {
                                waitTable.put(nextTask.getTaskId(), nextTask);
                                // }// synchronized
                            } else {
                                if (notEqualsToAnyRunTask(nextTask)) {

                                    // add to runTable
                                    // synchronized (runTable) {
                                    runTable.put(nextTask.getTaskId(), nextTask);
                                    // }// synchronized

                                    // runTask and decrement threadsAvail
                                    threadPool.execute(nextTask);
                                    // synchronized (threadPool) {
                                    // threadsAvail--;
                                    // }// synchronized
                                } else {
                                    // synchronized (waitTable) {
                                    waitTable.put(nextTask.getTaskId(), nextTask);
                                    // }// synchronized
                                }

                            }
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }
                }// more tasks in taskQueue

            }// while not done (more tasks available in task queue)

            // postProcessWaitTable();

            // Now do other processing and maintenance work
            // if taskQueue is empty then this processing will be the only thing that happens
            // during this callback

            // TODO:

            if (DebugLevels.DEBUG_9()) {
                System.out
                        .println("ExportTaskManager::processTaskQueue() after postProcessWaitTable, Wait table size: "
                                + waitTable.size());
                System.out
                        .println("ExportTaskManager::processTaskQueue() after postProcessWaitTable,  Run table size: "
                                + runTable.size());
                System.out.println("*** END ExportTaskManager::processTaskQueue() *** " + new Date());
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            }
        } catch (ConcurrentModificationException cmex) {
            // do nothing
            log.info("Java is complaining about a ConcurrentModificationException again");
            if (DebugLevels.DEBUG_9())
                System.out.println("Java is complaining about a ConcurrentModificationException again");
        }
    }

    private static synchronized boolean notEqualsToAnyRunTask(ExportTask tsk) {
        boolean notFound = true;
        if (DebugLevels.DEBUG_9())
            System.out.println("SIZE OF RUN list-table: " + runTable.size());

        Collection<Task> runTasks = runTable.values();
        Iterator<Task> iter = runTasks.iterator();
        // synchronized (runTable) {
        while (iter.hasNext()) {
            ExportTask runTask = (ExportTask) iter.next();
            if (DebugLevels.DEBUG_9())
                System.out.println("In ExportTaskManager::notEqualsToAnyRunTask " + " exportTask is of type= "
                        + tsk.getClass().getName() + " and runTask if of type= " + runTask.getClass().getName());
            if (runTask.isEquivalent(tsk)) {
                notFound = false;
                break;
            }
        }
        // }// synchronized

        return notFound;
    }

    public static synchronized void callBackFromThread(String taskId, String submitterId, String status, long id,
            String mesg) {
        if (DebugLevels.DEBUG_9())
            System.out.println("*** BEGIN ExportTaskManager::callBackFromThread() *** " + new Date());

        if (DebugLevels.DEBUG_9())
            System.out.println("ExportTaskManager refCount= " + refCount);
        if (DebugLevels.DEBUG_9())
            System.out.println("Size of PBQ taskQueue: " + taskQueue.size());
        if (DebugLevels.DEBUG_9())
            System.out.println("Size of WAIT TABLE: " + waitTable.size());
        if (DebugLevels.DEBUG_9())
            System.out.println("Size of RUN TABLE: " + runTable.size());

        if (status.equals("started")) {
            if (DebugLevels.DEBUG_9())
                System.out.println("%%%% ExportTaskManager reports that Task# " + taskId
                        + " that is running in thread#: " + id + " for submitter= " + submitterId + " has status= "
                        + status + " and message= " + mesg);

        } else {
            if (DebugLevels.DEBUG_9())
                System.out.println("%%%% ExportTaskManager reports that Task# " + taskId + " that ran in thread#: "
                        + id + " for submitter= " + submitterId + " completed with status= " + status
                        + " and message= " + mesg);
            // remove from waitTable
            if (DebugLevels.DEBUG_9())
                System.out.println("SIZE OF RUN TABLE BEFORE REMOVE= " + runTable.size());
            // synchronized (runTable) {
            runTable.remove(taskId);
            // }// synchronized

            if (DebugLevels.DEBUG_9())
                System.out.println("SIZE OF RUN TABLE AFTER REMOVE= " + runTable.size());
        }

        TaskSubmitter submitter = getCurrentSubmitter(submitterId);

        if (submitter != null) {
            try {
                submitter.callbackFromTaskManager(taskId, status, mesg);
            } catch (Exception e) {
                log.warn("ERROR calling back to ExportSubmitter. ", e);
            }

            if (submitter instanceof ExportJobSubmitter && ((ExportJobSubmitter) submitter).finished()) {
                if (DebugLevels.DEBUG_15())
                    System.out.println("ExportJobSubmitter " + submitter.getSubmitterId()
                            + " is deregistering itself. ");

                submitter.deregisterSubmitterFromRunManager(submitter);

                if (DebugLevels.DEBUG_15())
                    System.out.println("After deregistering itself, the number of submitters in import task manager: "
                            + submitters.size());
            }
        } else {
            log.warn("Submitter with id = " + submitterId + " had an early exit (could be due to job cancelation).");
        }

        // done with the call back ... so process the two tables and task queue
        processTaskQueue();

        if (DebugLevels.DEBUG_9())
            System.out.println("*** END ExportTaskManager::callBackFromThread() *** " + new Date());

    }

    public static synchronized void postProcessWaitTable() {
        List<Task> toBRemoved = new ArrayList<Task>();
        Collection<Task> waitTasks = waitTable.values();
        if (DebugLevels.DEBUG_9())
            System.out.println("postProcessWaitTable(): Number of waitTasks acquired from waitTable: "
                    + waitTasks.size());

        Iterator<Task> iter = waitTasks.iterator();

        while (iter.hasNext()) {
            ExportTask tsk = (ExportTask) iter.next();
            String status = "";
            String msg = "";

            if (tsk.fileExists()) {
                status = "completed";
                msg = "FILE EXISTS: Completed export of " + tsk.getDataset().getName() + ".";

                if (DebugLevels.DEBUG_9())
                    System.out.println("postProcessWaitTable(): File exists tested. Wait table size: "
                            + waitTable.size() + ".");

                byPassExport(tsk.getTaskId(), tsk.getSubmitterId(), status, 0, msg);
                toBRemoved.add(tsk);
            }

            if (tsk.isExternal()) {
                String[] statuses = tsk.quickRunExternalExport();
                status = statuses[0];
                msg = statuses[1];

                if (DebugLevels.DEBUG_9())
                    System.out.println("postProcessWaitTable(): External dataset exported. Wait table size: "
                            + waitTable.size() + ".");

                byPassExport(tsk.getTaskId(), tsk.getSubmitterId(), status, 0, msg);
                toBRemoved.add(tsk);
            }
        }

        for (Iterator<Task> task = toBRemoved.iterator(); iter.hasNext();) {
            waitTable.remove(task.next().getTaskId());
        }

    }

    private static synchronized void byPassExport(String taskId, String submitterId, String status, long id, String mesg) {
        if (DebugLevels.DEBUG_9()) {
            System.out.println("*** ExportTaskManager::byPassExport() *** " + new Date());
            System.out.println("ExportTaskManager refCount= " + refCount);
            System.out.println("Size of PBQ taskQueue: " + taskQueue.size());
            System.out.println("Size of WAIT TABLE: " + waitTable.size());
            System.out.println("Size of RUN TABLE: " + runTable.size());
            System.out.println("%%%% ExportTaskManager reports that Task# " + taskId + " that ran in thread#: " + id
                    + " for submitter= " + submitterId + " completed with status= " + status + " and message= " + mesg);
            System.out.println("SIZE OF RUN TABLE BEFORE REMOVE= " + runTable.size());
        }

        // if (runTable.contains(taskId))
        // runTable.remove(taskId);

        if (DebugLevels.DEBUG_9())
            System.out.println("SIZE OF RUN TABLE AFTER REMOVE= " + runTable.size());

        TaskSubmitter submitter = getCurrentSubmitter(submitterId);

        try {
            submitter.callbackFromTaskManager(taskId, status, mesg);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (submitter instanceof ExportJobSubmitter && ((ExportJobSubmitter) submitter).finished()) {
            if (DebugLevels.DEBUG_15())
                System.out.println("ExportJobSubmitter " + submitter.getSubmitterId() + " is deregistering itself. ");

            submitter.deregisterSubmitterFromRunManager(submitter);

            if (DebugLevels.DEBUG_15())
                System.out.println("After deregistering itself, the number of submitters in import task manager: "
                        + submitters.size());
        }
    }

    private static synchronized TaskSubmitter getCurrentSubmitter(String submitterId) {
        Iterator<TaskSubmitter> iter = submitters.iterator();

        while (iter.hasNext()) {
            TaskSubmitter submitter = iter.next();
            if (submitterId.equals(submitter.getSubmitterId())) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("@@##@@ Found a submitter in the taskmanager collection of submitters: "
                            + submitter.getSubmitterId());

                return submitter;
            }
        }

        return null;
    }

    private String createStatusMessage() throws EmfException {
        try {
            StringBuffer sbuf = new StringBuffer();
            Iterator<Task> iter;
            String labels;

            Collection<Task> waitingTasks = waitTable.values();
            Collection<Task> runningTasks = runTable.values();

            labels = "=======================================\n";
            sbuf.append(labels);
            labels = "Status of the ExportTaskManager\n\n";
            sbuf.append(labels);
            labels = "Tasks in the Wait Table\n";
            sbuf.append(labels);
            labels = "UserId,DatasetNamevVersion\n";
            sbuf.append(labels);

            if (waitingTasks.size() == 0) {
                labels = "There are no tasks in the ExportTaskManager WaitTable\n";
                sbuf.append(labels);

            } else {
                iter = waitingTasks.iterator();

                while (iter.hasNext()) {
                    ExportTask et = (ExportTask) iter.next();
                    String etStatus = et.getUser().getId() + "," + et.getDataset().getName() + ","
                            + et.getVersion().getVersion() + "\n";
                    sbuf.append(etStatus);
                }
            }

            labels = "=======================================\n";
            sbuf.append(labels);
            labels = "Tasks in the ExportTaskManager RunTable\n";
            sbuf.append(labels);
            labels = "UserId,DatasetName,Version\n";
            sbuf.append(labels);

            if (runningTasks.size() == 0) {
                labels = "There are no tasks in the ExportTaskManager RunTable\n";
                sbuf.append(labels);

            } else {

                iter = runningTasks.iterator();
                while (iter.hasNext()) {
                    ExportTask et = (ExportTask) iter.next();
                    String etStatus = et.getUser().getId() + "," + et.getDataset().getName() + ","
                            + et.getVersion().getVersion() + "\n";
                    sbuf.append(etStatus);
                }

            }

            labels = "=======================================\n";
            sbuf.append(labels);

            return sbuf.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System error in ExportTaskManager" + ex.getMessage());
        }
    }

    public static synchronized boolean cancelExports2Job(int jobId, User user) throws EmfException {
        boolean found = false;

        synchronized (submitters) {
            Iterator<TaskSubmitter> iter = submitters.iterator();

            while (iter.hasNext()) {
                TaskSubmitter submitter = iter.next();
                if (submitter instanceof ExportJobSubmitter) {
                    ExportJobSubmitter jobSM = (ExportJobSubmitter) submitter;
                    if ((jobSM.getJobId() == jobId && jobSM.getRunUser().equals(user))
                            || (jobSM.getJobId() == jobId && user.isAdmin())) {
                        found = true;
                        findNRemoveTasksFromWaitTable(jobSM);

                        if (DebugLevels.DEBUG_14()) {
                            System.out.println("---Found a submitter in the taskmanager collection of submitters: "
                                    + jobSM.getSubmitterId());
                            System.out.println("Before deregister export job submitter (job id = " + jobId + "), submitters size: " + submitters.size());
                        }
                        
                        jobSM.deregisterSubmitterFromRunManager(jobSM);
                        
                        if (DebugLevels.DEBUG_14()) 
                            System.out.println("After deregister export job submitter, submitters size: " + submitters.size());
                        
                        String message = "Exports canceled by user '" + user.getUsername() + "'";
                        CaseJobTaskManager.callBackFromExportJobSubmitter(jobSM.getCaseJobTaskId(), "failed", message);

                        break;
                    }
                }
            }
        }

        return found;
    }

    private static void findNRemoveTasksFromWaitTable(ExportJobSubmitter jobSM) {
        Hashtable<String, ExportTaskStatus> taskStatuses = jobSM.getAllTaskStatus();
        Collection<ExportTaskStatus> allSubTaskStatus = taskStatuses.values();
        Iterator<ExportTaskStatus> iter = allSubTaskStatus.iterator();

        synchronized (waitTable) {
            if (DebugLevels.DEBUG_14())
                System.out.println("---Before removing export tasks, waitTable size: " + waitTable.size() + ".");
            
            while (iter.hasNext()) {
                ExportTaskStatus tas = iter.next();
                String tid = tas.getExportTask().getTaskId();

                if (waitTable.containsKey(tid)) {
                    waitTable.remove(tid);

                    if (DebugLevels.DEBUG_14()) {
                        System.out.println("---Export task (ID: " + tid + ") removed from "
                                + " ExportTaskManager waitTable.");
                        System.out.println("---After removing task '" + tid + "', waitTable size: " + waitTable.size() + ".");
                    }
                }
            }
        }
    }

    public String getStatusOfWaitAndRunTable() throws EmfException {
        String mesg;

        mesg = createStatusMessage();
        return mesg;
    }

    private int runningThreadSize() {
        Session session = sessionFactory.getSession();
        int value = 4;

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("NUMBER_OF_SIMULT_EXPORTS", session);
            value = Integer.parseInt(property.getValue());
        } catch (Exception e) {
            return value;// Default value for maxpool and poolsize
        } finally {
            session.close();
        }

        return value;
    }

}
