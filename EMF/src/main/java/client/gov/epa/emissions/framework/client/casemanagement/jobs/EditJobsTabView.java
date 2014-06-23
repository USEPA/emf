package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenter;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditJobsTabView extends JobsTabView {

    void display(Case caseObj);
    
    void doDisplay(EditJobsTabPresenter presenter);

    CaseJob[] caseJobs();

    void refresh(CaseJob[] caseJobs);
    
    int numberOfRecord();
    
    void setMessage(String msg);

    void clearMessage();
    
    String getCaseOutputFileDir();
    
}
