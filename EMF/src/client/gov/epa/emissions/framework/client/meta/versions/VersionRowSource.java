package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.Date;

public class VersionRowSource implements RowSource {

    private Version source;

    private Boolean selected;

    public VersionRowSource(Version source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), Integer.valueOf(source.getVersion()),
                Long.valueOf(source.getBase()), source.getCreator().getName(),
                Boolean.valueOf(source.isFinalVersion()), source.getIntendedUse(), source.getNumberRecords(),
                format(source.getLastModifiedDate()), source.getDescription() };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        default:
            throw new RuntimeException("cannot edit column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        // FIXME: validate row source
    }
}