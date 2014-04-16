package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;

public interface MPSDTManagerPresenter {

    void doClose();

    void doDisplay() throws EmfException;
}