package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class FastRunInventory implements Serializable {

    private EmfDataset dataset;

    private int version;

    public FastRunInventory() {
        //
    }

    public FastRunInventory(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public FastRunInventory(EmfDataset dataset, int version) {
        this.dataset = dataset;
        this.version = version;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FastRunInventory)) {
            return false;
        }

        FastRunInventory other = (FastRunInventory) obj;

        return (dataset.getId() == other.getDataset().getId()) 
            && (version == other.getVersion());
    }

    public int hashCode() {
        return dataset != null ? dataset.hashCode() : "".hashCode();
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}