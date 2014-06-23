package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface CopyQAStepToDatasetSelectionView {

    void display(DatasetType[] datasetTypes);

    void display(DatasetType[] datasetTypes, DatasetType defaultType, boolean selectSingle);

    void observe(CopyQAStepToDatasetSelectionPresenter presenter);

    void refreshDatasets(EmfDataset[] datasets);

    EmfDataset[] getDatasets();

    boolean shouldReplace();
    
    void clearMessage();
}