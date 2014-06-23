package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.services.EmfException;

public interface LightSwingWorkerPresenter extends SwingWorkerPresenter{

    Object[] saveProcessData() throws EmfException;
    void saveData(Object[] objs) throws EmfException;
}
