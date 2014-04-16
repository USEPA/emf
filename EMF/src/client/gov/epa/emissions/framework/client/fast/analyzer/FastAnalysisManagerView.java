package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public interface FastAnalysisManagerView {

    void display(FastAnalysis[] analyses) throws EmfException;

    void observe(FastAnalysisManagerPresenterImpl presenter);

    void refresh(FastAnalysis[] analyses) throws EmfException;

    DesktopManager getDesktopManager();

    EmfConsole getParentConsole();

    boolean isAlive();

    boolean hasChanges();

    void resetChanges();

    boolean shouldDiscardChanges();

    /* Should release the locks if any and call disposeView() */
    void windowClosing();

}
