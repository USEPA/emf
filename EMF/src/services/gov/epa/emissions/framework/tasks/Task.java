package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.StatusDAO;

import java.util.Date;


public abstract class Task implements Runnable, Comparable<Object> {
    protected static int taskCount = 0;
    protected User user;
    protected StatusDAO statusServices;
    protected boolean isReadyFlag=false;

    protected String taskId = null;

    public synchronized String createId() {
        if (taskId == null) {
            taskCount++;
            
            this.taskId = "#" + taskCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        return "For label: " + taskId + " # of active objects of this type= " + taskCount;
    }
		
	protected String submitterId;
    private Task eqTask;
		
	public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}

    public User getUser() {
        return user;
    }

    public StatusDAO getStatusServices() {
        return statusServices;
    }


    public synchronized boolean isEquivalent(Task task){
        this.eqTask = task;
        eqTask.getTaskId();
        return false;
    }
    
	public int compareTo(Object o) {
		return 0;
	}

    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }

    public boolean isReady() {
        return isReadyFlag;
    }

    public void setReady(boolean isReady) {
        this.isReadyFlag = isReady;
    }

    public void setReadyTrue(){
        this.isReadyFlag=true;
    }
    
    public void setReadyFalse(){
        this.isReadyFlag=false;
    }
    
}
