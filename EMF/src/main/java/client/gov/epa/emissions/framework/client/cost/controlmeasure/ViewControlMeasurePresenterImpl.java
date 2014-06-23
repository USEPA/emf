package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ViewControlMeasurePresenterImpl extends EditorControlMeasurePresenterImpl {

    public ViewControlMeasurePresenterImpl(ControlMeasure measure, ControlMeasureView view, EmfSession session,
            RefreshObserver parent) {
        super(measure, view, session, parent);
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        measure = session.controlMeasureService().getMeasure(measure.getId());
        display();
    }

    public void doClose() {
        try {
            view.disposeView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
