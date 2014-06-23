package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class NewControlProgramWindow extends ControlProgramWindow {

    public NewControlProgramWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole, ControlMeasure[] controlMeasures) {
        super(desktopManager, session, parentConsole, controlMeasures);
        this.title = "New Control Program";
    }

    protected void save() throws EmfException {
        clearMessage();
        presenter.doAdd();
        messagePanel
            .setMessage("Program was saved successfully.");
        resetChanges();
    }
}