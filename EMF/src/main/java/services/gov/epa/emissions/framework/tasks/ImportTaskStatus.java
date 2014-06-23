package gov.epa.emissions.framework.tasks;


/**
 * This class holds the status of each import task in the import submitter or import task manager
 * 
 */
public class ImportTaskStatus implements TaskStatus{

    private String taskId = null;
    private int status = TaskStatus.NULL;
    private String fullImportPath = null;
    private String submitterId = null;
    private String threadId = null;
    private Task importTask = null;

    public Task getImportTask() {
        return importTask;
    }

    public void setImportTask(Task importTask) {
        this.importTask = importTask;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFullImportPath() {
        return fullImportPath;
    }

    public void setFullImportPath(String fullImportPath) {
        this.fullImportPath = fullImportPath;
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

    public ImportTaskStatus() {
        // NOTE Auto-generated constructor stub
    }

    public ImportTaskStatus(String taskId, String submitterId) {
        super();
        this.taskId = taskId;
        this.submitterId = submitterId;
    }

    public ImportTaskStatus(String taskId, int status, String fullImportPath, String submitterId, String threadId) {
        super();
        this.taskId = taskId;
        this.status = status;
        this.fullImportPath = fullImportPath;
        this.submitterId = submitterId;
        this.threadId = threadId;
    }

    @Override
    public boolean equals(Object obj) {//FIXME: redefine equality
        boolean eq = false;
        if (this.fullImportPath.equals(((ImportTaskStatus) obj).getFullImportPath())){
            eq = true;
        }
        return eq;
    }

    
}
