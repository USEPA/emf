package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;

public interface FastRunTabPresenter {

    void doSave(FastRun run) throws EmfException;

    void doRefresh(FastRun run);

    void doDisplay();
}
