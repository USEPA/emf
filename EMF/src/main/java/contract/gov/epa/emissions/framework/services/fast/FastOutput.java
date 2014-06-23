package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.LockableImpl;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FastOutput extends LockableImpl implements Serializable {

    private int id;

    private String type;

    private String output;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
    
    @Override
    public boolean equals(Object o) {

        boolean equals = false;
        if (o instanceof FastOutput) {

            FastOutput that = (FastOutput) o;
            equals = this.id == that.id;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.output;
    }

    public FastOutput clone() {
        
        FastOutput clone = new FastOutput();
        clone.type = this.type;
        clone.output = this.output;
        
        return clone;
    }
}
