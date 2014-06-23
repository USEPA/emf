package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;

public class FastRunTabPresenterImpl implements FastRunTabPresenter {

    private FastRunTabView view;

    public FastRunTabPresenterImpl(FastRunTabView view) {
        this.view = view;
    }

    public void doSave(FastRun run) throws EmfException {
        view.save(run);
    }

    public void doRefresh(FastRun run) {
        view.refresh(run);
    }

    public void doDisplay() {
        this.view.display();
    }
}
