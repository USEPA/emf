package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface NewQAStepView {

    void display(EmfDataset dataset, DatasetType type);
    
    QAStep[] steps();
    
    boolean shouldCreate();
}
