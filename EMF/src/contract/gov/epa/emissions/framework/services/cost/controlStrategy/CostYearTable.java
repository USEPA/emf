package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;

import java.io.Serializable;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

public class CostYearTable implements Serializable {

    private int targetYear;

    public static int REFERENCE_COST_YEAR = 2013;

    private DoubleList gdpValues;

    private int startYear;
    
    private int endYear;

    public CostYearTable() {
        gdpValues = new ArrayDoubleList();
    }

    public CostYearTable(int targetYear) {
        this();
        this.targetYear = targetYear;
    }

    public int size() {
        return gdpValues.size();
    }

    public void addFirst(int startYear, double gdp) {
        this.startYear = startYear;
        this.endYear = startYear;
        gdpValues.add(gdp);
    }

    public void add(int year, double gdp) {
        this.endYear = year;
        gdpValues.add(gdp);
    }

    public double factor(int year) throws EmfException {
        double yearGdp = gdpValue(year);
        double targetYearGdp = gdpValue(targetYear);
        return targetYearGdp / yearGdp;

    }

    public double factor(int targetYear, int referenceCostYear) throws EmfException {
        double yearGdp = gdpValue(referenceCostYear);
        double targetYearGdp = gdpValue(targetYear);
        return targetYearGdp / yearGdp;

    }

    private double gdpValue(int year) throws EmfException {
        int index = year - this.startYear;
        if (index > size() - 1 || index < 0) {
//            throw new EmfException("The cost year conversion is available between 1929 to 2005");
            throw new EmfException("The cost year conversion is available between " + this.startYear + " to " + this.endYear);
        }
        return gdpValues.get(index);
    }

    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
    }

    public int getTargetYear() {
        return this.targetYear;
    }

    public DoubleValue[] getGdpValues() {
        DoubleValue[] doubles = new DoubleValue[gdpValues.size()];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = new DoubleValue();
            doubles[i].setValue(gdpValues.get(i));
        }
        return doubles;
    }

    public void setGdpValues(DoubleValue[] values) {
        this.gdpValues = new ArrayDoubleList();
        for (int i = 0; i < values.length; i++) {
            gdpValues.add(values[i].getValue());
        }
    }

    public int getStartYear() {
        return this.startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public int getEndYear() {
        return this.endYear;
    }

}
