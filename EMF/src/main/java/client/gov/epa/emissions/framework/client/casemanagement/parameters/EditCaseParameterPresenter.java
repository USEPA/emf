package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public interface EditCaseParameterPresenter {
    
    void display(CaseParameter param, int model_id) throws EmfException;
    
//    void viewDisplay(CaseParameter param);
    
    void doSave() throws EmfException;
}
