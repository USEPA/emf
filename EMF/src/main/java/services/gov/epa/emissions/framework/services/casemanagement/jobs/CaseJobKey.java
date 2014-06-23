package gov.epa.emissions.framework.services.casemanagement.jobs;

public class CaseJobKey {
    
    private int id;
    
    private int jobId;
    
    private String key;
    
    public CaseJobKey() {
        this("");
    }
    
    public CaseJobKey(String key) {
        this.key = key;
    }
    
    public CaseJobKey(String key, int jobId) {
        this.key = key;
        this.jobId = jobId;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
