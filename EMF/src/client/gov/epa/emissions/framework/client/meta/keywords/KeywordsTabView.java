package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.services.data.EmfDataset;

public interface KeywordsTabView {

    void display(EmfDataset dataset, KeywordsTabPresenter presenter);
    void doRefresh(EmfDataset dataset);
}
