package gov.epa.emissions.framework.services.fast;

import java.io.Serializable;

public class FastAnalysisRun implements Serializable {

    private FastRun fastRun;

    private Grid grid;

    private String type;

    private Double adjustmentFactor = 1.0D;
    
    public static final String BASELINE_TYPE = "B";

    public static final String SENSITIVITY_TYPE = "S";

    public FastAnalysisRun() {
        //
    }

    private FastAnalysisRun(FastRun fastRun, String type) {
        this.fastRun = fastRun;
        this.grid = fastRun.getGrid();
        this.type = type;
    }

    public static FastAnalysisRun createBaselineRun(FastRun fastRun) {
        return new FastAnalysisRun(fastRun, BASELINE_TYPE);
    }

    public static FastAnalysisRun createSensitivityRun(FastRun fastRun) {
        return new FastAnalysisRun(fastRun, SENSITIVITY_TYPE);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FastAnalysisRun)) {
            return false;
        }

        FastAnalysisRun other = (FastAnalysisRun) obj;

        return (fastRun.equals(other.getFastRun()) && grid.equals(other.getGrid()));
    }

    public int hashCode() {
        return (this.fastRun.hashCode() + "_" + this.grid.hashCode()).hashCode();
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setFastRun(FastRun fastRun) {
        this.fastRun = fastRun;
    }

    public FastRun getFastRun() {
        return fastRun;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setAdjustmentFactor(Double adjustmentFactor) {
        this.adjustmentFactor = (adjustmentFactor != null ? adjustmentFactor : 1.0D);
    }

    public Double getAdjustmentFactor() {
        return adjustmentFactor;
    }
    
    public String toString() {
        return this.fastRun.toString();
    }
}