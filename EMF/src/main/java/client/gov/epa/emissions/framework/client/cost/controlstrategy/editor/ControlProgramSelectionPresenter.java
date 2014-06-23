package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;


public class ControlProgramSelectionPresenter {

    private ControlProgramTableData tableData;

    private ControlProgramSelectorView parentView;

    private EmfSession session;
    
    public ControlProgramSelectionPresenter(ControlProgramSelectorView parentView, 
            ControlProgramSelectionView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.session = session;
    }

    public void display(ControlProgramSelectionView view) throws Exception {
        view.observe(this);
        this.tableData = new ControlProgramTableData(getAllControlPrograms());
        view.display(tableData);

    }

    public void doAdd(ControlProgram[] controlPrograms) {
        parentView.add(controlPrograms);
    }

    public ControlProgram[] getAllControlPrograms() throws EmfException {
        return session.controlProgramService().getControlPrograms();
    }

}
