package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.LockableImpl;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FastAnalysisInput extends LockableImpl implements Serializable {

    private String name;

    private int id;

    private String orlInventory;

    private String ancillaryORLInventory;

    private String derivedORLPointInventory;

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

    public String getOrlInventory() {
        return orlInventory;
    }

    public void setOrlInventory(String orlInventory) {
        this.orlInventory = orlInventory;
    }

    public String getAncillaryORLInventory() {
        return ancillaryORLInventory;
    }

    public void setAncillaryORLInventory(String ancillaryORLInventory) {
        this.ancillaryORLInventory = ancillaryORLInventory;
    }

    public String getDerivedORLPointInventory() {
        return derivedORLPointInventory;
    }

    public void setDerivedORLPointInventory(String derivedORLPointInventory) {
        this.derivedORLPointInventory = derivedORLPointInventory;
    }
    
    @Override
    public boolean equals(Object o) {

        boolean equals = false;
        if (o instanceof FastAnalysisInput) {

            FastAnalysisInput that = (FastAnalysisInput) o;
            equals = this.id == that.id;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public FastAnalysisInput clone() {
        
        FastAnalysisInput clone = new FastAnalysisInput();
        clone.orlInventory = this.orlInventory;
        clone.ancillaryORLInventory = this.ancillaryORLInventory;
        clone.derivedORLPointInventory = this.derivedORLPointInventory;
        clone.name = this.name;
        
        return clone;
    }
}
