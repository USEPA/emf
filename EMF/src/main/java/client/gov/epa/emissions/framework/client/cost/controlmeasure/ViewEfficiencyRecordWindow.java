package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class ViewEfficiencyRecordWindow extends EditEfficiencyRecordWindow {

    public ViewEfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session, CostYearTable costYearTable) {
        super("View Efficiency Record", changeablesList, desktopManager, session, costYearTable);
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        super.display(measure, record);
        populateFields();
        resetChanges();
    }

    @Override
    public boolean shouldDiscardChanges() {
        return true;
    }
}