package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.data.datasettype.ViewableKeywordsTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class ViewableKeywordsTableDataTest extends TestCase {

    private ViewableKeywordsTableData data;

    private Keyword keyword1;

    private Keyword keyword2;

    protected void setUp() {
        keyword1 = new Keyword("keyword1");
        keyword2 = new Keyword("keyword2");
        data = new ViewableKeywordsTableData(new Keyword[] { keyword1, keyword2 });
    }

    public void testShouldHaveOneColumn() {
        String[] columns = data.columns();
        assertEquals(1, columns.length);
        assertEquals("Keyword", columns[0]);
    }

    public void testShouldReturnStringForKeywordColumn() {
        assertEquals(String.class, data.getColumnClass(0));
    }

    public void testColumnShouldBeNonEditable() {
        assertFalse("All cells should be non-editable", data.isEditable(0));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("keyword1", row.getValueAt(0));
    }

    public void testShouldReturnARowRepresentingAKeywordEntry() {
        assertEquals("keyword1", ((Keyword) data.element(0)).getName());
        assertEquals("keyword2", ((Keyword) data.element(1)).getName());
    }

}
