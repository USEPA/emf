package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.StatusDAO;

import java.util.Collection;
import java.util.Iterator;

public class ExportJobSubmitter extends ExportSubmitter {

    String caseJobTaskId = null;

    private String caseJobName = null;

    private int jobId;

    private User runUser;

    private boolean finished = false;

    public String getCaseJobTaskId() {
        return caseJobTaskId;
    }

    public void setCaseJobTaskId(String caseJobTaskId) {
        this.caseJobTaskId = caseJobTaskId;
    }

    public ExportJobSubmitter() {
        super();
        myTag();
        if (DebugLevels.DEBUG_9())
            System.out.println(">>>> For label: " + myTag());

        if (DebugLevels.DEBUG_9())
            System.out.println("Export Job Submitter @@@@@ THREAD ID: " + Thread.currentThread().getId());
    }

    /**
     * Set the job name for this submitter
     */
    public void setJobName(String jobName) {
        this.caseJobName = jobName;
    }

    public String getJobName() {
        return this.caseJobName;
    }

    public synchronized void callbackFromTaskManager(String taskId, String status, String mesg) {
        if (DebugLevels.DEBUG_9())
            System.out
                    .println(">>>>>>>> ExportJobSubmitter::callbackFromTaskManager id= " + submitterId
                            + " got callback from TaskManager for Task: " + taskId + " status= " + status
                            + " message= " + mesg);

        int statis = -99;
        User user = null;
        StatusDAO statusServices = null;
        Task task = null;

        task = submittedTable.get(taskId).getExportTask();
        user = task.getUser();
        statusServices = task.getStatusServices();

        if (DebugLevels.DEBUG_9())
            System.out.println("STATUS = " + status);
        if (status.equals("started")) {
            statis = TaskStatus.RUNNING;
            if (DebugLevels.DEBUG_9())
                System.out.println("STATIS set = " + statis);
        }
        if (status.equals("completed")) {
            statis = TaskStatus.COMPLETED;
            if (DebugLevels.DEBUG_9())
                System.out.println("STATIS set = " + statis);
        }
        if (status.equals("failed")) {
            statis = TaskStatus.FAILED;
            // Only write out failed exporttask messages here
            // Set the status in the EMF Status messages table corresponding the callback message received
            this.setStatus(user, statusServices, mesg);

            if (DebugLevels.DEBUG_9())
                System.out.println("STATIS set = " + statis);
        }

        if (DebugLevels.DEBUG_9()) {
            System.out.println("STATIS VALUE after switch= " + statis);
            System.out.println("SubmittedTable STATIS for this taskId before setStatus= "
                    + (submittedTable.get(taskId).getStatus()));
        }

        // Set the status of the TastStatus object for this taskId
        submittedTable.get(taskId).setStatus(statis);

        if (DebugLevels.DEBUG_9()) {
            System.out.println("SubmittedTable STATIS for this taskId after setStatus= "
                    + (submittedTable.get(taskId).getStatus()));
            System.out.println("DID THE STATUS GET SET IN THE TABLE? "
                    + (submittedTable.get(taskId).getStatus() == statis));
        }

        // remove completed and failed export tasks from the submitted list
        if (!(status.equals("started"))) {
            if (DebugLevels.DEBUG_9()) {
                System.out.println("In submitter staus of task was : " + status);
                System.out.println("In submitter: " + submitterId);
                System.out.println("$$$$ Size of export tasks list before remove: " + exportTasks.size());
                System.out.println("$$$$ Size of submitted tasks table before remove: " + submittedTable.size());

                // Since this is the Export Client Submitter, remove the taskstatus form the submitted Table
                // after the status has been logged and sent for completed or failed task statuses
                System.out.println("Size of submitted table before ETS removed= " + submittedTable.size());
                System.out.println("Size of submitted table after ETS removed= " + submittedTable.size());
                System.out.println("$$$$ Size of submitted tasks table after remove: " + submittedTable.size());
            }
        }

        int start = 0;
        int done = 0;
        int fail = 0;
        int canned = 0;

        Collection<ExportTaskStatus> allSubTaskStatus = submittedTable.values();

        Iterator<ExportTaskStatus> iter = allSubTaskStatus.iterator();

        while (iter.hasNext()) {
            ExportTaskStatus tas = iter.next();
            if (tas.getStatus() == TaskStatus.RUNNING)
                start++;
            if (tas.getStatus() == TaskStatus.COMPLETED)
                done++;
            if (tas.getStatus() == TaskStatus.FAILED)
                fail++;
            if (tas.getStatus() == TaskStatus.CANCELED)
                canned++;
        }

        if (DebugLevels.DEBUG_9())
            System.out.println(" RUN Count: " + start);
        if (DebugLevels.DEBUG_9())
            System.out.println(" COMPLETED Count: " + done);
        if (DebugLevels.DEBUG_9())
            System.out.println(" Failed Count: " + fail);
        if (DebugLevels.DEBUG_9())
            System.out.println(" Canceled Count: " + canned);
        if (DebugLevels.DEBUG_9())
            System.out.println(" Total Count: " + (start + done + fail + canned));
        if (DebugLevels.DEBUG_9())
            System.out.println(" Size of submittedTable: " + submittedTable.size());

        // jobName) are completed:
        String message = null;
        if (submittedTable.size() == (done + fail + canned)) {
            message = "Exports for job (" + this.caseJobName + ") are completed. Total exports submitted="
                    + submittedTable.size() + " Completed= " + done + " Failed= " + fail + " Canceled= " + canned;

            this.setStatus(user, statusServices, message);

            if (submittedTable.size() == done) {
                status = "completed";

            } else {
                status = "failed";
            }

            try {
                CaseJobTaskManager.callBackFromExportJobSubmitter(this.caseJobTaskId, status, message);
            } catch (EmfException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                this.finished = true;
            }
        }

        if (DebugLevels.DEBUG_9())
            System.out.println(">>>>>>>> Submitter: " + submitterId + " EXITING callback from TaskManager for Task: "
                    + taskId + " status= " + status + " message= " + message);
    }

    // WARNING: Call this method IF AND ONLY IF you know what you are doing!!!
    // Method used to cancel the export tasks in the submitter
    public synchronized void setTaskStatusFailed() {
        Collection<ExportTaskStatus> allSubTaskStatus = submittedTable.values();
        Iterator<ExportTaskStatus> iter = allSubTaskStatus.iterator();

        synchronized (submittedTable) {
            while (iter.hasNext()) {
                ExportTaskStatus tas = iter.next();
                String tid = tas.getExportTask().getTaskId();
                submittedTable.get(tid).setStatus(TaskStatus.FAILED);
            }
        }
    }

    public synchronized boolean finished() {
        return finished;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public User getRunUser() {
        return runUser;
    }

    public void setRunUser(User runUser) {
        this.runUser = runUser;
    }

}
