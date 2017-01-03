package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.module.Module;

public interface RelatedModulesView extends ManagedView {
    void observe(RelatedModulesPresenter presenter);

    EmfConsole getParentConsole();

    void display();

    void populate();
}
