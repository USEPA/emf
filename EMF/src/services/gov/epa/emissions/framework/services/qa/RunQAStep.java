package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunQAStep implements Runnable {

    private QAStep[] qaSteps;

    private DbServerFactory dbServerFactory;

    private EntityManagerFactory entityManagerFactory;

    private User user;

    private Log log = LogFactory.getLog(RunQAStep.class);

    public RunQAStep(QAStep[] steps, User user, 
            DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this.user = user;
        this.qaSteps = steps;
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public void run() {
        try {
            RunQAStepTask task = new RunQAStepTask(qaSteps, user, dbServerFactory, entityManagerFactory);
            task.run();
        } catch (EmfException e) {
            logError("Could not run all QA steps", e);
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

}
