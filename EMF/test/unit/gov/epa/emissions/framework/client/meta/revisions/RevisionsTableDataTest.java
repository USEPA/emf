package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.Row;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class RevisionsTableDataTest extends TestCase {

    private RevisionsTableData data;

    private Revision revision0;

    private Revision revision1;

    protected void setUp() {
        revision0 = new Revision();
        revision0.setVersion(0);
        revision0.setCreator(new User());
        revision0.setDate(new Date());
        revision0.setWhat("what0");
        revision0.setWhy("why0");
        revision0.setReferences("ref0");

        revision1 = new Revision();
        revision1.setVersion(1);
        revision1.setCreator(new User());
        revision1.setDate(new Date(revision0.getDate().getTime() + 12000));
        revision1.setWhat("what1");
        revision1.setWhy("why1");
        revision1.setReferences("ref1");

        data = new RevisionsTableData(new Revision[] { revision0, revision1 });
    }

    public void testShouldHaveSevenColumns() {
        String[] columns = data.columns();
        assertEquals(6, columns.length);
        assertEquals("What", columns[0]);
        assertEquals("Why", columns[1]);
        assertEquals("References", columns[2]);
        assertEquals("Version", columns[3]);
        assertEquals("Creator", columns[4]);
        assertEquals("Date", columns[5]);
    }

    public void testShouldReturnCorretColumnClass(){
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
    }

    public void testExceptForSelectAllOtherColumnsShouldBeUneditable() {
        assertTrue("Select column should be editable", data.isEditable(0));
        for (int i = 1; i < 7; i++) {
            assertFalse("All cells (except Select) should be uneditable", data.isEditable(1));
        }
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);

        assertEquals(revision0.getWhat(), row.getValueAt(0));
        assertEquals(revision0.getWhy(), row.getValueAt(1));
        assertEquals(revision0.getReferences(), row.getValueAt(2));
        assertEquals(revision0.getVersion(), ((Long) row.getValueAt(3)).longValue());
        assertEquals(revision0.getCreator().getName(), row.getValueAt(4));

        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(revision0.getDate()), row.getValueAt(5));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(revision0, data.element(0));
        assertEquals(revision1, data.element(1));
    }

}
