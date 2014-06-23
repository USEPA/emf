package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAArgumentsPresenter {
    
    private EditQAArgumentsView view;
    private EditQAStepView view2;
    
    public EditQAArgumentsPresenter(EditQAArgumentsView view, EditQAStepView view2) {
        this.view = view;
        this.view2 = view2;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public void refreshArgs(String argText) {
        view2.updateArgumentsTextArea(argText);
    }
}
