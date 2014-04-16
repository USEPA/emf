package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class CaseCategory implements Comparable, Serializable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public CaseCategory() {
        super();
    }

    public CaseCategory(String name) {
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
        if (other == null || !(other instanceof CaseCategory))
            return false;

        return ((CaseCategory) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((CaseCategory) other).getName());
    }
}
