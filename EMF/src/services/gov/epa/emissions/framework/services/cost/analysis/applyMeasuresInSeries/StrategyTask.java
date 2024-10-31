package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import javax.persistence.EntityManagerFactory;

public class StrategyTask extends AbstractStrategyTask {

    public StrategyTask(ControlStrategy controlStrategy, User user, DbServerFactory dbServerFactory,
            EntityManagerFactory entityManagerFactory, StrategyLoader loader) throws EmfException {
        super(controlStrategy, user, dbServerFactory, entityManagerFactory, loader);
    }

    public void run() throws EmfException {
        super.run(this.getLoader());
    }

    public void afterRun() throws EmfException {
        //now create the county summary result based on the results from the strategy run...
        generateStrategyCountySummaryResult(strategyResultList.toArray(new ControlStrategyResult[0]));
    }

    public void beforeRun() {
        // NOTE Auto-generated method stub
    }
}