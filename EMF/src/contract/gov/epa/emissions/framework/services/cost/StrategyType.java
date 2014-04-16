package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class StrategyType implements Serializable, Comparable {

    private int id;

    private String name;

    private String description;

    private String defaultSortOrder;

    private String strategyClassName;

    public static final String leastCost = "Least Cost";

    public static final String leastCostCurve = "Least Cost Curve";

    public static final String maxEmissionsReduction = "Max Emissions Reduction";

    public static final String MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION = "Multi-Pollutant Max Emissions Reduction";

    public static final String applyMeasuresInSeries = "Apply Measures In Series";

    public static final String projectFutureYearInventory = "Project Future Year Inventory";

    public static final String annotateInventory = "Annotate Inventory";
    
    //    private Mutex lock;
    
    public StrategyType() {
//        lock = new Mutex();
    }

    public StrategyType(String name) {
        this();
        this.name = name;
    }

    public String getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public Mutex getLock() {
//        return lock;
//    }
//
//    public void setLock(Mutex lock) {
//        this.lock = lock;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrategyClassName() {
        return strategyClassName;
    }

    public void setStrategyClassName(String strategyClassName) {
        this.strategyClassName = strategyClassName;
    }

//    public String getLockOwner() {
//        return lock.getLockOwner();
//    }
//
//    public void setLockOwner(String username) {
//        lock.setLockOwner(username);
//    }
//
//    public Date getLockDate() {
//        return lock.getLockDate();
//    }
//
//    public void setLockDate(Date lockDate) {
//        this.lock.setLockDate(lockDate);
//    }
//
//    public boolean isLocked(String owner) {
//        return lock.isLocked(owner);
//    }
//
//    public boolean isLocked(User owner) {
//        return lock.isLocked(owner);
//    }
//
//    public boolean isLocked() {
//        return lock.isLocked();
//    }

    public int compareTo(Object o) {
        return name.compareTo(((StrategyType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof StrategyType && ((StrategyType) other).id == id);
    }
}
