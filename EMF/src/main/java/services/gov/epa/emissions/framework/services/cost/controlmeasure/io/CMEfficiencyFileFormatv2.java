package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEfficiencyFileFormatv2 implements CMFileFormat {

    private String[] cols;

    public CMEfficiencyFileFormatv2() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "Pollutant", "Locale", "Effective Date", "ExistingMeasureAbbr",
                "NEIExistingDevCode", "MinEmissions", "MaxEmissions", "ControlEfficiency", "CostYear", 
                "CostPerTon", "RuleEff", "RulePen", "EquationType", "CapRecFactor", 
                "DiscountRate", "Details", "RefYrCostPerTon" };
        return cols;
    }

    public String identify() {
        return "Control Measure Efficiency";
    }

    public String[] cols() {
        return cols;
    }

}
