package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

    private Map<String, ModuleInternalDataset> moduleInternalDatasets;

    private Map<String, ModuleInternalParameter> moduleInternalParameters;

    private List<History> moduleHistory;

    public Module() {
        moduleDatasets = new HashMap<String, ModuleDataset>();
        moduleParameters = new HashMap<String, ModuleParameter>();
        moduleInternalDatasets = new HashMap<String, ModuleInternalDataset>();
        moduleInternalParameters = new HashMap<String, ModuleInternalParameter>();
        moduleHistory = new ArrayList<History>();
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

    public Module deepCopy(User creator) {
        Module newModule = new Module();
        newModule.setName(name + " Copy"); // TODO search for unique new name
        newModule.setDescription("Copy of " + name + " module.\n" + description);
        newModule.setModuleTypeVersion(moduleTypeVersion);
        newModule.setCreator(creator);
        Date date = new Date();
        newModule.setCreationDate(date);
        newModule.setLastModifiedDate(date);
        newModule.setIsFinal(false);
        for(ModuleDataset moduleDataset : moduleDatasets.values()) {
            newModule.addModuleDataset(moduleDataset.deepCopy(newModule));
        }
        for(ModuleParameter moduleParameter : moduleParameters.values()) {
            newModule.addModuleParameter(moduleParameter.deepCopy(newModule));
        }
        for(ModuleInternalDataset moduleInternalDataset : moduleInternalDatasets.values()) {
            newModule.addModuleInternalDataset(moduleInternalDataset.deepCopy(newModule));
        }
        for(ModuleInternalParameter moduleInternalParameter : moduleInternalParameters.values()) {
            newModule.addModuleInternalParameter(moduleInternalParameter.deepCopy(newModule));
        }
        return newModule;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);

        if (!moduleTypeVersion.isValid(error)) return false;
        
        for(ModuleDataset moduleDataset : moduleDatasets.values()) {
            if (!moduleDataset.isValid(error)) return false;
        }

        for(ModuleParameter moduleParameter : moduleParameters.values()) {
            if (!moduleParameter.isValid(error)) return false;
        }
        
        for(ModuleInternalDataset moduleInternalDataset : moduleInternalDatasets.values()) {
            if (!moduleInternalDataset.isValid(error)) return false;
        }

        for(ModuleInternalParameter moduleInternalParameter : moduleInternalParameters.values()) {
            if (!moduleInternalParameter.isValid(error)) return false;
        }
        
        return true;
    }

    // checks if this module is up-to-date with respect to its module type version
    public boolean isUpToDate() {
        return lastModifiedDate.after(moduleTypeVersion.getLastModifiedDate());
    }

    // synchronize this module with its module type version
    // using the old module type version in order to detect what has changed
    public boolean update(ModuleTypeVersion oldMTV) {
        boolean updated = false;
        
        if (isUpToDate())
            return updated;
        
        // handle new or modified datasets
        Map<String, ModuleTypeVersionDataset> oldMTVDatasets = oldMTV.getModuleTypeVersionDatasets();
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            if (oldMTVDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName())) {
                ModuleTypeVersionDataset oldMTVDataset = oldMTVDatasets.get(moduleTypeVersionDataset.getPlaceholderName());
                if (oldMTVDataset.getMode().equals(moduleTypeVersionDataset.getMode()))
                    continue;
                ModuleDataset moduleDataset = moduleDatasets.get(oldMTVDataset.getPlaceholderName()); // must exist
                moduleDataset.initSettings();
                updated = true;
            } else {
                ModuleDataset moduleDataset = new ModuleDataset();
                moduleDataset.setModule(this);
                moduleDataset.setPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
                moduleDataset.initSettings();
                addModuleDataset(moduleDataset);
                updated = true;
            }
        }
        
        // handle removed datasets
        Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = moduleTypeVersion.getModuleTypeVersionDatasets();
        for(ModuleTypeVersionDataset oldMTVDataset : oldMTVDatasets.values()) {
            if (moduleTypeVersionDatasets.containsKey(oldMTVDataset.getPlaceholderName()))
                continue;
            moduleDatasets.remove(oldMTVDataset.getPlaceholderName()); // must exist
            updated = true;
        }
        
        // handle new or modified parameters
        Map<String, ModuleTypeVersionParameter> oldMTVParameters = oldMTV.getModuleTypeVersionParameters();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersion.getModuleTypeVersionParameters().values()) {
            if (oldMTVParameters.containsKey(moduleTypeVersionParameter.getParameterName())) {
                ModuleTypeVersionParameter oldMTVParameter = oldMTVParameters.get(moduleTypeVersionParameter.getParameterName());
                if (oldMTVParameter.getMode().equals(moduleTypeVersionParameter.getMode()))
                    continue;
                ModuleParameter moduleParameter = moduleParameters.get(oldMTVParameter.getParameterName()); // must exist
                moduleParameter.initSettings();
                updated = true;
            } else {
                ModuleParameter moduleParameter = new ModuleParameter();
                moduleParameter.setModule(this);
                moduleParameter.setParameterName(moduleTypeVersionParameter.getParameterName());
                moduleParameter.initSettings();
                addModuleParameter(moduleParameter);
                updated = true;
            }
        }
        
        // handle removed parameters
        Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters = moduleTypeVersion.getModuleTypeVersionParameters();
        for(ModuleTypeVersionParameter oldMTVParameter : oldMTVParameters.values()) {
            if (moduleTypeVersionParameters.containsKey(oldMTVParameter.getParameterName()))
                continue;
            moduleParameters.remove(oldMTVParameter.getParameterName()); // must exist
            updated = true;
        }
        
        // TODO handle new or modified internal datasets
        // TODO handle removed internal datasets
        // TODO handle new or modified internal parameters
        // TODO handle removed internal parameters
        
        if (updated) {
            setLastModifiedDate(new Date());
        }
        
        return updated;
    }
    
    // synchronize this module with its module type version
    public boolean refresh(Date date) {
        boolean updated = false;
        
        if (isUpToDate())
            return updated;
        
        // handle new or modified datasets
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            if (moduleDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName())) {
                ModuleDataset moduleDataset = moduleDatasets.get(moduleTypeVersionDataset.getPlaceholderName());
                if (moduleDataset.updateSettings())
                    updated = true;
            } else {
                ModuleDataset moduleDataset = new ModuleDataset();
                moduleDataset.setModule(this);
                moduleDataset.setPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
                moduleDataset.initSettings();
                addModuleDataset(moduleDataset);
                updated = true;
            }
        }
        
        // handle removed datasets
        Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = moduleTypeVersion.getModuleTypeVersionDatasets();
        List<String> removedPlaceholders = new ArrayList<String>();
        for(ModuleDataset moduleDataset : moduleDatasets.values()) {
            if (!moduleTypeVersionDatasets.containsKey(moduleDataset.getPlaceholderName()))
                removedPlaceholders.add(moduleDataset.getPlaceholderName());
        }
        for(String removedPlaceholder : removedPlaceholders) {
            moduleDatasets.remove(removedPlaceholder);
            updated = true;
        }
        
        // handle new or modified parameters
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersion.getModuleTypeVersionParameters().values()) {
            if (moduleParameters.containsKey(moduleTypeVersionParameter.getParameterName())) {
                ModuleParameter moduleParameter = moduleParameters.get(moduleTypeVersionParameter.getParameterName());
                if (moduleParameter.updateSettings())
                    updated = true;
            } else {
                ModuleParameter moduleParameter = new ModuleParameter();
                moduleParameter.setModule(this);
                moduleParameter.setParameterName(moduleTypeVersionParameter.getParameterName());
                moduleParameter.initSettings();
                addModuleParameter(moduleParameter);
                updated = true;
            }
        }
        
        // handle removed parameters
        Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters = moduleTypeVersion.getModuleTypeVersionParameters();
        List<String> removedParameters = new ArrayList<String>();
        for(ModuleParameter moduleParameter : moduleParameters.values()) {
            if (!moduleTypeVersionParameters.containsKey(moduleParameter.getParameterName()))
                removedParameters.add(moduleParameter.getParameterName());
        }
        for(String removedParameter : removedParameters) {
            moduleParameters.remove(removedParameter);
            updated = true;
        }
        
        if (updated) {
            setLastModifiedDate(date);
        }
        
        return updated;
    }
    
    public boolean isComposite() {
        return moduleTypeVersion.isComposite();
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
        moduleDataset.setModule(this);
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
        moduleParameter.setModule(this);
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

    // moduleInternalDatasets

    public Map<String, ModuleInternalDataset> computeInternalDatasets() {
        return moduleTypeVersion.computeInternalDatasets(this);
    }

    public Map<String, ModuleInternalDataset> getModuleInternalDatasets() {
        return this.moduleInternalDatasets;
    }

    public void setModuleInternalDatasets(Map<String, ModuleInternalDataset> moduleInternalDatasets) {
        this.moduleInternalDatasets = moduleInternalDatasets;
    }

    public void addModuleInternalDataset(ModuleInternalDataset moduleInternalDataset) {
        moduleInternalDataset.setCompositeModule(this);
        this.moduleInternalDatasets.put(moduleInternalDataset.getPlaceholderPath(), moduleInternalDataset);
    }

    public void removeModuleInternalDataset(ModuleInternalDataset moduleInternalDataset) {
        removeModuleInternalDataset(moduleInternalDataset.getPlaceholderPath());
    }

    public void removeModuleInternalDataset(String placeholderPath) {
        this.moduleInternalDatasets.remove(placeholderPath);
    }

    public void clearModuleInternalDatasets() {
        this.moduleInternalDatasets.clear();
    }

    // moduleInternalParameters

    public Map<String, ModuleInternalParameter> computeInternalParameters() {
        return moduleTypeVersion.computeInternalParameters(this);
    }

    public Map<String, ModuleInternalParameter> getModuleInternalParameters() {
        return this.moduleInternalParameters;
    }

    public void setModuleInternalParameters(Map<String, ModuleInternalParameter> moduleInternalParameters) {
        this.moduleInternalParameters = moduleInternalParameters;
    }

    public void addModuleInternalParameter(ModuleInternalParameter moduleInternalParameter) {
        moduleInternalParameter.setCompositeModule(this);
        this.moduleInternalParameters.put(moduleInternalParameter.getParameterPath(), moduleInternalParameter);
    }

    public void removeModuleInternalParameter(ModuleInternalParameter moduleInternalParameter) {
        removeModuleInternalParameter(moduleInternalParameter.getParameterPath());
    }

    public void removeModuleInternalParameter(String placeholderPath) {
        this.moduleInternalParameters.remove(placeholderPath);
    }

    public void clearModuleInternalParameters() {
        this.moduleInternalParameters.clear();
    }

    // moduleHistory

    public List<History> getModuleHistory() {
        return moduleHistory;
    }

    public void setModuleHistory(List<History> moduleHistory) {
        this.moduleHistory = moduleHistory;
    }

    public void addModuleHistory(History history) {
        history.setModule(this);
        history.setRunId(this.moduleHistory.size() + 1);
        this.moduleHistory.add(history);
    }

    public void clearModuleHistory() {
        this.moduleHistory.clear();
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
