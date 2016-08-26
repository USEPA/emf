package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ModuleTypeVersion implements Serializable {

    private int id;

    private ModuleType moduleType;

    private int version;

    private String name;

    private String description;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int baseVersion;

    private String algorithm;

    private boolean isFinal;

    private Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets;

    private Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters;

    private List<ModuleTypeVersionRevision> moduleTypeVersionRevisions;

    public ModuleTypeVersion()
    {
        moduleTypeVersionDatasets = new HashMap<String, ModuleTypeVersionDataset>();
        moduleTypeVersionParameters = new HashMap<String, ModuleTypeVersionParameter>();
        moduleTypeVersionRevisions = new ArrayList<ModuleTypeVersionRevision>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public int getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(int baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    // moduleTypeVersionDatasets

    public Map<String, ModuleTypeVersionDataset> getModuleTypeVersionDatasets() {
        return this.moduleTypeVersionDatasets;
    }

    public void setModuleTypeVersionDatasets(Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets) {
        this.moduleTypeVersionDatasets = moduleTypeVersionDatasets;
    }

    public void addModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        this.moduleTypeVersionDatasets.put(moduleTypeVersionDataset.getPlaceholderName(), moduleTypeVersionDataset);
    }

    public void removeModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        removeModuleTypeVersionDataset(moduleTypeVersionDataset.getPlaceholderName());
    }

    public void removeModuleTypeVersionDataset(String placeholderName) {
        this.moduleTypeVersionDatasets.remove(placeholderName);
    }

    // moduleTypeVersionParameters

    public Map<String, ModuleTypeVersionParameter> getModuleTypeVersionParameters() {
        return this.moduleTypeVersionParameters;
    }

    public void setModuleTypeVersionParameters(Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters) {
        this.moduleTypeVersionParameters = moduleTypeVersionParameters;
    }

    public void addModuleTypeVersionParameter(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        this.moduleTypeVersionParameters.put(moduleTypeVersionParameter.getParameterName(), moduleTypeVersionParameter);
    }

    public void removeModuleTypeVersionParameter(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        removeModuleTypeVersionParameter(moduleTypeVersionParameter.getParameterName());
    }

    public void removeModuleTypeVersionParameter(String parameterName) {
        this.moduleTypeVersionParameters.remove(parameterName);
    }

    // moduleTypeVersionRevisions

    public List<ModuleTypeVersionRevision> getModuleTypeVersionRevisions() {
        return this.moduleTypeVersionRevisions;
    }

    public void setModuleTypeVersionRevisions(List<ModuleTypeVersionRevision> moduleTypeVersionRevisions) {
        this.moduleTypeVersionRevisions = moduleTypeVersionRevisions;
    }

    public void addModuleTypeVersionRevision(ModuleTypeVersionRevision moduleTypeVersionRevision) {
        moduleTypeVersionRevision.setRevision(this.moduleTypeVersionRevisions.size());
        this.moduleTypeVersionRevisions.add(moduleTypeVersionRevision);
    }

    // standard methods

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersion && ((ModuleTypeVersion) other).getId() == id);
    }

    public int compareTo(ModuleTypeVersion o) {
        return name.compareTo(o.getName());
    }
}
