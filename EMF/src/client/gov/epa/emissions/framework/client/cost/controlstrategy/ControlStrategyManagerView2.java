package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.GenericManagerView;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyManagerView2 extends GenericManagerView<ControlStrategy, ControlStrategiesManagerPresenterImpl, ControlStrategy> {

    void displayControlStrategyComparisonResult(String string, String absolutePath);
}
