package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

import java.util.List;

public interface FastDatasetManagerView {

    void display(List<FastDatasetWrapper> datasetWrappers) throws EmfException;

    void observe(FastDatasetManagerPresenterImpl presenter);

    void refresh(List<FastDatasetWrapper> datasetWrappers) throws EmfException;

    DesktopManager getDesktopManager();

    EmfConsole getParentConsole();

    boolean isAlive();

    boolean hasChanges();

    void resetChanges();

    boolean shouldDiscardChanges();

    /* Should release the locks if any and call disposeView() */
    void windowClosing();

}
