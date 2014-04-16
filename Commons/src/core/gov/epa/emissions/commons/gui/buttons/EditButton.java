package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class EditButton extends Button {

    public EditButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('E');
    }

    public EditButton(final Action action) {
        this("Edit", action);
    }
}


