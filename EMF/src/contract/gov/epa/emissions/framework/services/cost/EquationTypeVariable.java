package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class EquationTypeVariable implements Serializable {
    private int id;

    private String name;

    private EquationType equationType;
    
    private short fileColPosition;

    private String description;
    
    private long listindex;

    private Object value;

    public long getListindex() {
        return listindex;
    }

    public void setListindex(long listindex) {
        this.listindex = listindex;
    }

    public EquationTypeVariable() {
        //
    }

    public EquationTypeVariable(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EquationTypeVariable)) {
            return false;
        }

        EquationTypeVariable other = (EquationTypeVariable) obj;

        return (id == other.getId() || name.equalsIgnoreCase(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        return this.name;
    }

    public void setFileColPosition(short fileColPosition) {
        this.fileColPosition = fileColPosition;
    }

    public short getFileColPosition() {
        return fileColPosition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
