package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.data.datasettype.KeywordsTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class KeywordsTableDataTest extends TestCase {

    private KeywordsTableData data;

    private Keyword keyword1;

    private Keyword keyword2;

    protected void setUp() {
        keyword1 = new Keyword("keyword1");
        keyword2 = new Keyword("keyword2");
        data = new KeywordsTableData(new Keyword[] { keyword1, keyword2 }, new Keywords(new Keyword[0]));
    }

    public void testShouldHaveTwoColumn() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Keyword", columns[1]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectColumnAndStringForKeywordColumn() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be editable", data.isEditable(0));
        assertTrue("All cells should be editable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals("keyword1", row.getValueAt(1));
    }

    public void testShouldReturnARowRepresentingAKeywordEntry() {
        assertEquals("keyword1", ((Keyword) data.element(0)).getName());
        assertEquals("keyword2", ((Keyword) data.element(1)).getName());
    }

    public void testShouldRemoveKeywordOnRemove() {
        data.remove(keyword1);
        assertEquals(1, data.rows().size());

        data.remove(new Keyword("non-existent keyword"));
        assertEquals(1, data.rows().size());
    }

    public void testShouldAddKeywordOnAdd() {
        data.add("keyword3");

        assertEquals(3, data.rows().size());
    }

    public void testShouldReturnCurrentlyHeldKeyword() throws EmfException {
        data.add("keyword3");

        Keyword[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals("keyword1", sources[0].getName());
        assertEquals("keyword2", sources[1].getName());
        assertEquals("keyword3", sources[2].getName());
    }

    public void testShouldConfirmTrackChangesForAllExceptSelectColumn() {
        assertFalse(data.shouldTrackChange(0));
        assertTrue(data.shouldTrackChange(1));
    }

}
