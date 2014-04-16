package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyMeasuresTabPresenter  implements EditControlStrategyTabPresenter {
    private ControlStrategyMeasuresTabView view;
    
    private EmfSession session;
    
    private ControlStrategy strategy;

    private ControlStrategiesManagerPresenter controlStrategiesManagerPresenter;

    public EditControlStrategyMeasuresTabPresenter(ControlStrategyMeasuresTabView view, 
            ControlStrategy strategy, EmfSession session, 
            ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.strategy = strategy;
        this.session = session;
        this.view = view;
        this.controlStrategiesManagerPresenter = controlStrategiesManagerPresenter;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.strategy);
    }

    public ControlMeasureClass[] getAllClasses() throws EmfException {
        return session.controlMeasureService().getMeasureClasses();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return strategy.getControlMeasureClasses();
    }

    public ControlStrategyMeasure[] getControlMeasures() {
        return strategy.getControlMeasures();
    }

    public LightControlMeasure[] getAllControlMeasures() {
        return controlStrategiesManagerPresenter.getControlMeasures();
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
