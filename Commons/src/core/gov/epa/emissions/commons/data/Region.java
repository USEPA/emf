package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class Region implements Serializable, Comparable{

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Region() {
        super();
    }

    public Region(String name) {
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
        if (this == other)
            return true;

        if (!(other instanceof Region))
            return false;

        final Region region = (Region) other;

        if (!(region.getName().equals(this.getName())))
            return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object o) {
        return name.compareToIgnoreCase(((Region) o).getName());
    }
}
