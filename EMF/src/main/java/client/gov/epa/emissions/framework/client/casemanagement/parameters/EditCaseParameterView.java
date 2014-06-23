package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public interface EditCaseParameterView {
    void display(CaseParameter param) throws EmfException;
    
    void observe(EditCaseParameterPresenterImpl presenter);

    void loadInput() throws EmfException;
    
    void populateFields();
    
    void viewOnly(String title);
}
