package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class Executable implements Serializable {

    private int id;
    
    private String name;
    
    public Executable() {
        //
    }
    
    public Executable(String name) {
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
    
    public String toString() {
        return this.name;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Executable))
            return false;
        Executable exec = (Executable) other;
        return (id == exec.getId()) || name.equals(exec.getName());
    }
    
}
