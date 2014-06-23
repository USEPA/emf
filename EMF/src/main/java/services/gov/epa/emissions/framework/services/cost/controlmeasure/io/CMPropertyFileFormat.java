package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMPropertyFileFormat implements CMFileFormat {

    private String[] cols;

    public CMPropertyFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMAbbreviation", "Name", 
                "Category", "Units", "Data_Type", 
                "DB_FieldName", "Value"};
        return cols;
    }

    public String identify() {
        return "Control Measure Property";
    }

    public String[] cols() {
        return cols;
    }

}
