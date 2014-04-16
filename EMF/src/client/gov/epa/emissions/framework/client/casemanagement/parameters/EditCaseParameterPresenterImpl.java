package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public class EditCaseParameterPresenterImpl implements EditCaseParameterPresenter {

    private EditCaseParameterView view;
    
    private EditCaseParametersTabView parentView;
    
    private EmfSession session;
    
    private ParameterFieldsPanelPresenter parameterFieldsPresenter;
    
    private CaseParameter parameter;
    
    private Case caseObj;
    
    private int model_id;

    public EditCaseParameterPresenterImpl(Case caseObj, EditCaseParameterView view, 
            EditCaseParametersTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
        this.caseObj = caseObj;
    }
    
    public EditCaseParameterPresenterImpl(Case caseObj, 
            EditCaseParameterView view, EmfSession session) {
        this.view = view;
        this.session = session;
        this.caseObj = caseObj;
    }
    
    public void display(CaseParameter param, int model_id) throws EmfException {
        this.parameter = param;
        this.model_id = model_id;
        view.observe(this);
        view.display(param);
        view.populateFields();
    }
    
    public void doAddInputFields(JComponent container, 
            ParameterFieldsPanelView parameterFields) throws EmfException {
        parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj, parameterFields, session);
        parameterFieldsPresenter.display(parameter, model_id, container);
    }
    
    public void doSave() throws EmfException {
        parameterFieldsPresenter.doSave();
        parentView.setMessage("Saved \"" + parameter.getName() + "\". Refresh to see the changes in the table.");
    }

}
