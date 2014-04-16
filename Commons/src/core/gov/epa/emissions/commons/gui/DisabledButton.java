package gov.epa.emissions.commons.gui;

import javax.swing.JButton;


public class DisabledButton extends JButton {

    public DisabledButton(String label) {

        super(label);
        this.setName(toCanonicalName(label));
        this.setEnabled(false);
    }

    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(false);
    }


    private String toCanonicalName(String name) {

        name = name.trim().replaceAll(" ", "");
        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }
}
