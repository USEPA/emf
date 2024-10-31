package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.data.DataServiceImpl;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunControlStrategy {

    private static Log log = LogFactory.getLog(RunControlStrategy.class);

    private StrategyFactory factory;

    private EntityManagerFactory entityManagerFactory;

    private PooledExecutor threadPool;

    private Services services;

    private DbServerFactory dbServerFactory;

    public RunControlStrategy(StrategyFactory factory, EntityManagerFactory entityManagerFactory, 
            DbServerFactory dbServerFactory, PooledExecutor threadPool) {
        this.factory = factory;
        this.entityManagerFactory = entityManagerFactory;
        this.threadPool = threadPool;
        this.dbServerFactory = dbServerFactory;
        this.services = services();
    }

    public void run(User user, ControlStrategy controlStrategy, ControlStrategyService service) throws EmfException {
        currentLimitations(user, controlStrategy);
        try {
            Strategy strategy = factory.create(controlStrategy, user, 
                    entityManagerFactory, dbServerFactory);
            StrategyTask task = new StrategyTask(strategy, user, 
                    services, service, 
                    entityManagerFactory);
            threadPool.execute(new GCEnforcerTask("Run Strategy: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            log.error("Error running control strategy: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private void currentLimitations(User user, ControlStrategy controlStrategy) throws EmfException {
//      if (controlStrategy.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
//      throw new EmfException("Least Cost Analysis is not supported.");

        //only the creator can run the control strategy
        if (!controlStrategy.getCreator().getName().equals(user.getName()))
            throw new EmfException("Only the creator, " + controlStrategy.getCreator().getName() + ", can run the control strategy.");

        ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
        if (controlStrategyInputDatasets.length == 0)
            return;
        
        for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
            DatasetType datasetType = controlStrategyInputDatasets[0].getInputDataset().getDatasetType();
            int indexOfNonpoint = datasetType.getName().indexOf("Nonpoint");
            int indexOfPoint = datasetType.getName().indexOf("Point");
            int indexOfOnroad = datasetType.getName().indexOf("Onroad");
            int indexOfNonroad = datasetType.getName().indexOf("Nonroad");
            if (indexOfNonpoint == -1 && indexOfOnroad == -1 && indexOfNonroad == -1 && indexOfPoint == -1)
                throw new EmfException("The dataset type '" + datasetType.getName() + "' is not supported yet.");
        }
    }

    public void stop() {
        threadPool.shutdownNow();
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(entityManagerFactory));
        services.setStatusService(new StatusDAO(entityManagerFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, entityManagerFactory));

        return services;
    }
}
