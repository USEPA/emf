package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;

public interface CopyQAStepTemplateToDatasetTypeSelectionView {

    void display(DatasetType[] datasetTypes, DatasetType[] defaultSelectedDatasetTypes);

    void observe(CopyQAStepTemplateToDatasetTypeSelectionPresenter presenter);

    DatasetType[] getSelectedDatasetTypes();

    boolean shouldReplace();

    void clearMessage();

}