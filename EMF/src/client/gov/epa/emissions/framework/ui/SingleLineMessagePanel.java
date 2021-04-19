package gov.epa.emissions.framework.ui;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class SingleLineMessagePanel extends MessagePanel {

    private JLabel label;

    private String message;

    private Color background;
    
    private Boolean requestFocus = false;
    
    public SingleLineMessagePanel() {
        this(true);
    }

    public SingleLineMessagePanel(Boolean requestFocus) {
        this.requestFocus = requestFocus;
        background = super.getBackground();
        label = new JLabel(" ");
        label.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void focusLost(FocusEvent e) {
                label.setBorder(null);
            }
        });
        super.add(label);

        super.setVisible(true);
    }

    public void clear() {
        message = "";
        label.setText(" ");
        label.setFocusable(false);
        super.setBackground(background);
    }

    public void setMessage(String message, Color color) {       
        clear();

        this.message = message;
        label.setForeground(color);
        super.setBackground(new Color(227, 224, 251));
        label.setText(message);
        label.setFocusable(true);
        if (requestFocus)
            label.requestFocusInWindow();

        super.setVisible(true);
        super.validate();
    }

    public String getMessage() {
        return message;
    }

}
