package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;


public interface EditOutputsTabView {
    
    void display();

    void refresh();

    void observe(EditOutputsTabPresenterImpl editOutputsTabPresenterImpl);

    void clearMessage();
    
    void setMessage(String msg);

    void addOutput(CaseOutput addCaseOutput);
    
}
