package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface VersionedDataView extends ManagedView {
    void observe(VersionedDataPresenter presenter);
    
    void refresh(); 

    void display(EmfDataset dataset, EditVersionsPresenter versionsPresenter);
}
