package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputPresenter {
    
    void display(CaseInput input, int modelToRunId) throws EmfException;
    
    void doSave() throws EmfException;
    
    EmfSession getSession();
}
