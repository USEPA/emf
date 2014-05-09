package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface DatasetsBrowserView extends ManagedView {

    void display(EmfDataset[] datasets) throws EmfException;

    void observe(DatasetsBrowserPresenter presenter);

    void refresh(EmfDataset[] datasets);

    String getNameContains();

    void showMessage(String message);

    void showError(String message);

    void clearMessage();

    void notifyLockFailure(EmfDataset dataset);

    EmfDataset[] getSelected();

    DatasetType getSelectedDSType();
    
    void setDSTypeSelection(int index);

    void notifyAdvancedSearchOff();
    
    void populate();

}
