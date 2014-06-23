package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.services.EmfException;

public interface SwingWorkerPresenter {

    Object[] swProcessData() throws EmfException;
    void swDisplay(Object[] objs) throws EmfException;

}