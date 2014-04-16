package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class StrategyLoader extends AbstractStrategyLoader {
    public StrategyLoader(User user, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset)
            throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        // not needed, done in the beforeRun method.
        // //make sure inventory has indexes created...
        // makeSureInventoryDatasetHasIndexes(inputDataset);

        // reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;

        // create detailed strategy result
        ControlStrategyResult result = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());

        createDetailedResultIndexes(result.getDetailedResultDataset());
        
        applyControlPrograms(controlStrategyInputDataset, result);

        // //create strategy messages result
        // strategyMessagesResult = createStrategyMessagesResult(inputDataset,
        // controlStrategyInputDataset.getVersion());
        // populateStrategyMessagesDataset(strategyMessagesResult);
        // setResultCount(strategyMessagesResult);
        //        
        // //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        // //is no point and keeping it around.
        // if (strategyMessagesResult.getRecordCount() == 0) {
        // deleteStrategyMessageResult(strategyMessagesResult);
        // //set it null, so it referenced later it will be known that it doesn't exist...
        // strategyMessagesResult = null;
        // } else {
        // strategyMessagesResult.setCompletionTime(new Date());
        // strategyMessagesResult.setRunStatus("Completed.");
        // saveControlStrategyResult(strategyMessagesResult);
        // }

        // do this after updating the previous result, else it will override it...
        // still need to set the record count...
        setResultCount(result);

        return result;
    }

    private void createDetailedResultIndexes(Dataset dataset) {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = emissionTableName(dataset);

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "source_id", false);
        dataTable.addIndex(table, "input_ds_id", false);
        dataTable.addIndex(table, "apply_order", false);

        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    
    }

    protected void populateMessageOutput() throws Exception {
        setStatus("Started post-run validation of control programs (i.e., identify unused packet records).");
        populateStrategyMessagesDataset(strategyMessagesResult);
        setStatus("Finished post-run validation of control programs.");
        setResultCount(strategyMessagesResult);

        // if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        // is no point and keeping it around.
        if (strategyMessagesResult.getRecordCount() == 0) {
            deleteStrategyMessageResult(strategyMessagesResult);
            // set it null, so it referenced later it will be known that it doesn't exist...
            strategyMessagesResult = null;
        } else {
            strategyMessagesResult.setCompletionTime(new Date());
            strategyMessagesResult.setRunStatus("Completed.");
            saveControlStrategyResult(strategyMessagesResult);
            creator.updateVersionZeroRecordCount((EmfDataset)strategyMessagesResult.getDetailedResultDataset());
        }
    }

    private void applyControlPrograms(ControlStrategyInputDataset controlStrategyInputDataset,
            ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.run_project_future_year_inventory(" + controlStrategy.getId() + ", "
                + controlStrategyInputDataset.getInputDataset().getId() + ", "
                + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId();
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            setStatus("Started applying \"plant closure\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            datasource.query().execute(query + ",'closure');");
            //this will update indexes for next step
            vacuumDetailedResultTable(controlStrategyResult);
            setStatus("Completed applying \"plant closure\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            setStatus("Started applying \"projection\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            datasource.query().execute(query + ",'projection');");
            //this will update indexes for next step
            vacuumDetailedResultTable(controlStrategyResult);
            setStatus("Completed applying \"projection\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            setStatus("Started applying \"control\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            datasource.query().execute(query + ",'control');");
            //this will update indexes for next step
            vacuumDetailedResultTable(controlStrategyResult);
            setStatus("Completed applying \"control\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            setStatus("Started applying \"allowable\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
            datasource.query().execute(query + ",'allowable');");
            setStatus("Completed applying \"allowable\" control programs on inventory, " 
                    + controlStrategyInputDataset.getInputDataset().getName() 
                    + ".");
        } catch (SQLException e) {
            if (DebugLevels.DEBUG_25())
                System.out.println("SQLException applyControlPrograms = " + e.getMessage());
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void vacuumDetailedResultTable(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        EmfDataset dataset = (EmfDataset)controlStrategyResult.getDetailedResultDataset();
        query = "vacuum analyze "  + qualifiedEmissionTableName(dataset) + ";";
        
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void populateStrategyMessagesDataset(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_project_future_year_inventory_strategy_messages(" + controlStrategy.getId()
                + ", " + controlStrategyResult.getId() + ");";
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            if (DebugLevels.DEBUG_25())
                System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    @Override
    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        // NOTE Auto-generated method stub

    }
    
    public void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        this.creator.addKeyVal(dataset, keywordName, value);
    }
    
    public void update(EmfDataset dataset) throws EmfException {
        this.creator.update(dataset);
    }
}
