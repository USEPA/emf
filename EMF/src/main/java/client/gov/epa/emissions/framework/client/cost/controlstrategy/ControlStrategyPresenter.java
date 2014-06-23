package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyPresenter {
    
    void doDisplay();

    void doClose() throws EmfException;
    
    int doSave(ControlStrategy controlStrategy) throws EmfException;

}
