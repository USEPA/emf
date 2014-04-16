package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Confirm;

import java.awt.Component;
import javax.swing.JOptionPane;

public class YesNoDialog implements Confirm {

    private Component parent;

    private Object message;

    private String title;

    public YesNoDialog(Component component, String title, Object message) {
        this.title = title;
        this.parent = component;
        this.message = message;
    }

    public boolean confirm() {
        int option = show();
        return option == JOptionPane.YES_OPTION || option == JOptionPane.OK_OPTION;
    }

    private int show() {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

}
