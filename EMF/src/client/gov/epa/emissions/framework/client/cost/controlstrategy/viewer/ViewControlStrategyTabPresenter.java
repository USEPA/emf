package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface ViewControlStrategyTabPresenter {

    void doRun(ControlStrategy controlStrategy) throws EmfException;

    void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);

    void doChangeStrategyType(StrategyType strategyType);
}
