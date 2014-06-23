package gov.epa.emissions.framework.services.data;

import java.io.Serializable;
import java.util.Date;

public class QAStepResult implements Serializable {

    private String table;

    private String tableCreationStatus;

    private Date tableCreationDate;

    private int qaStepId;

    private int datasetId;

    private int version;

    private int id;
    
    private boolean currentTable;

    public QAStepResult() {
        // empty
    }

    public QAStepResult(QAStep qaStep) {
        this.qaStepId = qaStep.getId();
        this.datasetId = qaStep.getDatasetId();
        this.version = qaStep.getVersion();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQaStepId() {
        return qaStepId;
    }

    public void setQaStepId(int qaStepId) {
        this.qaStepId = qaStepId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Date getTableCreationDate() {
        return tableCreationDate;
    }

    public void setTableCreationDate(Date tableCreationDate) {
        this.tableCreationDate = tableCreationDate;
    }

    public String getTableCreationStatus() {
        return tableCreationStatus;
    }

    public void setTableCreationStatus(String tableCreationStatus) {
        this.tableCreationStatus = tableCreationStatus;
    }

    public boolean isCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(boolean currentTable) {
        this.currentTable = currentTable;
    }

}
