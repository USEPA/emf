package gov.epa.emissions.framework.services.cost.controlStrategy.io;

public class CSCountyFileFormat implements CSCountyFormat {

    private String[] cols;

    public CSCountyFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "FIPs"};
        return cols;
    }

    public String identify() {
        return "Control Strategy County";
    }

    public String[] cols() { 
        return cols;
    }

}
