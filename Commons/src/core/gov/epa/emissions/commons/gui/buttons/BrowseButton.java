package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class BrowseButton extends Button {

    public BrowseButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_B);
    }

    public BrowseButton(final Action action) {
        this("Browse", action);
    }
}
    

