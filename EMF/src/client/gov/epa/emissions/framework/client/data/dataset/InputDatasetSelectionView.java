package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface InputDatasetSelectionView {

    void display(DatasetType[] datasetTypes);

    void display(DatasetType[] datasetTypes, DatasetType defaultType, boolean selectSingle);

    void observe(InputDatasetSelectionPresenter presenter);

    void refreshDatasets(EmfDataset[] datasets);

    EmfDataset[] getDatasets();

    boolean shouldCreate();
    
    void clearMessage();
}