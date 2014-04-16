package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class PersistedWaitTask implements Serializable {

    private int id;
    private int jobId;
    private int caseId;
    private int userId;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public PersistedWaitTask() {
        super();
    }

    public PersistedWaitTask(int id, int jobId, int caseId, int userId) {
        super();
        this.id = id;
        this.jobId = jobId;
        this.caseId = caseId;
        this.userId = userId;
    }

    public PersistedWaitTask(int jobId, int caseId, int userId) {
        super();
        this.jobId = jobId;
        this.caseId = caseId;
        this.userId = userId;
    }

    
}
