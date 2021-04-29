package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class CloseButton extends Button {

    public CloseButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_L);
    }

    public CloseButton(final Action action) {
        this("Close", action);
    }
}
    

