package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

public class ControlProgramTechnologiesTabPresenter {

    private ControlProgramTechnologiesTab view;

    private EmfSession session;

    private ControlProgram controlProgram;

    public ControlProgramTechnologiesTabPresenter(ControlProgramTechnologiesTab view, ControlProgram controlProgram,
            EmfSession session) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.display(this.controlProgram);
    }

    public ControlTechnology[] getAllControlTechnologies() throws EmfException {
        return session.controlMeasureService().getControlTechnologies();
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
