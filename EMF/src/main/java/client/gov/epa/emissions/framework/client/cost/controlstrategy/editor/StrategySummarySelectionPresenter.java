package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

public class StrategySummarySelectionPresenter {

    private EmfSession session;

    private StrategySummarySelectionView view;
    
    public StrategySummarySelectionPresenter(StrategySummarySelectionView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display() throws Exception {
//        view.observe(this);

        //get data...
        StrategyResultType[] strategyResultTypes = new StrategyResultType[] {};
        strategyResultTypes = session.controlStrategyService().getOptionalStrategyResultTypes();

        view.display(strategyResultTypes);
    }
    
    public StrategyResultType[] getStrategyResultTypes() {
        return view.getStrategyResultTypes();
    }

    public EmfSession getSession(){
        return session; 
    }
}