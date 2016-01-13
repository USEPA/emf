package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;

import java.io.Serializable;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

public class CostYearTable implements Serializable {

    private int targetYear;

    public static int REFERENCE_COST_YEAR = 2013;

    private DoubleList deflatorValues;

    private int startYear;
    
    private int endYear;

    public CostYearTable() {
        deflatorValues = new ArrayDoubleList();
    }

    public CostYearTable(int targetYear) {
        this();
        this.targetYear = targetYear;
    }

    public int size() {
        return deflatorValues.size();
    }

    public void addFirst(int startYear, double deflator) {
        this.startYear = startYear;
        this.endYear = startYear;
        deflatorValues.add(deflator);
    }

    public void add(int year, double deflator) {
        this.endYear = year;
        deflatorValues.add(deflator);
    }

    public double factor(int year) throws EmfException {
        double yearDeflator = deflatorValue(year);
        double targetYearDeflator = deflatorValue(targetYear);
        return targetYearDeflator / yearDeflator;

    }

    public double factor(int targetYear, int referenceCostYear) throws EmfException {
        double yearDeflator = deflatorValue(referenceCostYear);
        double targetYearDeflator = deflatorValue(targetYear);
        return targetYearDeflator / yearDeflator;

    }

    private double deflatorValue(int year) throws EmfException {
        int index = year - this.startYear;
        if (index > size() - 1 || index < 0) {
//            throw new EmfException("The cost year conversion is available between 1929 to 2005");
            throw new EmfException("The cost year conversion is available between " + this.startYear + " to " + this.endYear);
        }
        return deflatorValues.get(index);
    }

    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
    }

    public int getTargetYear() {
        return this.targetYear;
    }

    public DoubleValue[] getDeflatorValues() {
        DoubleValue[] doubles = new DoubleValue[deflatorValues.size()];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = new DoubleValue();
            doubles[i].setValue(deflatorValues.get(i));
        }
        return doubles;
    }

    public void setDeflatorValues(DoubleValue[] values) {
        this.deflatorValues = new ArrayDoubleList();
        for (int i = 0; i < values.length; i++) {
            deflatorValues.add(values[i].getValue());
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
