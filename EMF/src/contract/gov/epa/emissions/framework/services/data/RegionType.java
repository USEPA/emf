package gov.epa.emissions.framework.services.data;

import java.io.Serializable;

public class RegionType implements Serializable, Comparable<RegionType> {

    private int id;

    private String name;
    
    private String description;
    
    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public RegionType() {
        super();
    }

    public RegionType(String name) {
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof RegionType))
            return false;

        return ((RegionType) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(RegionType other) {
        return name.compareToIgnoreCase(other.getName());
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
