package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.exim.ExportTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

public abstract class ExportSubmitter implements TaskSubmitter {
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

    protected ArrayList<Runnable> exportTasks = null;

//    protected ArrayList<Runnable> submittedTasks = null;

    protected Hashtable<String, ExportTaskStatus> submittedTable = null;

    // protected ExportTaskManager taskManager = null;

    public ExportSubmitter() {
        myTag();
        submitterId = svcLabel;
        exportTasks = new ArrayList<Runnable>();
//        submittedTasks = new ArrayList<Runnable>();
        submittedTable = new Hashtable<String, ExportTaskStatus>();

    }

    public String getSubmitterId() {
        return submitterId;
    }

    private synchronized void addTaskToSubmitter(Runnable task) {
        ExportTask tsk = (ExportTask) task;
        if (DebugLevels.DEBUG_6())
            System.out.println("Export Task id= " + tsk.getTaskId() + " to be added to submitter: "
                    + tsk.getSubmitterId());

        // populate the submittedTable with an ExportTaskStatus object that also holds a reference to the task object

        ExportTaskStatus ets = new ExportTaskStatus(tsk.getTaskId(), tsk.getSubmitterId());

        if (DebugLevels.DEBUG_6())
            System.out.println("Created a new ExportTaskStatus? " + (ets != null));

        ets.setStatus(TaskStatus.NULL);
        if (DebugLevels.DEBUG_6())
            System.out.println("Set ExportTaskStatus status to NULL " + ets.getStatus());

        // Keep a reference to the task object in the TaskStatus for use in the callback
        ets.setExportTask(tsk);
        if (DebugLevels.DEBUG_6())
            System.out.println("Has the reference to the task been set correctly in ETS? "
                    + (tsk.getTaskId() == ets.getExportTask().getTaskId()));

        //Now add the ExportTaskStatus object to the submittedTable
        if (DebugLevels.DEBUG_6()) System.out.println("Size of submitted table before ETS added= " + submittedTable.size());   
        submittedTable.put(tsk.getTaskId(), ets);
        if (DebugLevels.DEBUG_6()) System.out.println("Size of submitted table after ETS added= " + submittedTable.size());   
        
        // Add this task to exportTasks queue to prepare for the submit to the TaskManager
        exportTasks.add(task);
    }

    public synchronized void addTasksToSubmitter(ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::addTasksToSubmitter= " + this.getSubmitterId());
        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::addTasksToSubmitter # of elements in param array = " + tasks.size());

        Iterator<Runnable> iter = tasks.iterator();

        while (iter.hasNext()) {
            Task task = (Task) iter.next();
            task.setSubmitterId(submitterId);
//            if (DebugLevels.DEBUG_9) System.out.println("&&&&& In ExportSubmitter::addTasksToSubmitter the types of TASK objects coming in are: " + task.getClass().getName());
            this.addTaskToSubmitter(task);
        }

        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter # of elements in exportTasks= " + this.exportTasks.size());
        this.submitTasksToTaskManager(submitterId, exportTasks);

    }

    public synchronized void submitTasksToTaskManager(String submitterId, ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::submitTasksToTaskManager= " + this.getSubmitterId());
        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::submitTasksToTaskManager # of elements in param array= " + tasks.size());

        if (DebugLevels.DEBUG_0())
            System.out.println("Submitter::exportTasks before ADD: " + this.submitterId + " has task count= "
                    + this.exportTasks.size());
//        if (DebugLevels.DEBUG_0)
//            System.out.println("SUBMITTER::submittedtasks before ADD: " + this.submitterId + " has task count= "
//                    + this.submittedTasks.size());

//        Iterator iter = tasks.iterator();
//        while (iter.hasNext()){
//            Task tsk = (Task) iter.next();
//            if (DebugLevels.DEBUG_9) System.out.println("&&&&& In ExportSubmitter::submitTasksToTaskManager the types of TASK objects coming in are: " + tsk.getClass().getName());
//        }

        TaskManagerFactory.getExportTaskManager().addTasks(tasks);

        // FIXME: May not need to do this next step since submitted Table is uptodate
//        submittedTasks.addAll(tasks);

        // Remove all tasks from exportTasks and keep it available for new submissions if necessary
        exportTasks.removeAll(tasks);

        if (DebugLevels.DEBUG_0())
            System.out.println("Submitter::exportTasks after ADD: " + this.submitterId + " has task count= "
                    + this.exportTasks.size());

//        if (DebugLevels.DEBUG_0)
//            System.out.println("SUBMITTER::submittedtasks after ADD: " + this.submitterId + " has task count= "
//                    + this.submittedTasks.size());

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
    
    public Hashtable<String, ExportTaskStatus> getAllTaskStatus() {
        return this.submittedTable;
    }

    public synchronized void deregisterSubmitterFromRunManager(TaskSubmitter ts) {
        ExportTaskManager.deregisterSubmitter(ts);
    }

    protected synchronized void setStatus(User user, StatusDAO statusServices, String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
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
