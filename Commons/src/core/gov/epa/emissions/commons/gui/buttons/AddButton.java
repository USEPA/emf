package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class AddButton extends Button {

    public AddButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('a');
    }

    public AddButton(final Action action) {
        this("Add", action);
    }
}
    

