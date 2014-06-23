package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.services.EmfException;

public interface HeavySwingWorkerPresenter extends SwingWorkerPresenter{

    Object[] removeProcessData() throws EmfException;
    void removeDisplay(Object[] objs) throws EmfException;
//    Object[] newProcessData() throws EmfException;
//    void newDisplay(Object[] objs);
    Object[] editProcessData() throws EmfException;
    void editDisplay(Object[] objs) throws EmfException;
    Object[] viewProcessData() throws EmfException;
    void viewDisplay(Object[] objs) throws EmfException;

}
