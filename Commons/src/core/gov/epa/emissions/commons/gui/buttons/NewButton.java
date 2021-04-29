package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class NewButton extends Button {

    public NewButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_N);
    }

    public NewButton(final Action action) {
        this("New", action);
    }
}
    

