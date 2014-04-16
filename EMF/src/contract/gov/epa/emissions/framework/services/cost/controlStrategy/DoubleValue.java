package gov.epa.emissions.framework.services.cost.controlStrategy;

public class DoubleValue {

    private double value;

    public DoubleValue() {
        //
    }
    
    public DoubleValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
