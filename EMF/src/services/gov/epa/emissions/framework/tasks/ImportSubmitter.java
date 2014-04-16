package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.exim.ImportTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

public abstract class ImportSubmitter implements TaskSubmitter {
    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    protected String submitterId;

    protected ArrayList<Runnable> importTasks = null;

    protected Hashtable<String, ImportTaskStatus> submittedTable = null;

    public ImportSubmitter() {
        myTag();
        submitterId = svcLabel;
        importTasks = new ArrayList<Runnable>();
        submittedTable = new Hashtable<String, ImportTaskStatus>();
    }

    public String getSubmitterId() {
        return submitterId;
    }

    private synchronized void addTaskToSubmitter(Runnable task) {
        Task tsk = (Task) task;

        // populate the submittedTable with an ImportTaskStatus object that also holds a reference to the task object
        ImportTaskStatus ets = new ImportTaskStatus(tsk.getTaskId(), tsk.getSubmitterId());
        ets.setStatus(TaskStatus.NULL);

        // Keep a reference to the task object in the TaskStatus for use in the callback
        ets.setImportTask(tsk);

        // Now add the ImportTaskStatus object to the submittedTable
        submittedTable.put(tsk.getTaskId(), ets);

        // Add this task to importTasks queue to prepare for the submit to the TaskManager
        importTasks.add(task);

        if (DebugLevels.DEBUG_6()) {
            System.out.println("Import Task id= " + tsk.getTaskId() + " to be added to submitter: "
                    + tsk.getSubmitterId());
            System.out.println("Created a new ImportTaskStatus? " + (ets != null));
            System.out.println("Set ImportTaskStatus status to NULL " + ets.getStatus());
            System.out.println("Has the reference to the task been set correctly in ETS? "
                    + (tsk.getTaskId() == ets.getImportTask().getTaskId()));
            System.out.println("Size of submitted table before ETS added= " + submittedTable.size());
            System.out.println("Size of submitted table after ETS added= " + submittedTable.size());
        }
    }

    public synchronized void addTasksToSubmitter(ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0()) {
            System.out.println("In submitter::addTasksToSubmitter= " + this.getSubmitterId());
            System.out.println("In submitter::addTasksToSubmitter # of elements in param array = " + tasks.size());
        }
        
        Iterator<Runnable> iter = tasks.iterator();

        while (iter.hasNext()) {
            Task task = (Task) iter.next();
            task.setSubmitterId(submitterId);
            this.addTaskToSubmitter(task);
        }

        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter # of elements in importTasks= " + this.importTasks.size());
        this.submitTasksToTaskManager(submitterId, importTasks);
    }

    public synchronized void submitTasksToTaskManager(String submitterId, ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0()) {
            System.out.println("In submitter::submitTasksToTaskManager= " + this.getSubmitterId());
            System.out.println("In submitter::submitTasksToTaskManager # of elements in param array= " + tasks.size());
            System.out.println("Submitter::importTasks before ADD: " + this.submitterId + " has task count= "
                    + this.importTasks.size());
        }

        TaskManagerFactory.getImportTaskManager().addTasks(tasks);

        // Remove all tasks from importTasks and keep it available for new submissions if necessary
        importTasks.removeAll(tasks);

        if (DebugLevels.DEBUG_0())
            System.out.println("Submitter::importTasks after ADD: " + this.submitterId + " has task count= "
                    + this.importTasks.size());
    }

    public synchronized void cancelTasks(ArrayList<Runnable> tasks) {
        // NOTE Auto-generated method stub

    }

    public synchronized void updateStatus(Runnable task) {
        // NOTE Auto-generated method stub

    }
    
    public synchronized int getTaskCount() {
        return this.submittedTable.size();
    }

    public synchronized int getTaskManagerRunCount() {
        return TaskManagerFactory.getImportTaskManager().getSizeofRunTable();
    }
    
    public synchronized void deregisterSubmitterFromRunManager(TaskSubmitter ts) {
        ImportTaskManager.deregisterSubmitter((ImportSubmitter)ts);
    }

    protected synchronized void setStatus(User user, StatusDAO statusServices, String message) {
        if (DebugLevels.DEBUG_10())
            System.out.println("Import submitter " + this.submitterId + " setting status " 
                    + "; Message: " + message + "; On thread " + Thread.currentThread().getId());
        
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.add(endStatus);
    }

    @Override
    protected void finalize() throws Throwable {
        svcCount--;
        if (DebugLevels.DEBUG_0())
            System.out.println(">>>> Destroying object: " + myTag());

        super.finalize();
    }

}
