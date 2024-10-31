package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMExportTask;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlMeasureExportServiceImpl implements ControlMeasureExportService {

    private static Log LOG = LogFactory.getLog(ControlMeasureExportServiceImpl.class);

    private EntityManagerFactory entityManagerFactory;

    private DataCommonsDAO dataCommonsDAO;
    
    private FileDownloadDAO fileDownloadDAO;

    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;
    
    public ControlMeasureExportServiceImpl() throws Exception {
        this(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    public ControlMeasureExportServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) throws Exception {
        this.entityManagerFactory = entityManagerFactory;
        this.dataCommonsDAO = new DataCommonsDAO();
        this.threadPool = createThreadPool();
        this.dbServerFactory = dbServerFactory;
        this.fileDownloadDAO = new FileDownloadDAO();
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

    public synchronized void exportControlMeasures(String folderPath, String prefix, int[] controlMeasureIds, User user, boolean download)
            throws EmfException {
        doExport(folderPath, prefix, controlMeasureIds, user, false, download);
    }

    public synchronized void exportControlMeasuresWithOverwrite(String folderPath, String prefix, int[] controlMeasureIds,
            User user, boolean download) throws EmfException {
        doExport(folderPath, prefix, controlMeasureIds, user, true, download);
    }

    public synchronized void exportControlMeasures(String folderPath, String prefix, int[] controlMeasureIds, User user)
            throws EmfException {
        exportControlMeasures(folderPath, prefix, controlMeasureIds, user, false);
    }

    public synchronized void exportControlMeasuresWithOverwrite(String folderPath, String prefix, int[] controlMeasureIds,
            User user) throws EmfException {
        exportControlMeasuresWithOverwrite(folderPath, prefix, controlMeasureIds, user, false);
    }

    private synchronized void doExport(String folderPath, String prefix, int[] controlMeasureIds, User user,
            boolean overwrite, boolean download) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            File dir;
            if (download) {
                dir = new File(this.fileDownloadDAO.getDownloadExportFolder(entityManager) + "/" + user.getUsername() + "/");
            } else {
                dir = new File(folderPath);
            }
            if (!dir.isDirectory()) {
                if (download) {
                    dir.mkdir();
                    dir.setReadable(true, true);
                    dir.setWritable(true, false);
                } else {
                    throw new EmfException("Export folder does not exist: " + folderPath);
                }
            }
            
            validateExportFile(dir, prefix, overwrite);
            CMExportTask exportTask = new CMExportTask(dir, prefix, controlMeasureIds, user,
                    entityManagerFactory, dbServerFactory, download);
            threadPool.execute(new GCEnforcerTask(
                    "Export control measures (id): " + controlMeasureIds[0] + ", etc.", exportTask));
        } catch (Exception e) {
            LOG.error("Could not export control measures.", e);
            throw new EmfException("Could not export control measures: " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public synchronized Status[] getExportStatus(User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List controlMeasureExportStatuses = dataCommonsDAO.getStatuses(user.getUsername(), entityManager);
            return (Status[]) controlMeasureExportStatuses.toArray(new Status[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get detail export status messages.", e);
            throw new EmfException("Could not get detail export status messages. " + e.getMessage());
        } finally {
            entityManager.clear();
        }
    }

    private synchronized void validateExportFile(File path, String prefix, boolean overwrite) throws EmfException {
        File[] files = new File[] { new File(path, prefix + "_summary.csv"), new File(path, prefix + "_efficiencies.csv"),
                new File(path, prefix + "_SCCs.csv"), new File(path, prefix + "_equations.csv") };

        if (!overwrite) {
            for (int i = 0; i < files.length; i++)
                if (files[i].exists() && files[i].isFile()) {
                    LOG.error("File exists and cannot be overwritten");
                    throw new EmfException("Files exist.  Choose overwrite option");
                }
        }
    }

}
