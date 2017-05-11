package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
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

    private Set<Tag> tags;

    public Module() {
        moduleDatasets = new HashMap<String, ModuleDataset>();
        moduleParameters = new HashMap<String, ModuleParameter>();
        moduleInternalDatasets = new HashMap<String, ModuleInternalDataset>();
        moduleInternalParameters = new HashMap<String, ModuleInternalParameter>();
        moduleHistory = new ArrayList<History>();
        tags = new HashSet<Tag>();
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

        if (!moduleTypeVersion.isValid(error))
            return false;
        
        Map<Integer, ModuleDataset> inputDatasets = new HashMap<Integer, ModuleDataset>();
        Map<Integer, ModuleDataset> outputDatasets = new HashMap<Integer, ModuleDataset>();
        for(ModuleDataset moduleDataset : moduleDatasets.values()) {
            if (!moduleDataset.isValid(error))
                return false;
            if (!moduleDataset.getModuleTypeVersionDataset().isModeOUT()) {
                if (outputDatasets.containsKey(moduleDataset.getDatasetId())) {
                    ModuleDataset outputModuleDataset = outputDatasets.get(moduleDataset.getDatasetId());
                    error.append(String.format("The %s output placeholder is replacing the dataset used by the %s input placeholder.",
                            outputModuleDataset.getPlaceholderName(), moduleDataset.getPlaceholderName()));
                    return false;
                }
                inputDatasets.put(moduleDataset.getDatasetId(), moduleDataset);
            } else if (moduleDataset.getOutputMethod().equals(ModuleDataset.REPLACE)) {
                if (outputDatasets.containsKey(moduleDataset.getDatasetId())) {
                    ModuleDataset outputModuleDataset = outputDatasets.get(moduleDataset.getDatasetId());
                    error.append(String.format("The %s and %s output placeholders are replacing the same dataset.",
                            outputModuleDataset.getPlaceholderName(), moduleDataset.getPlaceholderName()));
                    return false;
                }
                if (inputDatasets.containsKey(moduleDataset.getDatasetId())) {
                    ModuleDataset inputModuleDataset = inputDatasets.get(moduleDataset.getDatasetId());
                    error.append(String.format("The %s output placeholder is replacing the dataset used by the %s input placeholder.",
                            moduleDataset.getPlaceholderName(), inputModuleDataset.getPlaceholderName()));
                    return false;
                }
                outputDatasets.put(moduleDataset.getDatasetId(), moduleDataset);
            }
        }
        inputDatasets.clear();
        outputDatasets.clear();
        
        for(ModuleParameter moduleParameter : moduleParameters.values()) {
            if (!moduleParameter.isValid(error))
                return false;
        }
        
        for(ModuleInternalDataset moduleInternalDataset : moduleInternalDatasets.values()) {
            if (!moduleInternalDataset.isValid(error))
                return false;
        }

        for(ModuleInternalParameter moduleInternalParameter : moduleInternalParameters.values()) {
            if (!moduleInternalParameter.isValid(error))
                return false;
        }
        
        return true;
    }

    // checks if this module is up-to-date with respect to its module type version
    public boolean isUpToDate() {
        return lastModifiedDate.after(moduleTypeVersion.getLastModifiedDate());
    }

    // synchronize this module with its module type version or submodule type version
    // using the old module type version in order to detect what has changed
    public boolean update(ModuleTypeVersion oldMTV) {
        boolean updated = false;
        
        // is it the same module type version or is it a submodule?
        if (moduleTypeVersion.getId() == oldMTV.getId()) {
            // handle new or modified datasets
            Map<String, ModuleTypeVersionDataset> oldMTVDatasets = oldMTV.getModuleTypeVersionDatasets();
            for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
                if (oldMTVDatasets.containsKey(moduleTypeVersionDataset.getPlaceholderName())) {
                    ModuleTypeVersionDataset oldMTVDataset = oldMTVDatasets.get(moduleTypeVersionDataset.getPlaceholderName());
                    if (oldMTVDataset.getDatasetType().equals(moduleTypeVersionDataset.getDatasetType()) &&
                        oldMTVDataset.getMode().equals(moduleTypeVersionDataset.getMode()))
                        continue;
                    ModuleDataset moduleDataset = moduleDatasets.get(moduleTypeVersionDataset.getPlaceholderName()); // must exist
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
                    if (oldMTVParameter.getSqlParameterType().equals(moduleTypeVersionParameter.getSqlParameterType()) &&
                        oldMTVParameter.getMode().equals(moduleTypeVersionParameter.getMode()))
                        continue;
                    ModuleParameter moduleParameter = moduleParameters.get(moduleTypeVersionParameter.getParameterName()); // must exist
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
        }
        
        // update internal datasets
        Map<String, ModuleInternalDataset> newModuleInternalDatasets = computeInternalDatasets();
        Set<String> addedPlaceholderPaths = diff(newModuleInternalDatasets.keySet(), moduleInternalDatasets.keySet());
        for(String placeholderPath : addedPlaceholderPaths) {
            addModuleInternalDataset(newModuleInternalDatasets.get(placeholderPath));
            updated = true;
        }
        Set<String> removedPlaceholderPaths = diff(moduleInternalDatasets.keySet(), newModuleInternalDatasets.keySet());
        for(String placeholderPath : removedPlaceholderPaths) {
            removeModuleInternalDataset(placeholderPath);
            updated = true;
        }
        
        // update internal parameters
        Map<String, ModuleInternalParameter> newModuleInternalParameters = computeInternalParameters();
        Set<String> addedParameterPaths = diff(newModuleInternalParameters.keySet(), moduleInternalParameters.keySet());
        for(String parameterPath : addedParameterPaths) {
            addModuleInternalParameter(newModuleInternalParameters.get(parameterPath));
            updated = true;
        }
        Set<String> removedParameterPaths = diff(moduleInternalParameters.keySet(), newModuleInternalParameters.keySet());
        for(String parameterPath : removedParameterPaths) {
            removeModuleInternalParameter(parameterPath);
            updated = true;
        }
        
        if (updated) {
            setLastModifiedDate(new Date());
        }
        
        return updated;
    }
    
    // computes (left - right)
    private Set<String> diff(final Set<String> left, final Set<String> right) {
        Set<String> result = new HashSet<String>();
        for(String item : left) {
            if (!right.contains(item))
                result.add(item);
        }
        return result;
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

    public Map<String, String> getCompData(ModuleService moduleService, DataService dataService) {
        Map<String, String> compData = new HashMap<String, String>();
        
        String lockOwner = getLockOwner();
        String safeLockOwner = (lockOwner == null) ? "" : lockOwner;
        
        Date lockDate = getLockDate();
        String safeLockDate = (lockDate == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(lockDate);
        
        compData.put("Summary / Module Type", moduleTypeVersion.getModuleType().getName());
        compData.put("Summary / Module Type Version", moduleTypeVersion.versionName());
        compData.put("Summary / Module Name", name);
        compData.put("Summary / Description", description);
        compData.put("Summary / Creator", creator.getName());
        compData.put("Summary / Creation Date", CustomDateFormat.format_MM_DD_YYYY_HH_mm(creationDate));
        compData.put("Summary / Last Modified", CustomDateFormat.format_MM_DD_YYYY_HH_mm(lastModifiedDate));
        compData.put("Summary / Lock Owner", safeLockOwner);
        compData.put("Summary / Lock Date", safeLockDate);
        compData.put("Summary / Is Final", getIsFinal() ? "Yes" : "No");

        for(ModuleDataset moduleDataset : moduleDatasets.values()) {
            ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
            String mode = moduleTypeVersionDataset.getMode();
            String outputMethod = moduleDataset.getOutputMethod();
            String modeMethod = mode;
            if (mode.equals(ModuleTypeVersionDataset.IN) || mode.equals(ModuleTypeVersionDataset.INOUT)) {
                modeMethod = mode;
            } else if (outputMethod.equals(ModuleDataset.NEW)) {
                modeMethod = "OUT NEW";
            } else if (outputMethod.equals(ModuleDataset.REPLACE)) {
                modeMethod = "OUT REPLACE";
            }
            EmfDataset emfDataset = null;
            try {
                emfDataset = moduleService.getEmfDatasetForModuleDataset(moduleDataset.getId(), moduleDataset.getDatasetId(), moduleDataset.getDatasetNamePattern());
            } catch (EmfException e) {
                e.printStackTrace();
            }
            String datasetName = (emfDataset == null) ? "" : emfDataset.getName();
            String datasetExists = (emfDataset == null) ? "No" : "Yes"; // TODO check version also
            String datasetVersion = (moduleDataset.getVersion() == null) ? "" : (moduleDataset.getVersion() + "");
            String tabName = "Datasets / " + moduleDataset.getPlaceholderName() + " / ";
            compData.put(tabName + "Mode", modeMethod);
            compData.put(tabName + "Dataset Type", moduleTypeVersionDataset.getDatasetType().getName());
            compData.put(tabName + "Dataset Name Pattern", moduleDataset.getDatasetNamePattern());
            compData.put(tabName + "Dataset Name", datasetName);
            compData.put(tabName + "Dataset Version", datasetVersion);
            compData.put(tabName + "Dataset Exists", datasetExists);
            compData.put(tabName + "Description", moduleTypeVersionDataset.getDescription());
        }

        for(ModuleParameter moduleParameter : moduleParameters.values()) {
            History lastHistory = moduleParameter.getModule().lastHistory();
            HistoryParameter historyParameter = null;
            if (lastHistory != null) {
                if (History.SUCCESS.equals(lastHistory.getResult()) && lastHistory.getHistoryParameters().containsKey(moduleParameter.getParameterName())) {
                    historyParameter = lastHistory.getHistoryParameters().get(moduleParameter.getParameterName());
                }
            }
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
            String mode = moduleTypeVersionParameter.getMode();
            String inValue = mode.equals(ModuleTypeVersionParameter.OUT) ? "N/A" : moduleParameter.getValue();
            String outValue = mode.equals(ModuleTypeVersionParameter.IN) ? "N/A" : ((historyParameter == null) ? "N/A" : historyParameter.getValue());
            String tabName = "Parameters / " + moduleParameter.getParameterName() + " / ";
            compData.put(tabName + "Mode", mode);
            compData.put(tabName + "SQL Type", moduleTypeVersionParameter.getSqlParameterType());
            compData.put(tabName + "Input Value", inValue);
            compData.put(tabName + "Output Value", outValue);
            compData.put(tabName + "Description", moduleTypeVersionParameter.getDescription());
        }

        for(ModuleInternalDataset moduleInternalDataset : moduleInternalDatasets.values()) {
            EmfDataset emfDataset = moduleInternalDataset.getEmfDataset(dataService);
            String datasetName = (emfDataset == null) ? "" : emfDataset.getName();
            String datasetExists = (emfDataset == null) ? "No" : "Yes"; // TODO check version also
            String tabName = "Internal Datasets / " + moduleInternalDataset.getPlaceholderPathNames() + " / ";
            compData.put(tabName + "Keep", moduleInternalDataset.getKeep() ? "Yes" : "No");
            compData.put(tabName + "Dataset Type", moduleInternalDataset.getModuleTypeVersionDataset().getDatasetType().getName());
            compData.put(tabName + "Dataset Name Pattern", moduleInternalDataset.getDatasetNamePattern());
            compData.put(tabName + "Dataset Name", datasetName);
            compData.put(tabName + "Dataset Version", "0");
            compData.put(tabName + "Dataset Exists", datasetExists);
        }

        for(ModuleInternalParameter moduleInternalParameter : moduleInternalParameters.values()) {
            String parameterPath = moduleInternalParameter.getParameterPath();
            String parameterPathNames = moduleInternalParameter.getParameterPathNames();
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleInternalParameter.getModuleTypeVersionParameter();
            HistoryInternalParameter historyInternalParameter = null;
            History lastHistory = moduleInternalParameter.getCompositeModule().lastHistory();
            if (lastHistory != null) {
                Map<String, HistoryInternalParameter> historyInternalParameters = lastHistory.getHistoryInternalParameters();
                if (History.SUCCESS.equals(lastHistory.getResult()) && historyInternalParameters.containsKey(parameterPath)) {
                    historyInternalParameter = historyInternalParameters.get(parameterPath);
                }
            }
            String outValue = (historyInternalParameter == null) ? "N/A" : historyInternalParameter.getValue();
            String tabName = "Internal Parameters / " + parameterPathNames + " / ";
            compData.put(tabName + "Keep", moduleInternalParameter.getKeep() ? "Yes" : "No");
            compData.put(tabName + "SQL Type", moduleTypeVersionParameter.getSqlParameterType());
            compData.put(tabName + "Output Value", outValue);
        }
        
        return compData;
    }

    public boolean isComposite() {
        return (moduleTypeVersion != null) && moduleTypeVersion.isComposite();
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

    public Mutex getLock() {
        return lock;
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

    public History lastHistory() {
        if (moduleHistory.size() == 0)
            return null;
        return moduleHistory.get(moduleHistory.size() - 1);
    }

    // tags

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void clearTags() {
        this.tags.clear();
    }

    public String getTagsText() {
        StringBuilder tagsText = new StringBuilder();
        for(Tag tag : tags) {
            if (tagsText.length() > 0)
                tagsText.append(", ");
            tagsText.append(tag.name);
        }
        return tagsText.toString();
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
