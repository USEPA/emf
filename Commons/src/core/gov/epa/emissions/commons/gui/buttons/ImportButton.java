package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.Action;

public class ImportButton extends Button {

    public ImportButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic('i');
    }

    public ImportButton(final Action action) {
        this("Import", action);
    }
}
    

