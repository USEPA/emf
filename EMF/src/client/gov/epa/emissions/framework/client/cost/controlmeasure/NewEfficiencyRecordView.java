package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface NewEfficiencyRecordView {

    void observe(NewEfficiencyRecordPresenter presenter);

    void display(ControlMeasure measure, EfficiencyRecord record);
}
