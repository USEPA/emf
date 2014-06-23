package gov.epa.emissions.framework.services.cost.analysis.multiPollutantMaxReduction;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractCheckMessagesStrategyTask;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends AbstractCheckMessagesStrategyTask {

    public StrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            HibernateSessionFactory sessionFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
    }

    public void run() throws EmfException {
        super.run(this.getLoader());
    }

    public void afterRun() throws EmfException {

        if (controlStrategy.getApplyCAPMeasuresOnHAPPollutants())
            applyCAPMeasuresOnHAPPollutants(strategyResultList.toArray(new ControlStrategyResult[0]));
        
        // now create the county summary result based on the results from the strategy run...
        generateStrategyCountySummaryResult(strategyResultList.toArray(new ControlStrategyResult[0]));

        this.checkMessagesForWarnings();
    }

    public void beforeRun() {
        // NOTE Auto-generated method stub
    }
}
