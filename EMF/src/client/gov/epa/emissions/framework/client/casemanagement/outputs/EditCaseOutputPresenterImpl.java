package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

import javax.swing.JComponent;

public class EditCaseOutputPresenterImpl implements EditOutputPresenter {

    private EditCaseOutputView view;
    
    private EditOutputsTabView parentView;

    private EmfSession session;
    
    private OutputFieldsPanelPresenter outputFieldsPresenter;

    private CaseOutput output;

  private int caseid;

    public EditCaseOutputPresenterImpl(int caseid, EditCaseOutputView view, 
            EditOutputsTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
        this.caseid = caseid;
    }
    
    public EditCaseOutputPresenterImpl(int caseid, EditCaseOutputView view, 
            EmfSession session) {
        this.view = view;
        this.session = session;
        this.caseid = caseid;
    }
    
    public void display(CaseOutput output) throws EmfException {
        this.output = output;
        view.observe(this);
        view.display(output);
    }


    public void addOutputFields(JComponent container, 
            OutputFieldsPanelView outputFields) throws EmfException {
        outputFieldsPresenter = new OutputFieldsPanelPresenter(caseid, outputFields, session);
        outputFieldsPresenter.display(output, container);
    }
    
    
    public void doSave() throws EmfException {
        outputFieldsPresenter.doSave();
        parentView.setMessage("Saved \"" + output.getName() + "\". Refresh to see the changes in the table.");
    }

}
