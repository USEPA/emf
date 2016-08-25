package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.framework.client.ManagedView;

public interface NewModuleTypeView extends ManagedView, ModuleTypeDatasetsObserver, ModuleTypeParametersObserver {

    void observe(NewModuleTypePresenter presenter);
}
