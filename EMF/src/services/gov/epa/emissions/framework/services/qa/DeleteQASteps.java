package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.QAStep;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class DeleteQASteps {
    
    private QAStep[] steps;
    
    private int datasetId;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private EntityManagerFactory entityManagerFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(DeleteQASteps.class);
    
    public DeleteQASteps(QAStep[] steps, int datasetId, DbServerFactory dbServerFactory, 
            User user, EntityManagerFactory entityManagerFactory,
            PooledExecutor threadPool) {
        this.steps = steps;
        this.datasetId = datasetId;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.threadPool = threadPool;
    }
    
    public void delete() throws EmfException {
        DeleteQAStepsTask task = new DeleteQAStepsTask(steps, datasetId,
                user, entityManagerFactory, dbServerFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Delete QA steps", task));
        } catch (InterruptedException e) {
            LOG.error("Error while deleting QA steps: ", e);
            throw new EmfException(e.getMessage());
        }
    }

}
