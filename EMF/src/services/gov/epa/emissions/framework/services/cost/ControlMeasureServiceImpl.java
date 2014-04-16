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
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import com.itextpdf.text.DocumentException;

public class ControlMeasureServiceImpl implements ControlMeasureService {

    private static Log LOG = LogFactory.getLog(ControlMeasureServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private ControlMeasureDAO dao;

    private ControlTechnologiesDAO controlTechnologiesDAO;

    private DbServerFactory dbServerFactory;

    private ReferencesDAO referencesDAO;

    private PooledExecutor threadPool;

    public ControlMeasureServiceImpl() throws Exception {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    // public ControlMeasureServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
    // init(sessionFactory);
    // }
    //

    public ControlMeasureServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws Exception {
        // this(sessionFactory);
        this.sessionFactory = sessionFactory;
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
        Session session = sessionFactory.getSession();
        try {
            List all = dao.all(session);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measures.");
        } finally {
            session.close();

        }
    }

    public synchronized ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getControlMeasures(pollutant, session);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve all control measures with major pollutant -- " + pollutant.getName(), e);
            throw new EmfException("Could not retrieve all control measureswith major pollutant -- "
                    + pollutant.getName());
        } finally {
            session.close();
        }
    }

    public synchronized int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.add(measure, sccs, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure: " + measure.getName(), e);
            throw new EmfException("Could not add control measure: " + measure.getName());
        } finally {
            session.close();
        }
    }

    public synchronized void removeMeasure(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.remove(controlMeasureId, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            session.close();
        }
    }

    public synchronized int copyMeasure(int controlMeasureId, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.copy(controlMeasureId, creator, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public synchronized ControlMeasure obtainLockedMeasure(User owner, int controlMeasureId) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure locked = dao.obtainLocked(owner, controlMeasureId, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public synchronized ControlMeasure getMeasure(int controlMeasureId) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure measure = dao.current(controlMeasureId, session);
            return measure;
        } catch (RuntimeException e) {
            LOG.error("Could not get Control Measure for Control Measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get Control Measure for Control Measure Id: " + controlMeasureId);
        } finally {
            if (session != null)
                session.close();
        }
    }

    // public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
    // Session session = sessionFactory.getSession();
    // try {
    // ControlMeasure released = dao.releaseLocked(locked, session);
    // return released;
    // } catch (RuntimeException e) {
    // LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
    // + locked.getLockOwner(), e);
    // throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // session.close();
    // }
    // }

    public synchronized void releaseLockedControlMeasure(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for control measure id: " + id, e);
            throw new EmfException("Could not release lock for control measure id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasure updated = dao.update(measure, sccs, session);
            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            session.close();
        }
    }

    public synchronized ControlMeasure updateMeasureAndHoldLock(ControlMeasure measure, Scc[] sccs) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasure updated = dao.update(measure, sccs, session, false);
            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        try {
            Scc[] sccs = dao.getSccs(controlMeasureId, session);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get SCCs for ControlMeasure Id: " + controlMeasureId);
        } finally {
            session.close();
        }
    }

    public synchronized ControlTechnology[] getControlTechnologies() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = controlTechnologiesDAO.getAll(session);

            return (ControlTechnology[]) all.toArray(new ControlTechnology[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control technologies.", e);
            throw new EmfException("Could not retrieve control technologies.");
        } finally {
            session.close();
        }
    }

    public int getReferenceCount(String textContains) throws EmfException {

        Session session = sessionFactory.getSession();
        try {

            int count = 0;
            if (textContains == null || textContains.trim().length() == 0) {
                count = this.referencesDAO.getReferenceCount(session);
            } else {
                count = this.referencesDAO.getReferenceCount(session, textContains);
            }

            return count;
        } catch (RuntimeException e) {

            String errorMessage = "Could not get all references";

            LOG.error(errorMessage, e);
            throw new EmfException(errorMessage);
        } finally {
            session.close();
        }
    }

    public synchronized Reference[] getReferences(String textContains) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            List<Reference> all = null;
            if (textContains == null || textContains.trim().length() == 0) {
                all = this.referencesDAO.getReferences(session);
            } else {
                all = this.referencesDAO.getReferences(session, textContains);
            }

            return all.toArray(new Reference[0]);
        } catch (RuntimeException e) {

            String errorMessage = "Could not retrieve references.";
            LOG.error(errorMessage, e);
            throw new EmfException(errorMessage);
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        try {
            List all = dao.allCMClasses(session);
            return (ControlMeasureClass[]) all.toArray(new ControlMeasureClass[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure classes.", e);
            throw new EmfException("Could not retrieve control measure classes.");
        } finally {
            session.close();
        }
    }

    public synchronized ControlMeasureClass getMeasureClass(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCMClass(session, name);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure class.", e);
            throw new EmfException("Could not retrieve control measure class.");
        } finally {
            session.close();
        }
    }

    public synchronized LightControlMeasure[] getLightControlMeasures() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getLightControlMeasures(session);
            return (LightControlMeasure[]) all.toArray(new LightControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve light control measures.", e);
            throw new EmfException("Could not retrieve light control measures.");
        } finally {
            session.close();
        }
    }

    public synchronized EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getEfficiencyRecords(controlMeasureId, session);
            return (EfficiencyRecord[]) all.toArray(new EfficiencyRecord[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } finally {
            session.close();
        }
    }

    public synchronized int getEfficiencyRecordCount(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return (int) dao.getEfficiencyRecordCount(controlMeasureId, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency record count.", e);
            throw new EmfException("Could not retrieve control measures efficiency record count.");
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.addEfficiencyRecord(efficiencyRecord, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure efficiency record", e);
            throw new EmfException("Could not add control measure efficiency record");
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public synchronized void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.updateEfficiencyRecord(efficiencyRecord, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not update for control measure efficiency record Id: " + efficiencyRecord.getId(), e);
            throw new EmfException("Could not update for control measure efficiency record Id: "
                    + efficiencyRecord.getId());
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public synchronized void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.removeEfficiencyRecord(efficiencyRecordId, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure efficiency record Id: " + efficiencyRecordId, e);
            throw new EmfException("Could not remove control measure efficiency record Id: " + efficiencyRecordId);
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getEquationTypes(session);
            return (EquationType[]) all.toArray(new EquationType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure Equation Types.", e);
            throw new EmfException("Could not retrieve control measures Equation Types.");
        } finally {
            session.close();
        }
    }

    public ControlMeasurePropertyCategory[] getPropertyCategories() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<ControlMeasurePropertyCategory> all = dao.getPropertyCategories(session);
            return all.toArray(new ControlMeasurePropertyCategory[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure property categories.", e);
            throw new EmfException("Could not retrieve control measures property categories.");
        } finally {
            session.close();
        }
    }

    public ControlMeasurePropertyCategory getPropertyCategory(String categoryName) throws EmfException {
        return new ControlMeasurePropertyCategories(sessionFactory).getCategory(categoryName);
    }

    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<Sector> all = dao.getDistinctControlMeasureSectors(session);
            return all.toArray(new Sector[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure sectors.", e);
            throw new EmfException("Could not retrieve control measures sectors.");
        } finally {
            session.close();
        }

    }

    public ControlMeasure[] getControlMeasureBySector(int[] sectorIds) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<ControlMeasure> all = dao.getControlMeasureBySectors(sectorIds, session);
            return all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measuress.");
        } finally {
            session.close();
        }
    }

    // public List<ControlStrategy> getLightControlStrategies(int[] cmIds, Session session) {

    // public ControlStrategy[] getLightControlStrategies(int[] cmIds) throws EmfException {
    // Session session = sessionFactory.getSession();
    // try {
    // List<ControlMeasure> all = dao.getControlMeasureBySectors(sectorIds, session);
    // return all.toArray(new ControlMeasure[0]);
    // } catch (RuntimeException e) {
    // LOG.error("Could not retrieve control measures.", e);
    // throw new EmfException("Could not retrieve control measuress.");
    // } finally {
    // session.close();
    // }
    // }

    public void generateControlMeasurePDFReport(User user, int[] controlMeasureIds) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            threadPool.execute(new GCEnforcerTask("Generate control measures report.", 
                    new ControlMeasuresPDFReport(
                            user, controlMeasureIds, 
                            this, sessionFactory,
                            dbServerFactory)
            ));
        } catch (Exception e) {
            LOG.error("Could not create control measure PDF report.", e);
            throw new EmfException("Could not create control measure PDF report.", e);
        } finally {
            session.close();
        }
    }

}