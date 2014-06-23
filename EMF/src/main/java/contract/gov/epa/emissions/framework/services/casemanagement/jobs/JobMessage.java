package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.util.Date;

public class JobMessage {
    
    private int id;
    
    private int caseId;
    
    private int jobId;
    
    private String execPath;
    
    private String execName;
    
    private String period;
    
    private String message;
    
    private String messageType;
    
    private String status;
    
    private Date execModifiedDate;
    
    private String remoteUser;
    
    private Date receivedTime;
    
    public JobMessage() {
        // needed for hibernate and axis serialization 
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public Date getExecModifiedDate() {
        return execModifiedDate;
    }

    public void setExecModifiedDate(Date execModifiedDate) {
        this.execModifiedDate = execModifiedDate;
    }

    public String getExecName() {
        return execName;
    }

    public void setExecName(String execName) {
        this.execName = execName;
    }

    public String getExecPath() {
        return execPath;
    }

    public void setExecPath(String execPath) {
        this.execPath = execPath;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isEmpty() {
        boolean result = status.isEmpty() && message.isEmpty() && period.isEmpty();
        return result;
    }

}
