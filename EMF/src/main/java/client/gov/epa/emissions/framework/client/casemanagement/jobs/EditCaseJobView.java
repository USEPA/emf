package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditCaseJobView {
    void display(CaseJob job) throws EmfException;
    
    void observe(EditCaseJobPresenterImpl presenter);

    void loadCaseJob() throws EmfException;
    
    void populateFields();
}
