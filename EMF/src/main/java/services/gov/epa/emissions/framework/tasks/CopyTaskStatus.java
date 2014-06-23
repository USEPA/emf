package gov.epa.emissions.framework.tasks;

/**
 * This class holds the status of each import task in the import submitter or import task manager
 * 
 */
public class CopyTaskStatus implements TaskStatus{

    private String taskId = null;
    private int status = TaskStatus.NULL;
    
    private String submitterId = null;
    private String threadId = null;
    private Task copyTask = null;

    public Task getCopyTask() {
        return copyTask;
    }

    public void setCopyTask(Task copyTask) {
        this.copyTask = copyTask;
    }

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

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public CopyTaskStatus() {
        // NOTE Auto-generated constructor stub
    }

    public CopyTaskStatus(String taskId, String submitterId) {
        super();
        this.taskId = taskId;
        this.submitterId = submitterId;
    }

    public CopyTaskStatus(String taskId, int status, String submitterId, String threadId) {
        super();
        this.taskId = taskId;
        this.status = status;
        this.submitterId = submitterId;
        this.threadId = threadId;
    }

//    @Override
//    public boolean equals(Object obj) {//FIXME: redefine equality
//        boolean eq = false;
//        if (this..equals(((ImportTaskStatus) obj).getFullImportPath())){
//            eq = true;
//        }
//        return eq;
//    }

    
}
