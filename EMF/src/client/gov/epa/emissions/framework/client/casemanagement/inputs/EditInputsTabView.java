package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabView {

    void display(EmfSession session, Case caseObj, EditInputsTabPresenter presenter);

    CaseInput[] caseInputs();

    void addInput(CaseInput input);
    
    void refresh();
    
    String getCaseInputFileDir();

    int numberOfRecord();

    void clearMessage();

    void setMessage(String message);
    
//    void notifychanges();

}
