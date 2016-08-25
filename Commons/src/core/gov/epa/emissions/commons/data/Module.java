package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class Module implements Serializable, Lockable, Comparable<Module> {

    private int id;

    private String name;

    private String description;

    private ModuleTypeVersion moduleTypeVersion;

    private User creator;

    private Date creationDate;

    private Date lastModifiedDate;

    private boolean isFinal;

    private Mutex lock;

    private Map<String, ModuleDataset> moduleDatasets;

    private Map<String, ModuleParameter> moduleParameters;

//    private List<ModuleHistoryRecord> moduleHistory;

    public Module() {
        moduleDatasets = new HashMap<String, ModuleDataset>();
        moduleParameters = new HashMap<String, ModuleParameter>();
//        moduleHistory = new ArrayList<ModuleHistoryRecord>();
        lock = new Mutex();
    }

    public Module(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public Module(String name) {
        this();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String username) {
        lock.setLockOwner(username);
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        this.lock.setLockDate(lockDate);
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

    // moduleDatasets

    public Map<String, ModuleDataset> getModuleDatasets() {
        return this.moduleDatasets;
    }

    public void setModuleDatasets(Map<String, ModuleDataset> moduleDatasets) {
        this.moduleDatasets = moduleDatasets;
    }

    public void addModuleDataset(ModuleDataset moduleDataset) {
        this.moduleDatasets.put(moduleDataset.getModuleTypeVersionDataset().getPlaceholderName(), moduleDataset);
    }

    public void removeModuleDataset(ModuleDataset moduleDataset) {
        removeModuleDataset(moduleDataset.getModuleTypeVersionDataset().getPlaceholderName());
    }

    public void removeModuleDataset(String placeholderName) {
        this.moduleDatasets.remove(placeholderName);
    }

    public void clearModuleDatasets() {
        this.moduleDatasets.clear();
    }

    // moduleParameters

    public Map<String, ModuleParameter> getModuleParameters() {
        return this.moduleParameters;
    }

    public void setModuleParameters(Map<String, ModuleParameter> moduleParameters) {
        this.moduleParameters = moduleParameters;
    }

    public void addModuleParameter(ModuleParameter moduleParameter) {
        this.moduleParameters.put(moduleParameter.getModuleTypeVersionParameter().getParameterName(), moduleParameter);
    }

    public void removeModuleParameter(ModuleParameter moduleParameter) {
        removeModuleParameter(moduleParameter.getModuleTypeVersionParameter().getParameterName());
    }

    public void removeModuleParameter(String placeholderName) {
        this.moduleParameters.remove(placeholderName);
    }

    public void clearModuleParameters() {
        this.moduleParameters.clear();
    }

    // standard methods

    public int compareTo(Module o) {
        return name.compareTo(o.getName());
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof Module && ((Module) other).getId() == id);
    }
}
