package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunFastAnalysis {

    private static Log log = LogFactory.getLog(RunFastAnalysis.class);

    private EntityManagerFactory entityManagerFactory;

    private PooledExecutor threadPool;

//    private Services services;

    private DbServerFactory dbServerFactory;

    public RunFastAnalysis(EntityManagerFactory entityManagerFactory, 
            DbServerFactory dbServerFactory, PooledExecutor threadPool) {
        this.entityManagerFactory = entityManagerFactory;
        this.threadPool = threadPool;
        this.dbServerFactory = dbServerFactory;
//        this.services = services();
    }

    public void run(User user, FastAnalysis fastAnalysis, FastService service) throws EmfException {
        currentLimitations(user, fastAnalysis);
        try {
            FastAnalysisTask fastAnalysisTask = new FastAnalysisTask(fastAnalysis, user, dbServerFactory, entityManagerFactory);
            //factory.create(sectorScenario, user, 
            //        entityManagerFactory, dbServerFactory);
            FastAnalysisRunTask task = new FastAnalysisRunTask( fastAnalysisTask, user, 
                    services(), service, 
                    entityManagerFactory);
            threadPool.execute(new GCEnforcerTask("Run FastRun: " + fastAnalysis.getName(), task));
        } catch (Exception e) {
            log.error("Error running sector scenario: " + fastAnalysis.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private void currentLimitations(User user, FastAnalysis sectorScenario) throws EmfException {
//      if (sectorScenario.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
//      throw new EmfException("Least Cost Analysis is not supported.");

        //only the creator can run the control strategy
        if (!sectorScenario.getCreator().getName().equals(user.getName()))
            throw new EmfException("Only the creator, " + sectorScenario.getCreator().getName() + ", can run the sector scenario.");

//        FastRunInventory[] sectorScenarioInputDatasets = sectorScenario.getInventories();
//        if (sectorScenarioInputDatasets.length == 0)
//            return;
//        
//        for (int i = 0; i < sectorScenarioInputDatasets.length; i++) {
//            DatasetType datasetType = sectorScenarioInputDatasets[0].getDataset().getDatasetType();
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

    protected Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(entityManagerFactory));
        services.setStatusService(new StatusDAO(entityManagerFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, entityManagerFactory));

        return services;
    }
}
