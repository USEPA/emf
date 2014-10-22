package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public interface StrategyGroupView extends ManagedView {

    void observe(StrategyGroupPresenter presenter);
    
    void display(StrategyGroup strategyGroup);
    
    void notifyLockFailure(StrategyGroup strategyGroup);
}
