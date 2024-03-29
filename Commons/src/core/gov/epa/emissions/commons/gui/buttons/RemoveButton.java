package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class RemoveButton extends Button {

    public RemoveButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_O);
    }

    public RemoveButton(final Action action) {
        this("Remove", action);
    }
}        

