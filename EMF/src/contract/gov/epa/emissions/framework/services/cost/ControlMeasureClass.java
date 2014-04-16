package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureClass implements Serializable {
    private int id;

    private String name;

    private String description;
    
    public ControlMeasureClass() {
        //
    }

    public ControlMeasureClass(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureClass)) {
            return false;
        }

        ControlMeasureClass other = (ControlMeasureClass) obj;

        return (id == other.getId() || name.equalsIgnoreCase(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        return this.name;
    }
}
