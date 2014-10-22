package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public class ControlStrategySelectionPresenter {

    private ControlStrategiesTableData tableData;
    
    private StrategyGroupStrategiesTab parentView;

    private EmfSession session;
    
    public ControlStrategySelectionPresenter(StrategyGroupStrategiesTab parent, EmfSession session) {
        this.parentView = parent;
        this.session = session;
    }
    
    public void display(ControlStrategySelectionView view) throws EmfException {
        view.observe(this);
        tableData = new ControlStrategiesTableData(getAllControlStrategies());
        view.display(tableData);
    }
    
    public void doAdd(ControlStrategy[] strategies) {
        parentView.add(strategies);
    }
    
    public ControlStrategy[] getAllControlStrategies() throws EmfException {
        return session.controlStrategyService().getControlStrategies();
    }
}
