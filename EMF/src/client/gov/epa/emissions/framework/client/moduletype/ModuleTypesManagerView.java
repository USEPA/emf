package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.module.ModuleType;

public interface ModuleTypesManagerView extends ManagedView {
    void observe(ModuleTypesManagerPresenter presenter);

    void refresh(ModuleType[] types);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
