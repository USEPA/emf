package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class CancelButton extends Button {

    public CancelButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_L);
    }

    public CancelButton(final Action action) {
        this("Cancel", action);
    }
}
