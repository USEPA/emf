package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.sms.RunSectorScenario;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunTemporalAllocation {

    private static Log log = LogFactory.getLog(RunSectorScenario.class);

    private EntityManagerFactory entityManagerFactory;

    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    public RunTemporalAllocation(EntityManagerFactory entityManagerFactory, 
            DbServerFactory dbServerFactory, 
            PooledExecutor threadPool) {
        this.entityManagerFactory = entityManagerFactory;
        this.threadPool = threadPool;
        this.dbServerFactory = dbServerFactory;
    }

    public void run(User user, TemporalAllocation temporalAllocation, TemporalAllocationService service) throws EmfException {
        try {
            TemporalAllocationTask temporalAllocationTask = new TemporalAllocationTask(temporalAllocation, user, dbServerFactory, entityManagerFactory);
            TemporalAllocationRunTask task = new TemporalAllocationRunTask(temporalAllocationTask, user,
                    services(), service, entityManagerFactory);
            threadPool.execute(new GCEnforcerTask("Run TemporalAllocation: " + temporalAllocation.getName(), task));
        } catch (Exception e) {
            log.error("Error running temporal allocation: " + temporalAllocation.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    protected Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(entityManagerFactory));
        services.setStatusService(new StatusDAO(entityManagerFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, entityManagerFactory));

        return services;
    }
}
