package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.services.EmfException;

public class DoubleTextField extends TextField implements Changeable {

    private double min;

    private double max;

    public DoubleTextField(String name, int size) {
        this(name, Double.MIN_VALUE, Double.MAX_VALUE, size);
    }

    public DoubleTextField(String name, double min, double max, int size) {
        super(name, size);
        super.setText("" + min);
        this.min = min;
        this.max = max;
    }

    public double getValue() throws EmfException {
        String text = getText().trim();
        double value = parseDouble(text);
        return validate(value, min, max);
    }

    public void setValue(double value) {
        setText("" + value);
    }

    private double validate(double value, double min, double max) throws EmfException {
        if (value < min || value > max) {
            throw new EmfException("Invalid value for " + getName() + ": " + value + ", Input a value between " + min
                    + " and " + max);
        }
        return value;
    }

    private double parseDouble(String text) throws EmfException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new EmfException("Invalid value for " + getName() + ": " + text + ", Input a double value");
        }
    }

}
