package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class KeywordsTabPresenter implements LightSwingWorkerPresenter{

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

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        this.dataset = session.dataService().getDataset(dataset.getId());
        return null;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh(dataset);
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

}
