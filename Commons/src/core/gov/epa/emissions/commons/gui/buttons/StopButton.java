package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class StopButton extends Button {

    public StopButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_T);
    }

    public StopButton(final Action action) {
        this("Stop", action);
    }
}        

