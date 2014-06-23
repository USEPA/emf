package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface SectorScenarioManagerView extends ManagedView {

    void display(SectorScenario[] sectorScenarios) throws EmfException;

    void observe(SectorScenarioManagerPresenterImpl presenter);

    void refresh(SectorScenario[] sectorScenarios) throws EmfException;
    
    void addNewSSToTableData(SectorScenario sectorScenario);
}
