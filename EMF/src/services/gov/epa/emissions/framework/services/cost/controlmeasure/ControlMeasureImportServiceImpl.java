package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlMeasureImportServiceImpl implements ControlMeasureImportService {

    private static Log LOG = LogFactory.getLog(ControlMeasureImportServiceImpl.class);

    private EntityManagerFactory entityManagerFactory;

    private DataCommonsDAO dataCommonsDAO;
    
    private PooledExecutor threadPool;
    
    private DbServerFactory dbServerFactory;
    
    public ControlMeasureImportServiceImpl() throws Exception {
        this(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    public ControlMeasureImportServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) throws Exception {
        this.entityManagerFactory = entityManagerFactory;
        this.dataCommonsDAO = new DataCommonsDAO();
        this.threadPool = createThreadPool();
        this.dbServerFactory = dbServerFactory;
    }

    public synchronized void importControlMeasures(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user) throws EmfException {
        try {
            CMImportTask importTask = new CMImportTask(new File(folderPath), fileNames, user, purge, sectorIDs, entityManagerFactory, dbServerFactory);
            threadPool.execute(new GCEnforcerTask("Import control measures from files: " + fileNames[0] + ", etc.", importTask));
        } catch (Exception e) {
            LOG.error("Could not import control measures.", e);
            throw new EmfException("Could not import control measures: " + e.getMessage());
        }
    }

    public synchronized int getControlMeasureCountInSummaryFile(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user)  throws EmfException {
        try {
            CMImportTask importTask = new CMImportTask(new File(folderPath), fileNames, user, purge, sectorIDs, entityManagerFactory, dbServerFactory);
            int num = importTask.getControlMeasureCountInSummaryFile(purge, sectorIDs, folderPath, fileNames, user);
            return num;
        } catch (Exception e) {
            LOG.error("Could not import control measures.", e);
            throw new EmfException("Could not getControlMeasureCountInSummaryFile: " + e.getMessage());
        }
    }

    public synchronized Status[] getImportStatus(User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List controlMeasureImportStatuses = dataCommonsDAO.getControlMeasureImportStatuses(user.getUsername(),
                    entityManager);
            return (Status[]) controlMeasureImportStatuses.toArray(new Status[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get detail import status messages.", e);
            throw new EmfException("Could not get detail import status messages. " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized void removeImportStatuses(User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dataCommonsDAO.removeStatuses(user.getUsername(), "CMImportDetailMsg", entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove detail import status messages.", e);
            throw new EmfException("Could not remove detail import status messages. " + e.getMessage());
        } finally {
            entityManager.close();
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
