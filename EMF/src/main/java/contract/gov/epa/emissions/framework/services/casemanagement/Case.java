package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Case implements Comparable<Case>, Lockable, Serializable {

    private int id;
    
    private String name;

    private Abbreviation abbreviation;
    
    private ModelToRun model;
    
    private String modelVersion;

    private AirQualityModel airQualityModel;

    private CaseCategory caseCategory;

    private EmissionsYear emissionsYear;

    private String gridDescription;

    private MeteorlogicalYear meteorlogicalYear;

    private Speciation speciation;

    private String description;
    
    private String inputFileDir;

    private String outputFileDir;

    private Project project;

    private Mutex lock;

    private Region modelingRegion;

    private Region controlRegion;

    private String runStatus;

    private Date lastModifiedDate;

    private User creator;

    private User lastModifiedBy;
    
    private boolean caseTemplate;
    
    private Integer numMetLayers;

    private Integer numEmissionsLayers;
    
    private int baseYear;
    
    private int futureYear;
    
    private Date startDate;
    
    private Date endDate;
    
    private Sector[] sectors = new Sector[]{};
    
    private GeoRegion[] regions = new GeoRegion[]{};

    //private List caseInputs;
    
    private boolean isFinal;
    
    private String templateUsed;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Case() {
        lock = new Mutex();
    }

    public Case(String name) {
        this();
        this.name = name;
    }

    public Case(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Case))
            return false;
        final Case caze = (Case) other;
        return caze.name.equals(name) || caze.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public void setAbbreviation(Abbreviation abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Abbreviation getAbbreviation() {
        return abbreviation;
    }

    public void setAirQualityModel(AirQualityModel airQualityModel) {
        this.airQualityModel = airQualityModel;
    }

    public AirQualityModel getAirQualityModel() {
        return airQualityModel;
    }

    public void setCaseCategory(CaseCategory caseCategory) {
        this.caseCategory = caseCategory;
    }

    public CaseCategory getCaseCategory() {
        return caseCategory;
    }

    public void setEmissionsYear(EmissionsYear emissionsYear) {
        this.emissionsYear = emissionsYear;
    }

    public EmissionsYear getEmissionsYear() {
        return emissionsYear;
    }

    public void setMeteorlogicalYear(MeteorlogicalYear meteorlogicalYear) {
        this.meteorlogicalYear = meteorlogicalYear;
    }

    public MeteorlogicalYear getMeteorlogicalYear() {
        return meteorlogicalYear;
    }

    public void setSpeciation(Speciation speciation) {
        this.speciation = speciation;
    }

    public Speciation getSpeciation() {
        return speciation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
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

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getBaseYear() {
        return baseYear;
    }

    public void setBaseYear(int baseYear) {
        this.baseYear = baseYear;
    }

    public boolean isCaseTemplate() {
        return caseTemplate;
    }

    public void setCaseTemplate(boolean caseTemplate) {
        this.caseTemplate = caseTemplate;
    }

    public Region getControlRegion() {
        return controlRegion;
    }

    public void setControlRegion(Region controlRegion) {
        this.controlRegion = controlRegion;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getFutureYear() {
        return futureYear;
    }

    public void setFutureYear(int futureYear) {
        this.futureYear = futureYear;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Mutex getLock() {
        return lock;
    }

    public void setLock(Mutex lock) {
        this.lock = lock;
    }

    public Region getModelingRegion() {
        return modelingRegion;
    }

    public void setModelingRegion(Region modelingRegion) {
        this.modelingRegion = modelingRegion;
    }

    public Integer getNumEmissionsLayers() {
        return numEmissionsLayers;
    }

    public void setNumEmissionsLayers(Integer numEmissionsLayers) {
        this.numEmissionsLayers = numEmissionsLayers;
    }

    public Integer getNumMetLayers() {
        return numMetLayers;
    }

    public void setNumMetLayers(Integer numMetLayers) {
        this.numMetLayers = numMetLayers;
    }

    public void addSector(Sector sector) {
        List<Sector> sectorsList = new ArrayList<Sector>();
        sectorsList.addAll(Arrays.asList(this.sectors));
        sectorsList.add(sector);
        
        this.sectors = sectorsList.toArray(new Sector[0]);
    }

    public Sector[] getSectors() {
        return this.sectors;
    }

    public void setSectors(Sector[] sectors) {
        this.sectors = sectors;
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getTemplateUsed() {
        return templateUsed;
    }

    public void setTemplateUsed(String templateUsed) {
        this.templateUsed = templateUsed;
    }

    public String getInputFileDir() {
        return inputFileDir;
    }

    public void setInputFileDir(String inputFileDir) {
        this.inputFileDir = inputFileDir;
    }

    public String getOutputFileDir() {
        return outputFileDir;
    }

    public void setOutputFileDir(String outputFileDir) {
        this.outputFileDir = outputFileDir;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getGridDescription() {
        return gridDescription;
    }

    public void setGridDescription(String gridDescription) {
        this.gridDescription = gridDescription;
    }

    public ModelToRun getModel() {
        return model;
    }

    public void setModel(ModelToRun model) {
        this.model = model;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public int compareTo(Case other) {
        if (other == null)
            return -1;
        
        return name.compareToIgnoreCase(other.getName());
    }

    public GeoRegion[] getRegions() {
        return regions;
    }

    public void setRegions(GeoRegion[] regions) {
        this.regions = regions;
    }
    
}
