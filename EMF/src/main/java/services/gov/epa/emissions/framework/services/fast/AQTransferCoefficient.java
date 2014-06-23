package gov.epa.emissions.framework.services.fast;

public class AQTransferCoefficient {

    private String sector;

    private String pollutant;

    private double beta1;

    private double beta2;

    public AQTransferCoefficient() {
        //
    }

    public AQTransferCoefficient(String sector, String pollutant, double beta1, double beta2) {
        this();
        this.sector = sector;
        this.pollutant = pollutant;
        this.beta1 = beta1;
        this.beta2 = beta2;
    }

    public void setPollutant(String pollutant) {
        this.pollutant = pollutant;
    }

    public String getPollutant() {
        return pollutant;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void setBeta1(double beta1) {
        this.beta1 = beta1;
    }

    public double getBeta1() {
        return beta1;
    }

    public void setBeta2(double beta2) {
        this.beta2 = beta2;
    }

    public double getBeta2() {
        return beta2;
    }
}
