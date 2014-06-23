package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.StatusDAO;

public class ExportClientSubmitter extends ExportSubmitter {

    public ExportClientSubmitter() {
        super();
        myTag();
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> For label: " + myTag());

        if (DebugLevels.DEBUG_1())
            System.out.println("Export Client @@@@@ THREAD ID: " + Thread.currentThread().getId());
    }

    public synchronized void callbackFromTaskManager(String taskId, String status, String mesg) {
        if (DebugLevels.DEBUG_0())
            System.out
                    .println(">>>>>>>> ExportClientSubmitter::callbackFromTaskManager id= " + submitterId
                            + " got callback from TaskManager for Task: " + taskId + " status= " + status
                            + " message= " + mesg);

//        int statis = -99;
//
//        if (DebugLevels.DEBUG_6())
//            System.out.println("STATUS = " + status);
//        if (status.equals("started")) {
//            statis = TaskStatus.RUNNING;
//            if (DebugLevels.DEBUG_6())
//                System.out.println("STATIS set = " + statis);
//        }
//        if (status.equals("completed")) {
//            statis = TaskStatus.COMPLETED;
//            if (DebugLevels.DEBUG_6())
//                System.out.println("STATIS set = " + statis);
//        }
//        if (status.equals("failed")) {
//            statis = TaskStatus.FAILED;
//            if (DebugLevels.DEBUG_6())
//                System.out.println("STATIS set = " + statis);
//        }

//        if (DebugLevels.DEBUG_6())
//            System.out.println("STATIS VALUE after switch= " + statis);
//        if (DebugLevels.DEBUG_6())
//            System.out.println("SubmittedTable STATIS for this taskId before setStatus= "
//                    + (submittedTable.get(taskId).getStatus()));
//        submittedTable.get(taskId).setStatus(statis);
//        if (DebugLevels.DEBUG_6())
//            System.out.println("SubmittedTable STATIS for this taskId after setStatus= "
//                    + (submittedTable.get(taskId).getStatus()));
//
//        if (DebugLevels.DEBUG_6())
//            System.out.println("DID THE STATUS GET SET IN THE TABLE? "
//                    + (submittedTable.get(taskId).getStatus() == statis));

        User user = null;
        StatusDAO statusServices = null;
        Task task = null;

//        if (submittedTable.get(taskId) == null)
//            return;
//        
        task = submittedTable.get(taskId).getExportTask();
        user = task.getUser();
        statusServices = task.getStatusServices();

        // FIXME: After moving this code to ExportJobSubmitter only write out failed exporttask messages here
        // Set the status in the EMF Status messages table corresponding the callback message received
        this.setStatus(user, statusServices, mesg);

        // remove completed and failed export tasks from the submitted list
        if (!(status.equals("started"))) {
            if (DebugLevels.DEBUG_0())
                System.out.println("In submitter staus of task was : " + status);
            if (DebugLevels.DEBUG_0())
                System.out.println("In submitter: " + submitterId);
            if (DebugLevels.DEBUG_0())
                System.out.println("$$$$ Size of export tasks list before remove: " + exportTasks.size());
            if (DebugLevels.DEBUG_0())
                System.out.println("$$$$ Size of submitted tasks table before remove: " + submittedTable.size());

            // Since this is the Export Client Submitter, remove the taskstatus form the submitted Table
            // after the status has been logged and sent for completed or failed task statuses
            if (DebugLevels.DEBUG_6())
                System.out.println("Size of submitted table before ETS removed= " + submittedTable.size());

            //Remove the taskstatus form the submittedTable
            submittedTable.remove(taskId);

            if (DebugLevels.DEBUG_6())
                System.out.println("Size of submitted table after ETS removed= " + submittedTable.size());


        }

//        int start = 0;
//        int done = 0;
//        int fail = 0;
//        int canned = 0;
//
//        Collection<ExportTaskStatus> allSubTaskStatus = submittedTable.values();

//        Iterator<ExportTaskStatus> iter = allSubTaskStatus.iterator();
//
//        while (iter.hasNext()) {
//            ExportTaskStatus tas = iter.next();
//            if (tas.getStatus() == TaskStatus.RUNNING)
//                start++;
//            if (tas.getStatus() == TaskStatus.COMPLETED)
//                done++;
//            if (tas.getStatus() == TaskStatus.FAILED)
//                fail++;
//            if (tas.getStatus() == TaskStatus.CANCELED)
//                canned++;
//        }
//
//        if (DebugLevels.DEBUG_6()) System.out.println(" RUN Count: " + start);
//        if (DebugLevels.DEBUG_6()) System.out.println(" COMPLETED Count: " + done);
//        if (DebugLevels.DEBUG_6()) System.out.println(" Failed Count: " + fail);
//        if (DebugLevels.DEBUG_6()) System.out.println(" Canceled Count: " + canned);
//        if (DebugLevels.DEBUG_6()) System.out.println(" Total Count: " + (start + done + fail + canned));
//        if (DebugLevels.DEBUG_6()) System.out.println(" Size of submittedTable: " + submittedTable.size());
//
//        if (submittedTable.size() == (done + fail + canned)) {
//            String message = "%%% Submitted job completed. Total exports submitted=" + submittedTable.size()
//                    + " Completed= " + done + " Failed= " + fail + " Canceled= " + canned;
//
//            this.setStatus(user, statusServices, message);
//        }

        if (DebugLevels.DEBUG_0())
            System.out.println(">>>>>>>> Submitter: " + submitterId + " EXITING callback from TaskManager for Task: "
                    + taskId + " status= " + status + " message= " + mesg);

    }

}
