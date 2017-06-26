package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;

public class ModuleTypeVersionSubmodule implements Serializable {

    // By default, in PostgreSQL, NAMEDATALEN is 64 so the maximum identifier length is 63 bytes.
    public static final int MAX_NAME_LEN = 63; // NAMEDATALEN-1

    private int id;

    private ModuleTypeVersion compositeModuleTypeVersion;

    private String name;

    private ModuleTypeVersion moduleTypeVersion;

    private String description;

    public void exportTypes(final Map<Integer, DatasetType> datasetTypesMap, final Map<Integer, ModuleType> moduleTypesMap, final List<ModuleType> moduleTypesList, final Map<Integer, ModuleType> moduleTypesInProgress) throws EmfException {
        ModuleType moduleType = moduleTypeVersion.getModuleType();
        // detect recursive dependencies between module types here so we can produce a meaningful error message
        if (moduleTypesInProgress.containsKey(moduleType.getId())) {
            ModuleType compositeModuleType = compositeModuleTypeVersion.getModuleType();
            throw new EmfException("Internal error: recursive dependencies between module types: \"" + name + "\" and \"" + compositeModuleType.getName() + "\".");
        }
        moduleType.exportTypes(datasetTypesMap, moduleTypesMap, moduleTypesList, moduleTypesInProgress);
    }
    
    public boolean matchesImportedModuleTypeVersionSubmodule(String indent, final StringBuilder differences, ModuleTypeVersionSubmodule importedModuleTypeVersionSubmodule) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedModuleTypeVersionSubmodule)
            return result;

        // skipping id;

        // skipping compositeModuleTypeVersion
        
        if (!name.equals(importedModuleTypeVersionSubmodule.getName())) { // should never happen
            differences.append(String.format("%sERROR: Local \"%s\" submodule name differs from imported \"%s\" submodule name.\n",
                                             indent, name, importedModuleTypeVersionSubmodule.getName()));
            result = false;
        }

        StringBuilder mtvDifferences = new StringBuilder(0);
        if (moduleTypeVersion.matchesImportedModuleTypeVersion(indent + "    ", mtvDifferences, importedModuleTypeVersionSubmodule.getModuleTypeVersion())) {
            if (mtvDifferences.length() > 0) {
                differences.append(String.format("%sINFO: Local \"%s\" submodule module type version matches the imported \"%s\" submodule module type version:\n%s",
                                                 indent, name, importedModuleTypeVersionSubmodule.getName(), mtvDifferences.toString()));
            }
        } else {
            differences.append(String.format("%sERROR: Local \"%s\" submodule module type version differs from imported \"%s\" submodule module type version:\n%s",
                                             indent, name, importedModuleTypeVersionSubmodule.getName(), mtvDifferences.toString()));
            result = false;
        }
        
        if ((description == null) != (importedModuleTypeVersionSubmodule.getDescription() == null) ||
           ((description != null) && !description.equals(importedModuleTypeVersionSubmodule.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" submodule description differs from imported \"%s\" submodule description.\n",
                                             indent, name, importedModuleTypeVersionSubmodule.getName()));
            // result = false;
        }
        
        return result;
    }
    
    public void prepareForExport() {
        id = 0;
    }
    
    public void replaceModuleTypeVersion(ModuleTypeVersion importedModuleTypeVersion, ModuleTypeVersion localModuleTypeVersion) {
        if (moduleTypeVersion == importedModuleTypeVersion) {
            moduleTypeVersion = localModuleTypeVersion;
        }
    }

    public ModuleTypeVersionSubmodule deepCopy() {
        ModuleTypeVersionSubmodule newModuleTypeVersionSubmodule = new ModuleTypeVersionSubmodule();
        newModuleTypeVersionSubmodule.setCompositeModuleTypeVersion(compositeModuleTypeVersion);
        newModuleTypeVersionSubmodule.setName(name);
        newModuleTypeVersionSubmodule.setModuleTypeVersion(moduleTypeVersion);
        newModuleTypeVersionSubmodule.setDescription(description);
        return newModuleTypeVersionSubmodule;
    }
    
    public static boolean isValidName(String name, final StringBuilder error) {
        error.setLength(0);
        name = name.trim();
        if (name.length() == 0) {
            error.append("Submodule name cannot be empty.");
            return false;
        }
        if (name.length() > MAX_NAME_LEN) {
            error.append(String.format("Submodule name '%s' is longer than %d characters.", name, MAX_NAME_LEN));
            return false;
        }
        Matcher matcher = Pattern.compile("[^a-zA-Z0-9_ ]", Pattern.CASE_INSENSITIVE).matcher(name);
        if (matcher.find()) {
            error.append(String.format("Submodule name '%s' contains illegal characters.", name));
            return false;
        }
        if (name.charAt(0) != '_' && !Character.isLetter(name.charAt(0))) {
            error.append(String.format("Submodule name '%s' must begin with a letter or _ (underscore).", name));
            return false;
        }
        return true;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (!isValidName(name, error))
            return false;
        if (!moduleTypeVersion.isValid(error))
            return false;
        return true;
    }
    
    public void computeInternalDatasets(Map<String, ModuleInternalDataset> internalDatasets, String placeholderPath, String placeholderPathNames, Module compositeModule) {
        moduleTypeVersion.computeInternalDatasets(internalDatasets, placeholderPath, placeholderPathNames, this, compositeModule);        
    }

    public void computeInternalParameters(Map<String, ModuleInternalParameter> internalParameters, String parameterPath, String parameterPathNames, Module compositeModule) {
        moduleTypeVersion.computeInternalParameters(internalParameters, parameterPath, parameterPathNames, this, compositeModule);        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getCompositeModuleTypeVersion() {
        return compositeModuleTypeVersion;
    }

    public void setCompositeModuleTypeVersion(ModuleTypeVersion compositeModuleTypeVersion) {
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionSubmodule && ((ModuleTypeVersionSubmodule) other).getName() == name);
    }

    public int compareTo(ModuleTypeVersionSubmodule o) {
        return name.compareTo(o.getName());
    }
}
