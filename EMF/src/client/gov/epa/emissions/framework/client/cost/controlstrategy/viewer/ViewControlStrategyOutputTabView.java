package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface ViewControlStrategyOutputTabView extends ViewControlStrategyTabView {

    void observe(ViewControlStrategyOutputTabPresenter presenter);

    void export();

    void analyze();

    String getExportFolder();

    void displayAnalyzeTable(String controlStrategyName, String[] fileNames);

    void clearMessage();

    void display(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) throws EmfException;

    String promptForColumnPrefix();
}
