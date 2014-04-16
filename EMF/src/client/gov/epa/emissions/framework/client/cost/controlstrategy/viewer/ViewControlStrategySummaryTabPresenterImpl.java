package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ViewControlStrategySummaryTabPresenterImpl implements ViewControlStrategySummaryTabPresenter {

    private ViewControlStrategySummaryTabView view;

    private ViewControlStrategyPresenter mainPresenter;

    public ViewControlStrategySummaryTabPresenterImpl(ViewControlStrategyPresenter mainPresenter, ControlStrategy controlStrategy,
            ViewControlStrategySummaryTabView view) {
        this.mainPresenter = mainPresenter;
        this.view = view;
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        view.run(controlStrategy);
    }
    
    public void setResults(ControlStrategy controlStrategy) {
        view.setRunMessage(controlStrategy);
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategy, controlStrategyResults);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        mainPresenter.doChangeStrategyType(strategyType);
    }

}
