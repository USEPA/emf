package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface EfficiencyRecordView extends ManagedView {
    
    void observe(NewEfficiencyRecordPresenter presenter);
    
    void display(ControlMeasure measure, EfficiencyRecord record);
    
    void observe(EditEfficiencyRecordPresenter presenter);

}
