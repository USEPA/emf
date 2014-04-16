package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class JobRunStatus implements Serializable {

    private int id;
    
    private String name="Not Started";
    
    private String description;
    
    public JobRunStatus() {
        //
    }

    public JobRunStatus(String status) {
        this.name = status;
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
    
    public String toString() {
        return this.name;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof JobRunStatus))
            return false;

        return ((JobRunStatus) other).name.equals(this.name) || ((JobRunStatus)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
