package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ViewControlStrategyPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doRun(ControlStrategy controlStrategy) throws EmfException;

    void setInventoryFilterTab(ViewControlStrategyTabView view);

    void setSummaryTab(ViewControlStrategySummaryTabView view);
    
    void setOutputTab(ViewControlStrategyOutputTabView view) throws EmfException;

    void setMeasuresTab(ViewControlStrategyMeasuresTabView view) throws EmfException;

    void setProgramsTab(ViewControlStrategyProgramsTab view);

    void setConstraintsTab(ViewControlStrategyConstraintsTabView view);

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
    
    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
}
