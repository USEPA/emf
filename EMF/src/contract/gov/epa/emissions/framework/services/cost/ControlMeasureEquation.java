package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;

public class ControlMeasureEquation implements Serializable {

    private int id;

    private EquationType equationType;

    private Pollutant pollutant;

    private int costYear;

    private Double value1, value2, value3;
    private Double value4, value5, value6;
    private Double value7, value8, value9;
    private Double value10, value11;
    
    public ControlMeasureEquation() {// persistence/bean
    }

    public ControlMeasureEquation(EquationType equationType) {
        this();
        this.equationType = equationType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEquationType(EquationType equationType) {
        this.equationType = equationType;
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControlMeasureEquation)) {
            return false;
        }

        ControlMeasureEquation other = (ControlMeasureEquation) obj;

        return (
                id == other.getId() 
                && (
                        (equationType == null && other.getEquationType() == null) 
                        || (
                                (equationType == null && other.getEquationType() != null)
                                || (equationType != null && other.getEquationType() == null)
                        )
                        || equationType.equals(other.getEquationType())
                )
                && (
                        (pollutant == null && other.getPollutant() == null) 
                        || (
                                (pollutant == null && other.getPollutant() != null)
                                || (pollutant != null && other.getPollutant() == null)
                        )
                        || pollutant.equals(other.getPollutant())
                ));
    }

    public int hashCode() {
        return equationType.hashCode();
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setValue1(Double value1) {
        this.value1 = value1;
    }

    public Double getValue1() {
        return value1;
    }

    public void setValue2(Double value2) {
        this.value2 = value2;
    }

    public Double getValue2() {
        return value2;
    }

    public void setValue3(Double value3) {
        this.value3 = value3;
    }

    public Double getValue3() {
        return value3;
    }

    public void setValue4(Double value4) {
        this.value4 = value4;
    }

    public Double getValue4() {
        return value4;
    }

    public void setValue5(Double value5) {
        this.value5 = value5;
    }

    public Double getValue5() {
        return value5;
    }

    public void setValue6(Double value6) {
        this.value6 = value6;
    }

    public Double getValue6() {
        return value6;
    }

    public void setValue7(Double value7) {
        this.value7 = value7;
    }

    public Double getValue7() {
        return value7;
    }

    public void setValue8(Double value8) {
        this.value8 = value8;
    }

    public Double getValue8() {
        return value8;
    }

    public void setValue9(Double value9) {
        this.value9 = value9;
    }

    public Double getValue9() {
        return value9;
    }

    public void setValue10(Double value10) {
        this.value10 = value10;
    }

    public Double getValue10() {
        return value10;
    }

    public void setValue11(Double value11) {
        this.value11 = value11;
    }

    public Double getValue11() {
        return value11;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
    }

    public int getCostYear() {
        return costYear;
    }
}
