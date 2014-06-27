package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;

public interface ShowHistoryTabView {

    void doDisplay(ShowHistoryTabPresenter presenter, int caseId);

    void display( CaseJob[] objs);     

    int numberOfRecord();

    void clearMessage();

    void refresh(JobMessage[] mesgs);
    
    Integer getSelectedJobId();
    
    //void refreshJobList(CaseJob[] jobs);

}
