package gov.epa.emissions.framework.services.cost;

import java.io.Serializable;

public class ControlMeasureNEIDevice implements Serializable {
    private int id;

    private int neiDeviceCode;
    
    public ControlMeasureNEIDevice() {
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureNEIDevice)) {
            return false;
        }

        ControlMeasureNEIDevice other = (ControlMeasureNEIDevice) obj;

        return (id == other.getId() && neiDeviceCode == other.getNeiDeviceCode());
    }

    public int hashCode() {
        return toString().hashCode();
    }
    
    public void setNeiDeviceCode(int neiDeviceCode) {
        this.neiDeviceCode = neiDeviceCode;
    }

    public int getNeiDeviceCode() {
        return neiDeviceCode;
    }
}
