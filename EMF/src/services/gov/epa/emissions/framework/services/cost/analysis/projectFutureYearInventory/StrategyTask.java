package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractCheckMessagesStrategyTask;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class StrategyTask extends AbstractCheckMessagesStrategyTask {

    gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory.StrategyLoader projectLoader;
    
    public StrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
        projectLoader = 
            ((gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory.StrategyLoader) this.getLoader());
    }

    public void run() throws EmfException {

        //make sure there are programs to run
        if (controlStrategy.getControlPrograms() == null 
                || controlStrategy.getControlPrograms().length == 0)
            throw new EmfException("The strategy must have at least one control program specified for the run.");
        
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
            String detailedResults = "";
                                
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                ControlStrategyResult result = null;
                try {
                    result = this.getLoader().loadStrategyResult(controlStrategyInputDatasets[i]);
                    recordCount = this.getLoader().getRecordCount();
                    result.setRecordCount(recordCount);
                    
                    // BUG3602 - update date time here

                    Calendar cal = Calendar.getInstance();
                    Date dateTime = result.getInputDataset().getStartDateTime();
                    if (dateTime != null) {
                        cal.setTime(dateTime);
                        cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
                        result.getDetailedResultDataset().setStartDateTime(cal.getTime());
                    }
                    dateTime = result.getInputDataset().getStopDateTime();
                    if (dateTime != null) {
                        cal.setTime(dateTime);
                        cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
                        result.getDetailedResultDataset().setStopDateTime(cal.getTime());
                    }
                    
                    detailedResults += result.getDetailedResultDataset().getName();
                    
                    projectLoader.update((EmfDataset)result.getDetailedResultDataset());
                    
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
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
//                        status = "Cancelled. Strategy run was cancelled: " + controlStrategy.getName();
//                        setStatus(status);
                        throw new EmfException("Strategy run was cancelled.");
                    }
                }
            }
//            deleteStrategyResults();
            // update msg dataset
            ControlStrategyResult result = ((AbstractStrategyLoader) this.getLoader()).getStrategyMessagesResult();
            projectLoader.addKeyVal(((EmfDataset)result.getDetailedResultDataset()), "CONTROL_STRATEGY_DETAILED_RESULT_NAME", detailedResults);  
            projectLoader.update((EmfDataset)result.getDetailedResultDataset());
            
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
        //if (loader.getMessageDatasetRecordCount() == 0) deleteStrategyMessageResult();
    }

    public void afterRun() {
        try {
            ((gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory.StrategyLoader)(this.getLoader())).populateMessageOutput();

            this.checkMessagesForWarnings();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void beforeRun() throws EmfException {
        try {
            //lets clean the control program dataset tables, make sure missing values are converted to nulls (i.e., -9), 
            //trim fipscode to 5 characters not 6, etc...
            setStatus("Started cleaning control programs (i.e., removing -9 or 0).");
            cleanControlPrograms();
            setStatus("Finished cleaning control programs (i.e., removing -9 or 0).");
            //next lets vacuum the tables and index the control program packet tables...
            setStatus("Started indexing control programs.");
            for (ControlProgram controlProgram : controlStrategy.getControlPrograms()) {
                    vacuumControlProgramTables(controlProgram);
                    indexTable(controlProgram);
            }
            setStatus("Finished indexing control programs.");

            //next lets index the inventories, they'll be needed during the validation routine
            for (ControlStrategyInputDataset controlStrategyInputDataset : controlStrategy.getControlStrategyInputDatasets()) {
                loader.makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset.getInputDataset());
            }
            
            ControlStrategyResult result = ((AbstractStrategyLoader) this.getLoader()).createStrategyMessagesResult();
            
            // use which input dataset to set the start and stop date?? // BUG3602
            // use the 1/1 ~ 12/31 target year 
            Date start = new Date(); 
            start.setYear(controlStrategy.getInventoryYear()-1900);
            start.setMonth(0);
            start.setDate(1);
            start.setHours(0);
            start.setMinutes(0);
            start.setSeconds(0);
            result.getDetailedResultDataset().setStartDateTime(start);
            Date stop = new Date();
            stop.setYear(controlStrategy.getInventoryYear()-1900);
            stop.setMonth(11);
            stop.setDate(31);
            stop.setHours(23);
            stop.setMinutes(59);
            stop.setSeconds(59);
            result.getDetailedResultDataset().setStopDateTime(stop);
            
            projectLoader.update((EmfDataset)result.getDetailedResultDataset());
            
            //next lets validate the control program dataset tables, make sure format is correct, no data is missing, etc...
            setStatus("Started pre-run validation of control programs (i.e., duplicate packet records).");
            validateControlPrograms(this.getLoader().getStrategyMessagesResult());
            setStatus("Finished pre-run validation of control programs.");
            boolean hasErrors = checkMessagesForErrors();
            if (hasErrors) {
                //go ahead and update the strategy result count info.
                setSummaryResultCount(this.getLoader().getStrategyMessagesResult());
                saveControlStrategyResult(this.getLoader().getStrategyMessagesResult());
                
                throw new EmfException("Multiple error messages were detected while running control strategy. See " + StrategyResultType.strategyMessages + " output.");
            }
        } catch (EmfException e) {
            throw e;
        } catch (Exception e) {
            throw new EmfException(e.getMessage(), e);
        }
    }

    private void validateControlPrograms(ControlStrategyResult controlStrategyResult) throws EmfException {
        try {
            datasource.query().execute("select public.validate_project_future_year_inventory_control_programs(" + controlStrategy.getId() + "::integer, " + controlStrategyResult.getId() + "::integer);");
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }
    }

    private void cleanControlPrograms() throws EmfException {
        try {
            datasource.query().execute("select public.clean_project_future_year_inventory_control_programs(" + controlStrategy.getId() + "::integer);");
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }
    }

    private void vacuumControlProgramTables(ControlProgram controlProgram) throws EmfException {
        String query = "";
        EmfDataset dataset = controlProgram.getDataset();
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

    private void indexTable(ControlProgram controlProgram) throws EmfException {
        EmfDataset dataset = controlProgram.getDataset();
        String tableName = emissionTableName(dataset);
        String qualifiedTableName = qualifiedEmissionTableName(dataset);
        if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.plantClosure)) {
            indexTable(tableName, "fips,plantid,pointid,stackid,segment", "comp");
            indexTable(tableName, "fips", "fips");
            indexTable(tableName, "plantid", "plantid");
            indexTable(tableName, "pointid", "pointid");
            indexTable(tableName, "segment", "segment");
            indexTable(tableName, "stackid", "stackid");

            analyzeTable(qualifiedTableName);
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.projection)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.projectionPacket)) {
                indexTable(tableName, "fips,plantid,pointid,stackid,segment,scc,poll,sic,mact", "comp");
                indexTable(tableName, "fips", "fips");
                indexTable(tableName, "mact", "mact");
                indexTable(tableName, "plantid", "plantid");
                indexTable(tableName, "pointid", "pointid");
                indexTable(tableName, "poll", "poll");
                indexTable(tableName, "scc", "scc");
                indexTable(tableName, "segment", "segment");
                indexTable(tableName, "sic", "sic");
                indexTable(tableName, "stackid", "stackid");
                indexTable(tableName, "naics", "naics");

                analyzeTable(qualifiedTableName);
           } else if (dataset.getDatasetType().getName().equals(DatasetType.projectionPacket + " Extended")) {
               indexTable(tableName, "region_cd", "region_cd");
               indexTable(tableName, "reg_code", "reg_code");
               indexTable(tableName, "facility_id", "facility_id");
               indexTable(tableName, "unit_id", "unit_id");
               indexTable(tableName, "poll", "poll");
               indexTable(tableName, "scc", "scc");
               indexTable(tableName, "rel_point_id", "rel_point_id");
               indexTable(tableName, "sic", "sic");
               indexTable(tableName, "process_id", "process_id");
               indexTable(tableName, "naics", "naics");

               analyzeTable(qualifiedTableName);
          }

        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.allowable)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.allowablePacket)) {
                
                indexTable(tableName, "fips,plantid,pointid,stackid,segment,scc,poll,sic", "comp");
                indexTable(tableName, "fips", "fips");
                indexTable(tableName, "plantid", "plantid");
                indexTable(tableName, "pointid", "pointid");
                indexTable(tableName, "poll", "poll");
                indexTable(tableName, "scc", "scc");
                indexTable(tableName, "segment", "segment");
                indexTable(tableName, "sic", "sic");
                indexTable(tableName, "stackid", "stackid");
                indexTable(tableName, "naics", "naics");

                analyzeTable(qualifiedTableName);
           } else if (dataset.getDatasetType().getName().equals(DatasetType.allowablePacket + " Extended")) {
               indexTable(tableName, "region_cd", "region_cd");
               indexTable(tableName, "reg_code", "reg_code");
               indexTable(tableName, "facility_id", "facility_id");
               indexTable(tableName, "unit_id", "unit_id");
               indexTable(tableName, "poll", "poll");
               indexTable(tableName, "scc", "scc");
               indexTable(tableName, "rel_point_id", "rel_point_id");
               indexTable(tableName, "sic", "sic");
               indexTable(tableName, "process_id", "process_id");
               indexTable(tableName, "naics", "naics");

               analyzeTable(qualifiedTableName);
          }
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.control)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.controlPacket)) {
                indexTable(tableName, "fips,plantid,pointid,stackid,segment,scc,poll,sic,mact", "comp");
                indexTable(tableName, "fips", "fips");
                indexTable(tableName, "mact", "mact");
                indexTable(tableName, "plantid", "plantid");
                indexTable(tableName, "pointid", "pointid");
                indexTable(tableName, "poll", "poll");
                indexTable(tableName, "scc", "scc");
                indexTable(tableName, "segment", "segment");
                indexTable(tableName, "sic", "sic");
                indexTable(tableName, "stackid", "stackid");
                indexTable(tableName, "naics", "naics");

                analyzeTable(qualifiedTableName);
            } else if (dataset.getDatasetType().getName().equals(DatasetType.controlPacket + " Extended")) {
                indexTable(tableName, "region_cd", "region_cd");
                indexTable(tableName, "reg_code", "reg_code");
                indexTable(tableName, "facility_id", "facility_id");
                indexTable(tableName, "unit_id", "unit_id");
                indexTable(tableName, "poll", "poll");
                indexTable(tableName, "scc", "scc");
                indexTable(tableName, "rel_point_id", "rel_point_id");
                indexTable(tableName, "sic", "sic");
                indexTable(tableName, "process_id", "process_id");
                indexTable(tableName, "naics", "naics");

                analyzeTable(qualifiedTableName);
           }
        }
    }
    
    private void analyzeTable(String qualifiedTableName) {
        try {
            String query = "analyze " + qualifiedTableName.toLowerCase() + ";";
            dbServer.getEmissionsDatasource().query().execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    private void indexTable(String tableName, String columnList, String indexNamePrefix) {
        try {
            String query = "SELECT public.create_table_index('" + tableName.toLowerCase() + "','" + columnList.toLowerCase() + "','" + indexNamePrefix.toLowerCase() + "');";
            dbServer.getEmissionsDatasource().query().execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
}