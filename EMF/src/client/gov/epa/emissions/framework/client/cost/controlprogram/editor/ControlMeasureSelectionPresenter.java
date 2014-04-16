package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;


public class ControlMeasureSelectionPresenter {

    private ControlMeasureSelectorView parentView;

    private EmfSession session;
    
    public ControlMeasureSelectionPresenter(ControlMeasureSelectorView parentView, 
            ControlMeasureSelectionView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.session = session;
    }

    public void display(ControlMeasureSelectionView view) throws Exception {
        view.observe(this);
        view.display();

    }

    public void doAdd(ControlMeasure[] controlMeasures) {
        parentView.add(controlMeasures);
    }

    public ControlMeasure[] getAllControlMeasures() throws EmfException {
        return session.controlMeasureService().getControlMeasures("");
    }

}
