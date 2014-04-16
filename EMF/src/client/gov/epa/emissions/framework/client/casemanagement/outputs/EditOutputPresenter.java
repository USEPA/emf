package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public interface EditOutputPresenter {
    
    void display(CaseOutput output) throws EmfException;
    
    void doSave() throws EmfException;
}
