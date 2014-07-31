package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.services.EmfException;

public interface GenericSwingWorkerPresenter<E> {
    E getGenericSwingWorkerData();
    void genericSwingWorkerIsDone(E data);    
}
