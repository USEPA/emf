package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

public interface ViewControlStrategySummaryTabPresenter extends ViewControlStrategyTabPresenter {

    void setResults(ControlStrategy controlStrategy);

    void doChangeStrategyType(StrategyType strategyType);
}