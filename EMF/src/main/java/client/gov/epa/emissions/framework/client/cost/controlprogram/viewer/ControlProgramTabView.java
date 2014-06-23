package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.framework.services.cost.ControlProgramType;

public interface ControlProgramTabView {
    void notifyControlProgramTypeChange(ControlProgramType controlProgramType);
}
