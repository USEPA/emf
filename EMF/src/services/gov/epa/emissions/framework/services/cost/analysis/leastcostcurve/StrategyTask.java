package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.leastcost.LeastCostAbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.leastcost.LeastCostAbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends LeastCostAbstractStrategyTask {

    public StrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
        //get rid of strategy results
        deleteStrategyResults();

        //run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {
            //process/load each input dataset
            ControlStrategyInputDataset controlStrategyInputDataset = getInventory();
            try {
                this.getLoader().loadStrategyResult(controlStrategyInputDataset);
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". " + e.getMessage();
                setStatus(status);
            } finally {
                //
            }
            
        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
                updateVersionInfo();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                this.getLoader().disconnectDbServer();
                disconnectDbServer();
            }
        }
    }

    public void afterRun() throws EmfException {
        //finalize the result, update completion time and run status...
        super.finalizeCMWorksheetResult();

        super.finalizeCostCuveSummaryResult();
        
        if (controlStrategy.getApplyCAPMeasuresOnHAPPollutants())
            applyCAPMeasuresOnHAPPollutants(strategyResultList.toArray(new ControlStrategyResult[0]));
        
        this.checkMessagesForWarnings();
    }

    public void beforeRun() throws EmfException {
        //populate the Sources Table
        populateSourcesTable();

        //create the worksheet (strat result), if needed, maybe they don't want to recreate these...
        StrategyLoader loader = this.getLoader();
        if (loader instanceof LeastCostAbstractStrategyLoader) {

            LeastCostAbstractStrategyLoader leastCostAbstractStrategyLoader = (LeastCostAbstractStrategyLoader) loader;
            ControlStrategyResult[] results = leastCostAbstractStrategyLoader.getControlStrategyResults();
            if (controlStrategy.getDeleteResults() || results.length == 0) {
                
                leastCostCMWorksheetResult = leastCostAbstractStrategyLoader.loadLeastCostCMWorksheetResult();
                leastCostCurveSummaryResult = leastCostAbstractStrategyLoader.loadLeastCostCurveSummaryResult();
            } else {
            
                for (int i = results.length; i > 0; i--) {
                    ControlStrategyResult result = results[i - 1];
                    ControlStrategyResult leastCostCMWorksheetResult = null;
                    ControlStrategyResult leastCostCurveSummaryResult = null;
                    if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheet)) {
                        if (leastCostCMWorksheetResult == null) {
                            //set local and module level so we not to set again...
                            leastCostCMWorksheetResult = result;
                            this.leastCostCMWorksheetResult = result;
                        }
                    } else if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostCurveSummary)) {
                        if (leastCostCurveSummaryResult == null) {
                            //set local and module level so we not to set again...
                            leastCostCurveSummaryResult = result;
                            this.leastCostCurveSummaryResult = result;
                        }
                    }
                }
            }
            
            //create just in case these don't exist, maybe the strategy type was changed...
            if (leastCostCMWorksheetResult == null) leastCostCMWorksheetResult = leastCostAbstractStrategyLoader.loadLeastCostCMWorksheetResult();
            if (leastCostCurveSummaryResult == null) leastCostCurveSummaryResult = leastCostAbstractStrategyLoader.loadLeastCostCurveSummaryResult();
            
            //if there is more than one input inventory, then merge these into one dataset, 
            //then we use that as the input to the strategy run
            mergeInventoryDatasets();
        }
    }
}