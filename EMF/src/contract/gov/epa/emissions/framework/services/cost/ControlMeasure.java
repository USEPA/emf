package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.SumEffRec;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControlMeasure implements Lockable, Serializable {
    
    public static int ABBREV_LEN = 20; 

    private int id;

    private String name;

    private String description;

//    private int deviceCode;

    private int costYear;

    private Float equipmentLife;

    private Pollutant majorPollutant;

    private User creator;

    private float annualizedCost;

    private ControlMeasureClass cmClass;

    private String abbreviation;

    private ControlTechnology controlTechnology;

    private SourceGroup sourceGroup;

    private String dataSouce;

    private Date dateReviewed;

    private Date lastModifiedTime;

    private Mutex lock;

    private Reference[] references = new Reference[0];
    
    private Scc[] sccs = new Scc[] {};

    private EfficiencyRecord[] efficiencyRecords = new EfficiencyRecord[] {};
    private SumEffRec[] sumEffRecs = new SumEffRec[] {};

    private Sector[] sectors = new Sector[] {};
    
    private String lastModifiedBy;

    private Double ruleEffectiveness;

    private Double rulePenetration;

    private Double applyOrder;

    private ControlMeasureEquation[] equations = new ControlMeasureEquation[] {};

    private EmfDataset regionDataset;

    private Integer regionDatasetVersion;

    private ControlMeasureMonth[] months = new ControlMeasureMonth[] {};

    private ControlMeasureNEIDevice[] neiDevices = new ControlMeasureNEIDevice[] {};
    
    private ControlMeasureProperty[] properties = new ControlMeasureProperty[] {};
    
    public ControlMeasure() {
        this.lock = new Mutex();
//        this.sccs = new ArrayList();
//        this.sectors = new ArrayList();
//        this.equationTypeList = new ArrayList();
//        this.equationTypes = new ArrayList();
        
    }

    
    public ControlMeasure(String name) {
        this();
        this.name = name;
    }

    public ControlMeasure(int id, String name) {
        this(name);
        this.id = id;
    }
    
    public ControlMeasure(int id, String name, String abbreviation) {
        this(name);
        this.id = id;
        this.abbreviation = abbreviation;
    }

    public float getAnnualizedCost() {
        return annualizedCost;
    }

    public void setAnnualizedCost(float annualizedCost) {
        this.annualizedCost = annualizedCost;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public int getDeviceCode() {
//        return deviceCode;
//    }
//
//    public void setDeviceCode(int deviceCode) {
//        this.deviceCode = deviceCode;
//    }

    public Float getEquipmentLife() {
        return equipmentLife;
    }

    public void setEquipmentLife(Float equipmentLife) {
        this.equipmentLife = equipmentLife;
    }

    public Pollutant getMajorPollutant() {
        return majorPollutant;
    }

    public void setMajorPollutant(Pollutant majorPollutant) {
        this.majorPollutant = majorPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isLocked(User user) {
        return lock.isLocked(user);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasure)) {
            return false;
        }

        ControlMeasure other = (ControlMeasure) obj;

        return (id == other.getId() || name.equals(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public ControlMeasureClass getCmClass() {
        return cmClass;
    }

    public void setCmClass(ControlMeasureClass cmClass) {
        this.cmClass = cmClass;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public EfficiencyRecord[] getEfficiencyRecords() {
        return efficiencyRecords;
//        return (EfficiencyRecord[]) efficiencyRecords.toArray(new EfficiencyRecord[0]);
    }

    public void setEfficiencyRecords(EfficiencyRecord[] efficiencyRecords) {
        this.efficiencyRecords = efficiencyRecords;
//        this.efficiencyRecords = Arrays.asList(efficiencyRecords);
    }

    public ControlTechnology getControlTechnology() {
        return controlTechnology;
    }

    public void setControlTechnology(ControlTechnology controlTechnology) {
        this.controlTechnology = controlTechnology;
    }

    public String getDataSouce() {
        return dataSouce;
    }

    public void setDataSouce(String dataSouce) {
        this.dataSouce = dataSouce;
    }

    public Date getDateReviewed() {
        return dateReviewed;
    }

    public void setDateReviewed(Date dateReviewed) {
        this.dateReviewed = dateReviewed;
    }

    public SourceGroup getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(SourceGroup sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public Scc[] getSccs() {
        return sccs;
    }

    public void setSccs(Scc[] sccs) {
        this.sccs = sccs;
    }

    public Reference[] getReferences() {
        return references;
    }

    public void setReferences(Reference[] references) {
        this.references = references;
    }

    public Sector[] getSectors() {
        return sectors;//(Sector[]) sectors.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = sectors;//Arrays.asList(sectors);
    }

    public void addEfficiencyRecord(EfficiencyRecord efficiencyRecord) {
        List<EfficiencyRecord> efficiencyRecordList = new ArrayList<EfficiencyRecord>();
        efficiencyRecordList.addAll(Arrays.asList(efficiencyRecords));
        efficiencyRecordList.add(efficiencyRecord);
        this.efficiencyRecords = efficiencyRecordList.toArray(new EfficiencyRecord[0]);
    }

    public void addScc(Scc scc) {
        List<Scc> sccList = new ArrayList<Scc>();
        sccList.addAll(Arrays.asList(sccs));
        sccList.add(scc);
        this.sccs = sccList.toArray(new Scc[0]);
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public SumEffRec[] getSumEffRecs() {
        return sumEffRecs;
    }

    public void setSumEffRecs(SumEffRec[] sumEffRecs) {
        this.sumEffRecs = sumEffRecs;
    }

    public void addSumEffRec(SumEffRec aggregatedPollutantEfficiencyRecord) {
        List<SumEffRec> sumEffRecList = new ArrayList<SumEffRec>();
        sumEffRecList.addAll(Arrays.asList(sumEffRecs));
        sumEffRecList.add(aggregatedPollutantEfficiencyRecord);
        this.sumEffRecs = sumEffRecList.toArray(new SumEffRec[0]);
    }

    public void addSector(Sector sector) {
        List<Sector> equationList = new ArrayList<Sector>();
        equationList.addAll(Arrays.asList(sectors));
        equationList.add(sector);
        this.sectors = equationList.toArray(new Sector[0]);
    }

    //these properties will overide the efficiency record settings...
    public void setRuleEffectiveness(Double ruleEffectiveness) {
        this.ruleEffectiveness = ruleEffectiveness;
    }

    public Double getRuleEffectiveness() {
        return ruleEffectiveness;
    }

    public void setRulePenetration(Double rulePenetration) {
        this.rulePenetration = rulePenetration;
    }

    public Double getRulePenetration() {
        return rulePenetration;
    }

    public void setApplyOrder(Double applyOrder) {
        this.applyOrder = applyOrder;
    }

    public Double getApplyOrder() {
        return applyOrder;
    }

    public void setEquations(ControlMeasureEquation[] equations) {
        this.equations = equations;
//        this.equations.removeAll(this.equations);
//        for (int i = 0; i < equations.length; i++) {
//            this.equations.add(equations[i]);
//        }
    }

    public void addEquation(ControlMeasureEquation equation) {
        List<ControlMeasureEquation> equationList = new ArrayList<ControlMeasureEquation>();
        equationList.addAll(Arrays.asList(equations));
        equationList.add(equation);
        this.equations = equationList.toArray(new ControlMeasureEquation[0]);
    }

    public ControlMeasureEquation[] getEquations() {
        return equations;//(ControlMeasureEquation[]) equations.toArray(new ControlMeasureEquation[0]);
    }

    public void setRegionDataset(EmfDataset regionDataset) {
        this.regionDataset = regionDataset;
    }

    public EmfDataset getRegionDataset() {
        return regionDataset;
    }

    public void setRegionDatasetVersion(Integer regionDatasetVersion) {
        this.regionDatasetVersion = regionDatasetVersion;
    }

    public Integer getRegionDatasetVersion() {
        return regionDatasetVersion;
    }

    public ControlMeasureMonth[] getMonths() {
        return months;
    }

    public void setMonths(ControlMeasureMonth[] months) {
        this.months = months;
    }

    public ControlMeasureNEIDevice[] getNeiDevices() {
        return neiDevices;
    }

    public void setNeiDevices(ControlMeasureNEIDevice[] neiDevices) {
        this.neiDevices = neiDevices;
    }


    public void setProperties(ControlMeasureProperty[] properties) {
        this.properties = properties;
    }


    public ControlMeasureProperty[] getProperties() {
        return properties;
    }

    public void addProperty(ControlMeasureProperty property) {
        List<ControlMeasureProperty> propertyList = new ArrayList<ControlMeasureProperty>();
        propertyList.addAll(Arrays.asList(properties));
        propertyList.add(property);
        this.properties = propertyList.toArray(new ControlMeasureProperty[0]);
    }

}