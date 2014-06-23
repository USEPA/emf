package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasureView extends ManagedView {

    void observe(ControlMeasurePresenter presenter);

    void display(ControlMeasure measure);

    void showError(String message);

    void notifyLockFailure(ControlMeasure measure);
    
    void notifyModified();

    void signalChanges();

    void notifyEditFailure(ControlMeasure measure);
    
    void close();
    
 }
