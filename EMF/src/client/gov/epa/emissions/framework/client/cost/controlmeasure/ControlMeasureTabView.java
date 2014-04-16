package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;


public interface ControlMeasureTabView {
    
    void save(ControlMeasure measure) throws EmfException;
    
    void refresh(ControlMeasure measure);
    
    void modify();
    
    void viewOnly();

}
