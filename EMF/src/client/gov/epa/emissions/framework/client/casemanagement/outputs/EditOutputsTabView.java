package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;


public interface EditOutputsTabView {
    
    void display(CaseJob[] caseJobs);
    
    void doDisplay(EditOutputsTabPresenter presenter, Case caseObj);

    void refresh(CaseOutput[] outputs);

    void observe(EditOutputsTabPresenter editOutputsTabPresenterImpl);

    void clearMessage();
    
    void setMessage(String msg);

    void addOutput(CaseOutput addCaseOutput);
    
    Integer getSelectedJobId();
    
    void setAllJobs(CaseJob[] jobs);
    
    void refreshJobList(CaseJob[] jobs);
    
}
