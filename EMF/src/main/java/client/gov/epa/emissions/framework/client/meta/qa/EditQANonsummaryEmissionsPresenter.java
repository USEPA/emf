package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQANonsummaryEmissionsPresenter {
    
    private EditQANonsummaryEmissionsView view;
    
    private EditQAStepView view2;
        
    public EditQANonsummaryEmissionsPresenter(EditQANonsummaryEmissionsView view, EditQAStepView view2) {
        this.view = view;
        this.view2 = view2;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public void updateInventories(Object [] inventories) {
        view2.updateInventories(inventories);
    }
}
