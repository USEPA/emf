package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

import javax.persistence.Basic;

public interface ControlStrategyManagerView extends ManagedView {

    void display(ControlStrategy[] controlStrategies) throws EmfException;

    void observe(ControlStrategiesManagerPresenterImpl presenter);

    void refresh(ControlStrategy[] controlStrategies) throws EmfException;

    void displayControlStrategyComparisonResult(String string, String absolutePath);

    BasicSearchFilter getSearchFilter();
}
