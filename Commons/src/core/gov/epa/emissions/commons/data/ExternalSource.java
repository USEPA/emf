package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class ExternalSource implements Serializable {
    private int id;
    
    private int datasetId;

    private String datasource;

    private int listindex;

    public int getListindex() {
        return listindex;
    }

    public void setListindex(int listindex) {
        this.listindex = listindex;
    }

    public ExternalSource() {// dummy: needed by Hibernate
    }

    public ExternalSource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

}
