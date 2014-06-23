package gov.epa.emissions.framework.services.casemanagement.parameters;

import java.io.Serializable;

public class ParameterEnvVar implements Serializable, Comparable {
    
    private int id;
    
    private String name;
    
    private int modelToRunId;
    
    public ParameterEnvVar() {
        //
    }
    
    public ParameterEnvVar(String  name) {
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
    
    public String toString() {
        return name;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterEnvVar))
            return false;

        boolean eqID = (id == ((ParameterEnvVar)other).id);
        boolean eqName = ((ParameterEnvVar) other).getName().equals(this.name);
        boolean eqModel2Run = (((ParameterEnvVar) other).getModelToRunId() == this.modelToRunId);

        return  eqID || (eqName && eqModel2Run);
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((ParameterEnvVar) other).getName());
    }

}
