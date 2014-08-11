package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;

public interface ShowHistoryTabPresenter extends LightSwingWorkerPresenter{

    public void display() ;
    public void doSave();
   
    //public boolean jobsUsed(CaseJob[] jobs) throws EmfException;
    
    public CaseJob[] getCaseJobs() throws EmfException;
   
    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException;
    public void doRemove(JobMessage[] msgs) throws EmfException;
}
