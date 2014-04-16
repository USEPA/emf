package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;

public class ControlProgramMeasuresTabPresenter implements ControlProgramTabPresenter {
    private ControlProgramMeasuresTab view;
    
    private EmfSession session;
    
    private ControlProgram controlProgram;

    public ControlProgramMeasuresTabPresenter(ControlProgramMeasuresTab view, 
            ControlProgram controlProgram, 
            EmfSession session) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay()  {
        view.observe(this);
        view.display(this.controlProgram);
    }

    public ControlMeasure[] getAllControlMeasures() throws EmfException {
        return session.controlMeasureService().getControlMeasures("");
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
    }

    public void doRefresh(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub
    }

    public void doSave(ControlProgram controlProgram) {
        view.save(controlProgram);
    }
}
