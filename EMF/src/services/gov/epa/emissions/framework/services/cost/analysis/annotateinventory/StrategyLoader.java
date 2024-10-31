package gov.epa.emissions.framework.services.cost.analysis.annotateinventory;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class StrategyLoader extends AbstractStrategyLoader {
    
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            EntityManagerFactory entityManagerFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                entityManagerFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //make sure inventory has indexes created...
        makeSureInventoryDatasetHasIndexes(inputDataset);

        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        
        ControlStrategyResult result = createAnnotatedInventoryResult(inputDataset, controlStrategyInputDataset.getVersion());
        
        populateInventory(controlStrategyInputDataset, result);

        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " done with");

        //still need to set the record count...
        setResultCount(result);
        return result;
    }

    private void populateInventory(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.run_annotate_inventory("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        if (DebugLevels.DEBUG_25())
            System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            if (DebugLevels.DEBUG_25())
                System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //3
        }
    }
    
    protected ControlStrategyResult createAnnotatedInventoryResult(EmfDataset inventory, int inventoryVersion) throws Exception {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inventory);
        result.setInputDatasetVersion(inventoryVersion);
        result.setDetailedResultDataset(createDataset(inventory));

        result.setStrategyResultType(getAnnotatedInventoryResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing annotated inventory result");

        //persist result
        addControlStrategyResult(result);

        return result;
    }

    private EmfDataset createDataset(EmfDataset inventory) throws Exception {
        //"LeatCostCM_", 
        return creator.addDataset("DS", 
                DatasetCreator.createDatasetName(inventory.getName() + "_annotated"), 
                inventory.getDatasetType(), 
                new FileFormatFactory(dbServer).tableFormat(inventory.getDatasetType()), 
                inventory.getDescription());
    }

    private StrategyResultType getAnnotatedInventoryResultType() throws EmfException {
        StrategyResultType resultType = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.annotatedInventory, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            entityManager.close();
        }
        return resultType;
    }

    @Override
    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        // NOTE Auto-generated method stub
        
    }
}
