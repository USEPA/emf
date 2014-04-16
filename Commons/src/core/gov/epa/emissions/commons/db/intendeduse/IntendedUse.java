package gov.epa.emissions.commons.db.intendeduse;

import java.io.Serializable;

public class IntendedUse implements Comparable, Serializable {

    private int id;

    private String name;

    public IntendedUse() {// bean-style
    }

    public IntendedUse(String name) {
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

    public String toString() {
        return getName();
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof IntendedUse))
            return false;

        final IntendedUse iu = (IntendedUse) other;

        if (!(iu.getName().equals(this.getName())))
            return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(Object o) {
        return name.compareToIgnoreCase(((IntendedUse) o).getName());
    }

}
