package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class UserFeature implements Serializable, Comparable{

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public UserFeature() {
        super();
    }

    public UserFeature(String name) {
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

        if (!(other instanceof UserFeature))
            return false;

        final UserFeature userFeature = (UserFeature) other;

        if (!(userFeature.getName().equals(this.getName())))
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
        return name.compareToIgnoreCase(((UserFeature) o).getName());
    }
}
