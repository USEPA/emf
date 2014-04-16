package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface DatasetPropertiesEditorView extends ManagedView {

    void observe(PropertiesEditorPresenter presenter);

    void display(EmfDataset dataset, Version[] versions);

    void showError(String message);

    void notifyLockFailure(EmfDataset dataset);
    
    void setDefaultTab(int index);

}
