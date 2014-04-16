package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

public interface StrategySummarySelectionView {

    void display(StrategyResultType[] strategyResultTypes);

//    void observe(StrategySummarySelectionPresenter presenter);

    StrategyResultType[] getStrategyResultTypes();

    void clearMessage();
}