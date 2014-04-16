package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class Country implements Serializable {

    private int id;

    private String name;

    public Country() {// needed for serialization
    }

    public Country(String name) {
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

        if (!(other instanceof Country))
            return false;

        final Country country = (Country) other;

        if (!(country.getName().equals(this.getName())))
            return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString(){
        return getName();
    }

}
