package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class CopyButton extends Button {

    public CopyButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_C);
    }
    
    public CopyButton(final Action action) {
        this("Copy", action);
    }
}
    

