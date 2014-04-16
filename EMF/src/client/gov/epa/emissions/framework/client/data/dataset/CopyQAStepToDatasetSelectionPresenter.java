package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class CopyQAStepToDatasetSelectionPresenter {

    private EmfSession session;

    private CopyQAStepToDatasetSelectionView view;
    
    private DatasetType[] datasetTypesToInclude;

    public CopyQAStepToDatasetSelectionPresenter(CopyQAStepToDatasetSelectionView view, EmfSession session,
            DatasetType[] datasetTypesToInclude) {
        this(view, session);
        this.datasetTypesToInclude = datasetTypesToInclude;
    }

    public CopyQAStepToDatasetSelectionPresenter(CopyQAStepToDatasetSelectionView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display(DatasetType defaultType, boolean selectSingle) throws Exception {
        view.observe(this);

        //get data...
        DatasetType[] datasetTypes = new DatasetType[] {};
        if (datasetTypesToInclude == null)
            datasetTypes = session.getLightDatasetTypes();
        else
            datasetTypes = datasetTypesToInclude;

        view.display(datasetTypes, defaultType, selectSingle);
    }
    
    public void refreshDatasets(DatasetType datasetType, String nameContaining) throws EmfException {
        view.refreshDatasets(session.dataService().getDatasets(datasetType.getId(), nameContaining));
    }
    
    public EmfDataset[] getDatasets() {
        return view.getDatasets();
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }

    public boolean shouldReplace() {
        // NOTE Auto-generated method stub
        return view.shouldReplace();
    }
    
}