package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

public interface ViewControlStrategySummaryTabView extends ViewControlStrategyTabView {

    void setRunMessage(ControlStrategy controlStrategy);

    void stopRun();

    void notifyStrategyTypeChange(StrategyType strategyType);
}
