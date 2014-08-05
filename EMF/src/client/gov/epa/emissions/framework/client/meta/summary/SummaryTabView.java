package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface SummaryTabView {// tagged interface
    void observe(SummaryTabPresenter presenter);  
    void doRefresh(EmfDataset dataset, Version version);
}
