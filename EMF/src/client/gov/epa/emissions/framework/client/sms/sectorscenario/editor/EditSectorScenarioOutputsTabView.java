package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

public interface EditSectorScenarioOutputsTabView extends EditSectorScenarioTabView {

    void observe(EditSectorScenarioOutputsTabPresenter presenter);
    
    void export();
    
    void analyze();
    
    String getExportFolder();

    void displayAnalyzeTable(String name, String[] fileNames);
    
    void clearMsgPanel();
    
    void display(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) throws EmfException ;
    
}
