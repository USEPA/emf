package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEquationFileFormat implements CMFileFormat {

    private String[] cols;

    public CMEquationFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "CMEqnType", 
                "Pollutant", "CostYear", 
                "Var1", "Var2", 
                "Var3", "Var4", 
                "Var5", "Var6", 
                "Var7", "Var8", 
                "Var9", "Var10", 
                "Var11"
                };
        return cols;
    }

    public String identify() {
        return "Control Measure Equation";
    }

    public String[] cols() {
        return cols;
    }

}
