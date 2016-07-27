package gov.epa.emissions.framework.client.data.module;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface ModulesManagerView extends ManagedView {
    void observe(ModulesManagerPresenter presenter);

    void refresh(Module[] modules);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
