package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlStrategyInventoryOutputFactory {

    private ControlStrategy controlStrategy;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private String namePrefix;

    public ControlStrategyInventoryOutputFactory(User user, ControlStrategy controlStrategy,
            String namePrefix, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.namePrefix = namePrefix;
    }

    public ControlStrategyInventoryOutput get(ControlStrategyResult controlStrategyResult) throws Exception {
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.applyMeasuresInSeries)) 
            return new ApplyMeasureInSeriesControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    sessionFactory, dbServerFactory);
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) 
            return new ProjectFutureYearInventoryControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    sessionFactory, dbServerFactory);
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.annotateInventory)) 
            return new AnnotatedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix, 
                    sessionFactory, dbServerFactory);
        if (controlStrategyResult.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory))
            return new MergedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    sessionFactory, dbServerFactory);
        return new AbstractControlStrategyInventoryOutput(user, controlStrategy, 
                controlStrategyResult, namePrefix,
                sessionFactory, dbServerFactory);
    }
}
