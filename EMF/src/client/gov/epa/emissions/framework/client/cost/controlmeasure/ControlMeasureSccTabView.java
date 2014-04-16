package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

public interface ControlMeasureSccTabView extends ControlMeasureTabView {
    
    Scc[] sccs();
}
