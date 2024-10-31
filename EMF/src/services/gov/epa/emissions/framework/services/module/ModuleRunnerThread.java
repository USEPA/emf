package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModuleRunnerThread implements Runnable {

    private int[] moduleIds;

    private User user;

    private DbServerFactory dbServerFactory;

    private EntityManagerFactory entityManagerFactory;

    private Log log = LogFactory.getLog(ModuleRunnerThread.class);

    public ModuleRunnerThread(int[] moduleIds, User user, 
            DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this.moduleIds = moduleIds;
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public void run() {
        try {
            ModuleRunnerTask task = new ModuleRunnerTask(moduleIds, user, dbServerFactory, entityManagerFactory);
            task.run();
        } catch (EmfException e) {
            logError("Could not run all modules", e);
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);
    }
}
