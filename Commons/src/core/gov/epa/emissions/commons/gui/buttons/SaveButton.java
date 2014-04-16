package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class SaveButton extends Button {

    public SaveButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('S');
    }
    
    public SaveButton(final Action action) {
        this("Save", action);
    }
}    

