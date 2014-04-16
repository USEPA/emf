package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;

public interface ControlStrategyConstraintsTabView extends EditControlStrategyTabView {
 //type holder now
    void observe(EditControlStrategyConstraintsTabPresenter presenter);

    void display(ControlStrategy strategy);

    void notifyStrategyTypeChange(StrategyType strategyType);

    void setTargetPollutant(ControlStrategyTargetPollutant controlStrategyTargetPollutant);
}
