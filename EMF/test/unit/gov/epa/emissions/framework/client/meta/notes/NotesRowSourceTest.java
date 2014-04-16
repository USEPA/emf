package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.NoteType;

import java.util.Date;

import junit.framework.TestCase;

public class NotesRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        DatasetNote note = new DatasetNote();
        note.setId(45);
        note.getNote().setName("note0");
        note.setVersion(2);
        note.getNote().setCreator(new User());
        note.getNote().setDate(new Date());
        note.getNote().setNoteType(new NoteType("type0"));

        NotesRowSource source = new NotesRowSource(note);

        Object[] values = source.values();
        assertEquals(8, values.length);
        assertEquals(new Long(note.getId()), values[0]);
        assertEquals(note.getNote().getName(), values[1]);
        assertEquals(note.getNote().getNoteType().getType(), values[2]);
        assertEquals(note.getVersion(), ((Long)values[3]).longValue());
        assertEquals(note.getNote().getCreator().getName(), values[4]);

        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(note.getNote().getDate()), values[5]);

        assertEquals(note.getNote().getReferences(), values[6]);
        assertEquals(note.getNote().getDetails(), values[7]);
    }

    public void testShouldTrackOriginalSource() {
        DatasetNote note = new DatasetNote();
        NotesRowSource rowSource = new NotesRowSource(note);

        assertEquals(note, rowSource.source());
    }
}
