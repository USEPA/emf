package gov.epa.emissions.framework.client.fast.analyzer.tabs.inputs;

import gov.epa.emissions.framework.services.EmfException;

public interface FastAnalysisInputPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void fireTracking();

    boolean hasResults();
}
