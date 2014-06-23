package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEfficiencyFileFormatv3 implements CMFileFormat {

    private String[] cols;

    public CMEfficiencyFileFormatv3() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "Pollutant", "Locale", "Effective Date", "ExistingMeasureAbbr",
                "NEIExistingDevCode", "MinEmissions", "MaxEmissions", "ControlEfficiency", "CostYear", 
                "CostPerTon", "RuleEff", "RulePen", "EquationType", "CapRecFactor", 
                "DiscountRate", "CapAnnRatio", "IncrementalCPT", "Details", "RefYrCostPerTon" };
        return cols;
    }

    public String identify() {
        return "Control Measure Efficiency";
    }

    public String[] cols() {
        return cols;
    }

}
