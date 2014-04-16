package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;

import java.io.Serializable;
import java.util.Date;

public class QAStep implements Serializable {

    public static final String invTag = "-inventories";
    
    public static final String invBaseTag = "-inv_base";
    public static final String invCompareTag = "-inv_compare";

    public static final String invTableTag = "-invtable";

    public static final String summaryTypeTag = "-summaryType";
    
    public static final String filterTag = "-filter";
    
    public static final String emissionTypeTag = "-emissionType";
    
    public static final String FF10P_TAG = "-ff10p"; 
    
    public static final String SSFF_TAG = "-ssff"; 
    
    public static final String FAC_TAG = "-fac"; 
    
    public static final String MANYNEIID_TAG = "-manyneiid"; 
    
    public static final String MANYFRS_TAG = "-manyfrs"; 
    
    public static final String WHERE_FILTER_TAG = "-where";

    public static final String avgDaySummaryProgram = "Average day to Annual Summary";

    public static final String avgDayToAnnualProgram = "Average day to Annual Inventory";

    public static final String compareVOCSpeciationWithHAPInventoryProgram = "Compare VOC Speciation With HAP Inventory";

    public static final String fireDataSummaryProgram = "Fire Data Summary (Day-specific)";

    public static final String MultiInvSumProgram = "Multi-inventory sum";

    public static final String MultiInvRepProgram = "Multi-inventory column report";

    public static final String COMPARE_DATASETS_PROGRAM = "Compare Datasets";
    
    public static final String COMPARE_DATASET_FIELDS_PROGRAM = "Compare Dataset Fields";
    
    public static final String MERGE_IN_SUPPORTING_DATA = "Merge In Supporting Data";

    public static final String MultiInvDifRepProgram = "Multi-inventory difference report";
    public static final String CompareControlStrategies = "Compare Control Strategies";
    public static final String createMoEmisByCountyFromAnnEmisProgram = "Create monthly emissions by county from annual emissions";
    public static final String compareAnnStateSummaryProgram = "Compare annual state summaries";
    
    public static final String smokeOutputAnnStateSummaryCrosstabProgram = "SMOKE output annual state summaries crosstab";
    
    public static final String ecControlScenarioProgram = "Estimate EC Impacts";
    
    public static final String sqlProgram = "SQL";
    
    public static final String poundQueryTag = "#";

    private String name;

    private QAProgram program;

    private int version;

    private String programArguments;

    private boolean required;

    private float order;

    private String status;

    private String comments;

    private String who;

    private Date date;

    private int datasetId;

    private int id;

    private String description;

    private String configuration;

    private String outputFolder;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QAStep() {// needed as it's a Java bean
    }
    
    public QAStep(QAStep step) {// copy constructor
        this.comments = step.comments;
        this.configuration = step.configuration;
        this.datasetId = step.datasetId;
        if ( step.date != null) {
            this.date = new Date(step.date.getTime());
        } else {
            this.date = new Date();
        }
        this.description = step.description;
        this.id = step.id;
        this.name = step.name;
        this.order = step.order;
        this.outputFolder = step.outputFolder;
        this.program = new QAProgram(step.program);
        this.programArguments = step.programArguments;
        this.required = step.required;
        this.status = step.status;
        this.version = step.version;
        this.who = step.who;
    }

    public QAStep(QAStepTemplate template, int version) {
        this.name = template.getName();
        this.version = version;
        this.program = template.getProgram();
        this.programArguments = template.getProgramArguments();
        this.required = template.isRequired();
        this.order = template.getOrder();
        this.description = template.getDescription();
    }

    public QAStep(EmfDataset dataset, Version version, QAStepTemplate template) {
        this.name = template.getName();
        this.datasetId = dataset.getId();
        this.version = version.getVersion();
        this.program = template.getProgram();
        this.programArguments = template.getProgramArguments();
        this.required = template.isRequired();
        this.order = template.getOrder();
        this.description = template.getDescription();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof QAStep))
            return false;

        QAStep step = (QAStep) obj;
        if (id == step.id
                || (name.equals(step.getName()) && version == step.getVersion() && datasetId == step.getDatasetId()))
            return true;

        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public QAProgram getProgram() {
        return program;
    }

    public void setProgram(QAProgram program) {
        this.program = program;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getOrder() {
        return order;
    }

    public void setOrder(float order) {
        this.order = order;
    }

    public String getProgramArguments() {
        return programArguments;
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments = programArguments;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public String getStatus() {
        return status;
    }

    public Date getDate() {
        return date;
    }

    public String getWho() {
        return who;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String toString() {
        return name;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

}
