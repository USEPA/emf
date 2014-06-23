package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.fast.FastRun;

public interface FastRunView extends ManagedView {

    void observe(FastRunPresenter presenter);

    void display(FastRun run);

    void refresh(FastRun run);

    void notifyLockFailure(FastRun run);

    void signalChanges();

    void showError(String message);

    void showMessage(String message);

    void clearMessage();
}
