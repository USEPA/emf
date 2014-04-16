package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMSCCsFileFormat implements CMFileFormat {

    private String[] cols;

    public CMSCCsFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "SCC", "Status" };
        return cols;
    }

    public String identify() {
        return "Control Measure SCC";
    }

    public String[] cols() {
        return cols;
    }

}
