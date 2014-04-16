package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlProgramType implements Serializable, Comparable {

    private int id;

    private String name;

    private String description;

    public static final String plantClosure = "Plant Closure";
    public static final String projection = "Projection";
    public static final String control = "Control";
    public static final String allowable = "Allowable";

    public ControlProgramType() {
        //
    }

    public ControlProgramType(String name) {
        this();
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return name.compareTo(((ControlProgramType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ControlProgramType && ((ControlProgramType) other).id == id);
    }
}
