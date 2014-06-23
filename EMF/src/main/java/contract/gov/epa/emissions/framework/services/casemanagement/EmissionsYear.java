package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class EmissionsYear implements Comparable<EmissionsYear>, Serializable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public EmissionsYear() {
        super();
    }

    public EmissionsYear(String name) {
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
        if (other == null || !(other instanceof EmissionsYear))
            return false;

        return ((EmissionsYear) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(EmissionsYear other) {
        return name.compareToIgnoreCase(other.getName());
    }
}
