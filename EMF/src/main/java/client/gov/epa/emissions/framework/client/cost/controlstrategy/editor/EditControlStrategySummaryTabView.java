package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

public interface EditControlStrategySummaryTabView  extends EditControlStrategyTabView{
    
    void setRunMessage(ControlStrategy controlStrategy);
    
    void stopRun();

    void notifyStrategyTypeChange(StrategyType strategyType);
}
