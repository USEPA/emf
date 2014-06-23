package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMExportTask;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlMeasureExportServiceImpl implements ControlMeasureExportService {

    private static Log LOG = LogFactory.getLog(ControlMeasureExportServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dataCommonsDAO;

    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;
    
    public ControlMeasureExportServiceImpl() throws Exception {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public ControlMeasureExportServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        this.sessionFactory = sessionFactory;
        this.dataCommonsDAO = new DataCommonsDAO();
        this.threadPool = createThreadPool();
        this.dbServerFactory = dbServerFactory;
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

    public synchronized void exportControlMeasures(String folderPath, String prefix, int[] controlMeasureIds, User user)
            throws EmfException {
        doExport(folderPath, prefix, controlMeasureIds, user, false);
    }

    public synchronized void exportControlMeasuresWithOverwrite(String folderPath, String prefix, int[] controlMeasureIds,
            User user) throws EmfException {
        doExport(folderPath, prefix, controlMeasureIds, user, true);
    }

    private synchronized void doExport(String folderPath, String prefix, int[] controlMeasureIds, User user,
            boolean overwrite) throws EmfException {
        try {
            File dir = new File(folderPath);
            if (!dir.isDirectory())
                throw new EmfException("Export folder does not exist: " + folderPath);
            
            validateExportFile(new File(folderPath), prefix, overwrite);
            CMExportTask exportTask = new CMExportTask(new File(folderPath), prefix, controlMeasureIds, user,
                    sessionFactory, dbServerFactory);
            threadPool.execute(new GCEnforcerTask(
                    "Export control measures (id): " + controlMeasureIds[0] + ", etc.", exportTask));
        } catch (Exception e) {
            LOG.error("Could not export control measures.", e);
            throw new EmfException("Could not export control measures: " + e.getMessage());
        }
    }

    public synchronized Status[] getExportStatus(User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List controlMeasureExportStatuses = dataCommonsDAO.getStatuses(user.getUsername(), session);
            return (Status[]) controlMeasureExportStatuses.toArray(new Status[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get detail export status messages.", e);
            throw new EmfException("Could not get detail export status messages. " + e.getMessage());
        } finally {
            session.clear();
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
