package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class Speciation implements Comparable<Speciation>, Serializable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Speciation() {
        super();
    }

    public Speciation(String name) {
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
        if (other == null || !(other instanceof Speciation))
            return false;

        return ((Speciation) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Speciation other) {
        return name.compareToIgnoreCase(other.getName());
    }
}
