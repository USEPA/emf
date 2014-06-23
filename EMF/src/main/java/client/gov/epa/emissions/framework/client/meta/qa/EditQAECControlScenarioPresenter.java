package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAECControlScenarioPresenter {
    
    private QAECControlScenarioWindow view;
    
    private EditQAStepView editQAStepView;
    
    private EmfSession session;
        
    public EditQAECControlScenarioPresenter(QAECControlScenarioWindow view, EditQAStepView view2, EmfSession session) {
        this.view = view;
        this.editQAStepView = view2;
        this.session = session;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

    public void updateECControlScenarioArguments(Object inventory, Object detailedResult, Object[] gsrefs,
            Object[] gspros) {
        editQAStepView.updateECControlScenarioArguments(inventory,
                detailedResult,
                gsrefs, 
                gspros);
    }
}
