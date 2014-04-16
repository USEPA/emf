package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;


public class ControlTechnologySelectionPresenter {

    private ControlTechnologyTableData tableData;

    private ControlProgramTechnologiesTab parentView;

    private EmfSession session;
    
    public ControlTechnologySelectionPresenter(ControlProgramTechnologiesTab parentView, 
            ControlTechnologySelectionView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.session = session;
    }

    public void display(ControlTechnologySelectionView view) throws Exception {
        view.observe(this);
        this.tableData = new ControlTechnologyTableData(getAllControlTechnologies());
        view.display(tableData);

    }

    public void doAdd(ControlTechnology[] controlTechnologies) {
        parentView.add(controlTechnologies);
    }

    public ControlTechnology[] getAllControlTechnologies() throws EmfException {
        return session.controlMeasureService().getControlTechnologies();
    }

}
