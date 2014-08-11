package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.data.EmfDataset;

public interface DataTabView {

    void display(EmfDataset dataset);
    
    void doRefresh(EmfDataset dataset);

    void observe(DataTabPresenter presenter);
}
