package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class BrowseButton extends Button {

    public BrowseButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('b');
    }

    public BrowseButton(final Action action) {
        this("Browse", action);
    }
}
    

