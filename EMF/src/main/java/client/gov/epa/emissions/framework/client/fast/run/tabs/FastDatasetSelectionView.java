package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.services.fast.FastDataset;

import java.util.List;

public interface FastDatasetSelectionView {

    void display();

    void display(boolean selectSingle);

    void observe(FastDatasetSelectionPresenter presenter);

    void refreshDatasets(List<FastDataset> datasets);

    List<FastDataset> getDatasets();

    boolean shouldCreate();

    void clearMessage();
}