package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.framework.client.ManagedView;

public interface NewModuleTypeView extends ManagedView {

    void observe(NewModuleTypePresenter presenter);

    void refreshDatasets();
    void refreshParameters();
}
