package gov.epa.emissions.framework.services.cost.controlmeasure.io;

public class CMSummaryFileFormat implements CMFileFormat {

    private String[] cols;

    public CMSummaryFileFormat() {
        this.cols = createCols();
    }

    private String[] createCols() {
        String[] cols = { "CMName", "CMAbbreviation", 
                "MajorPoll", "ControlTechnology", 
                "SourceGroup", "Sector",
                "Class", "EquipLife", 
                "NEIDeviceCode", "DateReviewed", 
                "DataSource", "Description", };
        return cols;
    }

    public String identify() {
        return "Control Measure Summary";
    }

    public String[] cols() {
        return cols;
    }

}
