package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

public interface FastDatasetManagerPresenter {

    void doRefresh() throws EmfException;

    void doClose();

    void doNew() throws EmfException;

    void doEdit(int id) throws EmfException;

    void doView(int id) throws EmfException;

    void doRemove(FastDatasetWrapper wrapper) throws EmfException;

    void doControl(int id) throws EmfException;

    void doSaveDataset(FastDatasetWrapper fastDataset) throws EmfException;

    void display() throws EmfException;

    void loadDatasets() throws EmfException;

    DatasetType getDatasetType(String orlPointInventory) throws EmfException;
}