package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;


public class MeasurePropertyPresenter {

    protected ControlMeasurePropertyTab parentView;
    
    protected MeasurePropertyWindow view;

    public MeasurePropertyPresenter(ControlMeasurePropertyTab parentView,
            MeasurePropertyWindow view) {
        this.parentView = parentView;
        this.view = view;
    }

    //use this method when adding a new property
    public void display(ControlMeasure measure) {
        view.observe(this);
        view.display(measure);
    }

    //use this method when editing a existing property
    public void display(ControlMeasure measure, ControlMeasureProperty property) {
        view.observe(this);
        view.display(measure, property);
    }

    public void refresh() {
        parentView.refresh();
    }

    public void add(ControlMeasureProperty property) {
        parentView.add(property);
    }

    public void doSave(ControlMeasure measure) {
        parentView.save(measure);
    }
}