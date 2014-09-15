package gov.epa.emissions.framework.services.tempalloc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.tempalloc.DatasetCreator;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;

public class TemporalAllocationTask {

    protected TemporalAllocation temporalAllocation;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    protected Datasource datasource;

    private User user;
    
    private StatusDAO statusDAO;
    
    private TemporalAllocationDAO temporalAllocationDAO;
    
    private Keywords keywords;
    
    private DatasetCreator creator;

    public TemporalAllocationTask(TemporalAllocation temporalAllocation, User user,
            DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory) throws EmfException {
        this.temporalAllocation = temporalAllocation;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.temporalAllocationDAO = new TemporalAllocationDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(temporalAllocation, user, sessionFactory, dbServerFactory, datasource, keywords);
    }
    
    public TemporalAllocation getTemporalAllocation() {
        return temporalAllocation;
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("TemporalAllocation");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
    
    public void run() throws EmfException {
        deleteOutputsAndDatasets();
        
        // run any pre-processing
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
        
        try {
            setStatus("Creating Temporal Allocation Result datasets.");
            TemporalAllocationOutput monthlyOutput = createMonthlyOutput();
            TemporalAllocationOutput dailyOutput = createDailyOutput();
            TemporalAllocationOutput episodicOutput = createEpisodicOutput();
            
            for (TemporalAllocationInputDataset inputDataset : temporalAllocation.getTemporalAllocationInputDatasets()) {
                setStatus("Processing inventory: " + inputDataset.getInputDataset().getName());
                String query = "SELECT public.run_temporal_allocation";
                String type = inputDataset.getInputDataset().getDatasetTypeName();
                if (type.equals(DatasetType.FLAT_FILE_2010_NONPOINT_DAILY) ||
                    type.equals(DatasetType.FLAT_FILE_2010_POINT_DAILY)) {
                    query += "_daily";
                }
                query += "(" + temporalAllocation.getId() + ", " + 
                        inputDataset.getInputDataset().getId() + ", " + 
                        inputDataset.getVersion() + ", " +
                        monthlyOutput.getOutputDataset().getId() + ", " +
                        dailyOutput.getOutputDataset().getId() + ", " +
                        episodicOutput.getOutputDataset().getId() + ")";
                try {
                    datasource.query().execute(query);
                } catch (SQLException e) {
                    throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
                }
            }
            setStatus("Finished Temporal Allocation run.");
        } finally {
            disconnectDbServer();
        }
    }
    
    private void deleteOutputsAndDatasets() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            
            // first get the datasets to delete
            EmfDataset[] datasets = temporalAllocationDAO.getTemporalAllocationOutputDatasets(temporalAllocation.getId(), session);
            if (datasets != null) {
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        setStatus("The Temporal Allocation output dataset, " + dataset.getName()
                                + ", will not be deleted since you are not the creator.");
                    } else {
                        dsList.add(dataset);
                    }
                }
            }
            
            removeOutputs();

            // delete and purge datasets
            if (dsList != null && dsList.size() > 0) {
                temporalAllocationDAO.removeOutputDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove temporal allocation outputs.");
        } finally {
            session.close();
        }
    }
    
    private void removeOutputs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            temporalAllocationDAO.removeOutputs(temporalAllocation.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous temporal allocation result(s)");
        } finally {
            session.close();
        }
    }
    
    private void beforeRun() throws EmfException {
        // make sure inventories have indexes
        for (TemporalAllocationInputDataset dataset : temporalAllocation.getTemporalAllocationInputDatasets()) {
            makeSureInventoryDatasetHaveIndexes(dataset);
        }
        
        try {
            // clean cross-reference dataset
            setStatus("Started cleaning cross-reference dataset (i.e., removing -9 or 0).");
            cleanCrossReferenceDataset();
            setStatus("Finished cleaning cross-reference dataset (i.e., removing -9 or 0).");
        } catch (EmfException e) {
            throw e;
        }
    }

    private void cleanCrossReferenceDataset() throws EmfException {
        try {
            datasource.query().execute("select public.clean_temporal_allocation_xref(" + temporalAllocation.getXrefDataset().getId() + ");");
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    private TemporalAllocationOutput createMonthlyOutput() throws EmfException {
        TemporalAllocationOutput output = new TemporalAllocationOutput();
        output.setTemporalAllocationId(temporalAllocation.getId());
        output.setOutputDataset(createMonthlyResultDataset());
        output.setType(getOutputType(TemporalAllocationOutputType.monthlyType));
        saveOutput(output);
        return output;
    }

    private TemporalAllocationOutput createDailyOutput() throws EmfException {
        TemporalAllocationOutput output = new TemporalAllocationOutput();
        output.setTemporalAllocationId(temporalAllocation.getId());
        output.setOutputDataset(createDailyResultDataset());
        output.setType(getOutputType(TemporalAllocationOutputType.dailyType));
        saveOutput(output);
        return output;
    }
    
    private TemporalAllocationOutput createEpisodicOutput() throws EmfException {
        TemporalAllocationOutput output = new TemporalAllocationOutput();
        output.setTemporalAllocationId(temporalAllocation.getId());
        output.setOutputDataset(createEpisodicResultDataset());
        output.setType(getOutputType(TemporalAllocationOutputType.episodicType));
        saveOutput(output);
        return output;
    }
    
    private void saveOutput(TemporalAllocationOutput output) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            temporalAllocationDAO.updateTemporalAllocationOutput(output, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save temporal allocation output: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    private EmfDataset createMonthlyResultDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.TEMPORAL_ALLOCATION_MONTHLY_RESULT);
        return creator.addDataset("ds",
                DatasetCreator.createDatasetName("Temp_Alloc_Monthly"),
                datasetType, 
                new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }
    
    private EmfDataset createDailyResultDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.TEMPORAL_ALLOCATION_DAILY_RESULT);
        return creator.addDataset("ds",
                DatasetCreator.createDatasetName("Temp_Alloc_Daily"),
                datasetType, 
                new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }
    
    private EmfDataset createEpisodicResultDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.TEMPORAL_ALLOCATION_EPISODIC_RESULT);
        return creator.addDataset("ds",
                DatasetCreator.createDatasetName("Temp_Alloc_Episodic"),
                datasetType,
                new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }

    protected DatasetType getDatasetType(String name) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
        return datasetType;
    }
    
    private TemporalAllocationOutputType getOutputType(String name) throws EmfException {
        TemporalAllocationOutputType outputType = null;
        Session session = sessionFactory.getSession();
        try {
            outputType = temporalAllocationDAO.getTemporalAllocationOutputType(name, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get temporal allocation output type");
        } finally {
            session.close();
        }
        return outputType;
    }
    
    public void makeSureInventoryDatasetHaveIndexes(TemporalAllocationInputDataset inputDataset) {
        try {
            createInventoryIndexes(inputDataset.getInputDataset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createInventoryIndexes(EmfDataset inventory) {
        DataTable dataTable = new DataTable(inventory, this.datasource);
        String tableName = inventory.getInternalSources()[0].getTable().toLowerCase();
        setStatus("Started creating indexes on inventory, " 
                + inventory.getName() 
                + ".  Depending on the size of the dataset, this could take several minutes.");

        dataTable.addIndex(tableName, "record_id", true);
        dataTable.addIndex(tableName, "dataset_id", false);
        dataTable.addIndex(tableName, "version", false);
        dataTable.addIndex(tableName, "delete_versions", false);

        //for orl inventories
        dataTable.addIndex(tableName, "fips", false);
        dataTable.addIndex(tableName, "plantid", false);
        dataTable.addIndex(tableName, "pointid", false);
        dataTable.addIndex(tableName, "stackid", false);
        dataTable.addIndex(tableName, "segment", false);

        dataTable.addIndex(tableName, "poll", false);
        dataTable.addIndex(tableName, "scc", false);
        
        //for flat file inventories
        dataTable.addIndex(tableName, "country_cd", false);
        dataTable.addIndex(tableName, "region_cd", false);
        dataTable.addIndex(tableName, "facility_id", false);
        dataTable.addIndex(tableName, "unit_id", false);
        dataTable.addIndex(tableName, "rel_point_id", false);
        dataTable.addIndex(tableName, "process_id", false);
        
        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(tableName);
    
        setStatus("Completed creating indexes on inventory, " 
                + inventory.getName() 
                + ".");
    }

    private void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }
}
