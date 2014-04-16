package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlStrategy implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private Region region;

    private Project project;

    private Double discountRate;

    private int costYear;

    private int inventoryYear; // == targetYear

    private User creator;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private ControlStrategyInputDataset[] controlStrategyInputDatasets = new ControlStrategyInputDataset[] {};

    private Pollutant targetPollutant;

    private ControlStrategyTargetPollutant[] targetPollutants = new ControlStrategyTargetPollutant[] {};

    private String runStatus;

    private StrategyType strategyType;

    private String filter;

    private ControlMeasureClass[] controlMeasureClasses = new ControlMeasureClass[] {};

    private ControlStrategyMeasure[] controlMeasures = new ControlStrategyMeasure[] {};

//    private String countyFile;

    private EmfDataset countyDataset;

    private Integer countyDatasetVersion;

    private Mutex lock;

    private ControlStrategyConstraint constraint;

    private Boolean useCostEquations;

    private Double totalCost;
    
    private Double totalReduction;
    
    private String exportDirectory;
    
    private Boolean deleteResults = false;

    private Boolean mergeInventories;
    
    private ControlProgram[] controlPrograms = new ControlProgram[] {};

    private Boolean includeUnspecifiedCosts;
    
    private String copiedFrom;
    
    private Boolean isFinal;

    private Boolean applyCAPMeasuresOnHAPPollutants;
    
    public ControlStrategy() {
        this.lock = new Mutex();
//        this.controlStrategyInputDatasets = new ArrayList();
//        this.controlMeasureClasses = new ArrayList();
//        this.controlMeasures = new ArrayList();
    }

    public ControlStrategy(String name) {
        this();
        this.name = name;
    }

    public ControlStrategy(int id, String name) {
        this(name);
        this.id = id;
    }

    public ControlStrategy(int id, String name,
            Date lastModifiedDate, String runStatus,
            Region region, Pollutant targetPollutant,
            Project project, StrategyType strategyType,
            int costYear, int inventoryYear,
            User creator, Double totalCost,
            Double totalReduction,
            Boolean isFinal) {
        this();
        this.id = id;
        this.name = name;
        this.lastModifiedDate = lastModifiedDate;
        this.runStatus = runStatus;
        this.region = region;
        this.targetPollutant = targetPollutant;
        this.project = project;
        this.strategyType = strategyType;
        this.costYear = costYear;
        this.inventoryYear = inventoryYear;
        this.creator = creator;
        this.setTotalCost(totalCost);
        this.setTotalReduction(totalReduction);
        this.setIsFinal(isFinal);
    }

//    ControlStrategy(cS.id, cS.name, " +
//            "cS.lastModifiedDate, cS.runStatus, " +
//            "cS.region, cs.targetPollutant, " +
//            "cS.project, cS.strategyType, " +
//            "cS.costYear, cS.inventoryYear, " +
//            "cS.creator)" +

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControlStrategy))
            return false;

        final ControlStrategy cs = (ControlStrategy) other;

        return cs.name.equals(name) || cs.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public int getInventoryYear() {
        return inventoryYear;
    }

    public void setInventoryYear(int inventoryYear) {
        this.inventoryYear = inventoryYear;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public ControlStrategyInputDataset[] getControlStrategyInputDatasets() {
        return controlStrategyInputDatasets;//(ControlStrategyInputDataset[]) controlStrategyInputDatasets.toArray(new ControlStrategyInputDataset[0]);
    }

    public void setControlStrategyInputDatasets(ControlStrategyInputDataset[] inputDatasets) {
        this.controlStrategyInputDatasets = inputDatasets;//Arrays.asList(inputDatasets);
    }

    public void addControlStrategyInputDatasets(ControlStrategyInputDataset inputDataset) {
        List<ControlStrategyInputDataset> inputDatasetList = new ArrayList<ControlStrategyInputDataset>();
        inputDatasetList.addAll(Arrays.asList(controlStrategyInputDatasets));
        inputDatasetList.add(inputDataset);
        this.controlStrategyInputDatasets = inputDatasetList.toArray(new ControlStrategyInputDataset[0]);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Pollutant getTargetPollutant() {
        return targetPollutant;
    }

    public void setTargetPollutant(Pollutant targetPollutant) {
        this.targetPollutant = targetPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(StrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setControlMeasureClasses(ControlMeasureClass[] controlMeasureClasses) {
        this.controlMeasureClasses = controlMeasureClasses;//(controlMeasureClasses != null) ? Arrays.asList(controlMeasureClasses) : new ArrayList();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return controlMeasureClasses;//(ControlMeasureClass[])controlMeasureClasses.toArray(new ControlMeasureClass[0]);
    }

    public void setControlMeasures(ControlStrategyMeasure[] controlMeasures) {
        this.controlMeasures = controlMeasures;// (controlMeasures != null) ? Arrays.asList(controlMeasures) : new ArrayList();
    }

    public ControlStrategyMeasure[] getControlMeasures() {
        return controlMeasures;//(ControlStrategyMeasure[])controlMeasures.toArray(new ControlStrategyMeasure[0]);
    }

//    public void setCountyFile(String countyFile) {
//        this.countyFile = countyFile;
//    }
//
//    public String getCountyFile() {
//        return countyFile;
//    }

    public void setCountyDataset(EmfDataset countyDataset) {
        this.countyDataset = countyDataset;
    }

    public EmfDataset getCountyDataset() {
        return countyDataset;
    }

    public void setCountyDatasetVersion(Integer countyDatasetVersion) {
        this.countyDatasetVersion = countyDatasetVersion;
    }

    public Integer getCountyDatasetVersion() {
        return countyDatasetVersion;
    }

    public void setConstraint(ControlStrategyConstraint constraint) {
        this.constraint = constraint;
    }

    public ControlStrategyConstraint getConstraint() {
        return constraint;
    }

    public String toString() {
        return name;
    }

    public void setUseCostEquations(Boolean useCostEquations) {
        this.useCostEquations = useCostEquations;
    }

    public Boolean getUseCostEquations() {
        return useCostEquations;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalReduction(Double totalReduction) {
        this.totalReduction = totalReduction;
    }

    public Double getTotalReduction() {
        return totalReduction;
    }

    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public String getExportDirectory() {
        return exportDirectory;
    }

    public void setDeleteResults(Boolean deleteResults) {
        this.deleteResults = deleteResults;
    }

    public Boolean getDeleteResults() {
        return deleteResults;
    }

    public void setMergeInventories(Boolean mergeInventories) {
        this.mergeInventories = mergeInventories;
    }

    public Boolean getMergeInventories() {
        return mergeInventories;
    }

    public void setIncludeUnspecifiedCosts(Boolean includeUnspecifiedCosts) {
        this.includeUnspecifiedCosts = includeUnspecifiedCosts;
    }

    public Boolean getIncludeUnspecifiedCosts() {
        return includeUnspecifiedCosts;
    }
    
    public void setControlPrograms(ControlProgram[] controlPrograms) {
        this.controlPrograms = controlPrograms;
    }

    public ControlProgram[] getControlPrograms() {
        return controlPrograms;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }
    
    public void setTargetPollutants(ControlStrategyTargetPollutant[] targetPollutants) {
        this.targetPollutants = targetPollutants;
    }

    public ControlStrategyTargetPollutant[] getTargetPollutants() {
        return targetPollutants;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setApplyCAPMeasuresOnHAPPollutants(Boolean applyCAPMeasuresOnHAPPollutants) {
        this.applyCAPMeasuresOnHAPPollutants = applyCAPMeasuresOnHAPPollutants;
    }

    public Boolean getApplyCAPMeasuresOnHAPPollutants() {
        return this.applyCAPMeasuresOnHAPPollutants;
    }
}
