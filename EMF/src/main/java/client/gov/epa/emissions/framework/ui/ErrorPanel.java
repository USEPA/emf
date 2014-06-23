package gov.epa.emissions.framework.ui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ErrorPanel extends JPanel {

    public ErrorPanel(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);
    }

}
