package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface ControlProgramView extends ManagedView {

    void observe(ControlProgramPresenter presenter);

    void display(ControlProgram controlProgram);
    
    void refresh(ControlProgram controlProgram);
    
    void notifyLockFailure(ControlProgram controlProgram);
    
    void signalChanges();    
}
