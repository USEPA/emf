package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEfficiencyFileFormat implements CMFileFormat {

    private String[] cols;

    public CMEfficiencyFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "Pollutant", "Locale", "Effective Date", "ExistingMeasureAbbr",
                "NEIExistingDevCode", "ControlEfficiency", "CostYear", "CostPerTon", "RuleEff", "RulePen",
                "EquationType", "CapRecFactor", "DiscountRate", "Details", "RefYrCostPerTon" };
        return cols;
    }

    public String identify() {
        return "Control Measure Efficiency";
    }

    public String[] cols() {
        return cols;
    }

}
