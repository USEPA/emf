package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

public class StrategyTask extends LeastCostAbstractStrategyTask {

    public StrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
    }

    public void run() throws EmfException {
        
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
            ControlStrategyResult result = null;
            try {
                result = this.getLoader().loadStrategyResult(controlStrategyInputDataset);
                recordCount = this.getLoader().getRecordCount();
                result.setRecordCount(recordCount);
                status = "Completed.";
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". " + e.getMessage();
                setStatus(status);
            } finally {
                if (result != null) {
                    result.setCompletionTime(new Date());
                    result.setRunStatus(status);
                    saveControlStrategyResult(result);
                    strategyResultList.add(result);
                }
            }
            
            //now create the measure summary result based on the results from the strategy run...
            generateStrategyMeasureSummaryResult();

        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
                updateVersionInfo();
                updateStrategy();
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
        super.finalizeCMWorksheetResult();

        if (controlStrategy.getApplyCAPMeasuresOnHAPPollutants())
            applyCAPMeasuresOnHAPPollutants(strategyResultList.toArray(new ControlStrategyResult[0]));
        
        //now create the county summary result based on the results from the strategy run...
        generateStrategyCountySummaryResult(strategyResultList.toArray(new ControlStrategyResult[0]));

        this.checkMessagesForWarnings();
    }

    public void beforeRun() throws EmfException {
        //populate the Sources Table
        populateSourcesTable();

        StrategyLoader loader = this.getLoader();
        if (loader instanceof LeastCostAbstractStrategyLoader) {

            LeastCostAbstractStrategyLoader leastCostAbstractStrategyLoader = (LeastCostAbstractStrategyLoader) loader;
            leastCostCMWorksheetResult = leastCostAbstractStrategyLoader.loadLeastCostCMWorksheetResult();

            // create just in case these don't exist, maybe the strategy type was changed...
            if (leastCostCMWorksheetResult == null) {
                leastCostCMWorksheetResult = leastCostAbstractStrategyLoader.loadLeastCostCMWorksheetResult();
            }

            mergeInventoryDatasets();
        }
   }
}