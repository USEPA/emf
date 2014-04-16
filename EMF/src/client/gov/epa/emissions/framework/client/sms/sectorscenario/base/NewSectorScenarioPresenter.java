package gov.epa.emissions.framework.client.sms.sectorscenario.base;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface NewSectorScenarioPresenter {

    void display() throws EmfException;
    
    void addSectorScenario(SectorScenario sectorScenario) throws EmfException;

    EmfSession getSession();


}