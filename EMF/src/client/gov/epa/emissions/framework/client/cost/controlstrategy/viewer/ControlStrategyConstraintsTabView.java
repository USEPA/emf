package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyTabView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

public interface ControlStrategyConstraintsTabView extends EditControlStrategyTabView {
 //type holder now
    void observe(ViewControlStrategyConstraintsTabPresenter presenter);

    void display(ControlStrategy strategy);

    void notifyStrategyTypeChange(StrategyType strategyType);
}
