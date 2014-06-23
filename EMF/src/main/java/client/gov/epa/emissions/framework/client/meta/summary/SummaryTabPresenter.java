package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class SummaryTabPresenter {
    
    private EmfDataset dataset;
    
    private EmfSession session;
    
    //private SummaryTabView view;

    public SummaryTabPresenter(SummaryTabView view, EmfDataset dataset, EmfSession session) {
        //this.view = view;
        this.dataset = dataset;
        this.session = session;
        view.observe(this);
    }

    public void display() {// no op
    }
    
    public EmfDataset reloadDataset() throws EmfException {
        dataset= session.dataService().getDataset(dataset.getId());
        return dataset;
    }
       
    public Version getVersion() throws EmfException {
     return session.eximService().getVersion(dataset, dataset.getDefaultVersion());
    }

}
