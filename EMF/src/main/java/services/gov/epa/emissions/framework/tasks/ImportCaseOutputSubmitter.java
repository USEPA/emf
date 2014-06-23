package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.StatusDAO;

public class ImportCaseOutputSubmitter extends ImportSubmitter {

    public ImportCaseOutputSubmitter() {
        super();
        myTag();
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> For label: " + myTag());

        if (DebugLevels.DEBUG_1())
            System.out.println("Import Case Output @@@@@ THREAD ID: " + Thread.currentThread().getId());
    }

    public synchronized void callbackFromTaskManager(String taskId, String status, String mesg) {
        if (DebugLevels.DEBUG_0())
            System.out
                    .println(">>>>>>>> ImportCaseOutputSubmitter::callbackFromTaskManager id= " + submitterId
                            + " got callback from TaskManager for Task: " + taskId + " status= " + status
                            + " message= " + mesg);

        User user = null;
        StatusDAO statusServices = null;
        Task task = null;

        ImportTaskStatus ts = submittedTable.get(taskId);
        
        if (ts == null) { 
            if (DebugLevels.DEBUG_0())
                System.out.println("!!!ImportCaseOutputSubmitter: callbackFromTaskManger() returned with taskId: " 
                        + taskId + " not searchable from submittedTable: " + submittedTable);
           
            removeTask(taskId, status);
            
            return;
        }
        
        task = ts.getImportTask();
        user = task.getUser();
        statusServices = task.getStatusServices();
        
        if (DebugLevels.DEBUG_0()) {
            System.out.println("!!!ImportCaseOutputSubmitter: User: " + user);
            System.out.println("!!!ImportCaseOutputSubmitter: Task: " + task.getSubmitterId());
        }

        // FIXME: After moving this code to ImportJobSubmitter only write out failed importtask messages here
        // Set the status in the EMF Status messages table corresponding the callback message received
        if (status.toLowerCase().contains("fail") || mesg.toLowerCase().contains("fail") || mesg.toLowerCase().contains("error"))
            this.setStatus(user, statusServices, mesg);

        if (DebugLevels.DEBUG_0()) {
            System.out.println("!!!ImportCaseOutputSubmitter: passed setStatus()");
        }

        removeTask(taskId, status);

        if (DebugLevels.DEBUG_0())
            System.out.println(">>>>>>>> Submitter: " + submitterId + " EXITING callback from TaskManager for Task: "
                    + taskId + " status= " + status + " message= " + mesg);

    }

    private void removeTask(String taskId, String status) {
        // remove completed and failed import tasks from the submitted list
        if (!(status.equals("started"))) {
            if (DebugLevels.DEBUG_0()) {
                System.out.println("In submitter staus of task was : " + status);
                System.out.println("In submitter: " + submitterId);
                System.out.println("$$$$ Size of import tasks list before remove: " + importTasks.size());
                System.out.println("$$$$ Size of submitted tasks table before remove: " + submittedTable.size());
            }
            
            //Remove the taskstatus form the submittedTable
            submittedTable.remove(taskId);

            // Since this is the import Client Submitter, remove the taskstatus form the submitted Table
            // after the status has been logged and sent for completed or failed task statuses
            if (DebugLevels.DEBUG_6()) {
                System.out.println("Size of submitted table before ETS removed= " + submittedTable.size());
                System.out.println("Size of submitted table after ETS removed= " + submittedTable.size());
            }
        }
    }

}
