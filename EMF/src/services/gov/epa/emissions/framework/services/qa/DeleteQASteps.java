package gov.epa.emissions.framework.services.qa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class DeleteQASteps {
    
    private QAStep[] steps;
    
    private int datasetId;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(DeleteQASteps.class);
    
    public DeleteQASteps(QAStep[] steps, int datasetId, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
        this.steps = steps;
        this.datasetId = datasetId;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
    }
    
    public void delete() throws EmfException {
        DeleteQAStepsTask task = new DeleteQAStepsTask(steps, datasetId,
                user, sessionFactory, dbServerFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Delete QA steps", task));
        } catch (InterruptedException e) {
            LOG.error("Error while deleting QA steps: ", e);
            throw new EmfException(e.getMessage());
        }
    }

}
