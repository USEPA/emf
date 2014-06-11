package gov.epa.emissions.framework.client.swingworker;

import gov.epa.emissions.framework.services.EmfException;

public interface LightSwingWorkerPresenter extends SwingWorkerPresenter{

    Object[] refreshProcessData() throws EmfException;
    void refreshDisplay(Object[] objs) throws EmfException;
//    Object[] removeProcessData() throws EmfException;
//    void removeDisplay(Object[] objs) throws EmfException;
//    Object[] newProcessData() throws EmfException;
//    void newDisplay(Object[] objs);
//    Object[] editProcessData() throws EmfException;
//    void editDisplay(Object[] objs);
//    Object[] viewProcessData() throws EmfException;
//    void viewDisplay(Object[] objs);

}
