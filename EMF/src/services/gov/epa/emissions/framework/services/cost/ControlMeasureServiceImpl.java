package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasuresPDFReport;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.ControlMeasurePropertyCategories;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlMeasureServiceImpl implements ControlMeasureService {

    private static Log LOG = LogFactory.getLog(ControlMeasureServiceImpl.class);

    private EntityManagerFactory entityManagerFactory;
    
    private ControlMeasureDAO dao;

    private ControlTechnologiesDAO controlTechnologiesDAO;

    private DbServerFactory dbServerFactory;

    private ReferencesDAO referencesDAO;

    private PooledExecutor threadPool;

    public ControlMeasureServiceImpl() throws Exception {
        this(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    // public ControlMeasureServiceImpl(EntityManagerFactory entityManagerFactory) throws Exception {
    // init(entityManagerFactory);
    // }
    //

    public ControlMeasureServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory)
            throws Exception {
        // this(entityManagerFactory);
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        this.threadPool = createThreadPool();
        init();
    }

    public synchronized void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    private void init() {

        dao = new ControlMeasureDAO();
        controlTechnologiesDAO = new ControlTechnologiesDAO();
        this.referencesDAO = new ReferencesDAO();
    }

    public synchronized ControlMeasure[] getMeasures() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.all(entityManager);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measures.");
        } finally {
            entityManager.close();

        }
    }

    public synchronized ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getControlMeasures(pollutant, entityManager);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve all control measures with major pollutant -- " + pollutant.getName(), e);
            throw new EmfException("Could not retrieve all control measureswith major pollutant -- "
                    + pollutant.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.add(measure, sccs, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure: " + measure.getName(), e);
            throw new EmfException("Could not add control measure: " + measure.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void removeMeasure(int controlMeasureId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.remove(controlMeasureId, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            entityManager.close();
        }
    }

    public synchronized int copyMeasure(int controlMeasureId, User creator) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.copy(controlMeasureId, creator, entityManager, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            entityManager.close();
            close(dbServer);
        }
    }

    public synchronized ControlMeasure obtainLockedMeasure(User owner, int controlMeasureId) throws EmfException {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            ControlMeasure locked = dao.obtainLocked(owner, controlMeasureId, entityManager);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    public synchronized ControlMeasure getMeasure(int controlMeasureId) throws EmfException {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            ControlMeasure measure = dao.current(controlMeasureId, entityManager);
            return measure;
        } catch (RuntimeException e) {
            LOG.error("Could not get Control Measure for Control Measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get Control Measure for Control Measure Id: " + controlMeasureId);
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    // public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
    // EntityManager entityManager = entityManagerFactory.createEntityManager();
    // try {
    // ControlMeasure released = dao.releaseLocked(locked, entityManager);
    // return released;
    // } catch (RuntimeException e) {
    // LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
    // + locked.getLockOwner(), e);
    // throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // entityManager.close();
    // }
    // }

    public synchronized void releaseLockedControlMeasure(User user, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.releaseLocked(user, id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for control measure id: " + id, e);
            throw new EmfException("Could not release lock for control measure id: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ControlMeasure updated = dao.update(measure, sccs, entityManager);
            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlMeasure updateMeasureAndHoldLock(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ControlMeasure updated = dao.update(measure, sccs, entityManager, false);
            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            Scc[] sccs = dao.getSccsWithDescriptions(controlMeasureId, dbServer);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get SCCs for ControlMeasure Id: " + controlMeasureId);
        } finally {
            close(dbServer);
        }
    }

    public synchronized Scc[] getSccs(int controlMeasureId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Scc[] sccs = dao.getSccs(controlMeasureId, entityManager);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get SCCs for ControlMeasure Id: " + controlMeasureId);
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlTechnology[] getControlTechnologies() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = controlTechnologiesDAO.getAll(entityManager);

            return (ControlTechnology[]) all.toArray(new ControlTechnology[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control technologies.", e);
            throw new EmfException("Could not retrieve control technologies.");
        } finally {
            entityManager.close();
        }
    }

    public int getReferenceCount(String textContains) throws EmfException {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            int count = 0;
            if (textContains == null || textContains.trim().length() == 0) {
                count = this.referencesDAO.getReferenceCount(entityManager);
            } else {
                count = this.referencesDAO.getReferenceCount(entityManager, textContains);
            }

            return count;
        } catch (RuntimeException e) {

            String errorMessage = "Could not get all references";

            LOG.error(errorMessage, e);
            throw new EmfException(errorMessage);
        } finally {
            entityManager.close();
        }
    }

    public synchronized Reference[] getReferences(String textContains) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            List<Reference> all = null;
            if (textContains == null || textContains.trim().length() == 0) {
                all = this.referencesDAO.getReferences(entityManager);
            } else {
                all = this.referencesDAO.getReferences(entityManager, textContains);
            }

            return all.toArray(new Reference[0]);
        } catch (RuntimeException e) {

            String errorMessage = "Could not retrieve references.";
            LOG.error(errorMessage, e);
            throw new EmfException(errorMessage);
        } finally {
            entityManager.close();
        }
    }

    public synchronized CostYearTable getCostYearTable(int targetYear) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            CostYearTableReader reader = new CostYearTableReader(dbServer, targetYear);
            return reader.costYearTable();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    private synchronized void close(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null)
                dbServer.disconnect();

        } catch (Exception e) {
            LOG.error("Could not close database server", e);
            throw new EmfException("Could not close database server");
        }
    }

    public synchronized ControlMeasureClass[] getMeasureClasses() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.allCMClasses(entityManager);
            return (ControlMeasureClass[]) all.toArray(new ControlMeasureClass[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure classes.", e);
            throw new EmfException("Could not retrieve control measure classes.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlMeasureClass getMeasureClass(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getCMClass(entityManager, name);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure class.", e);
            throw new EmfException("Could not retrieve control measure class.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized LightControlMeasure[] getLightControlMeasures() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getLightControlMeasures(entityManager);
            return (LightControlMeasure[]) all.toArray(new LightControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve light control measures.", e);
            throw new EmfException("Could not retrieve light control measures.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getEfficiencyRecords(controlMeasureId, entityManager);
            return (EfficiencyRecord[]) all.toArray(new EfficiencyRecord[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int getEfficiencyRecordCount(int controlMeasureId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return (int) dao.getEfficiencyRecordCount(controlMeasureId, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency record count.", e);
            throw new EmfException("Could not retrieve control measures efficiency record count.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter)
            throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getEfficiencyRecords(controlMeasureId, recordLimit, filter, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public synchronized int addEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.addEfficiencyRecord(efficiencyRecord, entityManager, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure efficiency record", e);
            throw new EmfException("Could not add control measure efficiency record");
        } finally {
            entityManager.close();
            close(dbServer);
        }
    }

    public synchronized void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.updateEfficiencyRecord(efficiencyRecord, entityManager, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not update for control measure efficiency record Id: " + efficiencyRecord.getId(), e);
            throw new EmfException("Could not update for control measure efficiency record Id: "
                    + efficiencyRecord.getId());
        } finally {
            entityManager.close();
            close(dbServer);
        }
    }

    public synchronized void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.removeEfficiencyRecord(efficiencyRecordId, entityManager, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure efficiency record Id: " + efficiencyRecordId, e);
            throw new EmfException("Could not remove control measure efficiency record Id: " + efficiencyRecordId);
        } finally {
            entityManager.close();
            close(dbServer);
        }
    }

    public synchronized ControlMeasure[] getSummaryControlMeasures(String whereFilter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getSummaryControlMeasures(dbServer, whereFilter);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public synchronized ControlMeasure[] getControlMeasures(String whereFilter) throws EmfException {

        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getLightControlMeasures(whereFilter, dbServer).toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public synchronized ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, String whereFilter)
            throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getSummaryControlMeasures(majorPollutantId, dbServer, whereFilter);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public synchronized ControlMeasure[] getControlMeasures(int majorPollutantId, String whereFilter)
            throws EmfException {

        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getLightControlMeasures(majorPollutantId, whereFilter, dbServer)
                    .toArray((new ControlMeasure[0]));
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measures.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public synchronized EquationType[] getEquationTypes() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getEquationTypes(entityManager);
            return (EquationType[]) all.toArray(new EquationType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure Equation Types.", e);
            throw new EmfException("Could not retrieve control measures Equation Types.");
        } finally {
            entityManager.close();
        }
    }

    public ControlMeasurePropertyCategory[] getPropertyCategories() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<ControlMeasurePropertyCategory> all = dao.getPropertyCategories(entityManager);
            return all.toArray(new ControlMeasurePropertyCategory[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure property categories.", e);
            throw new EmfException("Could not retrieve control measures property categories.");
        } finally {
            entityManager.close();
        }
    }

    public ControlMeasurePropertyCategory getPropertyCategory(String categoryName) throws EmfException {
        return new ControlMeasurePropertyCategories(entityManagerFactory).getCategory(categoryName);
    }

    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<Sector> all = dao.getDistinctControlMeasureSectors(entityManager);
            return all.toArray(new Sector[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure sectors.", e);
            throw new EmfException("Could not retrieve control measures sectors.");
        } finally {
            entityManager.close();
        }

    }

    public ControlMeasure[] getControlMeasureBySector(int[] sectorIds) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<ControlMeasure> all = dao.getControlMeasureBySectors(sectorIds, true, entityManager);
            return all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measuress.");
        } finally {
            entityManager.close();
        }
    }

    public ControlMeasure[] getControlMeasureBySectorExcludeClasses(int[] sectorIds) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<ControlMeasure> all = dao.getControlMeasureBySectors(sectorIds, false, entityManager);
            return all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measuress.");
        } finally {
            entityManager.close();
        }
    }

    // public List<ControlStrategy> getLightControlStrategies(int[] cmIds, EntityManager entityManager) {

    // public ControlStrategy[] getLightControlStrategies(int[] cmIds) throws EmfException {
    // EntityManager entityManager = entityManagerFactory.createEntityManager();
    // try {
    // List<ControlMeasure> all = dao.getControlMeasureBySectors(sectorIds, entityManager);
    // return all.toArray(new ControlMeasure[0]);
    // } catch (RuntimeException e) {
    // LOG.error("Could not retrieve control measures.", e);
    // throw new EmfException("Could not retrieve control measuress.");
    // } finally {
    // entityManager.close();
    // }
    // }

    public void generateControlMeasurePDFReport(User user, int[] controlMeasureIds) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            threadPool.execute(new GCEnforcerTask("Generate control measures report.", 
                    new ControlMeasuresPDFReport(
                            user, controlMeasureIds, 
                            this, entityManagerFactory,
                            dbServerFactory)
            ));
        } catch (Exception e) {
            LOG.error("Could not create control measure PDF report.", e);
            throw new EmfException("Could not create control measure PDF report.", e);
        } finally {
            entityManager.close();
        }
    }
    
    public Pollutant[] getMeasurePollutants() throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        AggregateEfficiencyRecordDAO aerDao = new AggregateEfficiencyRecordDAO();
        return aerDao.getPollutants(dbServer);
    }

}