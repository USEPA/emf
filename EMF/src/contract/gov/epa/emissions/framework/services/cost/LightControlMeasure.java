package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

import java.io.Serializable;

public class LightControlMeasure implements Serializable {

    private int id;

    private String name;

    private String description;

    private Pollutant majorPollutant;

    private String abbreviation;

    private boolean includeExclude;
    
    private ControlTechnology controlTechnology;
    
    private User creator;
    
    private ControlMeasureClass cmClass;

    public LightControlMeasure() {
        //
    }

    public LightControlMeasure(String name) {
        this();
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pollutant getMajorPollutant() {
        return majorPollutant;
    }

    public void setMajorPollutant(Pollutant majorPollutant) {
        this.majorPollutant = majorPollutant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIncludeExclude() {
        return includeExclude;
    }

    public void setIncludeExclude(boolean includeExclude) {
        this.includeExclude = includeExclude;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LightControlMeasure)) {
            return false;
        }

        LightControlMeasure other = (LightControlMeasure) obj;

        return (id == other.getId() || name.equals(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setControlTechnology(ControlTechnology controlTechnology) {
        this.controlTechnology = controlTechnology;
    }

    public ControlTechnology getControlTechnology() {
        return controlTechnology;
    }
    
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    } 
    
    public ControlMeasureClass getCmClass() {
        return cmClass;
    }

    public void setCmClass(ControlMeasureClass cmClass) {
        this.cmClass = cmClass;
    }
}
