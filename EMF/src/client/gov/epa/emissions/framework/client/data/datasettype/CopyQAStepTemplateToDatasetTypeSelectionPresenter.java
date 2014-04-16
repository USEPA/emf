package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class CopyQAStepTemplateToDatasetTypeSelectionPresenter {

    private EmfSession session;

    private CopyQAStepTemplateToDatasetTypeSelectionView view;
    
    public CopyQAStepTemplateToDatasetTypeSelectionPresenter(CopyQAStepTemplateToDatasetTypeSelectionView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display(DatasetType[] defaultSelectedDatasetTypes) throws Exception {
        view.observe(this);

        //get data...
        DatasetType[] datasetTypes = session.dataCommonsService().getLightDatasetTypes();

        view.display(datasetTypes, defaultSelectedDatasetTypes);
    }
    
    public DatasetType[] getSelectedDatasetTypes() {
        return view.getSelectedDatasetTypes();
    }
    
    public boolean shouldReplace() {
        return view.shouldReplace();
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfSession getSession(){
        return session; 
    }
}