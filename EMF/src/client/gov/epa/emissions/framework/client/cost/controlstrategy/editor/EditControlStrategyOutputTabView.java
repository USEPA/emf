package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface EditControlStrategyOutputTabView extends EditControlStrategyTabView {

    void observe(EditControlStrategyOutputTabPresenter presenter);
    
    void export();
    
    void analyze();
    
    String getExportFolder();

    void displayAnalyzeTable(String controlStrategyName, String[] fileNames);
    
    void clearMsgPanel();
    
    void display(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) throws EmfException ;
    
}
