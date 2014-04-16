package gov.epa.emissions.framework.client.sms.sectorscenario.base;

import gov.epa.emissions.framework.services.EmfException;


public interface NewSectorScenarioView {

    void display() throws EmfException;

    void observe(NewSectorScenarioPresenter presenter);

    void clearMessage();
}