package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.security.User;

public class ModuleTypeVersionDataset implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
    // By default, in PostgreSQL, NAMEDATALEN is 64 so the maximum identifier length is 63 bytes.
    public static final int MAX_NAME_LEN = 63; // NAMEDATALEN-1

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String placeholderName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private DatasetType datasetType;

    private String description;

    private boolean isOptional;
    
    public void prepareForImport(final StringBuilder changeLog, User user) {
        if (id == 0)
            return;
        id = 0;
        // TODO dataset type
    }
    
    public ModuleTypeVersionDataset deepCopy() {
        ModuleTypeVersionDataset newModuleTypeVersionDataset = new ModuleTypeVersionDataset();
        newModuleTypeVersionDataset.setModuleTypeVersion(moduleTypeVersion);
        newModuleTypeVersionDataset.setPlaceholderName(placeholderName);
        newModuleTypeVersionDataset.setMode(mode);
        newModuleTypeVersionDataset.setDatasetType(datasetType);
        newModuleTypeVersionDataset.setDescription(description);
        newModuleTypeVersionDataset.setIsOptional(isOptional);
        return newModuleTypeVersionDataset;
    }
    
    // Technically, the placeholder names do not need to follow the PostgreSQL naming
    // conventions but we do that for uniformity.
    public static boolean isValidPlaceholderName(String name, final StringBuilder error) {
        error.setLength(0);
        name = name.trim();
        if (name.length() == 0) {
            error.append("Placeholder name cannot be empty.");
            return false;
        }
        if (name.length() > MAX_NAME_LEN) {
            error.append(String.format("Placeholder name '%s' is longer than %d characters.", name, MAX_NAME_LEN));
            return false;
        }
        Matcher matcher = Pattern.compile("[^a-zA-Z0-9_]", Pattern.CASE_INSENSITIVE).matcher(name);
        if (matcher.find()) {
            error.append(String.format("Placeholder name '%s' contains illegal characters.", name));
            return false;
        }
        if (name.charAt(0) != '_' && !Character.isLetter(name.charAt(0))) {
            error.append(String.format("Placeholder name '%s' must begin with a letter or _ (underscore).", name));
            return false;
        }
        return true;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (!isValidPlaceholderName(placeholderName, error)) return false;
        if (datasetType == null) {
            error.append(String.format("Dataset type for placeholder '%s' has not been set.", placeholderName));
            return false;
        }
        StringBuilder datasetTypeError = new StringBuilder();
        if (!isValidDatasetType(datasetTypeError, datasetType)) {
            error.append("[Placeholder '" + placeholderName + "'] ");
            error.append(datasetTypeError);
            return false;
        }
        return true;
    }
    
    public static boolean isValidDatasetType(DatasetType datasetType) {
        if (datasetType == null) {
            return false;
        }
        FileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat == null) {
            if (!datasetType.getName().equals(DatasetType.projectionPacket))
                return false;
        }
        if (datasetType.isExternal()) {
            return false;
        }
//        if (datasetType.getMinFiles() != 1 || datasetType.getMaxFiles() != 1) {
//            return false;
//        }
        if (datasetType.getTablePerDataset() != 1) {
            return false;
        }
        return true;
    }
    
    public static boolean isValidDatasetType(final StringBuilder error, DatasetType datasetType) {
        if (error != null) {
            error.setLength(0);
        }
        if (datasetType == null) {
            if (error != null) {
                error.append("Internal error: null dataset type.");
            }
            return false;
        }
        FileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat == null) {
            if (!datasetType.getName().equals(DatasetType.projectionPacket)) {
                if (error != null) {
                    error.append(String.format("The '%s' dataset type is not supported by the modules subsystem.",
                                               datasetType.getName()));
                }
                return false;
            }
        }
        if (datasetType.isExternal()) {
            if (error != null) {
                error.append(String.format("The '%s' dataset type is not supported by the modules subsystem because it's an external dataset type.",
                                           datasetType.getName()));
            }
            return false;
        }
//        if (datasetType.getMinFiles() != 1 || datasetType.getMaxFiles() != 1) {
//            if (error != null) {
//                error.append(String.format("The '%s' dataset type is not supported by the modules subsystem because it doesn't use exactly one file (min files = %d, max files = %d).",
//                                           datasetType.getName(), datasetType.getMinFiles(), datasetType.getMaxFiles()));
//            }
//            return false;
//        }
        if (datasetType.getTablePerDataset() != 1) {
            if (error != null) {
                error.append(String.format("The '%s' dataset type is not supported by the modules subsystem because it doesn't use exactly one table (tables per dataset = %d).",
                                           datasetType.getName(), datasetType.getTablePerDataset()));
            }
            return false;
        }
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isModeIN() {
        return this.mode.equals(IN);
    }

    public boolean isModeINOUT() {
        return this.mode.equals(INOUT);
    }

    public boolean isModeOUT() {
        return this.mode.equals(OUT);
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public String toString() {
        return getPlaceholderName();
    }

    public int hashCode() {
        return placeholderName.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionDataset && ((ModuleTypeVersionDataset) other).getPlaceholderName() == placeholderName);
    }

    public int compareTo(ModuleTypeVersionDataset o) {
        return placeholderName.compareTo(o.getPlaceholderName());
    }
}
