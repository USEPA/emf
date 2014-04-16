package gov.epa.emissions.framework.services.cost.analysis.multiPollutantMaxReduction;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class StrategyLoader extends AbstractStrategyLoader {

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //make sure inventory has indexes created...
        makeSureInventoryDatasetHasIndexes(inputDataset);
        //make sure inventory has the target pollutant, if not show a warning message
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

        EmfDataset detailedResultDS = (EmfDataset)detailedResult.getDetailedResultDataset();
        
        //index the detailed result, its will speed up the processing...
        createDetailedResultIndexes(detailedResultDS);
        
        //make sure and process in the order that the arrays are built (based on hibernate list_index column)
        for (ControlStrategyTargetPollutant controlStrategyTargetPollutant : controlStrategy.getTargetPollutants()) {
            setStatus("Started processing target pollutant, " + controlStrategyTargetPollutant.getPollutant().getName() + ", on inventory, " 
                    + inputDataset.getName() 
                    + ".");

            runStrategy(controlStrategyInputDataset, detailedResult, controlStrategyTargetPollutant.getPollutant());
            setStatus("Completed processing target pollutant, " + controlStrategyTargetPollutant.getPollutant().getName() + ", on inventory, " 
                    + inputDataset.getName() 
                    + ".");
        }
        
        runStrategyFinalize(controlStrategyInputDataset, detailedResult);
        
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
            saveControlStrategyResult(strategyMessagesResult);
            creator.updateVersionZeroRecordCount((EmfDataset)strategyMessagesResult.getDetailedResultDataset());
        }

        //do this after updating the previous result, else it will override it...
        //still need to set the record count...
        //still need to calculate the total cost and reduction...
        setResultTotalCostTotalReductionAndCount(detailedResult);


        return detailedResult;
    }

    private void createDetailedResultIndexes(EmfDataset detailedResult) {
        DataTable dataTable = new DataTable(detailedResult, datasource);
        String table = emissionTableName(detailedResult);

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "source_id", false);
        dataTable.addIndex(table, "fips", false);
        dataTable.addIndex(table, "scc", false);
        dataTable.addIndex(table, "plantid", false);
        dataTable.addIndex(table, "pointid", false);
        dataTable.addIndex(table, "stackid", false);
        dataTable.addIndex(table, "segment", false);

        
        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    }

    private void runStrategyFinalize(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.run_multi_pollutant_max_emis_red_strategy_finalize("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void runStrategy(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult, Pollutant targetPollutant) throws EmfException {
        String query = "";
        query = "SELECT public.run_multi_pollutant_max_emis_red_strategy("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ", " + targetPollutant.getId() + ");";
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //3
        }
    }

    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        //
    }
}