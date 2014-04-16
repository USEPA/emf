package gov.epa.emissions.commons.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattedDateField extends FormattedTextField implements Changeable {

    private SimpleDateFormat format;

    public FormattedDateField(String name, Date value, SimpleDateFormat format, MessageBoard messagePanel) {
        super(name, value, format, messagePanel);
        this.format = format;
    }

    public Date value() {
        String value = super.getText();
        if (value == null || value.length() == 0)
            return null;

        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("could not parse Date - " + value + ". Expected format - " + format.toPattern());
        }
    }

}
