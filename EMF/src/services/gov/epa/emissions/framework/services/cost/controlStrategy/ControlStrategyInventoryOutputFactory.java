package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;

import javax.persistence.EntityManagerFactory;

public class ControlStrategyInventoryOutputFactory {

    private ControlStrategy controlStrategy;

    private User user;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;

    private String namePrefix;

    public ControlStrategyInventoryOutputFactory(User user, ControlStrategy controlStrategy,
            String namePrefix, EntityManagerFactory entityManagerFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        this.namePrefix = namePrefix;
    }

    public ControlStrategyInventoryOutput get(ControlStrategyResult controlStrategyResult) throws Exception {
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.applyMeasuresInSeries)) 
            return new ApplyMeasureInSeriesControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    entityManagerFactory, dbServerFactory);
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) 
            return new ProjectFutureYearInventoryControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    entityManagerFactory, dbServerFactory);
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.annotateInventory)) 
            return new AnnotatedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix, 
                    entityManagerFactory, dbServerFactory);
        if (controlStrategyResult.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory) ||
            controlStrategyResult.getInputDataset().getDatasetType().getName().equals(DatasetType.ff10MergedInventory))
            return new MergedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, namePrefix,
                    entityManagerFactory, dbServerFactory);
        return new AbstractControlStrategyInventoryOutput(user, controlStrategy, 
                controlStrategyResult, namePrefix,
                entityManagerFactory, dbServerFactory);
    }
}
