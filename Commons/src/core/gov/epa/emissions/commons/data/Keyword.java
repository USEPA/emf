package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class Keyword implements Serializable {

    private int id;

    private String name;

    public static final String INDICES = "INDICES";

    public Keyword() {// dummy: needed by Hibernate
    }

    public Keyword(String name) {
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
        if (!(other instanceof Keyword))
            return false;

        return name.equals(((Keyword) other).name);
    }

    public int hashCode() {
        return id;
    }
    
    public String toString() {
        return name;
    }

}
