package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class IconButton extends Button {

    public IconButton(String name, String tooltip, ImageIcon icon, Action action) {
        super(name, action);
        super.setText("");// no label
        
        super.setIcon(icon);
        super.setToolTipText(tooltip);
        
        super.setBorderPainted(false);
        super.setVerticalTextPosition(AbstractButton.BOTTOM);
        super.setHorizontalTextPosition(AbstractButton.CENTER);
    }

}
