package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategySummaryTask implements Runnable {

    private static Log log = LogFactory.getLog(StrategySummaryTask.class);

    private User user;

    private IStrategySummaryTask strategyResult;

    private Services services;

//    private ControlStrategyService csService;
//
//    private HibernateSessionFactory sessionFactory;
    
    private StrategyResultType strategyResultType;

    public StrategySummaryTask(IStrategySummaryTask strategyResult, StrategyResultType strategyResultType, 
            User user, Services services, 
            HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.services = services;
        this.strategyResult = strategyResult;
        this.strategyResultType = strategyResultType;
//        this.csService = service;
//        this.sessionFactory = sessionFactory;
    }

    public void run() {
        String completeStatus = "";
        try {
            prepare();
            strategyResult.run();
            completeStatus = "Finished";
            addCompletedStatus();
        } catch (EmfException e) {
            completeStatus = "Failed";
            logError("Failed to run strategy : ", e);
            setStatus("Failed to run strategy: " + "Reason: " + e.getMessage() + completeStatus);
        } finally {
            //
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
    private void prepare() {
//        ControlStrategy controlStrategy = strategy.getControlStrategy();
//        controlStrategy = csService.obtainLocked(controlStrategy.getCreator(), controlStrategy.getId());
//        controlStrategy.setStartDate(new Date());
//        controlStrategy.setRunStatus("Running");
//        csService.updateControlStrategyWithLock(controlStrategy);
        addStartStatus();
    }

//    private void setRunStatusAndCompletionDate(String completeStatus, Date completionDate) throws EmfException {
//        strategy.getControlStrategy().setRunStatus(completeStatus);
//        strategy.getControlStrategy().setLastModifiedDate(new Date());
//        strategy.getControlStrategy().setCompletionDate(completionDate);
////        updateStrategy();
//        csService.setControlStrategyRunStatusAndCompletionDate(strategy.getControlStrategy().getId(), completeStatus, completionDate);
//    }

//    private void updateStrategy() {
//        try {
//            csService.updateControlStrategy(strategy.getControlStrategy());
//        } catch (EmfException e) {
//            logError("Failed to update the strategy : ", e);
//            setStatus("Failed to update strategy: " + "Reason: " + e.getMessage());
//        }
//
//    }

    private void addStartStatus() {
        setStatus("Started creating control strategy summary: " + strategyResultType.getName() + ".");
    }

    private void addCompletedStatus() {
        setStatus("Completed creating control strategy summary: " + strategyResultType.getName() + ".");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}
