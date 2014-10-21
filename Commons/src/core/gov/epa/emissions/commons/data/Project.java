package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class Project implements Serializable, Comparable {

    private int id;

    private String name;
    
    private String notes;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Project() {
        super();
    }

    public Project(String name) {
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Project))
            return false;

        return ((Project) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((Project) other).getName());
    }
}
