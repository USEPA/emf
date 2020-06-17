package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;


public class ControlProgramSelectionPresenter {

    private ControlProgramSelectorView parentView;

    private ControlProgramSelectionView view;

    private EmfSession session;
    
    public ControlProgramSelectionPresenter(ControlProgramSelectorView parentView, 
            ControlProgramSelectionView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.view = view;
        this.session = session;
    }

    public void display() throws Exception {
        view.observe(this);
        view.display(session.controlProgramService().getControlPrograms());

    }

    public void doRefresh() throws EmfException {
        // loadControlMeasures();
        view.refresh(getControlPrograms()); //view.getSearchFilter()
    }

    public void doAdd(ControlProgram[] controlPrograms) {
        parentView.add(controlPrograms);
    }

    public ControlProgram[] getControlPrograms() throws EmfException {
        return session.controlProgramService().getControlPrograms(view.getSearchFilter());
    }

}
