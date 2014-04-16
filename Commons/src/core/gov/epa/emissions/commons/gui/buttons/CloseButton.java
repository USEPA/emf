package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class CloseButton extends Button {

    public CloseButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('l');
    }

    public CloseButton(final Action action) {
        this("Close", action);
    }
}
    

