package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.ModuleType;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface ModuleTypesManagerView extends ManagedView {
    void observe(ModuleTypesManagerPresenter presenter);

    void refresh(ModuleType[] types);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
