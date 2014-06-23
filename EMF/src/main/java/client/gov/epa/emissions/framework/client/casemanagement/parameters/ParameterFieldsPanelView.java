package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public interface ParameterFieldsPanelView {

    void observe(ParameterFieldsPanelPresenter presenter);

    void display(CaseParameter param, int model_id, JComponent container) throws EmfException;

    CaseParameter setFields() throws EmfException;
    
    CaseParameter getParameter();
    
    void validateFields() throws EmfException;
}
