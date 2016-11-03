package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface ModuleTypeVersionsManagerView extends ManagedView, ModuleTypeVersionObserver {
    void observe(ModuleTypeVersionsManagerPresenter presenter);

    void refresh();

    EmfConsole getParentConsole();

    void display();
}
