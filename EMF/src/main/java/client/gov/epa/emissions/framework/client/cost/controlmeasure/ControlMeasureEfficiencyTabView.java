package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface ControlMeasureEfficiencyTabView extends ControlMeasureTabView {
    
    void add(EfficiencyRecord record);

    void update(EfficiencyRecord record);

    void refresh();

    void refresh(ControlMeasure measure);

    EfficiencyRecord[] records();

    void fireTracking();
    
}
