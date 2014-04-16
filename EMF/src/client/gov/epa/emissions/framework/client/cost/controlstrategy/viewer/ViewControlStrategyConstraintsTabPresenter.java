package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;

public interface ViewControlStrategyConstraintsTabPresenter extends ViewControlStrategyTabPresenter {

    void doDisplay();

    ControlStrategyConstraint getConstraint();
    
    Pollutant[] getAllPollutants() throws EmfException;
}