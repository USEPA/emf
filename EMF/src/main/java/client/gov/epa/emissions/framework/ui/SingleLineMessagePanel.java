package gov.epa.emissions.framework.ui;

import java.awt.Color;

import javax.swing.JLabel;

public class SingleLineMessagePanel extends MessagePanel {

    private JLabel label;

    private String message;

    private Color background;

    public SingleLineMessagePanel() {
        background = super.getBackground();
        label = new JLabel(" ");
        super.add(label);

        super.setVisible(true);
    }

    public void clear() {
        message = "";
        label.setText(" ");
        super.setBackground(background);
    }

    public void setMessage(String message, Color color) {       
        clear();

        this.message = message;
        label.setForeground(color);
        super.setBackground(new Color(227, 224, 251));
        label.setText(message);

        super.setVisible(true);
        super.validate();
    }

    public String getMessage() {
        return message;
    }

}
