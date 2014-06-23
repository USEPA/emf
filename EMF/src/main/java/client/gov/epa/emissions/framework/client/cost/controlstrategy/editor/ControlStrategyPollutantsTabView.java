package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyPollutantsTabView extends EditControlStrategyTabView {
 //type holder now
    void observe(EditControlStrategyPollutantsTabPresenter presenter);

    void display(ControlStrategy strategy);
}
