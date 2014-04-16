package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditControlStrategyPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave(ControlStrategy controlStrategy) throws EmfException;

    void doRun(ControlStrategy controlStrategy) throws EmfException;

    void set(EditControlStrategyTabView view);

    void set(EditControlStrategySummaryTabView view);
    
    EditControlStrategySummaryTabPresenter getSummaryPresenter();
    
    void set(EditControlStrategyOutputTabView view) throws EmfException;

    void set(ControlStrategyMeasuresTabView view) throws EmfException;

    void set(ControlStrategyProgramsTab view);

    void set(ControlStrategyPollutantsTabView view) throws EmfException;

    void set(ControlStrategyConstraintsTabView view);

    void runStrategy() throws EmfException;

    void setResults(ControlStrategy controlStrategy);

    void stopRun() throws EmfException;

    void doRefresh() throws EmfException;

    CostYearTable getCostYearTable() throws EmfException;
    
    void fireTracking();
    
    DatasetType getDatasetType(String name) throws EmfException;
    
    Version[] getVersions(EmfDataset dataset) throws EmfException; 

    Version getVersion(int datasetId, int version) throws EmfException; 

    EmfDataset[] getDatasets(DatasetType type) throws EmfException;

    EmfDataset getDataset(int id) throws EmfException;

    boolean hasResults() throws EmfException;
    
    void doChangeStrategyType(StrategyType strategyType);
    
    void resetButtons(boolean enable); 
    
    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
}
