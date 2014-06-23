package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class SectorScenarioRunTask implements Runnable {

    private static Log log = LogFactory.getLog(SectorScenarioRunTask.class);

    private User user;

    private SectorScenarioTask sectorScenarioTask;

    private Services services;

    private SectorScenarioService ssService;

    private HibernateSessionFactory sessionFactory;
    
    private String preStatus;

    public SectorScenarioRunTask(SectorScenarioTask sectorScenarioTask, User user, 
            Services services, SectorScenarioService service,
            HibernateSessionFactory sessionFactory, String preStatus) {
        this.user = user;
        this.services = services;
        this.sectorScenarioTask = sectorScenarioTask;
        this.ssService = service;
        this.sessionFactory = sessionFactory;
        this.preStatus = preStatus;
    }

    public void run() {
        String completeStatus = "";
//         //set strategy run status to waiting, this will make sure it is run in the make-shift queue
//        try {
//            setRunStatus("Waiting");
//        } catch (EmfException e1) {
//            // NOTE Auto-generated catch block
//            e1.printStackTrace();
//        }
        Long poolSize = strategyPoolSize();
        Long runningCount = getSectorScenarioRunningCount();
        
        //make sure we can add an work item to the queue
        //check the pool size and compare to the number of strategies currently running
        if (runningCount < poolSize) {
            try {
                prepare();
                sectorScenarioTask.run();
                completeStatus = "Finished";
                addCompletedStatus();
            } catch (EmfException e) {
                if (e.getType().equals("CannotRerun")) {
                    completeStatus = preStatus; // + "-Can't Rerun";
                    logError("Cannot rerun sector scenario : ", e);
                    setStatus("Cannot rerun sector scenario: " + "Reason: " + e.getMessage());
                } else {
                    completeStatus = "Failed";
                    logError("Failed to run sector scenario : ", e);
                    setStatus("Failed to run sector scenario: " + "Reason: " + e.getMessage());
                }
                
            } finally {
//                    closeConnection();
                
//                strategy.getSectorScenario().setRunStatus(completeStatus);
//                strategy.getSectorScenario().setCompletionDate(new Date());

                //check to see if there is another strategy to run...
                List<SectorScenario> waitingStrategies;
                try {
                    setRunStatusAndCompletionDate(completeStatus, new Date());
                    waitingStrategies = ssService.getSectorScenariosByRunStatus("Waiting");
                    if (waitingStrategies.size() > 0) {
                        runningCount = getSectorScenarioRunningCount();
                        if (runningCount < poolSize) {
                            for (SectorScenario controlStrategy : waitingStrategies.toArray(new SectorScenario[0])) {
                                if (runningCount < poolSize) {
                                    ssService.runSectorScenario(user, controlStrategy.getId());
                                    runningCount++;
                                }
                            }
                        }
                    }
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

//    private void closeConnection() {
//        try {
//            strategy.close();
//        } catch (EmfException e) {
//            logError("Failed to close connection : ", e);
//            setStatus("Failed to close connection " + "Reason: " + e.getMessage());
//        }
//    }
//
    private void prepare() throws EmfException {
        SectorScenario sectorScenario = sectorScenarioTask.getSectorScenario();
        sectorScenario = ssService.obtainLocked(sectorScenario.getCreator(), sectorScenario.getId());
        sectorScenario.setStartDate(new Date());
        sectorScenario.setRunStatus("Running");
        ssService.updateSectorScenarioWithLock(sectorScenario);
        addStartStatus();
    }

    private void setRunStatusAndCompletionDate(String completeStatus, Date completionDate) throws EmfException {
        sectorScenarioTask.getSectorScenario().setRunStatus(completeStatus);
        sectorScenarioTask.getSectorScenario().setLastModifiedDate(new Date());
        sectorScenarioTask.getSectorScenario().setCompletionDate(completionDate);
//        updateStrategy();
        ssService.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioTask.getSectorScenario().getId(), completeStatus, completionDate);
    }

//    private void updateStrategy() {
//        try {
//            csService.updateSectorScenario(strategy.getSectorScenario());
//        } catch (EmfException e) {
//            logError("Failed to update the strategy : ", e);
//            setStatus("Failed to update strategy: " + "Reason: " + e.getMessage());
//        }
//
//    }

    private Long strategyPoolSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("strategy-pool-size", session);
            return Long.parseLong(property.getValue());
        } finally {
            session.close();
        }
    }
    private Long getSectorScenarioRunningCount() {
        Long count = 0L;
        try {
            count = ssService.getSectorScenarioRunningCount();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return count;
    }
    
    private void addStartStatus() {
        setStatus("Started running sector scenario: " + sectorScenarioTask.getSectorScenario().getName());
    }

    private void addCompletedStatus() {
        setStatus("Completed running sector scenario: " + sectorScenarioTask.getSectorScenario().getName() + ".");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("SectorScenario");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}
