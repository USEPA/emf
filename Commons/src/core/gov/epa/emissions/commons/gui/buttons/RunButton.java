package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class RunButton extends Button {

    public RunButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_U);
    }

    public RunButton(final Action action) {
        this("Run", action);
    }
}        

