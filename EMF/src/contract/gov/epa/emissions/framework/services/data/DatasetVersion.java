package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.version.Version;

public class DatasetVersion {
    private EmfDataset dataset;
    private Version version;
    private int datasetVersion;
    private String datasetName;
    
    public DatasetVersion(EmfDataset dataset, Version version) {
        this.setDataset(dataset);
        this.setVersion(version);
    }

    public DatasetVersion(String datasetName, int datasetVersion) {
        this.setDatasetName(datasetName);
        this.setDatasetVersion(datasetVersion);
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetVersion(int datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public int getDatasetVersion() {
        return datasetVersion;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof DatasetVersion)) {
            return false;
        }

        DatasetVersion otherDataset = (DatasetVersion) other;

        return (this.getDataset() != null && otherDataset != null && this.getDataset().getId() == otherDataset.getDataset().getId()
//                && this.getVersion().getId() == otherDataset.getVersion().getId()
                );
    }

    public String toString() {
        return (this.getDataset() != null ? this.getDataset().getName() : datasetName) + " [" + (this.getVersion() != null ? this.getVersion().getVersion() + " (" + this.getVersion().getName() + ")" : datasetVersion) + "]";
    }
    
}
