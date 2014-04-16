package gov.epa.emissions.framework.client.sms.sectorscenario.viewer;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class ViewSectorScenarioWindow extends EditSectorScenarioWindow implements ViewSectorScenarioView {

    public ViewSectorScenarioWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        
        super("View Sector Scenario",desktopManager, session, parentConsole);

    }

    public void observe(ViewSectorScenarioPresenter presenter) {
        presenter.getClass();
    }

    public void display(SectorScenario sectorScenario) throws EmfException {
        super.display(sectorScenario);
    }

    public void viewOnly() {
        saveButton.setVisible(false);
        runButton.setVisible(false);
        refreshButton.setVisible(false);
        stopButton.setVisible(false);
    }

    public void observe(EditSectorScenarioPresenter presenter) {
        this.presenter = presenter;
    }

    public void run(SectorScenario sectorScenario) {
        // NOTE Auto-generated method stub
        
    }

    public void windowClosing() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }


}