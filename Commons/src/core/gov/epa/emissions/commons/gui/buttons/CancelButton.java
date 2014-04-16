package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class CancelButton extends Button {

    public CancelButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('l');
    }

    public CancelButton(final Action action) {
        this("Cancel", action);
    }
}
