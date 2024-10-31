package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;

import java.lang.reflect.Constructor;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategySummaryFactory {

    private static Log log = LogFactory.getLog(StrategySummaryFactory.class);

    public StrategySummaryFactory() {
        //
    }

    public IStrategySummaryTask create(ControlStrategy controlStrategy, User user,
            StrategyResultType strategyResultType, EntityManagerFactory entityManagerFactory,
            DbServerFactory dbServerFactory) throws EmfException {
        try {

            if (strategyResultType == null) 
                throw new EmfException("Summary task is missing strategy result summary to run.");
            
            if (strategyResultType.getClassName() == null || strategyResultType.getClassName().isEmpty()) 
                throw new EmfException("Summary task is missing strategy result summary to run.");
            
            return doCreate(controlStrategy, user, 
                    strategyResultType, entityManagerFactory, 
                    dbServerFactory);
//            
//            if (strategyResultType.getName().equalsIgnoreCase(StrategyResultType.rsmPercentReduction))
//                return new StrategyRSMPctRedSummaryTask(controlStrategy, user, 
//                        dbServerFactory, entityManagerFactory);
//
//            if (strategyResultType.getName().equalsIgnoreCase(StrategyResultType.strategyImpactSummary))
//                return new StrategyCountyImpactSummaryTask(controlStrategy, user, 
//                        dbServerFactory, entityManagerFactory);
//
//            //Don't assume a summary task, throw an error.
//            throw new EmfException("Summary task can not be run for, " + strategyResultType.getName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }
    
    private IStrategySummaryTask doCreate(ControlStrategy controlStrategy, User user,
            StrategyResultType strategyResultType, EntityManagerFactory entityManagerFactory,
            DbServerFactory dbServerFactory) throws Exception {
        String className = strategyResultType.getClassName();
        Class strategySummaryClass = Class.forName(className);
        Class[] classParams = new Class[] { ControlStrategy.class, User.class, 
                DbServerFactory.class, EntityManagerFactory.class};
        Object[] params = new Object[] { controlStrategy, user, 
                dbServerFactory, entityManagerFactory};
        Constructor strategyConstructor = strategySummaryClass.getDeclaredConstructor(classParams);

        return (IStrategySummaryTask)strategyConstructor.newInstance(params);
    }
}