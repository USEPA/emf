package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class DependentJob implements Serializable {

    private int jobId;
    
    public DependentJob(){
        //
    }

    public DependentJob(int id){
        this.jobId = id;
    }
    
    public int getJobId() {
        return jobId;
    }
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
}
