package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;

public interface FastRunManagerView {

    void display(FastRun[] runs) throws EmfException;

    void observe(FastRunManagerPresenter presenter);

    void refresh(FastRun[] runs) throws EmfException;

    DesktopManager getDesktopManager();

    EmfConsole getParentConsole();

    String getName();

    boolean isAlive();

    boolean hasChanges();

    void resetChanges();

    boolean shouldDiscardChanges();

    /* Should release the locks if any and call disposeView() */
    void windowClosing();
}
