package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;

public class StrategyGroupNotesTabPresenter implements StrategyGroupTabPresenter {

    private StrategyGroupNotesTab view;
    
    public StrategyGroupNotesTabPresenter(StrategyGroupTabView view) {
        this.view = (StrategyGroupNotesTab) view;
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
