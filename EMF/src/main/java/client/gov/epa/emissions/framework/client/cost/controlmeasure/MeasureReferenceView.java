package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface MeasureReferenceView {

    void save();

    void display(ControlMeasure measure, Reference reference);

    // use this method when adding a new property
    void display(ControlMeasure measure);

    void observe(MeasureReferencePresenter presenter);

    void viewOnly();

}