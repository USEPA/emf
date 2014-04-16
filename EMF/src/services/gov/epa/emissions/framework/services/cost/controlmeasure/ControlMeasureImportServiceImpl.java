package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.ControlMeasuresImporter;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import java.io.File;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlMeasureImportServiceImpl implements ControlMeasureImportService {

    private static Log LOG = LogFactory.getLog(ControlMeasureImportServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dataCommonsDAO;
    
    private PooledExecutor threadPool;
    
    private DbServerFactory dbServerFactory;

    public ControlMeasureImportServiceImpl() throws Exception {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public ControlMeasureImportServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        this.sessionFactory = sessionFactory;
        this.dataCommonsDAO = new DataCommonsDAO();
        this.threadPool = createThreadPool();
        this.dbServerFactory = dbServerFactory;
    }

    public synchronized void importControlMeasures(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user) throws EmfException {
        try {
            CMImportTask importTask = new CMImportTask(new File(folderPath), fileNames, user, purge, sectorIDs, sessionFactory, dbServerFactory);
            threadPool.execute(new GCEnforcerTask("Import control measures from files: " + fileNames[0] + ", etc.", importTask));
        } catch (Exception e) {
            LOG.error("Could not import control measures.", e);
            throw new EmfException("Could not import control measures: " + e.getMessage());
        }
    }

    public synchronized int getControlMeasureCountInSummaryFile(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user)  throws EmfException {
        try {
            CMImportTask importTask = new CMImportTask(new File(folderPath), fileNames, user, purge, sectorIDs, sessionFactory, dbServerFactory);
            int num = importTask.getControlMeasureCountInSummaryFile(purge, sectorIDs, folderPath, fileNames, user);
            return num;
        } catch (Exception e) {
            LOG.error("Could not import control measures.", e);
            throw new EmfException("Could not getControlMeasureCountInSummaryFile: " + e.getMessage());
        }
    }

    public synchronized Status[] getImportStatus(User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List controlMeasureImportStatuses = dataCommonsDAO.getControlMeasureImportStatuses(user.getUsername(),
                    session);
            return (Status[]) controlMeasureImportStatuses.toArray(new Status[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get detail import status messages.", e);
            throw new EmfException("Could not get detail import status messages. " + e.getMessage());
        } finally {
            session.clear();
        }
    }
    
    public synchronized void removeImportStatuses(User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dataCommonsDAO.removeStatuses(user.getUsername(), "CMImportDetailMsg", session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove detail import status messages.", e);
            throw new EmfException("Could not remove detail import status messages. " + e.getMessage());
        } finally {
            session.clear();
        }
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

}
