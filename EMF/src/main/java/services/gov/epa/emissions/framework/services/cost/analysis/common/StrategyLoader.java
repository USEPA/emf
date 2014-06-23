package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface StrategyLoader {

    ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset inputDataset) throws Exception;
    
    void disconnectDbServer() throws EmfException;

    int getRecordCount();
    
    int getMessageDatasetRecordCount();
    
    ControlStrategyResult[] getControlStrategyResults();

    ControlStrategyResult getStrategyMessagesResult();

    ControlStrategyResult[] getStrategyMessagesResults();
    
    void makeSureInventoryDatasetHasIndexes(Dataset dataset);
}
