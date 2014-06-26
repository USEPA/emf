package gov.epa.emissions.framework.services.tempalloc;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.sms.DatasetCreator;
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
        this.creator = new DatasetCreator(null, user, sessionFactory, dbServerFactory, datasource, keywords);
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
        deleteResults();
        
        // run any pre-processing
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
        
        setStatus("Started creating Temporal Allocation Result datasets.");
        temporalAllocation.setMonthlyResultDataset(createMonthlyResultDataset());
        temporalAllocation.setDailyResultDataset(createDailyResultDataset());
        temporalAllocation = temporalAllocationDAO.updateWithLock(temporalAllocation, sessionFactory.getSession());
        
        TemporalAllocationInputDataset[] temporalAllocationInputDatasets = temporalAllocation.getTemporalAllocationInputDatasets();
        TemporalAllocationInputDataset inputDataset = temporalAllocationInputDatasets[0];
        String query = "SELECT public.run_temporal_allocation(" + temporalAllocation.getId() + ", " + inputDataset.getInputDataset().getId() + ", " + inputDataset.getVersion() + ")";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        }
    }
    
    private void deleteResults() throws EmfException {
        Session session = sessionFactory.getSession();
        temporalAllocation.setMonthlyResultDataset(null);
        temporalAllocation.setDailyResultDataset(null);
        temporalAllocation = temporalAllocationDAO.updateWithLock(temporalAllocation, session);
        
        // TODO: delete results datasets
    }
    
    private void beforeRun() throws EmfException {
        // make sure inventories have indexes
        for (TemporalAllocationInputDataset dataset : temporalAllocation.getTemporalAllocationInputDatasets()) {
            // TODO: add indexing code
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
}
