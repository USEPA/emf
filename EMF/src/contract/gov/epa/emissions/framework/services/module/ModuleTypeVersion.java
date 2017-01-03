package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;

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

    public ModuleTypeVersion deepCopy(User user) {
        ModuleTypeVersion newModuleTypeVersion = new ModuleTypeVersion();
        newModuleTypeVersion.setModuleType(moduleType);
        newModuleTypeVersion.setVersion(version); // same version for now
        newModuleTypeVersion.setName(name + " Copy"); // TODO search for unique new name
        newModuleTypeVersion.setDescription("Copy of " + name + ".\n" + description);
        Date date = new Date();
        newModuleTypeVersion.setCreationDate(date);
        newModuleTypeVersion.setLastModifiedDate(date);
        newModuleTypeVersion.setCreator(user);   
        newModuleTypeVersion.setBaseVersion(version);
        newModuleTypeVersion.setAlgorithm(algorithm);
        newModuleTypeVersion.setIsFinal(false);
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            ModuleTypeVersionDataset newModuleTypeVersionDataset = moduleTypeVersionDataset.deepCopy();
            newModuleTypeVersion.addModuleTypeVersionDataset(newModuleTypeVersionDataset);
        }
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            ModuleTypeVersionParameter newModuleTypeVersionParameter = moduleTypeVersionParameter.deepCopy();
            newModuleTypeVersion.addModuleTypeVersionParameter(newModuleTypeVersionParameter);
        }
        ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
        moduleTypeVersionRevision.setDescription("Copy of " + name);
        moduleTypeVersionRevision.setCreationDate(date);
        moduleTypeVersionRevision.setCreator(user);
        newModuleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);
        
        return newModuleTypeVersion;
    }

    public boolean isValidAlgorithm(final StringBuilder error) {
        error.setLength(0);
        
        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleRunnerTask
        Set<String> validPlaceholders = new HashSet<String>();
        validPlaceholders.add("${user.full_name}");
        validPlaceholders.add("${user.id}");
        validPlaceholders.add("${user.account_name}");
        validPlaceholders.add("${module.name}");
        validPlaceholders.add("${module.id}");
        validPlaceholders.add("${module.final}");
        validPlaceholders.add("${run.id}");
        validPlaceholders.add("${run.date}");
        validPlaceholders.add("${run.time}");
        
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            String placeholderName = moduleTypeVersionDataset.getPlaceholderName().toLowerCase();
            validPlaceholders.add("${" + placeholderName + "}"); // new syntax using views
            validPlaceholders.add("${" + placeholderName + ".dataset_name}");
            validPlaceholders.add("${" + placeholderName + ".dataset_id}");
            validPlaceholders.add("${" + placeholderName + ".version}");
            validPlaceholders.add("${" + placeholderName + ".table_name}");
            validPlaceholders.add("${" + placeholderName + ".view}");
            validPlaceholders.add("${" + placeholderName + ".mode}");
            if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
                validPlaceholders.add("${" + placeholderName + ".output_method}");
            }
        }
        
        // verify that all placeholders in the algorithm are valid
        String startPattern = "\\$\\{\\s*";
        String endPattern = "\\s*\\}";
        Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(algorithm);
        while (matcher.find()) {
            int start = matcher.start();
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized placeholder %s at location %d.", match, start));
                return false;
            }
        }
        
        validPlaceholders.clear();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            String parameter = moduleTypeVersionParameter.getParameterName().toLowerCase();
            validPlaceholders.add("#{" + parameter + "}");
            validPlaceholders.add("#{" + parameter + ".sql_type}");
            validPlaceholders.add("#{" + parameter + ".mode}");
            if (!moduleTypeVersionParameter.getMode().equals(ModuleTypeVersionParameter.OUT)) {
                validPlaceholders.add("#{" + parameter + ".input_value}");
            }
        }
        
        // verify that all parameters in the algorithm are valid
        startPattern = "\\#\\{\\s*";
        matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(algorithm);
        while (matcher.find()) {
            int start = matcher.start();
            String match = matcher.group().replaceAll("\\{\\s*", "{").replaceAll("\\s*\\.\\s*", ".").replaceAll("\\s*\\}", "}").toLowerCase();
            if (!validPlaceholders.contains(match)) {
                error.append(String.format("Unrecognized parameter placeholder %s at location %d.", match, start));
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);

        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersionDatasets.values()) {
            if (!moduleTypeVersionDataset.isValid(error)) return false;
        }

        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersionParameters.values()) {
            if (!moduleTypeVersionParameter.isValid(error)) return false;
        }
        
        if (!isValidAlgorithm(error)) return false;
        
        return true;
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

    public String versionAndName() {
        return version + " - " + name;
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
        moduleTypeVersionDataset.setModuleTypeVersion(this);
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
        moduleTypeVersionParameter.setModuleTypeVersion(this);
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
        moduleTypeVersionRevision.setModuleTypeVersion(this);
        moduleTypeVersionRevision.setRevision(this.moduleTypeVersionRevisions.size());
        this.moduleTypeVersionRevisions.add(moduleTypeVersionRevision);
    }

    public String revisionsReport() {
        StringBuilder revisionsReport = new StringBuilder();
        
        for (ModuleTypeVersionRevision moduleTypeVersionRevision : moduleTypeVersionRevisions) {
            String creationDate = (moduleTypeVersionRevision.getCreationDate() == null)
                                  ? "?"
                                  : CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleTypeVersionRevision.getCreationDate());
            
            String creator = (moduleTypeVersionRevision.getCreator() == null)
                             ? "?"
                             : moduleTypeVersionRevision.getCreator().getName();
            
            String record = String.format("Revision %d created on %s by %s\n%s\n\n",
                                          moduleTypeVersionRevision.getRevision(),
                                          creationDate, creator,
                                          moduleTypeVersionRevision.getDescription());
            
            revisionsReport.append(record);
        }
        
        return revisionsReport.toString();
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
