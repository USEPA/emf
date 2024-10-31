package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.data.QAStep;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportShapeFileQAStep {

    private QAStep step;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private EntityManagerFactory entityManagerFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportShapeFileQAStep.class);

    private boolean verboseStatusLogging = true;

    private FileDownloadDAO fileDownloadDAO;

//    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
//            User user, EntityManagerFactory entityManagerFactory,
//            PooledExecutor threadPool) {
//        this.step = step;
//        this.dbServerFactory = dbServerFactory;
//        this.user = user;
//        this.entityManagerFactory = entityManagerFactory;
//        this.threadPool = threadPool;
//    }

    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, EntityManagerFactory entityManagerFactory,
            PooledExecutor threadPool, boolean verboseStatusLogging) {
//        this(step, dbServerFactory,
//            user, entityManagerFactory,
//            threadPool);
        this.step = step;
        this.dbServerFactory = dbServerFactory;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.fileDownloadDAO = new FileDownloadDAO();
        this.threadPool = threadPool;
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public void export(String dirName, String fileName, ProjectionShapeFile projectionShapeFile, boolean overide, String rowFilter, PivotConfiguration pivotConfiguration) throws EmfException {
        ExportShapeFileQAStepTask task = new ExportShapeFileQAStepTask(dirName, fileName, 
                overide, step, 
                user, entityManagerFactory,
                dbServerFactory, projectionShapeFile, 
                verboseStatusLogging, rowFilter,
                pivotConfiguration);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    public void download(String fileName, ProjectionShapeFile projectionShapeFile, String rowFilter, PivotConfiguration pivotConfiguration, boolean overwrite) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ExportShapeFileQAStepTask task = new ExportShapeFileQAStepTask(fileDownloadDAO.getDownloadExportFolder(entityManager) + "/" + user.getUsername(), fileName, 
                overwrite, step, 
                user, entityManagerFactory,
                dbServerFactory, projectionShapeFile, 
                verboseStatusLogging, rowFilter,
                pivotConfiguration, true);
        entityManager.close();
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }
}
