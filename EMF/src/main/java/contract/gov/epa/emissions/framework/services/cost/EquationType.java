package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EquationType implements Serializable {
    private int id;

    private String name;

    private String description = "";
    
    private String inventoryFields;
    
    private String equation;
    
    private List equationTypeVariables;

    private Pollutant pollutant;

    private int costYear;

    public EquationType() {
        this.equationTypeVariables = new ArrayList();
    }

    public EquationType(String name) {
        this();
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
        if (obj == null || !(obj instanceof EquationType)) {
            return false;
        }

        EquationType other = (EquationType) obj;

        return (id == other.getId() || (name != null ? name : "").equalsIgnoreCase((other.getName() != null ? other.getName() : "")));
    }

    
    public int hashCode() {
        return name.hashCode();
    }
    
    public String toString() {
        if (this.description.trim()=="")
            return this.name;
        String string=this.name +" -- " +this.description + ""; 
        return string;
    }

    public EquationTypeVariable[] getEquationTypeVariables() {
        return (EquationTypeVariable[]) equationTypeVariables.toArray(new EquationTypeVariable[0]);
    }

    public void setEquationTypeVariables(EquationTypeVariable[] equationTypeVariables) {
        this.equationTypeVariables = Arrays.asList(equationTypeVariables);
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public int getCostYear() {
        return costYear;
    }

    public String getInventoryFields() {
        return inventoryFields;
    }

    public void setInventoryFields(String inventoryFields) {
        this.inventoryFields = inventoryFields;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }
}
