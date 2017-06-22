package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ModuleType implements Serializable, Lockable, Comparable<ModuleType> {

    private int id;

    private String name;

    private String description;

    private Mutex lock;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int defaultVersion;

    private boolean isComposite;

    private Map<Integer, ModuleTypeVersion> moduleTypeVersions;

    private Set<Tag> tags;

    public ModuleType() {
        lock = new Mutex();
        isComposite = false;
        moduleTypeVersions = new HashMap<Integer, ModuleTypeVersion>();
        tags = new HashSet<Tag>();
    }

    public ModuleType(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public ModuleType(String name) {
        this();
        this.name = name;
    }

    public void exportTypes(final Map<Integer, DatasetType> datasetTypesMap, final Map<Integer, ModuleType> moduleTypesMap, final List<ModuleType> moduleTypesList, final Map<Integer, ModuleType> moduleTypesInProgress) throws EmfException {
        if (moduleTypesInProgress.containsKey(id)) {
            throw new EmfException("Internal error: recursive dependencies between module types: \"" + name + "\".");
        }
        moduleTypesInProgress.put(id,  this);
        
        for (ModuleTypeVersion moduleTypeVersion : moduleTypeVersions.values()) {
            moduleTypeVersion.exportTypes(datasetTypesMap, moduleTypesMap, moduleTypesList, moduleTypesInProgress);
        }
        
        if (!moduleTypesMap.containsKey(id)) {
            moduleTypesMap.put(id,  this);
            moduleTypesList.add(this);
        }
        moduleTypesInProgress.remove(id);
    }
    
    public boolean matchesImportedModuleType(String indent, final StringBuilder differences, ModuleType importedModuleType) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedModuleType)
            return result;

        // skipping id;

        if (!name.equals(importedModuleType.getName())) { // should never happen
            differences.append(String.format("%sERROR: Local module type name \"%s\" different than imported module type name \"%s\".\n",
                                             indent, name, importedModuleType.getName()));
            result = false;
        }

        if ((description == null) != (importedModuleType.getDescription() != null) ||
           ((description != null) && !description.equals(importedModuleType.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" module type description differs from imported module type description.\n",
                                             indent, name));
            // result = false;
        }
        
        // skip lock;

        if (!creationDate.equals(importedModuleType.getCreationDate())) { // should not happen but it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" module type creation date (%s) differs from imported module type creation date (%s).\n",
                                             indent, name, CustomDateFormat.format_MM_DD_YYYY_HH_mm(creationDate),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedModuleType.getCreationDate())));
            // result = false;
        }

        if (!lastModifiedDate.equals(importedModuleType.getLastModifiedDate())) { // should happen and it's OK
            differences.append(String.format("%sINFO: Local \"%s\" module type last modified date (%s) differs from imported module type last modified date (%s).\n",
                                             indent, name, CustomDateFormat.format_MM_DD_YYYY_HH_mm(lastModifiedDate),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedModuleType.getLastModifiedDate())));
            // result = false;
        }

        // skip creator

        if (defaultVersion != importedModuleType.getDefaultVersion()) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" module type default version (%d) differs from imported module type default version (%d).\n",
                                             indent, name, defaultVersion, importedModuleType.getDefaultVersion()));
            // result = false;
        }

        if (isComposite != importedModuleType.getIsComposite()) { // not allowed
            differences.append(String.format("%sERROR: Local module type \"%s\" is %s while the imported module type is %s.\n",
                                             indent, name, isComposite ? "composite" : "simple", importedModuleType.getIsComposite() ? "composite" : "simple"));
            result = false;
            return result; // stop comparison here, no need to compare the module type versions
        }

        Map<Integer, ModuleTypeVersion> importedModuleTypeVersions = importedModuleType.getModuleTypeVersions();
        
        for (Integer version : moduleTypeVersions.keySet()) {
            ModuleTypeVersion moduleTypeVersion = moduleTypeVersions.get(version);
            String fullName = moduleTypeVersion.fullNameSDS("module type \"%s\" version %d \"%s\"");
            if (importedModuleTypeVersions.containsKey(version)) {
                ModuleTypeVersion importedModuleTypeVersion = importedModuleTypeVersions.get(version);
                StringBuilder mtvDifferences = new StringBuilder();
                if (moduleTypeVersion.matchesImportedModuleTypeVersion(indent + "    ", mtvDifferences, importedModuleTypeVersion)) {
                    if (mtvDifferences.length() > 0) {
                        differences.append(String.format("%sINFO: Local %s matches the corresponding imported module type version:\n%s\n",
                                                         indent, fullName, mtvDifferences.toString()));
                    }
                    // result = false;
                } else {
                    differences.append(String.format("%sERROR: Local %s does not match the corresponding imported module type version:\n%s",
                                                     indent, fullName, mtvDifferences.toString()));
                    result = false;
                }
            } else { // could happen and it's OK (module type version was removed on the source server but it's still present on the local/destination server)
                differences.append(String.format("%sWARNING: Local %s missing from the imported module type.\n",
                                                 indent, fullName));
                // result = false;
            }
        }

        for (Integer version : importedModuleTypeVersions.keySet()) {
            ModuleTypeVersion importedModuleTypeVersion = importedModuleTypeVersions.get(version);
            String fullName = importedModuleTypeVersion.fullNameSDS("module type \"%s\" version %d \"%s\"");
            if (!moduleTypeVersions.containsKey(version)) { // could happen and we can't add the new module type version to the local module type at this moment
                differences.append(String.format("%sERROR: Imported %s does not exist in the local module type.\n",
                                                 indent, fullName));
                result = false;
            }
        }

        // skip tags
        
        return result;
    }

    public void prepareForExport(User user, String message) {
        if (this.id == 0)
            return;
        this.id = 0;
        this.creator = null;
        setLockDate(null);
        setLockOwner(null);
        description = (description == null) ? "" : (description + "\n");
        description += message;
        for (ModuleTypeVersion moduleTypeVersion : this.moduleTypeVersions.values()) {
            moduleTypeVersion.prepareForExport(user, message);
        }
        tags.clear();
    }

    public void prepareForImport(User user, String message) {
        if (this.creator == user)
            return;
        this.creator = user;
        description = (description == null) ? "" : (description + "\n");
        description += message;
        for (ModuleTypeVersion moduleTypeVersion : this.moduleTypeVersions.values()) {
            moduleTypeVersion.prepareForImport(user, message);
        }
    }
    
    public void replaceDatasetType(DatasetType importedDatasetType, DatasetType localDatasetType) {
        for (ModuleTypeVersion moduleTypeVersion : this.moduleTypeVersions.values()) {
            moduleTypeVersion.replaceDatasetType(importedDatasetType, localDatasetType);
        }
    }

    public void replaceModuleTypeVersion(ModuleTypeVersion importedModuleTypeVersion, ModuleTypeVersion localModuleTypeVersion) {
        for (ModuleTypeVersion moduleTypeVersion : this.moduleTypeVersions.values()) {
            moduleTypeVersion.replaceModuleTypeVersion(importedModuleTypeVersion, localModuleTypeVersion);
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int compareTo(ModuleType o) {
        return name.compareTo(o.getName());
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(int defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public boolean getIsComposite() {
        return isComposite;
    }

    public void setIsComposite(boolean isComposite) {
        this.isComposite = isComposite;
    }

    public boolean isComposite() {
        return getIsComposite();
    }
    
    public void setComposite(boolean isComposite) {
        setIsComposite(isComposite);
    }
    
    // moduleTypeVersions

    public Map<Integer, ModuleTypeVersion> getModuleTypeVersions() {
        return this.moduleTypeVersions;
    }

    /// may return null
    public ModuleTypeVersion getLastModuleTypeVersion() {
        ModuleTypeVersion lastModuleTypeVersion = null;
        for (ModuleTypeVersion moduleTypeVersion : this.moduleTypeVersions.values())
            if ((lastModuleTypeVersion == null) || (lastModuleTypeVersion.getVersion() < moduleTypeVersion.getVersion()))
                lastModuleTypeVersion = moduleTypeVersion;
        return lastModuleTypeVersion;
    }

    public void setModuleTypeVersions(Map<Integer, ModuleTypeVersion> moduleTypeVersions) {
        this.moduleTypeVersions = moduleTypeVersions;
    }

    public void addModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        moduleTypeVersion.setModuleType(this);
        ModuleTypeVersion lastModuleTypeVersion = getLastModuleTypeVersion();
        int newVersion = (lastModuleTypeVersion == null) ? 0 : (lastModuleTypeVersion.getVersion() + 1);
        moduleTypeVersion.setVersion(newVersion);
        this.moduleTypeVersions.put(moduleTypeVersion.getVersion(), moduleTypeVersion);
    }

    public void removeModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        removeModuleTypeVersion(moduleTypeVersion.getVersion());
    }

    public void removeModuleTypeVersion(Integer version) {
        this.moduleTypeVersions.remove(version);
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

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleType && ((ModuleType) other).getId() == id);
    }
}
