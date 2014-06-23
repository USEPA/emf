package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface ControlProgramView extends ManagedView {

    void observe(ControlProgramPresenter presenter);

    void display(ControlProgram controlProgram);
    
    void refresh(ControlProgram controlProgram);
    
    void notifyLockFailure(ControlProgram controlProgram);

    public void startControlMeasuresRefresh();

    public void signalControlMeasuresAreLoaded(ControlMeasure[] controlMeasures);
    
    void signalChanges();    
}
