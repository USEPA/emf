package gov.epa.emissions.framework.services.fast;

import java.io.Serializable;

public class FastRunOutputType implements Serializable, Comparable {

    private int id;

    private String name;

    public static final String INTERMEDIATE_INVENTORY = "Intermediate Inventory";

    public static final String INTERMEDIATE_AIR_QUALITY = "Intermediate Air Quality";

    public static final String GRIDDED_OUTPUT = "Gridded Output";

    public static final String DOMAIN_OUTPUT = "Domain Output";

    public FastRunOutputType() {
        //
    }

    public FastRunOutputType(String name) {
        this();
        this.name = name;
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
        return name.compareTo(((FastRunOutputType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FastRunOutputType && ((FastRunOutputType) other).id == id);
    }
}
