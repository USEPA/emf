package gov.epa.emissions.framework.services.fast;

public class FastGriddedInventoryPollutantAirQualityEmissionResult {

    private String inventoryPollutant;

    private double[][] emission;

    private double[][] airQuality;

    private float adjustmentFactor;

    private String tranferCoefficient;

    public FastGriddedInventoryPollutantAirQualityEmissionResult(String inventoryPollutant, float adjustmentFactor,
            String tranferCoefficient, int nCols, int nRows) {
        this.inventoryPollutant = inventoryPollutant;
        this.adjustmentFactor = adjustmentFactor;
        this.tranferCoefficient = tranferCoefficient;
        this.emission = new double[nCols][nRows];
        this.airQuality = null;//new double[nCols][nRows];
    }

    public void setPollutant(String inventoryPollutant) {
        this.inventoryPollutant = inventoryPollutant;
    }

    public String getPollutant() {
        return inventoryPollutant;
    }

    public void setEmission(double[][] emission) {
        this.emission = emission;
    }

    public double[][] getEmission() {
        return emission;
    }

    public void setAirQuality(double[][] airQuality) {
        this.airQuality = airQuality;
    }

    public double[][] getAirQuality() {
        return airQuality;
    }

    public void setAdjustmentFactor(float adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }

    public float getAdjustmentFactor() {
        return adjustmentFactor;
    }

    public void setTranferCoefficient(String tranferCoefficient) {
        this.tranferCoefficient = tranferCoefficient;
    }

    public String getTranferCoefficient() {
        return tranferCoefficient;
    }
}
