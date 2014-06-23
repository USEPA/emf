package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.EquationType;

public class EquationTypeSelectionPresenter {

    private EmfSession session;

    private EquationTypeSelectionView view;
    
    public EquationTypeSelectionPresenter(EquationTypeSelectionView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display() throws Exception {
        view.observe(this);

        //get equation types...
        view.display(session.controlMeasureService().getEquationTypes());
    }
    
    public EquationType getEquationType() {
        return view.getEquationType();
    }
}