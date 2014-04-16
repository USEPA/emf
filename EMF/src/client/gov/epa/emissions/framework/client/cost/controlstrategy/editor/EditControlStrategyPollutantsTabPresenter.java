package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyPollutantsTabPresenter  implements EditControlStrategyTabPresenter {
    private ControlStrategyPollutantsTabView view;
    
    private EmfSession session;
    
    private ControlStrategy strategy;

    public EditControlStrategyPollutantsTabPresenter(ControlStrategyPollutantsTabView view, 
            ControlStrategy strategy, EmfSession session) {
        this.strategy = strategy;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay() {
        view.observe(this);
        view.display(this.strategy);
    }

    public Pollutant[] getAllPollutants() throws EmfException {
        return session.dataCommonsService().getPollutants();
    }

    public Pollutant[] getPollutants() {
        return new Pollutant[] {strategy.getTargetPollutant()};
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        this.strategy = controlStrategy;
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        this.strategy = controlStrategy;
        view.save(controlStrategy);
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        this.strategy = controlStrategy;
        view.run(controlStrategy);
    }
    
    public void doChangeStrategyType(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }

    public void doSetTargetPollutants(Pollutant[] pollutants) {
        // NOTE Auto-generated method stub
        
    }
}