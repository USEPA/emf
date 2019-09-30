package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;

public class ModuleTypeVersionDataset implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
    public static final int MAX_NAME_LEN = 58;

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String placeholderName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private DatasetType datasetType;

    private String description;

    private boolean isOptional;
    
    public void exportTypes(final Map<Integer, DatasetType> datasetTypeMap) throws EmfException {
        if (datasetTypeMap.containsKey(datasetType.getId())) {
            DatasetType existingDatasetType = datasetTypeMap.get(datasetType.getId());
            if (datasetType != existingDatasetType) { // must be the same object
                throw new EmfException("Internal error: duplicate DatasetType objects.");
            }
        }
        datasetTypeMap.put(datasetType.getId(), datasetType);
    }
    
    public static boolean matchesImportedColumn(String indent, final StringBuilder differences, Column localColumn, Column importedColumn) {
        boolean result = true;
        differences.setLength(0);
        
        // skip id
        
        if (!localColumn.getName().toLowerCase().equals(importedColumn.getName().toLowerCase())) { // should never happen
            differences.append(String.format("%sERROR: Local column name \"%s\" is different than the imported column name \"%s\".\n",
                                             indent, localColumn.getName(), importedColumn.getName()));
            result = false;
        }
        
        if (!localColumn.getDescription().equals(importedColumn.getDescription())) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local column \"%s\" description differs from the imported column \"%s\" description.\n",
                                             indent, localColumn.getName(), importedColumn.getName()));
            // result = false;
        }
        
        if (!localColumn.getSqlType().toLowerCase().equals(importedColumn.getSqlType().toLowerCase())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column SQL type \"%s\" differs from the imported column SQL type \"%s\".\n",
                                             indent, localColumn.getName(), localColumn.getSqlType(), importedColumn.getSqlType()));
            result = false;
        }
        
        if (!localColumn.getDefaultValue().toLowerCase().equals(importedColumn.getDefaultValue().toLowerCase())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column default value \"%s\" differs from the imported column default value \"%s\".\n",
                                             indent, localColumn.getName(), localColumn.getDefaultValue(), importedColumn.getDefaultValue()));
            result = false;
        }
        
        if (localColumn.isMandatory() != importedColumn.isMandatory()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column is %s while the imported column is %s.\n",
                                             indent, localColumn.getName(),
                                             localColumn.isMandatory() ? "mandatory" : "not mandatory",
                                             importedColumn.isMandatory() ? "mandatory" : "not mandatory"));
            result = false;
        }
        
        if (localColumn.getFixFormatStart() != importedColumn.getFixFormatStart()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column fix format start (%d) differs from the imported column fix format start (%d).\n",
                                             indent, localColumn.getName(), localColumn.getFixFormatStart(), importedColumn.getFixFormatStart()));
            result = false;
        }
        
        if (localColumn.getFixFormatEnd() != importedColumn.getFixFormatEnd()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column fix format end (%d) differs from the imported column fix format end (%d).\n",
                                             indent, localColumn.getName(), localColumn.getFixFormatEnd(), importedColumn.getFixFormatEnd()));
            result = false;
        }

        // skip formatter
        
        if (!localColumn.getFormatterClass().equals(importedColumn.getFormatterClass())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column formatter class \"%s\" differs from the imported column formatter class \"%s\".\n",
                                             indent, localColumn.getName(), localColumn.getFormatterClass(), importedColumn.getFormatterClass()));
            result = false;
        }
        
        if (localColumn.getWidth() != importedColumn.getWidth()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column width (%d) differs from the imported column width (%d).\n",
                                             indent, localColumn.getName(), localColumn.getWidth(), importedColumn.getWidth()));
            result = false;
        }

        if (localColumn.getSpaces() != importedColumn.getSpaces()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column spaces (%d) differs from the imported column spaces (%d).\n",
                                             indent, localColumn.getName(), localColumn.getSpaces(), importedColumn.getSpaces()));
            result = false;
        }

        if (!localColumn.getConstraints().equals(importedColumn.getConstraints())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" column constraints \"%s\" differs from the imported column constraints \"%s\".\n",
                                             indent, localColumn.getName(), localColumn.getConstraints(), importedColumn.getConstraints()));
            result = false;
        }
        
        return result;
    }
    
    public static boolean matchesImportedFileFormat(String indent, final StringBuilder differences, XFileFormat localFileFormat, XFileFormat importedFileFormat) {
        boolean result = true;
        differences.setLength(0);
        
        // skip id
        
        if (!localFileFormat.getName().equals(importedFileFormat.getName())) { // should never happen
            differences.append(String.format("%sERROR: Local file format name \"%s\" is different than the imported file format name \"%s\".\n",
                                             indent, localFileFormat.getName(), importedFileFormat.getName()));
            result = false;
        }
        
        if (!localFileFormat.getDescription().equals(importedFileFormat.getDescription())) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local file format \"%s\" description differs from the imported file format \"%s\" description.\n",
                                             indent, localFileFormat.getName(), importedFileFormat.getName()));
            // result = false;
        }

        if (!localFileFormat.getDelimiter().equals(importedFileFormat.getDelimiter())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local file format \"%s\" delimiter \"%s\" differs from the imported file format \"%s\" delimiter \"%s\".\n",
                                             indent, localFileFormat.getName(), localFileFormat.getDelimiter(),
                                             importedFileFormat.getName(), importedFileFormat.getDelimiter()));
            result = false;
        }

        if (!localFileFormat.isFixedFormat() == importedFileFormat.isFixedFormat()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local file format \"%s\" is %s while the imported file format \"%s\" is %s.\n",
                                             indent, localFileFormat.getName(), localFileFormat.isFixedFormat() ? "fixed" : "not fixed",
                                             importedFileFormat.getName(), importedFileFormat.isFixedFormat() ? "fixed" : "not fixed"));
            result = false;
        }

        Map<String, Column> localColumns = new HashMap<String, Column>();
        for (Column column : localFileFormat.getColumns()) {
            localColumns.put(column.getName().toLowerCase(), column);
        }
        
        Map<String, Column> importedColumns = new HashMap<String, Column>();
        for (Column column : importedFileFormat.getColumns()) {
            importedColumns.put(column.getName().toLowerCase(), column);
        }
        
        for (String columnName : localColumns.keySet()) {
            Column localColumn = localColumns.get(columnName); 
            if (importedColumns.containsKey(columnName)) {
                Column importedColumn = importedColumns.get(columnName);
                StringBuilder columnDifferences = new StringBuilder();
                if (matchesImportedColumn(indent + "    ", columnDifferences, localColumn, importedColumn)) {
                    if (columnDifferences.length() > 0) {
                        differences.append(String.format("%sINFO: Local \"%s\" column matches the imported column:\n%s\n",
                                                         indent, localColumn.getName(), columnDifferences.toString()));
                    }
                } else {
                    differences.append(String.format("%sERROR: Local \"%s\" column doesn't match the imported column:\n%s\n",
                                                     indent, localColumn.getName(), columnDifferences.toString()));
                    result = false;
                }
            } else {
                differences.append(String.format("%sERROR: Local file format \"%s\" has a \"%s\" column while the imported file format does not.\n",
                                                  indent, localFileFormat.getName(), localColumn.getName()));
                result = false;
            }
        }
        for (String columnName : importedColumns.keySet()) {
            Column importedColumn = importedColumns.get(columnName); 
            if (!localColumns.containsKey(columnName)) {
                differences.append(String.format("%sERROR: Imported file format \"%s\" has a \"%s\" column while the local file format does not.\n",
                                                  indent, importedFileFormat.getName(), importedColumn.getName()));
                result = false;
            }
        }
        
        if (!localFileFormat.getDateAdded().equals(importedFileFormat.getDateAdded())) { // should not happen but it's OK
            differences.append(String.format("%sWARNING: Local file format \"%s\" date added (%s) differs from imported file format date added (%s).\n",
                                             indent, localFileFormat.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(localFileFormat.getDateAdded()),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedFileFormat.getDateAdded())));
            // result = false;
        }

        if (!localFileFormat.getLastModifiedDate().equals(importedFileFormat.getLastModifiedDate())) { // should not happen but it's OK
            differences.append(String.format("%sWARNING: Local file format \"%s\" last modified date (%s) differs from imported file format last modified date (%s).\n",
                                             indent, localFileFormat.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(localFileFormat.getLastModifiedDate()),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedFileFormat.getLastModifiedDate())));
            // result = false;
        }

        // skip creator
        
        return result;
    }
    
    public static boolean matchesImportedDatasetType(String indent, final StringBuilder differences, DatasetType localDatasetType, DatasetType importedDatasetType) {
        boolean result = true;
        differences.setLength(0);
        
        if (localDatasetType == importedDatasetType)
            return result;

        // skip id

        if (!localDatasetType.getName().equals(importedDatasetType.getName())) { // should never happen
            differences.append(String.format("%sERROR: Local dataset type name \"%s\" is different than the imported dataset type name \"%s\".\n",
                                             indent, localDatasetType.getName(), importedDatasetType.getName()));
            result = false;
        }
        
        if (!localDatasetType.getDescription().equals(importedDatasetType.getDescription())) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local dataset type \"%s\" description differs from the imported dataset type \"%s\" description.\n",
                                             indent, localDatasetType.getName(), importedDatasetType.getName()));
            // result = false;
        }

        if (localDatasetType.getMinFiles() != importedDatasetType.getMinFiles()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" min files (%d) differs from the imported dataset type \"%s\" min files (%d).\n",
                                             indent, localDatasetType.getName(), localDatasetType.getMinFiles(),
                                             importedDatasetType.getName(), importedDatasetType.getMinFiles()));
            result = false;
        }
        
        if (localDatasetType.getMaxFiles() != importedDatasetType.getMaxFiles()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" max files (%d) differs from the imported dataset type \"%s\" max files (%d).\n",
                                             indent, localDatasetType.getName(), localDatasetType.getMaxFiles(),
                                             importedDatasetType.getName(), importedDatasetType.getMaxFiles()));
            result = false;
        }
        
        if (localDatasetType.isExternal() != importedDatasetType.isExternal()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" is %s while the imported dataset type \"%s\" is %s.\n",
                                             indent, localDatasetType.getName(), localDatasetType.isExternal() ? "external" : "not external",
                                             importedDatasetType.getName(), importedDatasetType.isExternal() ? "external" : "not external"));
            result = false;
        }
        
        if (localDatasetType.getTablePerDataset() != importedDatasetType.getTablePerDataset()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" tables per dataset (%d) differs from the imported dataset type \"%s\" tables per dataset (%d).\n",
                                             indent, localDatasetType.getName(), localDatasetType.getTablePerDataset(),
                                             importedDatasetType.getName(), importedDatasetType.getTablePerDataset()));
            result = false;
        }
        
        if (!localDatasetType.getDefaultSortOrder().equals(importedDatasetType.getDefaultSortOrder())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" default sort order (%s) differs from the imported dataset type \"%s\" default sort order (%s).\n",
                                             indent, localDatasetType.getName(), localDatasetType.getDefaultSortOrder(),
                                             importedDatasetType.getName(), importedDatasetType.getDefaultSortOrder()));
            result = false;
        }

        if (!localDatasetType.getImporterClassName().equals(importedDatasetType.getImporterClassName())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" importer class name \"%s\" differs from the imported dataset type \"%s\" importer class name \"%s\".\n",
                                             indent, localDatasetType.getName(), localDatasetType.getImporterClassName(),
                                             importedDatasetType.getName(), importedDatasetType.getImporterClassName()));
            result = false;
        }

        if (!localDatasetType.getExporterClassName().equals(importedDatasetType.getExporterClassName())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local dataset type \"%s\" exporter class name \"%s\" differs from the imported dataset type \"%s\" exporter class name \"%s\".\n",
                                             indent, localDatasetType.getName(), localDatasetType.getExporterClassName(),
                                             importedDatasetType.getName(), importedDatasetType.getExporterClassName()));
            result = false;
        }

        // skip lock

        XFileFormat localFileFormat = localDatasetType.getFileFormat();
        XFileFormat importedFileFormat = importedDatasetType.getFileFormat();
        
        if ((localFileFormat == null) != (importedFileFormat == null)) {
            differences.append(String.format("%sERROR: The local dataset type \"%s\" %s a file format while the imported file format %s a file format.\n",
                                             indent, localDatasetType.getName(),
                                             (localFileFormat == null) ? "does not have" : "has",
                                             (importedFileFormat == null) ? "does not have" : "has"));
            result = false;
        }

        if ((localFileFormat != null) && (importedFileFormat != null)) {
            StringBuilder ffDifferences = new StringBuilder();
            if (matchesImportedFileFormat(indent + "    ", ffDifferences, localDatasetType.getFileFormat(), importedDatasetType.getFileFormat())) {
                if (ffDifferences.length() > 0) {
                    differences.append(String.format("%sINFO: Local dataset type \"%s\" file format matches the imported file format:\n%s\n",
                                                     indent, localDatasetType.getName(), ffDifferences.toString()));
                }
            } else {
                differences.append(String.format("%sERROR: Local dataset type \"%s\" file format doesn't match the imported file format:\n%s\n",
                                                 indent, localDatasetType.getName(), ffDifferences.toString()));
                result = false;
            }
        }

        // skip keyValsList

        // skip qaStepTemplates
        
        if (localDatasetType.getCreationDate() != null &&
            !localDatasetType.getCreationDate().equals(importedDatasetType.getCreationDate())) { // should not happen but it's OK
            differences.append(String.format("%sWARNING: Local dataset type \"%s\" creation date (%s) differs from imported dataset type creation date (%s).\n",
                                             indent, localDatasetType.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(localDatasetType.getCreationDate()),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedDatasetType.getCreationDate())));
            // result = false;
        }

        if (localDatasetType.getLastModifiedDate() != null &&
            !localDatasetType.getLastModifiedDate().equals(importedDatasetType.getLastModifiedDate())) { // should not happen but it's OK
            differences.append(String.format("%sWARNING: Local dataset type \"%s\" last modified date (%s) differs from imported dataset type last modified date (%s).\n",
                                             indent, localDatasetType.getName(),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(localDatasetType.getLastModifiedDate()),
                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importedDatasetType.getLastModifiedDate())));
            // result = false;
        }

        // skip creator;

        return result;
    }
    
    public boolean matchesImportedModuleTypeVersionDataset(String indent, final StringBuilder differences, ModuleTypeVersionDataset importedModuleTypeVersionDataset) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedModuleTypeVersionDataset)
            return result;

        String fullName = moduleTypeVersion.fullNameSDS("module type \"%s\" version %d \"%s\"");
        String importedFullName = importedModuleTypeVersionDataset.getModuleTypeVersion().fullNameSDS("module type \"%s\" version %d \"%s\"");

        // skipping id;

        // skipping moduleTypeVersion
        
        if (!placeholderName.equals(importedModuleTypeVersionDataset.getPlaceholderName())) { // should never happen
            differences.append(String.format("%sERROR: Local %s dataset placeholder name \"%s\" is different than imported %s dataset placeholder name \"%s\".\n",
                                             indent, fullName, placeholderName, importedFullName, importedModuleTypeVersionDataset.getPlaceholderName()));
            result = false;
        }
        
        if (!mode.equals(importedModuleTypeVersionDataset.getMode())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local %s dataset placeholder \"%s\" mode (%s) is different than imported %s dataset placeholder \"%s\" mode (%s).\n",
                                             indent, fullName, placeholderName, mode, importedFullName,
                                             importedModuleTypeVersionDataset.getPlaceholderName(), importedModuleTypeVersionDataset.getMode()));
            result = false;
        }
        
        StringBuilder dtDifferences = new StringBuilder();
        if (matchesImportedDatasetType(indent + "    ", dtDifferences, datasetType, importedModuleTypeVersionDataset.getDatasetType())) {
            if (dtDifferences.length() > 0) {
                differences.append(String.format("%sINFO: Local %s placeholder \"%s\" dataset type \"%s\" matches the dataset type for the corresponding imported module type version dataset placeholder:\n%s\n",
                                                 indent, fullName, placeholderName, datasetType.getName(), dtDifferences.toString()));
            }
        } else {
            differences.append(String.format("%sERROR: Local %s placeholder \"%s\" dataset type \"%s\" does not match the dataset type for the corresponding imported module type version dataset placeholder:\n%s\n",
                                             indent, fullName, placeholderName, datasetType.getName(), dtDifferences.toString()));
            result = false;
        }
        
        if ((description == null) != (importedModuleTypeVersionDataset.getDescription() == null) ||
           ((description != null) && !description.equals(importedModuleTypeVersionDataset.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local %s placeholder \"%s\" description differs from the corresponding imported module type version dataset placeholder description.\n",
                                             indent, fullName, placeholderName));
            // result = false;
        }

        if (isOptional != importedModuleTypeVersionDataset.getIsOptional()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local %s placeholder \"%s\" is %s while the imported %s placeholder \"%s\" is %s.\n",
                                             indent, fullName, placeholderName, isOptional ? "optional" : "not optional",
                                             importedFullName, importedModuleTypeVersionDataset.getPlaceholderName(),
                                             importedModuleTypeVersionDataset.getIsOptional() ? "optional" : "not optional"));
            result = false;
        }

        return result;
    }
    
    public void replaceDatasetType(DatasetType importedDatasetType, DatasetType localDatasetType) {
        if (datasetType == importedDatasetType) {
            datasetType = localDatasetType;
        }
    }

    public void prepareForExport() {
        id = 0;
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
