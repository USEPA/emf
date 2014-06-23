package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public interface EditSectorScenarioTabPresenter {
    void doSave(SectorScenario sectorScenario) throws EmfException;
    
    void doRefresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException;
    
    void doViewOnly();
}
