package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public interface EditCaseOutputView {
    
    CaseOutput setFields() throws EmfException;
    
    void display(CaseOutput output) throws EmfException;
    
    void observe(EditCaseOutputPresenterImpl presenter);

    void loadOutput() throws EmfException;
    
    void viewOnly(String title);
}
