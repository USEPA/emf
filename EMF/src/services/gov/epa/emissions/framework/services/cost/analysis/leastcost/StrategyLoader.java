package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class StrategyLoader extends LeastCostAbstractStrategyLoader {
    
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            EntityManagerFactory entityManagerFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                entityManagerFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();

        //make sure inventory has the target pollutant, if not show a warning message
        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
            setStatus("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
//            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
        }
        
//        //see if inventory is a monthly, if so warn user these inventories are currently not supported.
//        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
//            setStatus("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
////            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
//        }
        
        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        
        //setup result
        ControlStrategyResult detailedResult = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
//        populateWorksheet(controlStrategyInputDataset);
        //setup result
//        if (controlStrategy.getDeleteResults() || results.length == 0)
            populateWorksheet(controlStrategyInputDataset);
//        else {
//            for (ControlStrategyResult result : results) {
//                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheet)) {
//                    leastCostCMWorksheetResult = result;
//                }
//            }
//        }

        Double targetEmissionReduction = getTargetEmissionReduction();
        populateDetailedResult(controlStrategyInputDataset, detailedResult, targetEmissionReduction);

        //create strategy messages result
        strategyMessagesResult = createStrategyMessagesResult(inputDataset, controlStrategyInputDataset.getVersion());
        populateStrategyMessagesDataset(controlStrategyInputDataset, strategyMessagesResult, detailedResult);
        setResultCount(strategyMessagesResult);
        
        //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        //is no point and keeping it around.
        if (strategyMessagesResult.getRecordCount() == 0) {
            deleteStrategyMessageResult(strategyMessagesResult);
            //set it null, so it referenced later it will be known that it doesn't exist...
            strategyMessagesResult = null;
        } else {
            strategyMessagesResult.setCompletionTime(new Date());
            strategyMessagesResult.setRunStatus("Completed.");
            updateControlStrategyResult(strategyMessagesResult);
            creator.updateVersionZeroRecordCount((EmfDataset)strategyMessagesResult.getDetailedResultDataset());
        }

        
        //still need to calculate the total cost and reduction...
        setResultTotalCostTotalReductionAndCount(detailedResult);
        
        //also get the uncontrolled emission...
        uncontrolledEmis = getUncontrolledEmission(controlStrategyInputDataset);
        addDetailedResultSummaryDatasetKeywords((EmfDataset)detailedResult.getDetailedResultDataset(), targetEmissionReduction);
        return detailedResult;
    }

    // return ControlStrategies orderby name
    public Double getTargetEmissionReduction() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return (Double)entityManager.createQuery("select coalesce(cS.domainWideEmisReduction, 0.0) " +
                    "from ControlStrategyConstraint cS " +
                    "where cS.controlStrategyId = " + controlStrategy.getId()).getSingleResult();
        } catch (RuntimeException e) {
            throw new EmfException("Could not get strategy target emission reduction");
        } finally {
            entityManager.close();
        }
    }
}
