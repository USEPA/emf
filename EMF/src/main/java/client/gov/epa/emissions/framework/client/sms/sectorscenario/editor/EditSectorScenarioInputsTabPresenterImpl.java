package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public class EditSectorScenarioInputsTabPresenterImpl implements EditSectorScenarioInputsTabPresenter{

    private EmfSession session;

    private EditSectorScenarioInputsTabView view;
    
    public EditSectorScenarioInputsTabPresenterImpl(EmfSession session, EditSectorScenarioInputsTabView view) {
        this.session = session;
        this.view = view;
    }
    
    public void doSave(SectorScenario sectorScenario) throws EmfException{
        view.save(sectorScenario);
    }   


    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
//    
//    public EmfSession getSession(){
//        return session; 
//    }

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }
    
    public EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException{
        return session.dataService().getDatasets(datasetType);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException{
        return session.getLightDatasetType(name);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public void doRefresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException {
        view.refresh(sectorScenario, sectorScenarioOutputs);  
        
    }

    public void doViewOnly() {
        view.viewOnly();  
    }
}