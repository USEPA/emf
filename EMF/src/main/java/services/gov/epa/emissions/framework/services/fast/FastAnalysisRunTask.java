package gov.epa.emissions.framework.services.fast;

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

public class FastAnalysisRunTask implements Runnable {

    private static Log log = LogFactory.getLog(FastAnalysisRunTask.class);

    private User user;

    private FastAnalysisTask fastAnalysisTask;

    private Services services;

    private FastService ssService;

    private HibernateSessionFactory sessionFactory;

    public FastAnalysisRunTask(FastAnalysisTask fastRunTask, User user, 
            Services services, FastService service,
            HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.services = services;
        this.fastAnalysisTask = fastRunTask;
        this.ssService = service;
        this.sessionFactory = sessionFactory;
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
        Long runningCount = getFastAnalysisRunningCount();
        
        //make sure we can add an work item to the queue
        //check the pool size and compare to the number of strategies currently running
        if (runningCount < poolSize) {
            try {
                prepare();
                fastAnalysisTask.run();
                completeStatus = "Finished";
                addCompletedStatus();
            } catch (EmfException e) {
                completeStatus = "Failed";
                logError("Failed to run FAST Analysis : ", e);
                setStatus("Failed to run FAST Analysis : " + "Reason: " + e.getMessage());
            } finally {
//                    closeConnection();
                
//                strategy.getFastRun().setRunStatus(completeStatus);
//                strategy.getFastRun().setCompletionDate(new Date());

                //check to see if there is another strategy to run...
                List<FastRun> waitingStrategies;
                try {
                    setRunStatusAndCompletionDate(completeStatus, new Date());
                    waitingStrategies = ssService.getFastRunsByRunStatus("Waiting");
                    if (waitingStrategies.size() > 0) {
                        runningCount = getFastAnalysisRunningCount();
                        if (runningCount < poolSize) {
                            for (FastRun controlStrategy : waitingStrategies.toArray(new FastRun[0])) {
                                if (runningCount < poolSize) {
                                    ssService.runFastRun(user, controlStrategy.getId());
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
        FastAnalysis fastAnalysis = fastAnalysisTask.getFastAnalysis();
        fastAnalysis = ssService.obtainLockedFastAnalysis(fastAnalysis.getCreator(), fastAnalysis.getId());
        fastAnalysis.setStartDate(new Date());
        fastAnalysis.setRunStatus("Running");
        ssService.updateFastAnalysisWithLock(fastAnalysis);
        addStartStatus();
    }

    private void setRunStatusAndCompletionDate(String completeStatus, Date completionDate) throws EmfException {
        fastAnalysisTask.getFastAnalysis().setRunStatus(completeStatus);
        fastAnalysisTask.getFastAnalysis().setLastModifiedDate(new Date());
        fastAnalysisTask.getFastAnalysis().setCompletionDate(completionDate);
//        updateStrategy();
        ssService.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisTask.getFastAnalysis().getId(), completeStatus, completionDate);
    }

//    private void updateStrategy() {
//        try {
//            csService.updateFastRun(strategy.getFastRun());
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

    private Long getFastAnalysisRunningCount() {
        Long count = 0L;
        try {
            count = ssService.getFastRunRunningCount();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return count;
    }
    
    private void addStartStatus() {
        setStatus("Started running FAST Analysis: " + fastAnalysisTask.getFastAnalysis().getName());
    }

    private void addCompletedStatus() {
        setStatus("Completed running FAST Analysis: " + fastAnalysisTask.getFastAnalysis().getName() + ".");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("FASTAnalysis");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}
