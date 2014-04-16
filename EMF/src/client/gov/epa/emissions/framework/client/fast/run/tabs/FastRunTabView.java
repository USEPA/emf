package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;

public interface FastRunTabView {

    void save(FastRun run) throws EmfException;

    void refresh(FastRun run);

    void display();

    void viewOnly();
}
