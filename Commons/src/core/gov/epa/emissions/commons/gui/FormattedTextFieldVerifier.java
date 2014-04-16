package gov.epa.emissions.commons.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class FormattedTextFieldVerifier extends InputVerifier {
    private MessageBoard messagePanel;
    
    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    
    public FormattedTextFieldVerifier(MessageBoard messagePanel) {
        this.messagePanel = messagePanel;
    }
    
    public boolean verify(JComponent input) {
        JFormattedTextField ftf = (JFormattedTextField) input;
        AbstractFormatter formatter = ftf.getFormatter();
        String text = ftf.getText();
        if (text.trim().length() == 0) {// need not validate empty field
            return true;
        }
        try {
            formatter.stringToValue(text);
            messagePanel.clear();
        } catch (ParseException pe) {
            messagePanel.setError("Invalid date - " + text + ".  Please use the format - "
                    + DATE_FORMATTER.toPattern());
            return false;
        }

        return true;
    }

    public boolean shouldYieldFocus(JComponent input) {
        return verify(input);
    }

}
