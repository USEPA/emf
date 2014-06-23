package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMReferenceFileFormat implements CMFileFormat {

    private String[] cols;

    public CMReferenceFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "Datasource", "Description" };
        return cols;
    }

    public String identify() {
        return "Control Measure Reference";
    }

    public String[] cols() {
        return cols;
    }

}
