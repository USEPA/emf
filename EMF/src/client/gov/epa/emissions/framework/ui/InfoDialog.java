package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Confirm;

import java.awt.Component;
import javax.swing.JOptionPane;

public class InfoDialog implements Confirm {

    private Component parent;

    private Object message;

    private String title;

    public InfoDialog(Component component, String title, Object message) {
        this.title = title;
        this.parent = component;
        this.message = message;
    }

    public boolean confirm() {
        int option = show();
        return option == JOptionPane.YES_OPTION || option == JOptionPane.OK_OPTION;
    }

    private int show() {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

}
