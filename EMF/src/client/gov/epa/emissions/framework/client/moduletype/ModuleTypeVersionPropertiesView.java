package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.ManagedView;

public interface ModuleTypeVersionPropertiesView extends ManagedView, ModuleTypeDatasetsObserver, ModuleTypeParametersObserver, ModuleTypeRevisionsObserver {

    void observe(ModuleTypeVersionPropertiesPresenter presenter);
}
