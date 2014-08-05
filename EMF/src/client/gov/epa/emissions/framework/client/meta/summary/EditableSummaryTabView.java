package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditableSummaryTabView {
    // update dataset with the view contents
    void save(EmfDataset dataset) throws EmfException;
    
    void doRefresh(EmfDataset dataset, Version[] versions);
    
    void observe(EditableSummaryTabPresenter presenter);    

}
