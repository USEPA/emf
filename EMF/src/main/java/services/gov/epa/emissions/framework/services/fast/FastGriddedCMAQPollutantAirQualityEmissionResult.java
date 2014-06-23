package gov.epa.emissions.framework.services.fast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FastGriddedCMAQPollutantAirQualityEmissionResult {

    private String sector;

    private String cmaqPollutant;

    private FastGriddedInventoryPollutantAirQualityEmissionResult[] inventoryPollutantResults = new FastGriddedInventoryPollutantAirQualityEmissionResult[] {};

    public FastGriddedCMAQPollutantAirQualityEmissionResult(String sector, String cmaqPollutant) {
        super();
        this.sector = sector;
        this.cmaqPollutant = cmaqPollutant;
    }

    public FastGriddedCMAQPollutantAirQualityEmissionResult() {
        // NOTE Auto-generated constructor stub
    }

    // private Map<String, FastCMAQInventoryPollutantResult> cmaqInventoryPollutantResults;
    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void setCmaqPollutant(String cmaqPollutant) {
        this.cmaqPollutant = cmaqPollutant;
    }

    public String getCmaqPollutant() {
        return cmaqPollutant;
    }

    public void addCmaqInventoryPollutantResults(FastGriddedInventoryPollutantAirQualityEmissionResult result) {
        List<FastGriddedInventoryPollutantAirQualityEmissionResult> inventoryPollutantResultList = new ArrayList<FastGriddedInventoryPollutantAirQualityEmissionResult>();
        inventoryPollutantResultList.addAll(Arrays.asList(inventoryPollutantResults));
        inventoryPollutantResultList.add(result);
        this.inventoryPollutantResults = inventoryPollutantResultList.toArray(new FastGriddedInventoryPollutantAirQualityEmissionResult[0]);
    }

    public void setCmaqInventoryPollutantResults(FastGriddedInventoryPollutantAirQualityEmissionResult[] cmaqInventoryPollutantResults) {
        this.inventoryPollutantResults = cmaqInventoryPollutantResults;
    }

    public FastGriddedInventoryPollutantAirQualityEmissionResult[] getCmaqInventoryPollutantResults() {
        return inventoryPollutantResults;
    }

    public double[][] getEmission() {
        double[][] emission = new double[][] {};
        int nCols = 0;
        int nRows = 0;
        //determine dimensions of array
        for (FastGriddedInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
            double[][] resultEmission = result.getEmission();
            if (resultEmission != null) {
                nCols = resultEmission.length;
                nRows = resultEmission[nCols - 1].length;
                break;
            }
        }
        //set array dimensions...
        emission = new double[nCols][nRows];
        for (FastGriddedInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
            double[][] resultEmission = result.getEmission();
            if (resultEmission != null) {
                nCols = resultEmission.length;
                nRows = resultEmission[nCols - 1].length;
//                emission = new double[nCols][nRows];
                for (int x = 1; x <= nCols; x++) {
                    for (int y = 1; y <= nRows; y++) {
                        emission[x - 1][y - 1] += resultEmission[x - 1][y - 1];
                    }
                }
            }
        }
        return emission;
    }

    public double[][] getAirQuality() {
        double[][] airQuality = new double[][] {};
        int nCols = 0;
        int nRows = 0;
        for (FastGriddedInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
            double[][] resultAirQuality = result.getAirQuality();
            if (resultAirQuality != null) {
                nCols = resultAirQuality.length;
                nRows = resultAirQuality[nCols - 1].length;
                break;
            }
        }
        //set array dimensions...
        airQuality = new double[nCols][nRows];
        for (FastGriddedInventoryPollutantAirQualityEmissionResult result : inventoryPollutantResults) {
            double[][] resultAirQuality = result.getAirQuality();
            if (resultAirQuality != null) {
                nCols = resultAirQuality.length;
                nRows = resultAirQuality[nCols - 1].length;
//                airQuality = new double[nCols][nRows];
                double adjustmentFactor = result.getAdjustmentFactor();
                for (int x = 1; x <= nCols; x++) {
                    for (int y = 1; y <= nRows; y++) {
                        airQuality[x - 1][y - 1] += adjustmentFactor * resultAirQuality[x - 1][y - 1];
                    }
                }
            //if any of the air quality numbers are null, than set to unknown since not all info is available...
            } else {
                return null;
          
            }
        }
        return airQuality;
    }

}
