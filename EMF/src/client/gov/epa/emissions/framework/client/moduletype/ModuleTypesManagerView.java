package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface ModuleTypesManagerView extends ManagedView, ModuleTypeVersionObserver {
    void observe(ModuleTypesManagerPresenter presenter);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
