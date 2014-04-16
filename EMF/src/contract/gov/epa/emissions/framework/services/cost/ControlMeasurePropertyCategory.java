package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasurePropertyCategory implements Serializable {
    private int id;

    private String name;

    public ControlMeasurePropertyCategory() {
        //
    }

    public ControlMeasurePropertyCategory(String name) {
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
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasurePropertyCategory)) {
            return false;
        }

        ControlMeasurePropertyCategory other = (ControlMeasurePropertyCategory) obj;

        return (id == other.getId() || name.equalsIgnoreCase(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        return this.name;
    }
}
