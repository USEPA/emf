package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.ManagedView;

public interface NewModuleView extends ManagedView, ModuleDatasetsObserver, ModuleParametersObserver {

    void observe(NewModulePresenter presenter);
}
