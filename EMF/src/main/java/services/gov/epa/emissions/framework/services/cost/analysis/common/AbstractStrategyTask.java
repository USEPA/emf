package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.data.SectorsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;


public abstract class AbstractStrategyTask implements Strategy {
    
    protected ControlStrategy controlStrategy;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    protected User user;
    
    protected int recordCount;
    
    protected int controlStrategyInputDatasetCount;
    
    private StatusDAO statusDAO;
    
    protected ControlStrategyDAO controlStrategyDAO;
    
    protected DatasetCreator creator;
    
    private Keywords keywords;

//    private TableFormat tableFormat;
    
    protected List<ControlStrategyResult> strategyResultList;

    protected StrategyLoader loader;
    
    public AbstractStrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {

        this.controlStrategy = controlStrategy;
        this.controlStrategyInputDatasetCount = controlStrategy.getControlStrategyInputDatasets().length;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.loader = loader;
        this.user = user;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.controlStrategyDAO = new ControlStrategyDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.strategyResultList = new ArrayList<ControlStrategyResult>();
        //setup the strategy run
        setup();
    }

    private void setup() {
        //
    }
    
    public void run(StrategyLoader loader) throws EmfException {
        
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
            ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
//                if (!controlStrategyInputDatasets[i].getInputDataset().getDatasetType().getName().contains("ORL")) {
//                    setStatus("The inventory, " + controlStrategyInputDatasets[i].getInputDataset().getName() + ", won't be processed only ORL Inventores are currently supported.");
//                    break;
//                }
                ControlStrategyResult result = null;
                try {
                    result = loader.loadStrategyResult(controlStrategyInputDatasets[i]);
                    recordCount = loader.getRecordCount();
                    result.setRecordCount(recordCount);
                    status = "Completed.";
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Failed. Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". " + e.getMessage();
                    setStatus(status);
                } finally {
                    if (result != null) {
                        result.setCompletionTime(new Date());
                        result.setRunStatus(status);
                        saveControlStrategyResult(result);
                        strategyResultList.add(result);
                    }
                    //see if there was an error, if so, make sure and propogate to the calling method.
                    if (status.startsWith("Failed"))
                        throw new EmfException(status);
                            
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
                        status = "Cancelled. Strategy run was cancelled: " + controlStrategy.getName();
                        setStatus(status);
                        return;
//                        throw new EmfException("Strategy run was cancelled.");
                    }
                    //
                }
            }

            //now create the measure summary result based on the results from the strategy run...
            generateStrategyMeasureSummaryResult();

//            //now create the county summary result based on the results from the strategy run...
//            generateStrategyCountySummaryResult();

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
                loader.disconnectDbServer();
                disconnectDbServer();
            }
        }
    }

    protected void updateVersionInfo() throws EmfException {
//        ControlStrategyResult[] results = results;//getControlStrategyResults();
        
        for (ControlStrategyResult result : strategyResultList)
            updateResultDataset(result);
    }
    
    protected void updateStrategy() throws EmfException {
        double totalCost = 0;
        double totalReduction = 0;
        
        for (ControlStrategyResult result: strategyResultList) {
            totalCost += result.getTotalCost() != null ? result.getTotalCost() : 0.0;
            totalReduction += result.getTotalReduction() != null ? result.getTotalReduction() : 0.0;
        }
        
        controlStrategy.setTotalCost(new Double(totalCost));
        controlStrategy.setTotalReduction(new Double(totalReduction));
        
        Session session = sessionFactory.getSession();
        
        try {
            controlStrategyDAO.updateWithoutLock(controlStrategy, session);
//            controlStrategyDAO.updateWithLock(controlStrategy, session); //NOTE: user should have lock at this time
            if (DebugLevels.DEBUG_25())
                System.out.println("totalCost: " + totalCost + "  totalReduciton: " + totalReduction);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Error updating control strategy's total cost and reduction. " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.clear();
        }
    }

    protected void deleteStrategyResults() throws EmfException { // JIZHEN-SECTOR
        //get rid of strategy results...
        if (controlStrategy.getDeleteResults()){
            Session session = sessionFactory.getSession();
            try {
                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
                //first get the datasets to delete
                EmfDataset[] datasets = controlStrategyDAO.getResultDatasets(controlStrategy.getId(), session);
                if (datasets != null) {
                    for (EmfDataset dataset : datasets) {
                        if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                            setStatus("The control strategy result dataset, " + dataset.getName() + ", will not be deleted since you are not the creator of, this control strategy result will not be deleted.");
                        } else {
                            dsList.add(dataset);
                        }
                    }
                }
//                EmfDataset[] dsList = controlStrategyDAO.getResultDatasets(controlStrategy.getId(), session);
                //get rid of old strategy results...
                removeControlStrategyResults();  //JIZHEN-SECTOR
                //delete and purge datasets
                if (dsList != null && dsList.size() > 0){
                    controlStrategyDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw new EmfException("Could not remove Control Strategies results.");
            } finally {
                session.close();
            }
        }
    }
    
    protected void deleteStrategyResults(ControlStrategyResult[] results) throws EmfException {
        //get rid of strategy results...
        if (controlStrategy.getDeleteResults()){
            Session session = sessionFactory.getSession();
            try {
                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
                //first ge tthe datasets to delete
                if (results != null) {
                    for (ControlStrategyResult result : results) {
                        for (EmfDataset dataset : controlStrategyDAO.getResultDatasets(controlStrategy.getId(), result.getId(), session)) {
                            if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                                setStatus("Since you are not the creator of " + dataset.getName() + ", this control strategy result will not be deleted.");
                            } else {
                                dsList.add(dataset);
                            }
                        }
                    }
                    for (ControlStrategyResult result : results) {
                        //get rid of old strategy results...
                        removeControlStrategyResult(result.getId());
                    }
                }
                //delete and purge datasets
                if (dsList != null && dsList.size() > 0) {
                    controlStrategyDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw new EmfException("Could not remove Control Strategies results.");
            } finally {
                session.close();
            }
        }
    }

    protected void generateStrategyMeasureSummaryResult() throws EmfException {
        //now create the summary detailed result based on the results from the strategy run...
        if (strategyResultList.size() > 0) {
            //create dataset and strategy summary result 
            ControlStrategyResult measureSummaryResult = createStrategyMeasureSummaryResult();
            //now populate the summary result with data...
            populateStrategyMeasureSummaryDataset(strategyResultList.toArray(new ControlStrategyResult[0]), measureSummaryResult);
            
            //finalize the result, update completion time and run status...
            measureSummaryResult.setCompletionTime(new Date());
            measureSummaryResult.setRunStatus("Completed.");
            setSummaryResultCount(measureSummaryResult);
            saveControlStrategySummaryResult(measureSummaryResult);
            strategyResultList.add(measureSummaryResult);
//            runSummaryQASteps((EmfDataset)measureSummaryResult.getDetailedResultDataset(), 0);
        }
    }
    
    protected void applyCAPMeasuresOnHAPPollutants(ControlStrategyResult[] results) throws EmfException {
        String detailedStrategyResultIdList = "";
        if (results.length > 0) {
            for (int j = 0; j < results.length; j++) {
                if (results[j].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    detailedStrategyResultIdList += (detailedStrategyResultIdList.length() > 0 ? "," : "") + results[j].getId();
                }
            }
        }
        
        if (detailedStrategyResultIdList.length() > 0) {
            String sql = "select public.apply_cap_measures_on_hap_pollutants(" + controlStrategy.getId() + ", '{ " + detailedStrategyResultIdList + " }');";
            if (DebugLevels.DEBUG_25())
                System.out.println(sql);
            try {
                setStatus("Started applying CAP measures on hap pollutants.");
                datasource.query().execute(sql);
                setStatus("Completed applying CAP measures on hap pollutants.");
            } catch (SQLException e) {
                throw new EmfException("Error occured when applying CAP measures on HAP pollutants:" + "\n" + e.getMessage());
            }
            //update the record count for the result
            for (int j = 0; j < results.length; j++) {
                if (results[j].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    setSummaryResultCount(results[j]);
                    saveControlStrategySummaryResult(results[j]);
                }
            }
        }
    }
    
    protected void generateStrategyCountySummaryResult(ControlStrategyResult[] results) throws EmfException {
        //now create the summary detailed result based on the results from the strategy run...
        if (results.length > 0) {
            //create dataset and strategy region summary result 
            ControlStrategyResult countySummaryResult = createStrategyCountySummaryResult();
            //now populate the summary result with data...
            populateStrategyCountySummaryDataset(results, countySummaryResult);
            
            //finalize the result, update completion time and run status...
            countySummaryResult.setCompletionTime(new Date());
            countySummaryResult.setRunStatus("Completed.");
            setSummaryResultCount(countySummaryResult);
            saveControlStrategySummaryResult(countySummaryResult);
            strategyResultList.add(countySummaryResult);
//            runSummaryQASteps((EmfDataset)countySummaryResult.getDetailedResultDataset(), 0);
        }
    }

    private void populateStrategyMeasureSummaryDataset(ControlStrategyResult[] results, ControlStrategyResult summaryResult) throws EmfException {
        if (results.length > 0) {
            
            String sql = "INSERT INTO " + qualifiedEmissionTableName(summaryResult.getDetailedResultDataset()) + " (dataset_id, version, sector, fips, scc, poll, Control_Measure_Abbreviation, Control_Measure, Control_Technology, source_group, avg_ann_cost_per_ton, Annual_Cost, input_emis, Emis_Reduction, Pct_Red) " 
            + "select " + summaryResult.getDetailedResultDataset().getId() + ", 0, "
            + "summary.sector, "
            + "summary.fips, "
            + "summary.scc, "
            + "summary.poll, "
            + "cm.abbreviation, "
            + "cm.name, "
            + "ct.name as Control_Technology, "
            + "sg.name as source_group, "
            + "case when sum(summary.Emis_Reduction) <> 0 then sum(summary.Annual_Cost) / sum(summary.Emis_Reduction) else null::double precision end as avg_cost_per_ton, " 
            + "sum(summary.Annual_Cost) as Annual_Cost, "
            + "sum(summary.input_emis) as input_emis, " 
            + "sum(summary.Emis_Reduction) as Emis_Reduction, " 
            + "case when sum(summary.input_emis) <> 0 then (sum(summary.Emis_Reduction)) / sum(summary.input_emis) * 100.0 else null::double precision end as Pct_Red " 
            + "from (";
            int count = 0;
            for (int i = 0; i < results.length; i++) {
                if (results[i].getDetailedResultDataset() != null) {
                    String tableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
                    sql += (count > 0 ? " union all " : "") 
                        + "select e.sector, e.fips, e.scc, e.poll, e.cm_id, sum(e.Annual_Cost) as Annual_Cost, sum(e.Emis_Reduction) as Emis_Reduction, "
                        + "sum(e.input_emis) as input_emis "
                        + "from " + tableName + " e "
                        + "group by e.sector, e.fips, e.scc, e.poll, e.cm_id ";
                    ++count;
                }
            }




//            ControlStrategyInputDataset[] inventories = controlStrategy.getControlStrategyInputDatasets();
//            int count = 0;
//            
//            EmfDataset mergedInventory = null;
//            //we need to create a controlled inventory for each inventory, except the merged inventory
//            for (int i = 0; i < inventories.length; i++) {
//                if (inventories[i].getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
//                    mergedInventory = inventories[i].getInputDataset();
//                    break;
//                }
//            }
//            //if merged inventory, then there is only one result
//            if (controlStrategy.getMergeInventories() && mergedInventory != null) {
//                for (int i = 0; i < inventories.length; i++) {
////                      EmfDataset inventory = inventories[i].getInputDataset();
//                  ControlStrategyInputDataset inventory = inventories[i];
//                  if (!inventory.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
//
//                      for (int j = 0; j < results.length; j++) {
//                          if (results[j].getDetailedResultDataset() != null 
//                              && results[j].getInputDataset() != null) {
//                              String detailedresultTableName = qualifiedEmissionTableName(results[j].getDetailedResultDataset());
//                              String inventoryTableName = qualifiedEmissionTableName(inventory.getInputDataset());
//                              String sector = inventory.getInputDataset().getSectors().length > 0 ? inventory.getInputDataset().getSectors()[0].getName() : "";
//                              Version v = version(inventory);
//                              VersionedQuery versionedQuery = new VersionedQuery(v);
//                              int month = inventory.getInputDataset().applicableMonth();
//                              int noOfDaysInMonth = 31;
//                              if (month != -1) {
//                                  noOfDaysInMonth = getDaysInMonth(month);
//                              }
//                              String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
//                              sql += (count > 0 ? " union all " : "") 
//                                  + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
//                                  + "i.fips, "
//                                  + "i.poll, "
//                                  + "i.scc, "
//                                  + "e.cm_id, "
//                                  + "sum(" + sqlAnnEmis + ") as Input_Emis, "
//                                  + "sum(e.Emis_Reduction) as Emis_Reduction, "
//                                  + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
//                                  + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
//                                  + "sum(e.Annual_Cost) as Annual_Cost, "
//                                  + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
//                                  + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
//                                  + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
//                                  + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
//                                  + "from " + inventoryTableName + " i "
//                                  + "left outer join " + detailedresultTableName + " e "
//                                  + "on e.source_id = i.record_id "
//                                  + "and e.ORIGINAL_DATASET_ID = " + inventory.getInputDataset().getId() + " "
//                                  + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
//                                  + "group by i.fips, i.poll, i.scc, e.cm_id ";
//                              ++count;
//                              }
//                          }
//                      }
//                }
//            //not a merged inventory, then there could be multiple results
//            } else {
////case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + " else 0.0 end as ceff
//                for (int i = 0; i < results.length; i++) {
//                    if (results[i].getDetailedResultDataset() != null && results[i].getInputDataset() != null) {
//                        String detailedresultTableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
//                        String inventoryTableName = qualifiedEmissionTableName(results[i].getInputDataset());
//                        String sector = results[i].getInputDataset().getSectors().length > 0 ? results[i].getInputDataset().getSectors()[0].getName() : "";
//                        Version v = version(results[i].getInputDataset().getId(), results[i].getInputDatasetVersion());
//                        VersionedQuery versionedQuery = new VersionedQuery(v);
//                        int month = results[i].getInputDataset().applicableMonth();
//                        int noOfDaysInMonth = 31;
//                        if (month != -1) {
//                            noOfDaysInMonth = getDaysInMonth(month);
//                        }
//                        String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
//                        sql += (count > 0 ? " union all " : "") 
//                            + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
//                            + "i.fips, "
//                            + "i.poll, "
//                            + "i.scc, "
//                            + "e.cm_id, "
//                            + "sum(" + sqlAnnEmis + ") as Input_Emis, "
//                            + "sum(e.Emis_Reduction) as Emis_Reduction, "
//                            + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
//                            + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
//                            + "sum(e.Annual_Cost) as Annual_Cost, "
//                            + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
//                            + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
//                            + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
//                            + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
//                            + "from " + inventoryTableName + " i "
//                            + "left outer join " + detailedresultTableName + " e "
//                            + "on e.source_id = i.record_id "
//                            + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
//                            + "group by i.fips, i.poll, i.scc, e.cm_id";
//                        ++count;
//                    }
//                }
//            }
//            
            
            sql += ") summary "
                + "left outer join emf.control_measures cm "
                + "on cm.id = summary.cm_id "
                + "left outer join emf.control_technologies ct "
                + "on ct.id = cm.control_technology "
                + "left outer join emf.source_groups sg "
                + "on sg.id = cm.source_group "
                + "group by summary.sector, summary.fips, summary.scc, summary.poll, cm.abbreviation, cm.name, ct.name, sg.name "
                + "order by summary.sector, summary.fips, summary.scc, summary.poll, cm.abbreviation, cm.name, ct.name, sg.name";
            if (DebugLevels.DEBUG_25())
                System.out.println(sql);
            try {
                datasource.query().execute(sql);
            } catch (SQLException e) {
                throw new EmfException("Error occured when inserting data to strategy measure summary table" + "\n" + e.getMessage());
            }
        }
    }

    private void populateStrategyCountySummaryDataset(ControlStrategyResult[] results, ControlStrategyResult countySummaryResult) throws EmfException {
        if (results.length > 0) {
            ControlStrategyInputDataset[] inventories = controlStrategy.getControlStrategyInputDatasets();

            //SET work_mem TO '512MB';
            //NOTE:  Still need to  support mobile monthly files
            String sql = "INSERT INTO " + qualifiedEmissionTableName(countySummaryResult.getDetailedResultDataset()) + " (dataset_id, version, sector, fips, poll, Input_Emis, Emis_Reduction, Remaining_Emis, Pct_Red, Annual_Cost, Annual_Oper_Maint_Cost, Annualized_Capital_Cost, Total_Capital_Cost, Avg_Ann_Cost_per_Ton) " 
            + "select " + countySummaryResult.getDetailedResultDataset().getId() + ", 0, "
                      + "sector, "
                      + "fips, "
                      + "poll, "
                      + "sum(summary.Input_Emis) as Input_Emis, "
                      + "sum(summary.Emis_Reduction) as Emis_Reduction, "
                      + "sum(summary.Remaining_Emis) as Remaining_Emis, "
                      + "case when sum(summary.Input_Emis) <> 0 then sum(summary.Emis_Reduction) / sum(summary.Input_Emis) * 100.0 else null::double precision end as Pct_Red, "
                      + "sum(summary.Annual_Cost) as Annual_Cost, "
                      + "sum(summary.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
                      + "sum(summary.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
                      + "sum(summary.Total_Capital_Cost) as Total_Capital_Cost, "
                      + "case when sum(summary.Emis_Reduction) <> 0 then sum(summary.Annual_Cost) / sum(summary.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton " 
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
                              int month = inventory.getInputDataset().applicableMonth();
                              String datsetTypeName = inventory.getInputDataset().getDatasetType().getName();
                              boolean isFlatFileInventory = datsetTypeName.equals(DatasetType.FLAT_FILE_2010_POINT) 
                                  || datsetTypeName.equals(DatasetType.FLAT_FILE_2010_NONPOINT);
                              int noOfDaysInMonth = 31;
                              if (month != -1) {
                                  noOfDaysInMonth = getDaysInMonth(month);
                              }
                              String sqlAnnEmis = (isFlatFileInventory ? "i.ann_value" : (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis"));
                              sql += (count > 0 ? " union all " : "") 
                                  + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
                                  + "i." + (isFlatFileInventory ? "region_cd" : "fips") + " as fips, "
                                  + "i.poll, "
                                  + "sum(" + sqlAnnEmis + ") as Input_Emis, "
                                  + "sum(e.Emis_Reduction) as Emis_Reduction, "
                                  + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
                                  + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
                                  + "sum(e.Annual_Cost) as Annual_Cost, "
                                  + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
                                  + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
                                  + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
                                  + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
                                  + "from " + inventoryTableName + " i "
                                  + "left outer join " + detailedresultTableName + " e "
                                  + "on e.source_id = i.record_id "
                                  + "and e.ORIGINAL_DATASET_ID = " + inventory.getInputDataset().getId() + " "
                                  + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
                                  + "group by i." + (isFlatFileInventory ? "region_cd" : "fips") + ", i.poll ";
                              ++count;
                              }
                          }
                      }
                }
            //not a merged inventory, then there could be multiple results
            } else {
//case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + " else 0.0 end as ceff
                for (int i = 0; i < results.length; i++) {
                    if (results[i].getDetailedResultDataset() != null && results[i].getInputDataset() != null) {
                        String detailedresultTableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
                        String inventoryTableName = qualifiedEmissionTableName(results[i].getInputDataset());
                        String sector = results[i].getInputDataset().getSectors().length > 0 ? results[i].getInputDataset().getSectors()[0].getName() : "";
                        Version v = version(results[i].getInputDataset().getId(), results[i].getInputDatasetVersion());
                        VersionedQuery versionedQuery = new VersionedQuery(v);
                        int month = results[i].getInputDataset().applicableMonth();
                        String datsetTypeName = results[i].getInputDataset().getDatasetType().getName();
                        boolean isFlatFileInventory = datsetTypeName.equals(DatasetType.FLAT_FILE_2010_POINT) 
                            || datsetTypeName.equals(DatasetType.FLAT_FILE_2010_NONPOINT);
                        int noOfDaysInMonth = 31;
                        if (month != -1) {
                            noOfDaysInMonth = getDaysInMonth(month);
                        }
                        String sqlAnnEmis = (isFlatFileInventory ? "i.ann_value" : (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis"));
                        sql += (count > 0 ? " union all " : "") 
                            + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
                            + "i." + (isFlatFileInventory ? "region_cd" : "fips") + " as fips, "
                            + "i.poll, "
                            + "sum(" + sqlAnnEmis + ") as Input_Emis, "
                            + "sum(e.Emis_Reduction) as Emis_Reduction, "
                            + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
                            + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
                            + "sum(e.Annual_Cost) as Annual_Cost, "
                            + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
                            + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
                            + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
                            + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
                            + "from " + inventoryTableName + " i "
                            + "left outer join " + detailedresultTableName + " e "
                            + "on e.source_id = i.record_id "
                            + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
                            + "group by i." + (isFlatFileInventory ? "region_cd" : "fips") + ", i.poll ";
                        ++count;
                    }
                }
            }
            sql += ") summary "
                + "group by sector, fips, poll ";
            sql += "order by sector, fips, poll ";
            
            if (DebugLevels.DEBUG_25())
                System.out.println(sql);
            try {
                datasource.query().execute(sql);
            } catch (SQLException e) {
                throw new EmfException("Error occured when inserting data to strategy county summary table" + "\n" + e.getMessage());
            }
        }
    }

    protected int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInZeroBasedMonth(controlStrategy.getInventoryYear(), month) : 31;
    }

    protected void populateSourcesTable() throws EmfException {
        ControlStrategyInputDataset[] datasets = controlStrategy.getControlStrategyInputDatasets();
        String filter = getFilterForSourceQuery();
        if (datasets.length > 0) {
            for (int i = 0; i < datasets.length; i++) {
                //ignore non ORL inventories
//                if (!datasets[i].getInputDataset().getDatasetType().getName().contains("ORL")) {
//                    break;
//                }

                //make sure inventory has indexes created...
//                makeSureInventoryDatasetHasIndexes(datasets[i]);
                loader.makeSureInventoryDatasetHasIndexes(datasets[i].getInputDataset());
                String sql = "select public.populate_sources_table('" + emissionTableName(datasets[i].getInputDataset()) + "'," + (filter.length() == 0 ? "null::text" : "'" + filter.replaceAll("'", "''").substring(5) + "'") + ");";
                if (DebugLevels.DEBUG_25())
                    System.out.println( sql);
                try {
                    //populate source table...
                    datasource.query().execute(sql);
                    //analzye source table...
                    datasource.query().execute("vacuum analyze emf.sources;");
                } catch (SQLException e) {
                    throw new EmfException("Error occured when populating the sources table " + "\n" + e.getMessage());
                }
            }
        }
    }

    protected boolean isRunStatusCancelled() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return controlStrategyDAO.getControlStrategyRunStatus(controlStrategy.getId(), session).equals("Cancelled");
        } catch (RuntimeException e) {
            throw new EmfException("Could not check if strategy run was cancelled.");
        } finally {
            session.close();
        }
    }
    
    protected void setSummaryResultCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT count(1) as record_count "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        Statement statement = null;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                controlStrategyResult.setRecordCount(rs.getInt(1));
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /**/ }
                rs = null;
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { /**/ }
                statement = null;
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

    private ControlStrategyResult createStrategyMeasureSummaryResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        EmfDataset summaryResultDataset = createMeasureSummaryDataset();
        
        result.setDetailedResultDataset(summaryResultDataset);
        
        result.setStrategyResultType(getStrategyResultType(StrategyResultType.strategyMeasureSummary));
        result.setStartTime(new Date());
        result.setRunStatus("Start processing summary result");

        //persist result
        saveControlStrategySummaryResult(result);
        return result;
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
        saveControlStrategySummaryResult(result);
        return result;
    }

    private StrategyResultType getStrategyResultType(String name) throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = controlStrategyDAO.getStrategyResultType(name, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    protected Sector getSector(String name) throws EmfException {
        Sector sector = null;
        Session session = sessionFactory.getSession();
        try {
            sector = new SectorsDAO().getSector(name, session);

        } catch (RuntimeException e) {
            throw new EmfException("Could not get sector");
        } finally {
            session.close();
        }
        return sector;
    }

    private DatasetType getDatasetType(String name) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    protected ControlStrategyResult[] getControlStrategyResults() {
        ControlStrategyResult[] results = new ControlStrategyResult[] {};
        Session session = sessionFactory.getSession();
        try {
            results = controlStrategyDAO.getControlStrategyResults(controlStrategy.getId(), session).toArray(new ControlStrategyResult[0]);
        } finally {
            session.close();
        }
        return results;
    }

    private EmfDataset createMeasureSummaryDataset() throws EmfException {
        return creator.addDataset("CSMS", 
                DatasetCreator.createDatasetName("Strat_Meas_Sum"), 
                getDatasetType(DatasetType.strategyMeasureSummary), 
                new StrategyMeasureSummaryTableFormat(dbServer.getSqlDataTypes()), 
                summaryResultDatasetDescription(DatasetType.strategyMeasureSummary));
    }

    private EmfDataset createCountySummaryDataset() throws EmfException {
        return creator.addDataset("CSCS", 
                DatasetCreator.createDatasetName("Strat_County_Sum"), 
                getDatasetType(DatasetType.strategyCountySummary), 
                new StrategyCountySummaryTableFormat(dbServer.getSqlDataTypes()), 
                summaryResultDatasetDescription(DatasetType.strategyCountySummary));
    }

    private String summaryResultDatasetDescription(String datasetTypeName) {
        return "#" + datasetTypeName + " result\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
    }

    protected void saveControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.updateControlStrategyResult(strategyResult, session);
            if (controlStrategyInputDatasetCount < 2) {
//                runQASteps(strategyResult);
            }
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveControlStrategy(ControlStrategy strategy) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.update(strategy, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void updateControlStrategyWithLock(ControlStrategy strategy) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.updateWithLock(strategy, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveControlStrategySummaryResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.updateControlStrategyResult(strategyResult, session);
        } catch (Exception e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    protected void updateResultDataset(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();
        
        try {
            EmfDataset result = (EmfDataset) strategyResult.getDetailedResultDataset();
            EmfDataset contldInv = (EmfDataset) strategyResult.getControlledInventoryDataset();
            
            if (result != null) {
//System.out.println(result.getName());

//                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());
//System.out.println(result.getName());
                
//                if (version != null) {
//                    version.setCreator(user);
//                    System.out.println(result.getName());
                    updateVersion(result, null, dbServer, session, dao);
//                    System.out.println(result.getName());
//                }
            }
            
            if (contldInv != null) {
//                System.out.println(result.getName());
//                Version version = dao.getVersion(session, contldInv.getId(), contldInv.getDefaultVersion());


//                if (version != null) {
//                    version.setCreator(user);
                    updateVersion(contldInv, null, dbServer, session, dao);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Cannot update result datasets (strategy id: " + strategyResult.getControlStrategyId() + "). " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session, DatasetDAO dao) throws Exception {
        creator.updateVersionZeroRecordCount(dataset);
        
//        version = dao.obtainLockOnVersion(user, version.getId(), session);
//        version.setNumberRecords((int)dao.getDatasetRecordsNumber(dbServer, session, dataset, version));
//        dao.updateVersionNReleaseLock(version, session);
    }

    private void removeControlStrategyResults() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.removeControlStrategyResults(controlStrategy.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

    private void removeControlStrategyResult(int resultId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), resultId, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

    protected void runQASteps(ControlStrategyResult strategyResult) {
//        EmfDataset resultDataset = (EmfDataset)strategyResult.getDetailedResultDataset();
        if (recordCount > 0) {
//            runSummaryQASteps(resultDataset, 0);
        }
//        excuteSetAndRunQASteps(inputDataset, controlStrategy.getDatasetVersion());
    }

    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
        //11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step templates...
        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
        if (qaStepTemplates != null) {
            String[] qaStepTemplateNames = new String[qaStepTemplates.length];
            for (int i = 0; i < qaStepTemplates.length; i++) qaStepTemplateNames[i] = qaStepTemplates[i].getName();
            qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, controlStrategy.getExportDirectory(), null);
        }
    }

    protected void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }
    
    public long getRecordCount() {
        return recordCount;
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
    
    public String getFilterForSourceQuery() throws EmfException {
        String filterForSourceQuery = "";
        String sqlFilter = getFilterFromRegionDataset();
        String filter = controlStrategy.getFilter();
        
        //get and build strategy filter...
        if (filter == null || filter.trim().length() == 0)
            sqlFilter = "";
        else 
            sqlFilter = " and (" + filter + ") "; 

        filterForSourceQuery = sqlFilter;
        return filterForSourceQuery;
    }

    private String getFilterFromRegionDataset() throws EmfException {
        if (controlStrategy.getCountyDataset() == null) return "";
        String sqlFilter = "";
        String versionedQuery = new VersionedQuery(version(controlStrategy.getCountyDataset().getId(), controlStrategy.getCountyDatasetVersion())).query();
        String query = "SELECT distinct fips "
            + " FROM " + qualifiedEmissionTableName(controlStrategy.getCountyDataset()) 
            + " where " + versionedQuery;
//        ResultSet rs = null;
//        try {
//            rs = datasource.query().executeQuery(query);
//            while (rs.next()) {
//                if (sqlFilter.length() > 0) {
//                    sqlFilter += ",'" + rs.getString(1) + "'";
//                } else {
//                    sqlFilter = "'" + rs.getString(1) + "'";
//                }
//            }
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            if (rs != null)
//                try {
//                    rs.close();
//                } catch (SQLException e) {
//                    //
//                }
//        }
        return sqlFilter.length() > 0 ? " and fips in (" + query + ")" : "" ;
    }

    protected Version version(ControlStrategyInputDataset controlStrategyInputDataset) {
        return version(controlStrategyInputDataset.getInputDataset().getId(), controlStrategyInputDataset.getVersion());
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }

    public StrategyLoader getLoader() {
        return loader;
    }

}