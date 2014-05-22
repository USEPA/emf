package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class TemporalAllocationInputDataset implements Serializable {

    private EmfDataset inputDataset;

    private int version;

    public TemporalAllocationInputDataset() {
        
    }
    
    public TemporalAllocationInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TemporalAllocationInputDataset)) {
            return false;
        }

        TemporalAllocationInputDataset other = (TemporalAllocationInputDataset) obj;

        return (inputDataset.getId() == other.getInputDataset().getId()) 
            && (version == other.getVersion());
    }

    public int hashCode() {
        return inputDataset != null ? inputDataset.hashCode() : "".hashCode();
    }

    public EmfDataset getInputDataset() {
        return inputDataset;
    }

    public void setInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
