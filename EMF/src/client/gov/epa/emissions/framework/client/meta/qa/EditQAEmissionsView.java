package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.ManagedView;
//import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditQAEmissionsView extends ManagedView {
    
    void display(EmfDataset dataset, QAStep qaStep);

    void observe(EditQAEmissionsPresenter presenter);
    
}
