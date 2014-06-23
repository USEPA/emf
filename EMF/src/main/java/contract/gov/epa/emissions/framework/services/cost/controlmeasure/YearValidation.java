package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;

public class YearValidation {

    private String name;

    private int startYear;

    private int endYear;

    public YearValidation(String name) {
        this.name = name;
        this.startYear = 1980;
        this.endYear = 2100;
    }

    public int value(String text) throws EmfException {
        return value(text, this.startYear, this.endYear);
    }

    public int value(String text, int startYear, int endYear) throws EmfException {
        int value = intValue(text, startYear, endYear);
        if (text.length() != 4)
            throw new EmfException(message(name, startYear, endYear));
        if (value < startYear || value > endYear) {
            throw new EmfException(message(name, startYear, endYear));
        }
        return value;
    }

    private int intValue(String text, int startYear, int endYear) throws EmfException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new EmfException(message(name, startYear, endYear));
        }
    }

    private String message(String name, int startYear, int endYear) {
        return "Please enter a " + name + " (as a four digit integer) between " + startYear + " and " + endYear + ".";
    }

}
