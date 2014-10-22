package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesTableData;

public interface ControlStrategySelectionView {

    void display(ControlStrategiesTableData tableData);
    
    void observe(ControlStrategySelectionPresenter presenter);
}
