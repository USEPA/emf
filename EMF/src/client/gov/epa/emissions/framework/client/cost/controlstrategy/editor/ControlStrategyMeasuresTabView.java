package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyMeasuresTabView extends EditControlStrategyTabView {
 //type holder now
    void observe(EditControlStrategyMeasuresTabPresenter presenter);

    void display(ControlStrategy strategy) throws EmfException ;
}
