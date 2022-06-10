package gov.epa.emissions.commons.gui;

import javax.swing.*;
import java.awt.*;

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
