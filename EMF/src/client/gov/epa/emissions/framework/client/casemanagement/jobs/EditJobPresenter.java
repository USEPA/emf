package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditJobPresenter {
    
    void display(CaseJob job) throws EmfException;
    
    void saveJob() throws EmfException;
}
