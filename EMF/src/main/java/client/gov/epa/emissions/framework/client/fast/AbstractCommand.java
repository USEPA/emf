package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.RefreshObserver;

public abstract class AbstractCommand implements ExceptionHandlingCommand {

    private RefreshObserver refreshObserver;

    public AbstractCommand(RefreshObserver refreshObserver) {
        this.refreshObserver = refreshObserver;
    }

    public void handleException(EmfException e) throws EmfException {
        throw e;
    }

    public void postExecute() throws EmfException {
        this.refreshObserver.doRefresh();
    }
}
