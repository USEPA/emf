package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;


public class ParameterName implements Serializable, Comparable {

    private int id;
    
    private String name;
    
    private int modelToRunId;
    
    public ParameterName() {
        //
    }
    
    public ParameterName(String name) {
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
    
    public int getModelToRunId() {
        return modelToRunId;
    }

    public void setModelToRunId(int modelToRunId) {
        this.modelToRunId = modelToRunId;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterName))
            return false;

        boolean eqID = (id == ((ParameterName)other).id);
        boolean eqName = ((ParameterName) other).name.equals(this.name);
        boolean eqModel2Run = (((ParameterName) other).modelToRunId == this.modelToRunId);

        return  eqID || (eqName && eqModel2Run);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }
    
    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((ParameterName) other).getName());
    }
}
