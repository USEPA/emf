package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.exim.ExportTask;

/**
 * This class holds the status of each export task in the export submitter or export task manager
 * 
 */
public class ExportTaskStatus implements TaskStatus{

    private String taskId=null;
    private int status=TaskStatus.NULL;
    private String fullExportPath=null;
    private String submitterId=null;
    private String threadId=null;
    private ExportTask exportTask = null;

    public ExportTask getExportTask() {
        return exportTask;
    }

    public void setExportTask(ExportTask exportTask) {
        this.exportTask = exportTask;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getFullExportPath() {
        return fullExportPath;
    }

    public void setFullExportPath(String fullExportPath) {
        this.fullExportPath = fullExportPath;
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

    public ExportTaskStatus() {
        // NOTE Auto-generated constructor stub
    }

    public ExportTaskStatus(String taskId, String submitterId) {
        super();
        this.taskId = taskId;
        this.submitterId = submitterId;
    }

    public ExportTaskStatus(String taskId, int status, String fullExportPath, String submitterId, String threadId) {
        super();
        this.taskId = taskId;
        this.status = status;
        this.fullExportPath = fullExportPath;
        this.submitterId = submitterId;
        this.threadId = threadId;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if (this.fullExportPath.equals(((ExportTaskStatus) obj).getFullExportPath())){
            eq = true;
        }
        return eq;
    }

    
}
