package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface MPSDTManagerView extends ManagedView {

    void observe(MPSDTManagerPresenter presenter);

    DesktopManager getDesktopManager();

    EmfConsole getParentConsole();

    void display();
}
