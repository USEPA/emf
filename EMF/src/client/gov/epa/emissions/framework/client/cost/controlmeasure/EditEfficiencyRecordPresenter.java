package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EditEfficiencyRecordPresenter extends EfficiencyRecordPresenter {

    private EditEfficiencyRecordView view;

    private ControlMeasureService cmService;

    public EditEfficiencyRecordPresenter(ControlMeasureEfficiencyTabView parentView, EditEfficiencyRecordView view,
            EmfSession session, ControlMeasure measure) {
        super(parentView);
        this.view = view;
        cmService = session.controlMeasureService();
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        view.observe(this);
        view.display(measure, record);
    }

    public void doSave(ControlMeasure measure) throws EmfException {
        parentView.save(measure);
    }

    public void refresh() {
        parentView.refresh();
    }

    public void signalChanges(boolean hasChanges) {
       if (hasChanges)
           parentView.fireTracking();
    }

    public void update(EfficiencyRecord record) throws EmfException {
        cmService.updateEfficiencyRecord(record);
        parentView.update(record);
    }
}
