package gov.epa.emissions.framework.services.tempalloc;

import java.io.Serializable;

public class TemporalAllocationOutputType implements Serializable, Comparable {

    private int id;

    private String name;
    
    public static final String monthlyType = "Temporal Allocation Monthly Result";
    public static final String dailyType = "Temporal Allocation Daily Result";
    public static final String episodicType = "Temporal Allocation Episodic Result";
    public static final String messagesType = "Temporal Allocation Messages";

    public TemporalAllocationOutputType() {
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int compareTo(Object o) {
        return name.compareTo(((TemporalAllocationOutputType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof TemporalAllocationOutputType && ((TemporalAllocationOutputType) other).id == id);
    }
}
