package gov.epa.emissions.framework.services.cost.analysis;

import java.lang.reflect.Constructor;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategySummaryFactory {

    private static Log log = LogFactory.getLog(StrategySummaryFactory.class);

    public StrategySummaryFactory() {
        //
    }

    public IStrategySummaryTask create(ControlStrategy controlStrategy, User user,
            StrategyResultType strategyResultType, HibernateSessionFactory sessionFactory,
            DbServerFactory dbServerFactory) throws EmfException {
        try {

            if (strategyResultType == null) 
                throw new EmfException("Summary task is missing strategy result summary to run.");
            
            if (strategyResultType.getClassName() == null || strategyResultType.getClassName().isEmpty()) 
                throw new EmfException("Summary task is missing strategy result summary to run.");
            
            return doCreate(controlStrategy, user, 
                    strategyResultType, sessionFactory, 
                    dbServerFactory);
//            
//            if (strategyResultType.getName().equalsIgnoreCase(StrategyResultType.rsmPercentReduction))
//                return new StrategyRSMPctRedSummaryTask(controlStrategy, user, 
//                        dbServerFactory, sessionFactory);
//
//            if (strategyResultType.getName().equalsIgnoreCase(StrategyResultType.strategyImpactSummary))
//                return new StrategyCountyImpactSummaryTask(controlStrategy, user, 
//                        dbServerFactory, sessionFactory);
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
            StrategyResultType strategyResultType, HibernateSessionFactory sessionFactory,
            DbServerFactory dbServerFactory) throws Exception {
        String className = strategyResultType.getClassName();
        Class strategySummaryClass = Class.forName(className);
        Class[] classParams = new Class[] { ControlStrategy.class, User.class, 
                DbServerFactory.class, HibernateSessionFactory.class};
        Object[] params = new Object[] { controlStrategy, user, 
                dbServerFactory, sessionFactory};
        Constructor strategyConstructor = strategySummaryClass.getDeclaredConstructor(classParams);

        return (IStrategySummaryTask)strategyConstructor.newInstance(params);
    }
}