package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.TableFormat;


public class DatasetTypeUnit implements FormatUnit {

    private TableFormat tableFormat;

    private FileFormat fileFormat;

    private boolean required;

    private InternalSource internalSource;

    public DatasetTypeUnit(TableFormat tableFormat, FileFormat fileFormat) {
        this.tableFormat = tableFormat;
        this.fileFormat = fileFormat;
        this.required = false;
    }

    public DatasetTypeUnit(TableFormat tableFormat, FileFormat fileFormat, boolean required) {
        this.tableFormat = tableFormat;
        this.fileFormat = fileFormat;
        this.required = required;
    }

    public FileFormat fileFormat() {
        return fileFormat;
    }

    public TableFormat tableFormat() {
        return tableFormat;
    }

    public boolean isRequired() {
        return required;
    }

    public InternalSource getInternalSource() {
        return internalSource;
    }

    public void setInternalSource(InternalSource internalSource) {
        this.internalSource = internalSource;
    }

}
