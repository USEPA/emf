package gov.epa.emissions.framework.tasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class CaseJobSubmitter implements TaskSubmitter {
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

    protected ArrayList<Runnable> caseJobTasks = null;
    
    private HibernateSessionFactory sessionFactory = null;

    public CaseJobSubmitter(HibernateSessionFactory sessionFactory) {
        myTag();
        submitterId = svcLabel;
        if (DebugLevels.DEBUG_0()) System.out.println("CaseJobSubmitter myTag called: " + submitterId);
        caseJobTasks = new ArrayList<Runnable>();
        this.sessionFactory=sessionFactory;
    }

    public void addTasksToSubmitter(ArrayList<Runnable> tasksForSubmitter) {
//        Iterator iter = tasksForSubmitter.iterator();
//        while (iter.hasNext()){
//            Task tsk = (Task) iter.next();
//            if (DebugLevels.DEBUG_9) System.out.println("&&&&& In CaseJobSubmitter::addTasksToSubmitter the types of TASK objects coming in are: " + tsk.getClass().getName());
//        }

        if (DebugLevels.DEBUG_0()) System.out.println("CaseJobSubmitter::addTasksToSubmitter Total number of tasks= " + tasksForSubmitter.size());
        this.submitTasksToTaskManager(submitterId, tasksForSubmitter);
    }

    public void callbackFromTaskManager(String taskId, String status, String mesg) {
        // NOTE Auto-generated method stub
        
    }

    public void cancelTasks(ArrayList<Runnable> tasks) {
        // NOTE Auto-generated method stub
        
    }

    public void deregisterSubmitterFromRunManager(TaskSubmitter ts) {
        // NOTE Auto-generated method stub
        
    }

    public String getSubmitterId() {
        return this.submitterId;
    }

    public int getTaskCount() {
        // NOTE Auto-generated method stub
        return 0;
    }

    public synchronized void submitTasksToTaskManager(String submitterId, ArrayList<Runnable> tasks) {
        Iterator iter = tasks.iterator();
        while (iter.hasNext()){
            Task tsk = (Task) iter.next();
            if (DebugLevels.DEBUG_9()) System.out.println("&&&&& In CaseJobSubmitter::submitTasksToTaskManager the types of TASK objects coming in are: " + tsk.getClass().getName());
        }

        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::submitTasksToTaskManager= " + this.getSubmitterId());
        if (DebugLevels.DEBUG_0())
            System.out.println("In submitter::submitTasksToTaskManager # of elements in param array= " + tasks.size());

        if (DebugLevels.DEBUG_0())
            System.out.println("Submitter::caseJobTasks size before ADD: " + this.submitterId + " has task count= "
                    + this.caseJobTasks.size());
        if (DebugLevels.DEBUG_0())
            System.out.println("#### SUBMITTER:: incoming tasks size before ADD: " + this.submitterId + " has task count= "
                    + tasks.size());

        //FIXME:  FIND OUT WHY THIS METHOD IS THROWING AN UNHANDLED EXCEPTION
        try {
            TaskManagerFactory.getCaseJobTaskManager(sessionFactory).addTasks(tasks);
        } catch (EmfException e) {
            e.printStackTrace();
        }

        // FIXME: May not need to do this next step since submitted Table is uptodate
//        submittedTasks.addAll(tasks);

        // Remove all tasks from  and keep it available for new submissions if necessary
        caseJobTasks.removeAll(tasks);

        if (DebugLevels.DEBUG_0())
            System.out.println("CaseJobSubmitter::submitTasksToRunManager after ADD: " + this.submitterId + " has task count= "
                    + this.caseJobTasks.size());

//        if (DebugLevels.DEBUG_0)
//            System.out.println("SUBMITTER::submittedtasks after ADD: " + this.submitterId + " has task count= "
//                    + this.submittedTasks.size());

    }


    public void updateStatus(Runnable task) {
        // NOTE Auto-generated method stub
        
    }


}
