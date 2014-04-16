package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

public interface EditControlStrategySummaryTabPresenter extends EditControlStrategyTabPresenter {

    void setResults(ControlStrategy controlStrategy);

    void doChangeStrategyType(StrategyType strategyType);
}