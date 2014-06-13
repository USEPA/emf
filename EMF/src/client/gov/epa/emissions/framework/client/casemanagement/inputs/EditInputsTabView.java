package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabView {

    void display(EmfSession session, Case caseObj);

    CaseInput[] caseInputs();

    void addInput(CaseInput input);
    
    void refresh(CaseInput[] caseInputs);
    
    String getCaseInputFileDir();

    int numberOfRecord();

    void clearMessage();

    void setMessage(String message);
    
    void doDisplay(EditInputsTabPresenter presenter);
    
    Sector getSelectedSector();
    
    String nameContains();
    
    Boolean isShowAll();

}
