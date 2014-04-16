package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class OKButton extends Button {
    
    public OKButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('O');
    }
    
    public OKButton(final Action action) {
        this("OK", action);
    }
}    

