package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface ViewControlStrategyTabView {
    
    void run(ControlStrategy controlStrategy) throws EmfException;
    
    void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);

    void notifyStrategyTypeChange(StrategyType strategyType);

    void notifyStrategyRun(ControlStrategy controlStrategy);
}
