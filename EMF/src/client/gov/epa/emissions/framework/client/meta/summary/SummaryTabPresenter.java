package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class SummaryTabPresenter implements LightSwingWorkerPresenter {
    
    private EmfDataset dataset;
    
    private EmfSession session;
    
    private SummaryTabView view;

    public SummaryTabPresenter(SummaryTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
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
        this.dataset = reloadDataset();
        return  new Version[] {getVersion()};
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh(dataset, (Version) objs[0]);
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
       //
    }

}
