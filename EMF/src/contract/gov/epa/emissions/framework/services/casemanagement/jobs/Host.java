package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class Host implements Serializable, Comparable<Host> {

    private int id;
    
    private String name;
    
    private String ipAddress;
    
    public Host() {
        //
    }
    
    public Host(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
        if (other == null || !(other instanceof Host))
            return false;

        return ((Host) other).name.equals(this.name) || ((Host)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(Host o) {
        return name.compareToIgnoreCase(o.getName());
    }
    
}
