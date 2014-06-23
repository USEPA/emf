package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface PropertiesView extends ManagedView {

    void observe(PropertiesViewPresenter presenter);

    void display(EmfDataset dataset, Version version);

    void showError(String message);

    void clearMessage();

    void setDefaultTab(int index);
}
