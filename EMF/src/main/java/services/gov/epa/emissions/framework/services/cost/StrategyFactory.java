package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyFactory {
    
    private static Log log = LogFactory.getLog(StrategyFactory.class);

    public StrategyFactory() {
        //
    }

    public Strategy create(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws EmfException {
        try {
            
            StrategyLoader loader = this.doCreateLoader(controlStrategy, user, sessionFactory, dbServerFactory);
            return doCreateStrategy(controlStrategy, user, 
                    sessionFactory, dbServerFactory, loader);
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }

    private Strategy doCreateStrategy(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory, StrategyLoader loader)
            throws Exception {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class<?> strategyClass = Class.forName(strategyClassName);
        Class<?>[] classParams = new Class[] { ControlStrategy.class, User.class, 
                DbServerFactory.class, HibernateSessionFactory.class, StrategyLoader.class };
        Object[] params = new Object[] { controlStrategy, user, 
                dbServerFactory, sessionFactory, loader };
        Constructor<?> strategyConstructor = strategyClass.getDeclaredConstructor(classParams);

        return (Strategy) strategyConstructor.newInstance(params);
    }

    private StrategyLoader doCreateLoader(ControlStrategy controlStrategy, User user,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {

        //TODO Used this method to avoid schema change. Do we want to do this some other way?
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        String strategyPackageName = strategyClassName.substring(0, strategyClassName.lastIndexOf("."));
        String strategyLoaderClassName = strategyPackageName + ".StrategyLoader";
        Class<?> strategyLoaderClass = Class.forName(strategyLoaderClassName);
        Class<?>[] classParams = new Class[] {User.class, DbServerFactory.class,
                HibernateSessionFactory.class, ControlStrategy.class };
        Object[] params = new Object[] { user, dbServerFactory, sessionFactory, controlStrategy };
        Constructor<?> strategyConstructor = strategyLoaderClass.getDeclaredConstructor(classParams);

        return (StrategyLoader) strategyConstructor.newInstance(params);
    }
}