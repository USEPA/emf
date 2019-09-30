package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface ModuleComparisonView extends ManagedView {
    void observe(ModuleComparisonPresenter presenter);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
