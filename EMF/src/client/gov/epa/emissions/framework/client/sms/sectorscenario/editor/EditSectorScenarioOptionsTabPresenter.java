package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public class EditSectorScenarioOptionsTabPresenter implements EditSectorScenarioTabPresenter{

    private EmfSession session;

    private EditSectorScenarioOptionsTabView view;
    
    public EditSectorScenarioOptionsTabPresenter(EmfSession session, EditSectorScenarioOptionsTabView view) {
        this.session = session;
        this.view = view;
    }
    
    public void doSave(SectorScenario sectorScenario) throws EmfException{
        view.save(sectorScenario);
    }   

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }
    
    public EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException{
        return session.dataService().getDatasets(datasetType);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException{
        return session.getLightDatasetType(name);
    }

    public void doRefresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException {
        view.refresh(sectorScenario, sectorScenarioOutputs);  
        
    }

    public void doViewOnly() {
        view.viewOnly();  
    }
}