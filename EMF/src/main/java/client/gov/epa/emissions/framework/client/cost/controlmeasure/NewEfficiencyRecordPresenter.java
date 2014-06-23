package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.Date;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class NewEfficiencyRecordPresenter extends EfficiencyRecordPresenter {

    private NewEfficiencyRecordView view;
    private ControlMeasureService cmService;
    private ControlMeasure measure;
    private EmfSession session;

    public NewEfficiencyRecordPresenter(ControlMeasureEfficiencyTabView parentView, NewEfficiencyRecordView view, 
            EmfSession session, ControlMeasure measure) {
        super(parentView);
        this.view = view;
        this.measure = measure;
        this.session = session;
        cmService = session.controlMeasureService();
    }

    public void display(ControlMeasure measure) {
        view.observe(this);
        view.display(measure, newRecord());
    }

    private EfficiencyRecord newRecord() {
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setRecordId(newRecordId());
        efficiencyRecord.setControlMeasureId(measure.getId());
        efficiencyRecord.setLastModifiedTime(new Date());
        efficiencyRecord.setLastModifiedBy(session.user().getName());
        return efficiencyRecord;
    }

    private int newRecordId() {
        int maxRecordId = maxRecordId(parentView.records());
        
        return ++maxRecordId;
    }

    private int maxRecordId(EfficiencyRecord[] records) {
        int id = 0;
        for (int i = 0; i < records.length; i++) {
            if (records[i].getRecordId() > id)
                id = records[i].getRecordId();
        }
        return id;
    }

    public void add(EfficiencyRecord record) throws EmfException {
        record.setId(cmService.addEfficiencyRecord(record));
        parentView.add(record);
    }
}