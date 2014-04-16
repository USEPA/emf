package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportShapeFileQAStep {

    private QAStep step;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportShapeFileQAStep.class);

    private boolean verboseStatusLogging = true;

    private FileDownloadDAO fileDownloadDAO;

//    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
//            User user, HibernateSessionFactory sessionFactory,
//            PooledExecutor threadPool) {
//        this.step = step;
//        this.dbServerFactory = dbServerFactory;
//        this.user = user;
//        this.sessionFactory = sessionFactory;
//        this.threadPool = threadPool;
//    }

    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool, boolean verboseStatusLogging) {
//        this(step, dbServerFactory,
//            user, sessionFactory,
//            threadPool);
        this.step = step;
        this.dbServerFactory = dbServerFactory;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.fileDownloadDAO = new FileDownloadDAO(sessionFactory);
        this.threadPool = threadPool;
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public void export(String dirName, String fileName, ProjectionShapeFile projectionShapeFile, boolean overide, String rowFilter, PivotConfiguration pivotConfiguration) throws EmfException {
        ExportShapeFileQAStepTask task = new ExportShapeFileQAStepTask(dirName, fileName, 
                overide, step, 
                user, sessionFactory,
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
        ExportShapeFileQAStepTask task = new ExportShapeFileQAStepTask(fileDownloadDAO.getDownloadExportFolder() + "/" + user.getUsername(), fileName, 
                overwrite, step, 
                user, sessionFactory,
                dbServerFactory, projectionShapeFile, 
                verboseStatusLogging, rowFilter,
                pivotConfiguration, true);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }
}
