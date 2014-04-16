package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.meta.keywords.KeyValueTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class KeyValueTableDataTest extends TestCase {

    private KeyValueTableData data;

    private KeyVal val1;

    private KeyVal val2;

    protected void setUp() {
        val1 = new KeyVal();
        val1.setId(1);
        val1.setKeyword(new Keyword("key1"));
        val1.setValue("val1");

        val2 = new KeyVal();
        val2.setId(2);
        val2.setKeyword(new Keyword("key2"));
        val2.setValue("val2");

        data = new KeyValueTableData(new KeyVal[] { val1, val2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("Keyword", columns[0]);
        assertEquals("Value", columns[1]);
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

        Row row1 = (Row) rows.get(0);
        assertEquals("key1", row1.getValueAt(0));
        assertEquals("val1", row1.getValueAt(1));

        Row row2 = (Row) rows.get(1);
        assertEquals("key2", row2.getValueAt(0));
        assertEquals("val2", row2.getValueAt(1));
    }

    public void testShouldReturnARowRepresentingAKeyValEntry() {
        assertEquals("key1", ((KeyVal) data.element(0)).getKeyword().getName());
        assertEquals("key2", ((KeyVal) data.element(1)).getKeyword().getName());
    }

}
