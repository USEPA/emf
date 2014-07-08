package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class TemporalAllocationOutput implements Serializable {

    private int id;
    private int temporalAllocationId;
    private TemporalAllocationOutputType type;
    private EmfDataset outputDataset;
    
    public TemporalAllocationOutput() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemporalAllocationId() {
        return temporalAllocationId;
    }

    public void setTemporalAllocationId(int id) {
        this.temporalAllocationId = id;
    }
    
    public TemporalAllocationOutputType getType() {
        return type;
    }
    
    public void setType(TemporalAllocationOutputType type) {
        this.type = type;
    }

    public EmfDataset getOutputDataset() {
        return outputDataset;
    }

    public void setOutputDataset(EmfDataset outputDataset) {
        this.outputDataset = outputDataset;
    }
}
