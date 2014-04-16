package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;
    
    private EmfSession session; 

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session =session; 
    }

    public void display() {
        view.display(dataset, this);
    }
    
    public EmfDataset reloadDataset() throws EmfException{
        this.dataset = session.dataService().getDataset(dataset.getId());
        return dataset; 
    }

}
