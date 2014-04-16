package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class PivotConfiguration implements Serializable {

    private String[] rowLabels;
    private String[] extraFields;
    private String[] columnLabels;
    private String[] values;
    private String summarizeValueBy = "sum";

    public PivotConfiguration() {
        //
    }
    
    public String[] getRowLabels() {
        return rowLabels;
    }

    public void setRowLabels(String[] rowLabels) {
        this.rowLabels = rowLabels;
    }

    public String[] getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(String[] extraFields) {
        this.extraFields = extraFields;
    }

    public String[] getColumnLabels() {
        return columnLabels;
    }

    public void setColumnLabels(String[] columnLabels) {
        this.columnLabels = columnLabels;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public void setSummarizeValueBy(String summarizeValueBy) {
        this.summarizeValueBy = summarizeValueBy;
    }

    public String getSummarizeValueBy() {
        return summarizeValueBy;
    }
}
