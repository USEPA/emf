package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.DefaultChangeables;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.ui.Row;

import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class NotesTableDataTest extends EmfMockObjectTestCase {

    private NotesTableData data;

    private DatasetNote note0;

    private DatasetNote note1;

    protected void setUp() {
        note0 = new DatasetNote();
        Note note=new Note(); 
        note.setId(234);
        note.setName("note0");
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type0"));
        note0.setNote(note);

        note1 = new DatasetNote();
        Note noted = new Note();
        noted.setName("note1");
        noted.setCreator(new User());
        noted.setDate(new Date(note0.getNote().getDate().getTime() + 12000));
        noted.setNoteType(new NoteType("type1"));
        note1.setNote(noted);
        data = new NotesTableData(new DatasetNote[] { note0, note1 });
    }

    public void testShouldHaveSevenColumns() {
        String[] columns = data.columns();
        assertEquals(8, columns.length);
        assertEquals("Id", columns[0]);
        assertEquals("Summary", columns[1]);
        assertEquals("Type", columns[2]);
        assertEquals("Version", columns[3]);
        assertEquals("Creator", columns[4]);
        assertEquals("Date", columns[5]);
        assertEquals("References", columns[6]);
        assertEquals("Details", columns[7]);
    }

    public void testShouldReturnCorrectColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Long.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
    }

    public void testAllColumnsShouldBeUneditable() {
        for (int i = 0; i < 7; i++)
            assertFalse("All cells should be editable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        Note note=note0.getNote();
        assertEquals(Long.valueOf(note0.getId()), row.getValueAt(0));
        assertEquals(note.getName(), row.getValueAt(1));
        assertEquals(note.getNoteType().getType(), row.getValueAt(2));
        assertEquals(note0.getVersion(), ((Long) row.getValueAt(3)).longValue());
        assertEquals(note.getCreator().getName(), row.getValueAt(4));

        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(note.getDate()), row.getValueAt(5));

        assertEquals(note.getDetails(), row.getValueAt(6));
        assertEquals(note.getReferences(), row.getValueAt(7));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(note0, data.element(0));
        assertEquals(note1, data.element(1));
    }

    public void testShouldAddRowOnAddingNewNote() {
        int count = data.rows().size();
        DatasetNote daNote = new DatasetNote();
        Note note = new Note();
        note.setName("note");
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type"));

        Mock observer = mock(ChangeObserver.class);
        expects(observer, 2, "signalSaved");
        Changeables changeablesList = new DefaultChangeables((ChangeObserver) observer.proxy());
        daNote.setNote(note);
        
        data.observe(changeablesList);
        data.add(daNote);
        data.add(daNote);

        assertEquals(count + 2, data.rows().size());
        assertEquals(2, data.additions().length);
    }
}
