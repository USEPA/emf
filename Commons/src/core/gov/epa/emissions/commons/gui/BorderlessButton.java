package gov.epa.emissions.commons.gui;

import java.awt.Insets;

import javax.swing.Action;

public class BorderlessButton extends Button {

    public BorderlessButton(String label, final Action action) {
        super(label, action);
        super.setMargin(new Insets(2, 2, 2, 2));
    }

}
