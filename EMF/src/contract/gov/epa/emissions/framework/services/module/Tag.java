package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class Tag implements Serializable {
    int id;
    String name;
    String description;

    public Tag() {
    }
    
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // standard methods

    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof Tag && ((Tag) other).getId() == id);
    }
}
