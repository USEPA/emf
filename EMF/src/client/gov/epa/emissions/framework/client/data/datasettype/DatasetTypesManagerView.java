package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface DatasetTypesManagerView extends ManagedView {
    void observe(DatasetTypesManagerPresenter presenter);

    void refresh(DatasetType[] types);

    EmfConsole getParentConsole();

    void display(DatasetType[] datasetTypes);
}
