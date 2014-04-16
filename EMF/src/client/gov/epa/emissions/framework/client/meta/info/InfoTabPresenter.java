package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InfoTabPresenter implements PropertiesEditorTabPresenter {

    private InfoTabView view;
    
    private ExternalSource[] extSrcs;

    private EmfDataset dataset;
    
    private EmfSession session;

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
        view.observe(this);
    }

    public void doSave() {
        //NOTE: don't need to do anything, dataset's been updated
    }

    public void doDisplay(String filter) throws EmfException {
        DatasetType type = dataset.getDatasetType();

        if (!type.isExternal())
            view.displayInternalSources(dataset.getInternalSources());
        else {
            int numOfSrcs = getNumExternalSrcs(dataset.getId(), filter);
            view.displayExternalSources(numOfSrcs);
        }
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public EmfSession getSession() {
        return session;
    }

    public void refreshExternalSources() throws EmfException {
        int numOfSrcs = getNumExternalSrcs(dataset.getId(), view.getNameFilter());
        view.displayExternalSources(numOfSrcs);
    }
    
    public ExternalSource[] getExternalSrcs(int dsId, int limit, String filter) throws EmfException {
        extSrcs = session.dataService().getExternalSources(dsId, limit, filter);
        return extSrcs;
    }
    
    private int getNumExternalSrcs(int dsId, String filter) throws EmfException {
        return session.dataService().getNumExternalSources(dsId, filter);
    }
    
    public ExternalSource[] getViewableSources() {
        return extSrcs;
    }
    
    public int getCurrentDatasetId() {
        return dataset.getId();
    }

    public void checkIfLockedByCurrentUser(){
        //
    }
}
