package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunSectorScenario {

    private static Log log = LogFactory.getLog(RunSectorScenario.class);

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

//    private Services services;

    private DbServerFactory dbServerFactory;

    public RunSectorScenario(HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, PooledExecutor threadPool) {
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.dbServerFactory = dbServerFactory;
//        this.services = services();
    }

    public void run(User user, SectorScenario sectorScenario, SectorScenarioService service, String preStatus) throws EmfException {
        currentLimitations(user, sectorScenario);
        try {
            SectorScenarioTask sectorScenarioTask = new SectorScenarioTask(sectorScenario, user, dbServerFactory, sessionFactory);
            //factory.create(sectorScenario, user, 
            //        sessionFactory, dbServerFactory);
            SectorScenarioRunTask task = new SectorScenarioRunTask( sectorScenarioTask, user, 
                    services(), service, 
                    sessionFactory, preStatus);
            threadPool.execute(new GCEnforcerTask("Run SectorScenario: " + sectorScenario.getName(), task));
        } catch (Exception e) {
            log.error("Error running sector scenario: " + sectorScenario.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private void currentLimitations(User user, SectorScenario sectorScenario) throws EmfException {
//      if (sectorScenario.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
//      throw new EmfException("Least Cost Analysis is not supported.");

        //only the creator can run the control strategy
        if (!sectorScenario.getCreator().getName().equals(user.getName()))
            throw new EmfException("Only the creator, " + sectorScenario.getCreator().getName() + ", can run the sector scenario.");

        SectorScenarioInventory[] sectorScenarioInputDatasets = sectorScenario.getInventories();
        if (sectorScenarioInputDatasets.length == 0)
            return;
        
        for (int i = 0; i < sectorScenarioInputDatasets.length; i++) {
            DatasetType datasetType = sectorScenarioInputDatasets[0].getDataset().getDatasetType();
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

    protected Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(dbServerFactory, sessionFactory));

        return services;
    }
}
