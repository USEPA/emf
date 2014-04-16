package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import java.io.Serializable;

public class ControlStrategyMeasureMap implements Serializable {
    
    private int controlStrategyId;
//    private byte mapType;

    private ControlMeasureClass[] controlMeasureClasses;

    public ControlStrategyMeasureMap() {
        //
    }
    
    public ControlStrategyMeasureMap(int controlStrategyId) {
        this.controlStrategyId = controlStrategyId;
    }
    
    public int getControlStrategyId() {
        return controlStrategyId;
    }

    public void setControlStrategyId(int id) {
        this.controlStrategyId = id;
    }

    public void setControlMeasureClasses(ControlMeasureClass[] controlMeasureClasses) {
        this.controlMeasureClasses = controlMeasureClasses;
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return controlMeasureClasses;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControlStrategyMeasureMap))
            return false;

        final ControlStrategyMeasureMap cs = (ControlStrategyMeasureMap) other;

        return cs.controlStrategyId == this.controlStrategyId;
    }
}