package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class InputEnvtVar implements Serializable, Comparable {

    private int id;

    private String name;
    
    private int modelToRunId;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public InputEnvtVar() {
        super();
    }

    public InputEnvtVar(String name) {
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
        if (other == null || !(other instanceof InputEnvtVar))
            return false;

        boolean eqID = (id == ((InputEnvtVar)other).id);
        boolean eqName = ((InputEnvtVar) other).getName().equals(this.name);
        boolean eqModel2Run = (((InputEnvtVar) other).getModelToRunId() == this.modelToRunId);

        return  eqID || (eqName && eqModel2Run);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((InputEnvtVar) other).getName());
    }
}
