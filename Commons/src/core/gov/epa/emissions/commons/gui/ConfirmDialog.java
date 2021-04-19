package gov.epa.emissions.commons.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

public class ConfirmDialog {

    private String message;

    private String title;

    private Component parentWindow;
    
    public ConfirmDialog(String message, String title, Component parentWindow) {
        this.message = message;
        this.title = title;
        this.parentWindow = parentWindow;

    }
    
    public boolean confirm() {
        int option = JOptionPane.showConfirmDialog(parentWindow, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return (option == 0);
    }
    
}
