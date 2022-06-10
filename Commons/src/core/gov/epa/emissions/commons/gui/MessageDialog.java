package gov.epa.emissions.commons.gui;

import javax.swing.JOptionPane;
import java.awt.Component;

public class MessageDialog {

    private String message;

    private String title;

    private Component parentWindow;

    public MessageDialog(String message, String title, Component parentWindow) {
        this.message = message;
        this.title = title;
        this.parentWindow = parentWindow;

    }
    
    public void prompt() {
        JOptionPane.showMessageDialog(parentWindow, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
}
