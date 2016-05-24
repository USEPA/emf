package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMSCCsFileFormatv2 implements CMFileFormat {

    private String[] cols;

    public CMSCCsFileFormatv2() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "SCC", "Status", "CombustionEfficiency" };
        return cols;
    }

    public String identify() {
        return "Control Measure SCC";
    }

    public String[] cols() {
        return cols;
    }

}
