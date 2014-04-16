package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatasetType implements Serializable, Lockable, Comparable<DatasetType> {

    private int id;

    private String name;

    private String description;

    private int minFiles;

    private int maxFiles;

    private boolean external;

    private int tablePerDataset;

    private String defaultSortOrder;

    private String importerClassName;

    private String exporterClassName;

    private Mutex lock;
    
    private XFileFormat fileFormat;

    private KeyVal[] keyValsList = new KeyVal[]{};

    private QAStepTemplate[] qaStepTemplates = new QAStepTemplate[]{};
    
    private Date creationDate;
    
    private Date lastModifiedDate;
    
    private User creator;

    public static final String orlMergedInventory = "ORL Merged Inventory";

    public static final String orlNonpointInventory = "ORL Nonpoint Inventory (ARINV)";

    public static final String orlPointInventory = "ORL Point Inventory (PTINV)";

    public static final String ORL_FIRE_INVENTORY = "ORL Fire Inventory (PTINV)";

    public static final String orlNonroadInventory = "ORL Nonroad Inventory (ARINV)";

    public static final String orlOnroadInventory = "ORL Onroad Inventory (MBINV)";

    public static final String projectionPacket = "Projection Packet";

    public static final String allowablePacket = "Allowable Packet";

    public static final String controlPacket = "Control Packet";
    
    public static final String strategyDetailedResult = "Control Strategy Detailed Result";

    public static final String strategyCountySummary = "Strategy County Summary";
    
    public static final String strategyImpactSummary = "Strategy Impact Summary";

    public static final String strategyMeasureSummary = "Strategy Measure Summary";

    public static final String rsmPercentReduction = "RSM Percent Reduction";

    public static final String smokeReportCountyMoncodeAnnual = "Smkreport county-moncode annual";

    public static final String temporalProfile = "Temporal Profile (A/M/PTPRO)";

    public static final String smkmergeRptStateAnnualSummary = "Smkmerge report state annual summary (CSV)";
    
    public static final String invTable = "Inventory Table Data (INVTABLE)";
    
    public static final String stateComparisonTolerance = "State Comparison Tolerance (CSV)";
    
    public static final String countryStateCountyNamesAndDataCOSTCY = "Country, state, and county names and data (COSTCY)";
    
    public static final String chemicalSpeciationCrossReferenceGSREF = "Chemical Speciation Cross-Reference (GSREF)";
    
    public static final String chemicalSpeciationProfilesGSPRO = "Chemical Speciation Profiles (GSPRO)";
    
    public static final String FAST_TRANSFER_COEFFICIENTS = "FAST Transfer Coefficients";

    public static final String FAST_DOMAIN_POPULATION = "FAST Domain Population";

    public static final String FAST_CANCER_RISK = "FAST Cancer Risk";
    
    public static final String FAST_SPECIES_MAPPING = "FAST Species Mapping";
    
    public static final String FAST_ANALYSIS_DOMAIN_DIFFERENCE_RESULT = "FAST Analysis Domain Difference Result";
    
    public static final String FAST_ANALYSIS_GRIDDED_DIFFERENCE_RESULT = "FAST Analysis Gridded Difference Result";
    
    public static final String FAST_RUN_INTERMEDIATE_AIR_QUALITY = "FAST Run Intermediate Air Quality";
    
    public static final String FAST_RUN_GRIDDED_OUTPUT = "FAST Run Gridded Output";
    
    public static final String FAST_RUN_DOMAIN_OUTPUT = "FAST Run Domain Output";
    
    public static final String FAST_RUN_INTERMEDIATE_INVENTORY = "FAST Run Intermediate Inventory";
    
    public static final String FAST_SMOKE_GRIDDED_SCC_REPORT = "FAST SMOKE Gridded SCC Report";
    
    public static final String SECTOR_DETAILED_MAPPING_RESULT = "Sector Detailed Mapping Result";
    
    public static final String EECS_DETAILED_MAPPING_RESULT = "EECS Detailed Mapping Result";
    
    public static final String SECTOR_MAPPING = "Sector Mapping";
    
    public static final String EECS_MAPPING = "EECS Mapping";
    
    public static final String ORL_POINT_NATA = "ORL Point NATA";
    
    public static final String ORL_POINT_NATA_SECTOR_ANNOTATED = "ORL Point NATA Sector Annotated";
    
//    public static final String ORL_NONPOINT_NATA = "ORL Nonpoint NATA";
//    
//    public static final String ORL_NONPOINT_NATA_SECTOR_ANNOTATED = "ORL Nonpoint NATA Sector Annotated";
    
    public static final String NOF_POINT = "NOF Point";
    
    public static final String NOF_NONPOINT = "NOF Nonpoint";
    
    public static final String LIST_OF_COUNTIES = "List of Counties (CSV)";
    
    //For new dataset type   
    public static final String EXTERNAL = "External File";
    
    public static final String CSV = "CSV File";
    
    public static final String LINE_BASED = "Line-based";
    
    public static final String SMOKE = "SMOKE Report File";

    public static final String SMOKE_REPORT = "SMOKE Report";
    
    public static final String FLEXIBLE = "Flexible File Format";
    
    public static final String FLAT_FILE_2010_POINT = "Flat File 2010 Point";
    
    public static final String FLAT_FILE_2010_NONPOINT = "Flat File 2010 Nonpoint";
    
    public static final String EXTERNAL_IMPORTER = "gov.epa.emissions.commons.io.external.ExternalFilesImporter";
    
    public static final String CSV_IMPORTER = "gov.epa.emissions.commons.io.csv.CSVImporter";
    
    public static final String LINE_IMPORTER = "gov.epa.emissions.commons.io.generic.LineImporter";
    
    public static final String SMOKE_IMPORTER = "gov.epa.emissions.commons.io.other.SMKReportImporter";
    
    public static final String FLEXIBLE_IMPORTER = "gov.epa.emissions.commons.io.orl.FlexibleDBImporter";
    
    public static final String EXTERNAL_EXPORTER = "gov.epa.emissions.commons.io.external.ExternalFilesExporter";
    
    public static final String CSV_EXPORTER = "gov.epa.emissions.commons.io.csv.CSVExporter";
    
    public static final String LINE_EXPORTER = "gov.epa.emissions.commons.io.generic.LineExporter";
    
    public static final String SMOKE_EXPORTER = "gov.epa.emissions.commons.io.other.SMKReportExporter";
    
    public static final String FLEXIBLE_EXPORTER = "gov.epa.emissions.commons.io.orl.FlexibleDBExporter";
    
    public DatasetType() {
//        keyValsList = new ArrayList();
//        qaStepTemplates = new ArrayList();
        lock = new Mutex();
    }

    public DatasetType(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public DatasetType(String name) {
        this();
        this.name = name;
    }

    public String getExporterClassName() {
        return exporterClassName;
    }

    public void setExporterClassName(String exporterClassName) {
        this.exporterClassName = exporterClassName;
    }

    public String getImporterClassName() {
        return importerClassName;
    }

    public void setImporterClassName(String importerClassName) {
        this.importerClassName = importerClassName;
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

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxfiles) {
        this.maxFiles = maxfiles;
    }

    public int getMinFiles() {
        return minFiles;
    }

    public void setMinFiles(int minfiles) {
        this.minFiles = minfiles;
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

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof DatasetType && ((DatasetType) other).getId() == id);
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
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

    public String getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    public int compareTo(DatasetType o) {
        return name.compareTo(o.getName());
    }

    public KeyVal[] getKeyVals() {
        return this.keyValsList;
    }

    public void setKeyVals(KeyVal[] keyvals) {
        this.keyValsList = keyvals;
    }

    public void addKeyVal(KeyVal val) {
        List<KeyVal> keyVals = new ArrayList<KeyVal>();
        keyVals.addAll(Arrays.asList(this.keyValsList));
        keyVals.add(val);
        
        this.keyValsList = keyVals.toArray(new KeyVal[0]);
    }

    public void setQaStepTemplates(QAStepTemplate[] templates) {
        this.qaStepTemplates = templates;
    }

    public void addQaStepTemplate(QAStepTemplate template) {
        List<QAStepTemplate> templates = new ArrayList<QAStepTemplate>();
        templates.addAll(Arrays.asList(this.qaStepTemplates));
        templates.add(template);
        
        this.qaStepTemplates = templates.toArray(new QAStepTemplate[0]);
    }

    public void removeQaStepTemplate(QAStepTemplate template) {
        List<QAStepTemplate> templates = new ArrayList<QAStepTemplate>();
        templates.addAll(Arrays.asList(this.qaStepTemplates));
        for (int i = 0; i < templates.size(); i++) {
            if (template.getName().equals(templates.get(i).getName())) templates.remove(i);
        }
        this.qaStepTemplates = templates.toArray(new QAStepTemplate[0]);
    }

    public QAStepTemplate[] getQaStepTemplates() {
        return this.qaStepTemplates;
    }

    public int getTablePerDataset() {
        return tablePerDataset;
    }

    public void setTablePerDataset(int tablePerDataset) {
        this.tablePerDataset = tablePerDataset;
    }

    public XFileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(XFileFormat fileFormat) {
        this.fileFormat = fileFormat;
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

}
