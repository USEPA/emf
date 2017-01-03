package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMEquationFileFormatv2 extends CMEquationFileFormat {

    protected String[] createCols() {
        String[] cols = { "CMAbbreviation", "CMEqnType", 
                "Pollutant", "CostYear", 
                "Var1", "Var2", 
                "Var3", "Var4", 
                "Var5", "Var6", 
                "Var7", "Var8", 
                "Var9", "Var10", 
                "Var11", "Var12"
                };
        return cols;
    }
    
}
