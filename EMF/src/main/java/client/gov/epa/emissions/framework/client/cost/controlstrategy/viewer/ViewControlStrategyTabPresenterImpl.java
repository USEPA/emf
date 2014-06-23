package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ViewControlStrategyTabPresenterImpl implements ViewControlStrategyTabPresenter {

    private ViewControlStrategyTabView view;

    public ViewControlStrategyTabPresenterImpl(ControlStrategy controlStrategy, ViewControlStrategyTabView view) {
        this.view = view;
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategy, controlStrategyResults);
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        view.run(controlStrategy);
    }
    
    public void doChangeStrategyType(StrategyType strategyType) {
        view.notifyStrategyTypeChange(strategyType);
    }
}
