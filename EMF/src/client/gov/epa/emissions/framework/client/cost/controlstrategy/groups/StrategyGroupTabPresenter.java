package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public interface StrategyGroupTabPresenter {

    public void doDisplay() throws EmfException;
    
    public void doSave() throws EmfException;
    
    public void updateView(StrategyGroup strategyGroup);
}
