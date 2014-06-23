package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class ControlStrategyInputDataset implements Serializable {

    private EmfDataset inputDataset;

    private int version;

    public ControlStrategyInputDataset() {
        //
    }

    public ControlStrategyInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlStrategyInputDataset)) {
            return false;
        }

        ControlStrategyInputDataset other = (ControlStrategyInputDataset) obj;

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