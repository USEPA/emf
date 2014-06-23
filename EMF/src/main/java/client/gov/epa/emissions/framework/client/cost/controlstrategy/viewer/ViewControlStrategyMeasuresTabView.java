package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ViewControlStrategyMeasuresTabView extends ViewControlStrategyTabView {
    // type holder now
    void observe(ViewControlStrategyMeasuresTabPresenter presenter);

    void display(ControlStrategy strategy) throws EmfException;
}
