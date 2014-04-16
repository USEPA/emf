package gov.epa.emissions.framework.services.cost.analysis;

import java.sql.SQLException;
import java.util.Date;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyCountySummaryTableFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

public class StrategyCountySummaryTask extends AbstractStrategySummaryTask {
    
    private ControlStrategy controlStrategy;
    
    private Datasource datasource;

//    private HibernateSessionFactory sessionFactory;
//
//    private DbServerFactory dbServerFactory;

    private DbServer dbServer;

    private ControlStrategyResult countySummaryResult;
    
    public StrategyCountySummaryTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory);
        this.controlStrategy = controlStrategy;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();

//        super(controlStrategy, user, 
//                dbServerFactory, sessionFactory);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
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

            countySummaryResult = null;
            try {
                //create new result
                countySummaryResult = createStrategyCountySummaryResult();
                
                populateStrategyCountySummaryDataset(getControlStrategyResults(), countySummaryResult);
                status = "Completed.";
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing strategy summary: . " + e.getMessage();
                setStatus(status);
            } finally {
                //update result ending info...
                if (countySummaryResult != null) {
                    setSummaryResultCount(countySummaryResult);
                    countySummaryResult.setCompletionTime(new Date());
                    countySummaryResult.setRunStatus(status);
                    saveControlStrategyResult(countySummaryResult);
                }
            }

        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                //
            }
        }
    }

    public void afterRun() {
        //TODO:  might need to index tables
    }

    public void beforeRun() {
        //TODO:  might need to index tables
    }

    public ControlStrategyResult getStrategyResult() {
        // NOTE Auto-generated method stub
        return countySummaryResult;
    }

    private EmfDataset createCountySummaryDataset() throws EmfException {
        return creator.addDataset("CSCS", 
                DatasetCreator.createDatasetName("Strat_County_Sum"), 
                getDatasetType(DatasetType.strategyCountySummary), 
                new StrategyCountySummaryTableFormat(dbServer.getSqlDataTypes()), 
                summaryResultDatasetDescription(DatasetType.strategyCountySummary));
    }

    private ControlStrategyResult createStrategyCountySummaryResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        EmfDataset summaryResultDataset = createCountySummaryDataset();
        
        result.setDetailedResultDataset(summaryResultDataset);
        
        result.setStrategyResultType(getStrategyResultType(StrategyResultType.strategyCountySummary));
        result.setStartTime(new Date());
        result.setRunStatus("Start processing summary result");

        //persist result
        saveControlStrategyResult(result);
        return result;
    }

    private void populateStrategyCountySummaryDataset(ControlStrategyResult[] results, ControlStrategyResult countySummaryResult) throws EmfException {
        if (results.length > 0) {
            ControlStrategyInputDataset[] inventories = controlStrategy.getControlStrategyInputDatasets();

            //SET work_mem TO '512MB';
            //NOTE:  Still need to  support mobile monthly files
            String sql = "INSERT INTO " + qualifiedEmissionTableName(countySummaryResult.getDetailedResultDataset()) + " (dataset_id, version, sector, fips, poll, Uncontrolled_Emis, Emis_Reduction, Remaining_Emis, Pct_Red, Annual_Cost, Annual_Oper_Maint_Cost, Annualized_Capital_Cost, Total_Capital_Cost, Avg_Ann_Cost_per_Ton) " 
            + "select " + countySummaryResult.getDetailedResultDataset().getId() + ", 0, sector, fips, poll, Uncontrolled_Emis, Emis_Reduction, Remaining_Emis, Pct_Red, Annual_Cost, Annual_Oper_Maint_Cost, Annualized_Capital_Cost, Total_Capital_Cost, Avg_Ann_Cost_per_Ton " 
            + "from (";
            int count = 0;
            
            EmfDataset mergedInventory = null;
            //we need to create a controlled inventory for each invnentory, except the merged inventory
            for (int i = 0; i < inventories.length; i++) {
                if (inventories[i].getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    mergedInventory = inventories[i].getInputDataset();
                    break;
                }
            }
            //if merged inventory, then there is only one result
            if (controlStrategy.getMergeInventories() && mergedInventory != null) {
                for (int i = 0; i < inventories.length; i++) {
//                      EmfDataset inventory = inventories[i].getInputDataset();
                  ControlStrategyInputDataset inventory = inventories[i];
                  if (!inventory.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                      for (int j = 0; j < results.length; j++) {
                          if (results[j].getDetailedResultDataset() != null 
                              && results[j].getInputDataset() != null) {
                              String detailedresultTableName = qualifiedEmissionTableName(results[j].getDetailedResultDataset());
                              String inventoryTableName = qualifiedEmissionTableName(inventory.getInputDataset());
                              String sector = inventory.getInputDataset().getSectors().length > 0 ? inventory.getInputDataset().getSectors()[0].getName() : "";
                              Version v = version(inventory);
                              VersionedQuery versionedQuery = new VersionedQuery(v);

                              sql += (count > 0 ? " union all " : "") 
                                  + "select '" + sector.replace("'", "''") + "' as sector, i.fips, i.poll, sum(i.ann_emis) as Uncontrolled_Emis, sum(e.Emis_Reduction) as Emis_Reduction, sum(i.ann_emis) - sum(e.Emis_Reduction) as Remaining_Emis, sum(e.Emis_Reduction) / sum(i.ann_emis) * 100.0 as Pct_Red, sum(e.Annual_Cost) as Annual_Cost, "
                                  + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
                                  + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
                                  + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
                                  + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
                                  + "from " + inventoryTableName + " i "
                                  + "left outer join " + detailedresultTableName + " e "
                                  + "on e.source_id = i.record_id "
                                  + "and e.ORIGINAL_DATASET_ID = " + inventory.getInputDataset().getId() + " "
                                  + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id")
                                  + "group by i.fips, i.poll ";
                              ++count;
                              }
                          }
                      }
                }
            //not a merged inventory, then there could be multiple results
            } else {

                for (int i = 0; i < results.length; i++) {
                    if (results[i].getDetailedResultDataset() != null && results[i].getInputDataset() != null) {
                        String detailedresultTableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
                        String inventoryTableName = qualifiedEmissionTableName(results[i].getInputDataset());
                        String sector = results[i].getInputDataset().getSectors().length > 0 ? results[i].getInputDataset().getSectors()[0].getName() : "";
                        Version v = version(results[i].getInputDataset().getId(), results[i].getInputDatasetVersion());
                        VersionedQuery versionedQuery = new VersionedQuery(v);
                        sql += (count > 0 ? " union all " : "") 
                            + "select '" + sector.replace("'", "''") + "' as sector, i.fips, i.poll, sum(i.ann_emis) as Uncontrolled_Emis, sum(e.Emis_Reduction) as Emis_Reduction, sum(i.ann_emis) - sum(e.Emis_Reduction) as Remaining_Emis, sum(e.Emis_Reduction) / sum(i.ann_emis) * 100.0 as Pct_Red, sum(e.Annual_Cost) as Annual_Cost, "
                            + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
                            + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
                            + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
                            + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
                            + "from " + inventoryTableName + " i "
                            + "left outer join " + detailedresultTableName + " e "
                            + "on e.source_id = i.record_id "
                            + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id")
                            + "group by i.fips, i.poll ";
                        ++count;
                    }
                }
            }
            sql += ") summary ";
            sql += "order by fips, sector, poll ";
            
            if (DebugLevels.DEBUG_25())
                System.out.println(sql);
            try {
                datasource.query().execute(sql);
            } catch (SQLException e) {
                throw new EmfException("Error occured when inserting data to strategy summary table" + "\n" + e.getMessage());
            }
        }
    }

    protected String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + table;
    }
}