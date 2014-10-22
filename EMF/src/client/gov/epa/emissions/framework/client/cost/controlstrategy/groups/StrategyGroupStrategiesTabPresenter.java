package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public class StrategyGroupStrategiesTabPresenter implements StrategyGroupTabPresenter {

    private StrategyGroupStrategiesTab view;
    
    public StrategyGroupStrategiesTabPresenter(StrategyGroupTabView view) {
        this.view = (StrategyGroupStrategiesTab) view;
    }
    
    public void doDisplay() throws EmfException {
        view.display();
    }

    public void doSave() throws EmfException {
        view.save();
    }
    
    public void updateView(StrategyGroup strategyGroup) {
        view.setStrategyGroup(strategyGroup);
    }

}
