package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.framework.services.data.EmfDataset;

final class DatasetVersion {
    private EmfDataset dataset;
    private int version;
    private boolean keep;
    
    public DatasetVersion(EmfDataset dataset, int version, boolean keep) {
        this.dataset = dataset;
        this.version = version;
        this.setKeep(keep);
    }
    
    public DatasetVersion(EmfDataset dataset, int version) {
        this(dataset, version, true);
    }
    
    public EmfDataset getDataset() {
        return dataset;
    }
    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

}
