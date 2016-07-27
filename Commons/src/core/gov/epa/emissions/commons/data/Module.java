package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
//import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
//import java.util.List;

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

    private Map<String, ModuleDatasetIn> moduleDatasetsIn;

    private Map<String, ModuleDatasetOutNew> moduleDatasetsOutNew;

    private Map<String, ModuleDatasetOutReplace> moduleDatasetsOutReplace;

    private Map<String, ModuleParameterIn> moduleParametersIn;

//    private List<ModuleHistoryRecord> moduleHistory;

    public Module() {
        moduleDatasetsOutNew = new HashMap<String, ModuleDatasetOutNew>();
        moduleDatasetsOutReplace = new HashMap<String, ModuleDatasetOutReplace>();
        moduleParametersIn = new HashMap<String, ModuleParameterIn>();
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

    // moduleDatasetsIn

    public Map<String, ModuleDatasetIn> getModuleDatasetsIn() {
        return this.moduleDatasetsIn;
    }

    public void setModuleDatasetsIn(Map<String, ModuleDatasetIn> moduleDatasetsIn) {
        this.moduleDatasetsIn = moduleDatasetsIn;
    }

    public void addModuleDatasetIn(ModuleDatasetIn moduleDatasetIn) {
        this.moduleDatasetsIn.put(moduleDatasetIn.getModuleTypeVersionDataset().getPlaceholderName(), moduleDatasetIn);
    }

    public void removeModuleDatasetIn(ModuleDatasetIn moduleDatasetIn) {
        removeModuleDatasetIn(moduleDatasetIn.getModuleTypeVersionDataset().getPlaceholderName());
    }

    public void removeModuleDatasetIn(String placeholderName) {
        this.moduleDatasetsIn.remove(placeholderName);
    }

    // moduleDatasetsOutNew

    public Map<String, ModuleDatasetOutNew> getModuleDatasetsOutNew() {
        return this.moduleDatasetsOutNew;
    }

    public void setModuleDatasetsOutNew(Map<String, ModuleDatasetOutNew> moduleDatasetsOutNew) {
        this.moduleDatasetsOutNew = moduleDatasetsOutNew;
    }

    public void addModuleDatasetOutNew(ModuleDatasetOutNew moduleDatasetOutNew) {
        this.moduleDatasetsOutNew.put(moduleDatasetOutNew.getModuleTypeVersionDataset().getPlaceholderName(), moduleDatasetOutNew);
    }

    public void removeModuleDatasetOutNew(ModuleDatasetOutNew moduleDatasetOutNew) {
        removeModuleDatasetOutNew(moduleDatasetOutNew.getModuleTypeVersionDataset().getPlaceholderName());
    }

    public void removeModuleDatasetOutNew(String placeholderName) {
        this.moduleDatasetsOutNew.remove(placeholderName);
    }

    // moduleDatasetsOutReplace

    public Map<String, ModuleDatasetOutReplace> getModuleDatasetsOutReplace() {
        return this.moduleDatasetsOutReplace;
    }

    public void setModuleDatasetsOutReplace(Map<String, ModuleDatasetOutReplace> moduleDatasetsOutReplace) {
        this.moduleDatasetsOutReplace = moduleDatasetsOutReplace;
    }

    public void addModuleDatasetOutReplace(ModuleDatasetOutReplace moduleDatasetOutReplace) {
        this.moduleDatasetsOutReplace.put(moduleDatasetOutReplace.getModuleTypeVersionDataset().getPlaceholderName(), moduleDatasetOutReplace);
    }

    public void removeModuleDatasetOutReplace(ModuleDatasetOutReplace moduleDatasetOutReplace) {
        removeModuleDatasetOutReplace(moduleDatasetOutReplace.getModuleTypeVersionDataset().getPlaceholderName());
    }

    public void removeModuleDatasetOutReplace(String placeholderName) {
        this.moduleDatasetsOutReplace.remove(placeholderName);
    }

    // moduleParametersIn

    public Map<String, ModuleParameterIn> getModuleParametersIn() {
        return this.moduleParametersIn;
    }

    public void setModuleParametersIn(Map<String, ModuleParameterIn> moduleParametersIn) {
        this.moduleParametersIn = moduleParametersIn;
    }

    public void addModuleParameterIn(ModuleParameterIn moduleParameterIn) {
        this.moduleParametersIn.put(moduleParameterIn.getModuleTypeVersionParameter().getParameterName(), moduleParameterIn);
    }

    public void removeModuleParameterIn(ModuleParameterIn moduleParameterIn) {
        removeModuleParameterIn(moduleParameterIn.getModuleTypeVersionParameter().getParameterName());
    }

    public void removeModuleParameterIn(String placeholderName) {
        this.moduleParametersIn.remove(placeholderName);
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
