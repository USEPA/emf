package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.Date;

public class NotesRowSource implements RowSource {

    private DatasetNote datasetNote;

    public NotesRowSource(DatasetNote source) {
        this.datasetNote = source;
    }

    public Object[] values() {
        return new Object[] { Long.valueOf(datasetNote.getNote().getId()), datasetNote.getNote().getName(), datasetNote.getNote().getNoteType().getType(),
                Long.valueOf(datasetNote.getVersion()), datasetNote.getNote().getCreator().getName(), format(datasetNote.getNote().getDate()), datasetNote.getNote().getReferences(),
                datasetNote.getNote().getDetails() };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public Object source() {
        return datasetNote;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}