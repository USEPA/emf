package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

public class ViewControlMeasureWindow extends EditControlMeasureWindow {

    public ViewControlMeasureWindow(EmfConsole parent, EmfSession session, DesktopManager desktopManager, CostYearTable costYearTable) {
        super(parent, session, desktopManager, costYearTable);
    }

    public void display(ControlMeasure measure) {

        setWindowTitle(measure);
        buildDisplay(measure);
        viewOnly();
        super.display();
        super.resetChanges();
    }

    private void setWindowTitle(ControlMeasure measure) {

        this.setTitle("View Control Measure: " + measure.getName());
        this.setName("viewControlMeasure" + measure.getId());
    }

    //disable gui items via this method for "View" only purposes
    private void viewOnly() {
        
        saveButton.setVisible(false);
        controlMeasureSccTabView.viewOnly();
        controlMeasureEfficiencyTabView.viewOnly();
        editableCMSummaryTabView.viewOnly();
        controlMeasureEquationTabView.viewOnly();
        controlMeasurePropertyTabView.viewOnly();
        controlMeasureReferencesTabView.viewOnly();
    }
    
    public void close() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }
    
    public void windowClosing() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }

}
