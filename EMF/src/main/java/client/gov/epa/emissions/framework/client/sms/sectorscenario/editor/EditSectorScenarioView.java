package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public interface EditSectorScenarioView extends ManagedView {

    void observe(EditSectorScenarioPresenter presenter);

    void display(SectorScenario sectorScenario) throws EmfException;
    
    void refresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs);
    
    void notifyLockFailure(SectorScenario sectorScenario);

    void notifyEditFailure(SectorScenario sectorScenario);

    void signalChanges();

    void stopRun();
    
    //void enableButtons(boolean enable);
    
}
