package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ModuleDataset implements Serializable {

    public static final String NEW = "NEW";
    public static final String REPLACE = "REPLACE";
    
    private int id;

    private Module module;

    private String placeholderName;

    private String outputMethod; // 'NEW', 'REPLACE'

    private Integer datasetId;

    private Integer version;

    private String datasetNamePattern;

    private Boolean overwriteExisting;

    public ModuleDataset deepCopy(Module newModule) {
        ModuleDataset newModuleDataset = new ModuleDataset();
        newModuleDataset.setModule(newModule);
        newModuleDataset.setPlaceholderName(placeholderName);
        newModuleDataset.setOutputMethod(outputMethod);
        newModuleDataset.setDatasetId(datasetId);
        newModuleDataset.setVersion(version);
        newModuleDataset.setDatasetNamePattern(datasetNamePattern);
        newModuleDataset.setOverwriteExisting(overwriteExisting);
        return newModuleDataset;
    }

    public static String replacePatternWithSpaces(String text, String pattern) {

        while (true) {
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text);
            if (matcher.find()) {
                String part1 = (matcher.start() > 0) ? text.substring(0, matcher.start()) : "";
                String part2 = matcher.group().replaceAll(".", " ");
                String part3 = text.substring(matcher.end()); 
                text = part1 + part2 + part3;
            }
            return text;
        }
    }
    
    public static boolean isValidDatasetNamePattern(String datasetNamePattern, final StringBuilder error) {
        error.setLength(0);

        // Important: keep the list of valid placeholders in sync with
        //            gov.epa.emissions.framework.services.module.ModuleTypeVersion
        
        String startPattern = "\\$\\{\\s*";
        String separatorPattern = "\\s*\\.\\s*";
        String endPattern = "\\s*\\}";
        
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "user"   + separatorPattern + "full_name"    + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "user"   + separatorPattern + "id"           + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "user"   + separatorPattern + "account_name" + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "module" + separatorPattern + "name"         + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "module" + separatorPattern + "id"           + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "module" + separatorPattern + "final"        + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "run"    + separatorPattern + "id"           + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "run"    + separatorPattern + "date"         + endPattern);
        datasetNamePattern = replacePatternWithSpaces(datasetNamePattern, startPattern + "run"    + separatorPattern + "time"         + endPattern);
        
        Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(datasetNamePattern);
        if (matcher.find()) {
            error.append(String.format("Unrecognized placeholder %s at position %d.", matcher.group(), matcher.start()));
            return false; 
        }
        
        matcher = Pattern.compile("[^A-Za-z0-9 ~!@#$%^&*\\(\\)_\\-+=\\[\\]|:;,.<>?/]", Pattern.CASE_INSENSITIVE).matcher(datasetNamePattern);
        if (matcher.find()) {
            error.append(String.format("Invalid character %s at position %d.", matcher.group(), matcher.start()));
            return false;
        }
        
        return true;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        ModuleTypeVersionDataset moduleTypeVersionDataset = getModuleTypeVersionDataset();
        if (!moduleTypeVersionDataset.isValid(error)) return false;
        String mode = moduleTypeVersionDataset.getMode();
        boolean needsDatasetNamePattern = mode.equals(ModuleTypeVersionDataset.OUT) && outputMethod.equals(NEW);
        boolean hasDatasetNamePattern = (datasetNamePattern != null) && (datasetNamePattern.trim().length() > 0);
        if (needsDatasetNamePattern) {
            if (!hasDatasetNamePattern) {
                error.append(String.format("The dataset name pattern for placeholder '%s' has not been set.", placeholderName));
                return false;
            } else if (!isValidDatasetNamePattern(datasetNamePattern, error)) {
                error.insert(0, String.format("The dataset name pattern for placeholder '%s' is invalid: ", placeholderName));
                return false;
            }
        }
        else if (datasetId == null) {
            error.append(String.format("The dataset for placeholder '%s' has not been set.", placeholderName));
            return false;
        }
        return true;
    }

    // compares the settings against the moduleTypeVersionDataset
    // if the settings don't match, initialize this object
    // returns true if this object was modified in any way
    public boolean updateSettings() {
        // TODO add mode and datasetType to ModuleDataset class and modules.modules_datasets table also
        //      in order to detect moduleTypeVersion changes more reliably
        ModuleDataset copy = deepCopy(module);
        ModuleTypeVersionDataset moduleTypeVersionDataset = getModuleTypeVersionDataset();
        if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
            if (outputMethod != null)
                return false;
        } else { // IN and INOUT
            // TODO check that the dataset with datasetId has the same DatasetType as specified in the ModuleTypeVerswionDataset
            if (outputMethod == null && datasetId != null && version != null && datasetNamePattern == null && overwriteExisting == null)
                return false;
        }
        
        initSettings();

        return getOutputMethod() != copy.getOutputMethod() ||
               getDatasetId() != copy.getDatasetId() ||
               getVersion() != copy.getVersion() ||
               getDatasetNamePattern() != copy.getDatasetNamePattern() ||
               getOverwriteExisting() != copy.getOverwriteExisting();
    }
    
    public void initSettings() {
        ModuleTypeVersionDataset moduleTypeVersionDataset = getModuleTypeVersionDataset();
        setOutputMethod(moduleTypeVersionDataset.isModeOUT() ? ModuleDataset.NEW : null);
        setDatasetNamePattern(null);
        setOverwriteExisting(null);
        setDatasetId(null);
        setVersion(null);
    }
    
    public boolean transferSettings(Module otherModule) {
        Map<String, ModuleDataset> otherModuleDatasets = otherModule.getModuleDatasets();
        if (otherModuleDatasets.containsKey(placeholderName)) {
            ModuleDataset otherModuleDataset = otherModuleDatasets.get(placeholderName);
            ModuleTypeVersionDataset otherModuleTypeVersionDataset = otherModuleDataset.getModuleTypeVersionDataset();
            if (otherModuleTypeVersionDataset.getMode().equals(getModuleTypeVersionDataset().getMode())) {
                setOutputMethod(otherModuleDataset.getOutputMethod());
                setDatasetNamePattern(otherModuleDataset.getDatasetNamePattern());
                setOverwriteExisting(otherModuleDataset.getOverwriteExisting());
                setDatasetId(otherModuleDataset.getDatasetId());
                setVersion(otherModuleDataset.getVersion());
                return true;
            }
        }
        return false;
    }
    
    public static boolean isSimpleDatasetName(String datasetNamePattern) {
        if (datasetNamePattern == null)
            return false;
        String startPattern = "\\$\\{\\s*";
        String endPattern = "\\s*\\}";
        Matcher matcher = Pattern.compile(startPattern + ".*?" + endPattern, Pattern.CASE_INSENSITIVE).matcher(datasetNamePattern);
        return !matcher.find();
    }

    public boolean isSimpleDatasetName() {
        return isSimpleDatasetName(datasetNamePattern);
    }

    public EmfDataset getEmfDataset(DataService dataService) {
        try {
            if (datasetId != null) {
                return dataService.getDataset(datasetId);
            } else if (isSimpleDatasetName()) {
                return dataService.getDataset(datasetNamePattern);
            } else {
                List<History> history = module.getModuleHistory();
                HistoryDataset historyDataset = null;
                if (history.size() > 0) {
                    History lastHistory = history.get(history.size() - 1);
                    String result = lastHistory.getResult();
                    if (result != null && result.equals(History.SUCCESS)) {
                        historyDataset = lastHistory.getHistoryDatasets().get(placeholderName);
                    }
                }
                if ((historyDataset != null) && (historyDataset.getDatasetId() != null)) {
                    return dataService.getDataset(historyDataset.getDatasetId());
                }
            }
        } catch (EmfException ex) {
            // ignore exception
        }
        
        return null;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        return module.getModuleTypeVersion().getModuleTypeVersionDatasets().get(placeholderName);
    }

    public String getOutputMethod() {
        return outputMethod;
    }

    public void setOutputMethod(String outputMethod) {
        this.outputMethod = outputMethod;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDatasetNamePattern() {
        return datasetNamePattern;
    }

    public void setDatasetNamePattern(String datasetNamePattern) {
        this.datasetNamePattern = datasetNamePattern;
    }

    public Boolean getOverwriteExisting() {
        return overwriteExisting;
    }

    public void setOverwriteExisting(Boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    public String getQualifiedName() {
        return module.getName() + " . " + placeholderName;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleDataset && ((ModuleDataset) other).getQualifiedName().equals(getQualifiedName()));
    }

    public int compareTo(ModuleDataset o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
