package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface ViewControlStrategyView extends ManagedView {

    void observe(ViewControlStrategyPresenter presenter);

    void display(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);
    
    void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);
    
    void notifyLockFailure(ControlStrategy controlStrategy);
//
//    void notifyEditFailure(ControlStrategy controlStrategy);
//
//    void notifyStrategyTypeChange(StrategyType strategyType);
//
//    public void startControlMeasuresRefresh();
//
//    public void endControlMeasuresRefresh();
    
    void signalChanges();    
}
