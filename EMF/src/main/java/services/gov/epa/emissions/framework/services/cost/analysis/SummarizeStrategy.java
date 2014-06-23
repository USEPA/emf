package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class SummarizeStrategy {

    private static Log log = LogFactory.getLog(SummarizeStrategy.class);

    private StrategySummaryFactory factory;

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    private Services services;

    private DbServerFactory dbServerFactory;

    public SummarizeStrategy(StrategySummaryFactory factory, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, PooledExecutor threadPool) {
        this.factory = factory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.dbServerFactory = dbServerFactory;
        this.services = services();
    }

    public void run(User user, ControlStrategy controlStrategy, 
            StrategyResultType strategyResultType) throws EmfException {
        
        currentLimitations(controlStrategy);
        try {
            IStrategySummaryTask strategyResult = factory.create(controlStrategy, user, 
                    strategyResultType, sessionFactory, 
                    dbServerFactory);
            StrategySummaryTask task = new StrategySummaryTask(strategyResult, strategyResultType,
                    user, services, 
                    sessionFactory);
            threadPool.execute(new GCEnforcerTask("Run StrategyResult: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            log.error("Error running control strategy result: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private void currentLimitations(ControlStrategy controlStrategy) {
//        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
//            throw new EmfException("Least Cost Analysis is not supported.");

//        ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
//        if (controlStrategyInputDatasets.length == 0)
//            return;
//        
//        for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
//            DatasetType datasetType = controlStrategyInputDatasets[0].getInputDataset().getDatasetType();
//            int indexOfNonpoint = datasetType.getName().indexOf("Nonpoint");
//            int indexOfPoint = datasetType.getName().indexOf("Point");
//            int indexOfOnroad = datasetType.getName().indexOf("Onroad");
//            int indexOfNonroad = datasetType.getName().indexOf("Nonroad");
//            if (indexOfNonpoint == -1 && indexOfOnroad == -1 && indexOfNonroad == -1 && indexOfPoint == -1)
//                throw new EmfException("The dataset type '" + datasetType.getName() + "' is not supported yet.");
//        }
    }

    public void stop() {
        threadPool.shutdownNow();
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, sessionFactory));

        return services;
    }
}
