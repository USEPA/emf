package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModuleRunnerThread implements Runnable {

    private int[] moduleIds;

    private User user;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private Log log = LogFactory.getLog(ModuleRunnerThread.class);

    public ModuleRunnerThread(int[] moduleIds, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.moduleIds = moduleIds;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public void run() {
        try {
            ModuleRunnerTask task = new ModuleRunnerTask(moduleIds, user, dbServerFactory, sessionFactory);
            task.run();
        } catch (EmfException e) {
            logError("Could not run all modules", e);
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);
    }
}
