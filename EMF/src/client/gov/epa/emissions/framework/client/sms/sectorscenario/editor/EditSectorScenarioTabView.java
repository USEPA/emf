package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public interface EditSectorScenarioTabView {
    
    void save(SectorScenario sectorScenario) throws EmfException;
    //void run(SectorScenario sectorScenario) throws EmfException;
    void refresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException;
 
//  //void notifyScenarioRun(SectorScenario sectorScenario);
    
//    void showError(String message);
//      
//    void showRemindingMessage(String msg);
//    

    void viewOnly();
}
