package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.ManagedView;

public interface ControlStrategyView extends ManagedView {

    void observe(ControlStrategyPresenter presenter, ControlStrategiesManagerPresenter managerPresenter);

    void display();

}
