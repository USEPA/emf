package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.services.EmfException;

public class IntTextField extends TextField implements Changeable {

    private int min;

    private int max;

    public IntTextField(String name, int min, int max, int size) {
        super(name, size);
        //super.setText("" + min);
        this.min = min;
        this.max = max;
    }

    public int getValue() throws EmfException {
        String text = getText().trim();
        int value = parseInt(text);
        return validate(value, min, max);
    }

    private int validate(int value, int min, int max) throws EmfException {
        if (value < min || value > max) {
            throw new EmfException("Invalid value: " + value + ", Input value between " + min + " and " + max);
        }
        return value;
    }

    private int parseInt(String text) throws EmfException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new EmfException("Invalid value: " + text + ", Input a int value");
        }
    }

    public void setValue(int  value) {
        setText(""+ value);
    }

}
