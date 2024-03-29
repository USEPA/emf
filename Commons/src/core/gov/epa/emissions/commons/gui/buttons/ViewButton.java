package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class ViewButton extends Button {

    public ViewButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_V);
    }

    public ViewButton(final Action action) {
        this("View", action);
    }
}        

