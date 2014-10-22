package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public interface StrategyGroupManagerView extends ManagedView {

    void display(StrategyGroup[] strategyGroups) throws EmfException;

    void observe(StrategyGroupManagerPresenter presenter);

    void refresh(StrategyGroup[] strategyGroups) throws EmfException;

}
