package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public interface EditSectorScenarioPresenter{

    void doDisplay() throws EmfException;  
    
    void doClose() throws EmfException;
    
    void doLoad(String tabTitle) throws EmfException;
    
    EmfSession getSession();
    
    Project[] getProjects() throws EmfException;
    
    void set(EditSectorScenarioSummaryTabView summaryView);
    
    void set(EditSectorScenarioInputsTabView summaryView);
    
    void set(EditSectorScenarioOptionsTabView summaryView);
    
    void set(EditSectorScenarioOutputsTabView view);

    void doRefresh() throws EmfException;
    
    void doSave(SectorScenario sectorScenario) throws EmfException; 
    
    void fireTracking();

    void runSectorScenario(int sectorScenarioId) throws EmfException;
    
    void stopRunningSectorScenario(int sectorScenarioId) throws EmfException;
    
    EmfDataset getDataset(int id) throws EmfException;
    
    SectorScenarioOutput[] getOutputs() throws EmfException;
    
}
