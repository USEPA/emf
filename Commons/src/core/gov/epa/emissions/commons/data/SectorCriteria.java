package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class SectorCriteria implements Serializable {

    private long id;

    private String type;

    private String criteria;

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof SectorCriteria))
            return false;

        return this.id == ((SectorCriteria) obj).id;
    }

    public int hashCode() {
        return (int) id;
    }

}
