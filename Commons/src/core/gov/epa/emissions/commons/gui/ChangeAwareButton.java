package gov.epa.emissions.commons.gui;

import javax.swing.Action;

public class ChangeAwareButton extends Button implements ChangeObserver{

    public ChangeAwareButton(String label, Action action) {
        super(label, action);
    }

    public void signalChanges() {
        setEnabled(true);
    }

    public void signalSaved() {
        setEnabled(false);
    }

}
