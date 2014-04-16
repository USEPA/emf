package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

import javax.swing.JComponent;

public interface OutputFieldsPanelView {

    void observe(OutputFieldsPanelPresenter presenter);

    void display(CaseOutput output, JComponent container, EmfSession session) throws EmfException;

    CaseOutput setFields() throws EmfException;
    
    CaseOutput getOutput();
    
    void validateFields() throws EmfException;
}
