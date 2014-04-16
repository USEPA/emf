package gov.epa.emissions.commons.gui;

import java.awt.Insets;

public class DisabledBorderlessButton extends DisabledButton {

    public DisabledBorderlessButton(String label) {
        super(label);
        super.setMargin(new Insets(2, 2, 2, 2));
    }

}
