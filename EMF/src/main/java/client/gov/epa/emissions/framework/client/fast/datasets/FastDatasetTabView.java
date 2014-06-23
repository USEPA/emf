package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDataset;

public interface FastDatasetTabView {

    void save(FastDataset dataset) throws EmfException;

    void refresh(FastDataset dataset);

    void display();

    void viewOnly();
}
