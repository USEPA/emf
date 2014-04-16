package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;

public interface ControlProgramTabPresenter {

    void doSave(ControlProgram controlProgram) throws EmfException;

    void doChangeControlProgramType(ControlProgramType controlProgramType);
}
