package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

public class CopyCaseSubmitter implements TaskSubmitter {
    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private String submitterId;

    private ArrayList<Runnable> copyTasks = null;

    private Hashtable<String,CopyTaskStatus> submittedTable = null;

    public CopyCaseSubmitter() {
        myTag();
        submitterId = svcLabel;
        copyTasks = new ArrayList<Runnable>();
        submittedTable = new Hashtable<String, CopyTaskStatus>();
    }

    public String getSubmitterId() {
        return submitterId;
    }

    private synchronized void addTaskToSubmitter(Runnable task) {
        Task tsk = (Task) task;

        // populate the submittedTable with an TaskStatus object that also holds a reference to the task object
        CopyTaskStatus ets = new CopyTaskStatus(tsk.getTaskId(), tsk.getSubmitterId());
        ets.setStatus(TaskStatus.NULL);

        // Keep a reference to the task object in the TaskStatus for use in the callback
        ets.setCopyTask(tsk);

        // Now add the TaskStatus object to the submittedTable
        submittedTable.put(tsk.getTaskId(), ets);

        // Add this task to Tasks queue to prepare for the submit to the TaskManager
        copyTasks.add(task);

        if (DebugLevels.DEBUG_6()) {
            System.out.println(" Task id= " + tsk.getTaskId() + " to be added to submitter: "
                    + tsk.getSubmitterId());
            System.out.println("Created a new CopyTaskStatus? " + (ets != null));
            System.out.println("Set CopyTaskStatus status to NULL " + ets.getStatus());
            System.out.println("Has the reference to the task been set correctly in ETS? "
                    + (tsk.getTaskId() == ets.getCopyTask().getTaskId()));
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
            System.out.println("In submitter # of elements in Tasks= " + this.copyTasks.size());
        this.submitTasksToTaskManager(submitterId, copyTasks);
    }

    public synchronized void submitTasksToTaskManager(String submitterId, ArrayList<Runnable> tasks) {
        if (DebugLevels.DEBUG_0()) {
            System.out.println("In submitter::submitTasksToTaskManager= " + this.getSubmitterId());
            System.out.println("In submitter::submitTasksToTaskManager # of elements in param array= " + tasks.size());
            System.out.println("Submitter::Tasks before ADD: " + this.submitterId + " has task count= "
                    + this.copyTasks.size());
        }

        TaskManagerFactory.getCopyTaskManager().addTasks(tasks);

        // Remove all tasks from importTasks and keep it available for new submissions if necessary
        copyTasks.removeAll(tasks);

        if (DebugLevels.DEBUG_0())
            System.out.println("Submitter::CaseCopyTasks after ADD: " + this.submitterId + " has task count= "
                    + this.copyTasks.size());
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

    public synchronized void deregisterSubmitterFromRunManager(TaskSubmitter ts) {
        CopyTaskManager.deregisterSubmitter((CopyCaseSubmitter)ts);
    }

    private synchronized void setStatus(User user, StatusDAO statusServices, String message) {
        if (DebugLevels.DEBUG_10())
            System.out.println("Copy submitter " + this.submitterId + " setting status " 
                    + "; Message: " + message + "; On thread " + Thread.currentThread().getId());
        
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Copy Case");
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

    public void callbackFromTaskManager(String taskId, String status, String mesg) {
        if (DebugLevels.DEBUG_0())
            System.out
            .println(">>>>>>>> CopyClientSubmitter::callbackFromTaskManager id= " + submitterId
                    + " got callback from TaskManager for Task: " + taskId + " status= " + status
                    + " message= " + mesg);
        User user = null;
        StatusDAO statusServices = null;
        Task task = null;

        task = submittedTable.get(taskId).getCopyTask();
        user = task.getUser();
        statusServices = task.getStatusServices();

        this.setStatus(user, statusServices, mesg);
        
//        int statis = -99; 
//        if (status.equals("started")) {
//            statis = TaskStatus.RUNNING;
//            if (DebugLevels.DEBUG_9())
//                System.out.println("STATIS set = " + statis);
//        }
//        if (status.equals("completed")) {
//            statis = TaskStatus.COMPLETED;
//            if (DebugLevels.DEBUG_9())
//                System.out.println("STATIS set = " + statis);
//        }
//        if (status.equals("failed")) {
//            statis = TaskStatus.FAILED;
//            // Only write out failed exporttask messages here
//            // Set the status in the EMF Status messages table corresponding the callback message received
//            this.setStatus(user, statusServices, mesg);
//
//            if (DebugLevels.DEBUG_9())
//                System.out.println("STATIS set = " + statis);
//        }       
//        submittedTable.get(taskId).setStatus(statis);
//        
        if (!(status.equals("started"))) {
            if (DebugLevels.DEBUG_0())
                System.out.println("In submitter staus of task was : " + status);
            if (DebugLevels.DEBUG_0())
                System.out.println("In submitter: " + submitterId);
            if (DebugLevels.DEBUG_0())
                System.out.println("$$$$ Size of export tasks list before remove: " + copyTasks.size());
            if (DebugLevels.DEBUG_0())
                System.out.println("$$$$ Size of submitted tasks table before remove: " + submittedTable.size());

            // Since this is the Export Client Submitter, remove the taskstatus form the submitted Table
            // after the status has been logged and sent for completed or failed task statuses
            if (DebugLevels.DEBUG_6())
                System.out.println("Size of submitted table before ETS removed= " + submittedTable.size());

            //Remove the taskstatus form the submittedTable
            submittedTable.remove(taskId);
            if (DebugLevels.DEBUG_0())
                System.out.println(">>>>>>>> Submitter: " + submitterId + " EXITING callback from TaskManager for Task: "
                        + taskId + " status= " + status + " message= " + mesg);
        }

    }
}
