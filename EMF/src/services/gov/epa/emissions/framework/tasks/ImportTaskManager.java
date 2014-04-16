package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.exim.ImportTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportTaskManager implements TaskManager {
    private static Log log = LogFactory.getLog(ImportTaskManager.class);

    private static ImportTaskManager ref;

    private static int refCount = 0;

    private static int processQueueCount = 0;

    private final int poolSize = 1; // reduced from 4 to 1 to remove the sessions out of synch issue

    private final int maxPoolSize = 1; // reduced from 4 to 1 to remove the sessions out of synch issue

    private final long keepAliveTime = 60;

    private static ArrayList<ImportSubmitter> submitters = new ArrayList<ImportSubmitter>();

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
    }

    public synchronized void removeTask(Runnable task) {
        taskQueue.remove(task);
    }

    public synchronized void removeTasks(ArrayList<?> tasks) {
        taskQueue.removeAll(tasks);
    }

    public synchronized void registerTaskSubmitter(ImportSubmitter ts) {
        submitters.add(ts);
    }

    public static synchronized void deregisterSubmitter(ImportSubmitter ts) {
        if (DebugLevels.DEBUG_1())
            System.out.println("DeREGISTERED SUBMITTER: " + ts.getSubmitterId() + " Confirm task count= "
                    + ts.getTaskCount());

        submitters.remove(ts);
    }

    public synchronized void finalize() throws Throwable {
        if (DebugLevels.DEBUG_0())
            System.out.println("Finalizing TaskManager # of taskmanagers= " + refCount);

        shutDown();
        super.finalize();
    }

    // clone not supported needs to be added
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // Singleton factory method
    public static synchronized ImportTaskManager getImportTaskManager() {
        if (ref == null)
            ref = new ImportTaskManager();

        return ref;
    }

    // The constructor
    private ImportTaskManager() {
        super();
        log.info("ImportTaskManager");
        refCount++;
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, threadPoolQueue);

        if (DebugLevels.DEBUG_9()) {
            System.out.println("Import Task Manager created @@@@@ THREAD ID: " + Thread.currentThread().getId());
            System.out.println("Task Manager created refCount= " + refCount);
            System.out.println("Priority Blocking queue created? " + !(taskQueue == null));
            System.out.println("ThreadPool created? " + !(threadPool == null));
            System.out.println("Initial # of jobs in Thread Pool: " + threadPool.getPoolSize());
        }
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
        if (DebugLevels.DEBUG_10())
            System.out.println("<<<>>>ImportTaskManager: processTaskQueue() called " + ++processQueueCount + " times.");

        int threadsAvail = -99;
        try {
            if (DebugLevels.DEBUG_9()) {
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                System.out.println("*** BEGIN ImportTaskManager::processTaskQueue() *** " + new Date());
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

            // iterate over the tasks in the waitTable and find as many that can
            // be run in all available threads
            Collection<Task> waitTasks = waitTable.values();
            if (DebugLevels.DEBUG_9())
                System.out.println("Number of waitTasks acquired from waitTable: " + waitTasks.size());

            Iterator<Task> iter = waitTasks.iterator();

            while (iter.hasNext()) {
                // number of threads available before inspecting the waiting list
                threadsAvail = threadPool.getCorePoolSize() - runTable.size();

                if (DebugLevels.DEBUG_9())
                    System.out.println("Number of threads available before waiting list-table: " + threadsAvail);

                if (threadsAvail > 0) {
                    Task tsk = iter.next();
                    if (DebugLevels.DEBUG_9())
                        System.out
                                .println("&&&&& In ImportTaskManager::processQueue threadsAvail so pop a task to run the type of TASK objects coming in are: "
                                        + tsk.getClass().getName());

                    // look at this waitTable element and see if it is importEquivalent (same Absolute Path)
                    // to any of the tasks currently in the runTable
                    // synchronized (waitTable) {
                    if (notEqualsToAnyRunTask(tsk)) {
                        if (DebugLevels.DEBUG_9()) {
                            System.out.println("WAIT TABLE Before Moving Task from WAIT to RUN: " + waitTable.size());
                            System.out.println("RUN TABLE Before Moving Task from WAIT to RUN: " + runTable.size());
                            System.out.println("#THREADS Before Moving Task from WAIT to RUN: " + threadsAvail);
                        }

                        // remove from waitTable
                        waitTable.remove(tsk.getTaskId());

                        // add to runTable
                        runTable.put(tsk.getTaskId(), tsk);

                        // runTask and decrement threadsAvail
                        threadPool.execute(tsk);

                        if (DebugLevels.DEBUG_9()) {
                            System.out.println("WAITTABLE After Moving Task from WAIT to RUN: " + waitTable.size());
                            System.out.println("RUNTABLE After Moving Task from WAIT to RUN: " + runTable.size());
                            System.out.println("#THREADS After Moving Task from WAIT to RUN: " + threadsAvail);
                        }
                    }
                } else {
                    if (DebugLevels.DEBUG_9())
                        System.out.println("#THREADS == 0?? Breaking out of WAIT TEST loop: " + threadsAvail);
                    break;
                }
            }

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
                            Task nextTask = tt;
                            threadsAvail = threadPool.getCorePoolSize() - runTable.size();

                            if (DebugLevels.DEBUG_9()) {
                                System.out.println("Task Class Name: " + tt.getClass().getName());
                                System.out.println("Processing the PBQ taskId: " + tt.getTaskId());
                                System.out.println("Processing the PBQ submitterId: " + tt.getSubmitterId());
                                System.out.println("Number of threads available before taskQueue PDQ: " + threadsAvail);
                            }

                            if (threadsAvail == 0) {
                                waitTable.put(nextTask.getTaskId(), nextTask);
                            } else {
                                if (notEqualsToAnyRunTask(nextTask)) {
                                    runTable.put(nextTask.getTaskId(), nextTask);
                                    threadPool.execute(nextTask);
                                } else {
                                    waitTable.put(nextTask.getTaskId(), nextTask);
                                }

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }// more tasks in taskQueue

            }// while not done

            // Now do other processing and maintenance work
            // if taskQueue is empty then this processing will be the only thing that happens
            // during this callback

            // TODO:

            if (DebugLevels.DEBUG_9()) {
                System.out.println("*** END ImportTaskManager::processTaskQueue() *** " + new Date());
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            }
        } catch (ConcurrentModificationException cmex) {
            // do nothing
            log.info("Java is complaining about a ConcurrentModificationException again");

            if (DebugLevels.DEBUG_9())
                System.out.println("Java is complaining about a ConcurrentModificationException again");
        }
    }

    private static synchronized boolean notEqualsToAnyRunTask(Task tsk) {
        if (DebugLevels.DEBUG_9())
            System.out.println("SIZE OF RUN list-table: " + runTable.size());

        Collection<Task> runTasks = runTable.values();
        Iterator<Task> iter = runTasks.iterator();

        while (iter.hasNext()) {
            Task runTask = iter.next();
            if (DebugLevels.DEBUG_9())
                System.out.println("In ImportTaskManager::notEqualsToAnyRunTask " + " importTask is of type= "
                        + tsk.getClass().getName() + " and runTask if of type= " + runTask.getClass().getName());
            if (runTask.isEquivalent(tsk)) {
                return false;
            }
        }

        return true;
    }

    public static synchronized void callBackFromThread(String taskId, String submitterId, String status, long id,
            String mesg) {
        if (DebugLevels.DEBUG_9()) {
            System.out.println("*** BEGIN ImportTaskManager::callBackFromThread() *** " + new Date());
            System.out.println("ImportTaskManager refCount= " + refCount);
            System.out.println("Size of PBQ taskQueue: " + taskQueue.size());
            System.out.println("Size of WAIT TABLE: " + waitTable.size());
            System.out.println("Size of RUN TABLE: " + runTable.size());
        }

        boolean removeFromRun = true;
        
        if (status == null)
            status = "failed";

        try {
            ImportSubmitter submitter = getCurrentSubmitter(submitterId);

            if (status.equals("started")) {
                if (DebugLevels.DEBUG_9())
                    System.out.println("%%%% ImportTaskManager reports that Task# " + taskId
                            + " that is running in thread#: " + id + " for submitter= " + submitterId + " has status= "
                            + status + " and message= " + mesg);
                removeFromRun = false;

            } else {

                if (DebugLevels.DEBUG_9()) {
                    System.out.println("%%%% ImportTaskManager reports that Task# " + taskId + " that ran in thread#: "
                            + id + " for submitter= " + submitterId + " completed with status= " + status
                            + " and message= " + mesg);
                    System.out.println("SIZE OF RUN TABLE BEFORE REMOVE= " + runTable.size());
                    System.out.println("SIZE OF RUN TABLE AFTER REMOVE= " + runTable.size());
                }
            }

            if (DebugLevels.DEBUG_10())
                System.out.println("Submitter: " + submitterId + " now is null? " + (submitter == null));

            try {
                if (submitter != null)
                    submitter.callbackFromTaskManager(taskId, status, mesg);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (submitter != null && submitter.getTaskCount() == 0) {
                if (DebugLevels.DEBUG_10())
                    System.out.println("Submitter " + submitter.getSubmitterId() + " is deregistering itself. ");

                submitter.deregisterSubmitterFromRunManager(submitter);

                if (DebugLevels.DEBUG_10())
                    System.out.println("After deregistering itself, the number of submitters in import task manager: "
                            + submitters.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (!status.equals("started"))
                removeFromRun = true;     // if something wrong with current task, we want to remove it from run
        } finally {
            // remove from runTable
            if (removeFromRun)
                runTable.remove(taskId);

            // done with the call back ... so process the two tables and task queue
            processTaskQueue();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("*** END ImportTaskManager::callBackFromThread() *** " + new Date());

    }

    private static synchronized ImportSubmitter getCurrentSubmitter(String submitterId) {
        Iterator<ImportSubmitter> iter = submitters.iterator();

        while (iter.hasNext()) {
            ImportSubmitter submitter = iter.next();
            if (submitterId.equals(submitter.getSubmitterId())) {
                if (DebugLevels.DEBUG_10()) {
                    System.out.println("Number of submitters in import task manager: " + submitters.size());
                    System.out.println("current submitter id: " + submitterId + " registered submitter id: "
                            + submitter.getSubmitterId());
                    System.out.println("@@##@@ Found a submitter in the taskmanager collection of submitters: "
                            + submitter.getSubmitterId());
                }
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
            labels = "Status of the ImportTaskManager\n\n";
            sbuf.append(labels);
            labels = "Tasks in the Wait Table\n";
            sbuf.append(labels);
            labels = "UserId, Dataset Name\n";
            sbuf.append(labels);

            if (waitingTasks.size() == 0) {
                labels = "There are no tasks in the ImportTaskManager WaitTable\n";
                sbuf.append(labels);

            } else {
                iter = waitingTasks.iterator();

                while (iter.hasNext()) {
                    Task et = iter.next();
                    // Cast task to import task or import case output task and get dataset name
                    String dsname = null;
                    if (et instanceof ImportTask) {
                        dsname = ((ImportTask) et).getDataset().getName();
                        if ( dsname != null) {
                            dsname = dsname.trim();
                            ((ImportTask) et).getDataset().setName(dsname);
                        }
                    }
                    else {
                        dsname = ((ImportCaseOutputTask) et).getDataset().getName();
                        if ( dsname != null) {
                            dsname = dsname.trim();
                            ((ImportCaseOutputTask) et).getDataset().setName(dsname);
                        }
                    }
                    String etStatus = et.getUser().getId() + "," + dsname + "\n";
                    sbuf.append(etStatus);
                }
            }

            labels = "=======================================\n";
            sbuf.append(labels);
            labels = "Tasks in the ImportTaskManager RunTable\n";
            sbuf.append(labels);
            labels = "UserId, Dataset Name\n";
            sbuf.append(labels);

            if (runningTasks.size() == 0) {
                labels = "There are no tasks in the ImportTaskManager RunTable\n";
                sbuf.append(labels);

            } else {

                iter = runningTasks.iterator();
                while (iter.hasNext()) {
                    Task et = iter.next();
                    // Cast task to import task or import case output task and get dataset name
                    String dsname = null;
                    if (et instanceof ImportTask) {
                        dsname = ((ImportTask) et).getDataset().getName();
                        if ( dsname != null) {
                            dsname = dsname.trim();
                            ((ImportTask) et).getDataset().setName(dsname);
                        }
                    }
                    else {
                        dsname = ((ImportCaseOutputTask) et).getDataset().getName();
                        if ( dsname != null) {
                            dsname = dsname.trim();
                            ((ImportCaseOutputTask) et).getDataset().setName(dsname);
                        }
                    }
                    String etStatus = et.getUser().getId() + "," + dsname + "\n";
                    sbuf.append(etStatus);
                }

            }

            labels = "=======================================\n";
            sbuf.append(labels);

            return sbuf.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System error in ImportTaskManager" + ex.getMessage());
        }
    }

    public String getStatusOfWaitAndRunTable() throws EmfException {
        String mesg;

        mesg = createStatusMessage();
        return mesg;
    }

}
