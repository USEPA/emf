package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

import javax.swing.JComponent;

public interface JobFieldsPanelView {

    void observe(JobFieldsPanelPresenter presenter);

    void display(Case caseObj, CaseJob job, JComponent container) throws EmfException;

    CaseJob setFields() throws EmfException;
    
    CaseJob getJob() throws EmfException;
    
    void validateFields() throws EmfException;
    
    void setViewOnly();
}
