package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.framework.ui.YesNoDialog;

import java.awt.Component;

public class WidgetChangesMonitor {
    private Changeables list;

    private Component window;

    public WidgetChangesMonitor(Changeables list, Component componet) {
        this.list = list;
        this.window = componet;
    }

    public void resetChanges() {
        list.resetChanges();
        list.onChanges();
    }

    public boolean shouldDiscardChanges() {
        String message = "Would you like to discard the changes " + System.getProperty("line.separator")
                + " and close the current window?";
        String title = "Discard changes?";
        if (list.hasChanges()) {
            YesNoDialog dialog = new YesNoDialog(window, title, message);
            return dialog.confirm();
        }

        return true;
    }

    public boolean shouldProcessChanges(String title, String message) {

        boolean retVal = false;
        if (list.hasChanges()) {
            retVal = new YesNoDialog(window, title, message).confirm();
        }

        return retVal;
    }
}
