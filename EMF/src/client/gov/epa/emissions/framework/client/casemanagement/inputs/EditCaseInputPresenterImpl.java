package gov.epa.emissions.framework.client.casemanagement.inputs;

import javax.swing.JComponent;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public class EditCaseInputPresenterImpl implements EditInputPresenter {

    private EditCaseInputView view;
    
    private EditInputsTabView parentView;
    
    private EmfSession session;
    
    private InputFieldsPanelPresenter inputFieldsPresenter;
    
    private CaseInput input;
    
    private Case caseObj; 
    
    private int modelToRunId;

    public EditCaseInputPresenterImpl(Case caseObj, EditCaseInputView view, 
            EditInputsTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
        this.caseObj = caseObj;
    }
    
    public EditCaseInputPresenterImpl(Case caseObj, EditCaseInputView view, 
           EmfSession session) {
        this.view = view;
        this.session = session;
        this.caseObj = caseObj;
    }
    
    public void display(CaseInput input, int modelToRunId) throws EmfException {
        this.modelToRunId = modelToRunId;
        this.input = input;
        view.observe(this);
        view.display(input);
        view.populateFields();
    }
    
    public void doAddInputFields(JComponent container, 
            InputFieldsPanelView inputFields) throws EmfException {
        inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj, inputFields, session);
        inputFieldsPresenter.display(input, container, modelToRunId);
    }
    
    public void doSave() throws EmfException {
        inputFieldsPresenter.doSave();
        parentView.setMessage("Saved \"" + input.getName() + "\". Refresh to see the changes in the table.");
        
    }

    public EmfSession getSession() {
        return session;
    }
    
    

}
