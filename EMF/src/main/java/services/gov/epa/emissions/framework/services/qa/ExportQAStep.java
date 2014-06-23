package gov.epa.emissions.framework.services.qa;

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

public class ExportQAStep {

    private QAStep step;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportQAStep.class);

    private boolean verboseStatusLogging = true;

    private FileDownloadDAO fileDownloadDAO;

    private String rowFilter;

    public ExportQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool, String rowFilter) {
        this.step = step;
        this.dbServerFactory = dbServerFactory;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.fileDownloadDAO = new FileDownloadDAO(sessionFactory);
        this.threadPool = threadPool;
        this.rowFilter = rowFilter;
    }

    public ExportQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool, boolean verboseStatusLogging) {
        this(step, dbServerFactory,
            user, sessionFactory,
            threadPool, null);
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public void export(String dirName, String fileName, boolean overide) throws EmfException {
        ExportQAStepTask task = new ExportQAStepTask(dirName, fileName, 
                overide, step, 
                user, sessionFactory, dbServerFactory, verboseStatusLogging, rowFilter);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    public void download(String fileName, boolean overwrite) throws EmfException {
        ExportQAStepTask task = new ExportQAStepTask(fileDownloadDAO.getDownloadExportFolder() + "/" + user.getUsername(), fileName, 
                overwrite, step, 
                user, sessionFactory, dbServerFactory, verboseStatusLogging, true, rowFilter);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }
}
