package gov.epa.emissions.commons.gui.buttons;

import gov.epa.emissions.commons.gui.Button;

import java.awt.event.KeyEvent;

import javax.swing.Action;

public class ExportButton extends Button {

    public ExportButton(String label, final Action action) {
        super(label, action);
        this.setMnemonic(KeyEvent.VK_X);
    }

    public ExportButton(final Action action) {
        this("Export", action);
    }
}
