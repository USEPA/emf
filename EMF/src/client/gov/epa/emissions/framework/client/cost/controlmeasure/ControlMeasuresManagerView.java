package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

public interface ControlMeasuresManagerView extends ManagedView {

    void observe(ControlMeasuresManagerPresenter presenter);

    void refresh(ControlMeasure[] measures);
    
    void getEfficiencyAndCost() throws EmfException;

    void showMessage(String message);

    void showError(String message);

    void clearMessage();
    
    void display(ControlMeasure[] measures) throws EmfException;

    void doFind(Scc[] sccs);

}
