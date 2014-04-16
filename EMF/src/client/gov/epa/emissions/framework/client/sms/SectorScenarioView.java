package gov.epa.emissions.framework.client.sms;

import gov.epa.emissions.framework.services.EmfException;


public interface SectorScenarioView {

    void display() throws EmfException;

    void observe(SectorScenarioPresenter presenter);

    void clearMessage();
}