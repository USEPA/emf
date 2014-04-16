package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditCaseInputView {
    void display(CaseInput input) throws EmfException;
    
    void observe(EditCaseInputPresenterImpl presenter);

    void loadInput() throws EmfException;
    
    void populateFields();
    
    void viewOnly(String title );
    
    boolean hasChanges();
}
