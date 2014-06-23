package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class SubDir implements Serializable, Comparable {
    
    private int id;

    private String name;
    
    private int modelToRunId;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public SubDir() {
        super();
    }

    public SubDir(String name) {
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
        if (other == null || !(other instanceof SubDir))
            return false;

        boolean eqID = (id == ((SubDir)other).id);
        boolean eqName = ((SubDir) other).getName().equals(this.name);
        boolean eqModel2Run = (((SubDir) other).getModelToRunId() == this.modelToRunId);

        return  eqID || (eqName && eqModel2Run);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareToIgnoreCase(((SubDir) other).getName());
    }

}
