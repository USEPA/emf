package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class EditableKeyValueTableDataTest extends TestCase {

    private EditableKeyValueTableData data;

    private KeyVal val1;

    private KeyVal val2;
    
    private KeyVal val3;

    protected void setUp() {
        val1 = new KeyVal();
        val1.setId(1);
        val1.setKeyword(new Keyword("key1"));
        val1.setValue("val1");

        val2 = new KeyVal();
        val2.setId(2);
        val2.setKeyword(new Keyword("key2"));
        val2.setValue("val2");
        
        val3 = new KeyVal();
        val3.setId(3);
        val3.setKeyword(new Keyword("key3"));
        val3.setValue("val3");

        Keyword[] keywords = { new Keyword("1"), new Keyword("2") };
        KeyVal[] keyVals = new KeyVal[] { val1, val2 };
        KeyVal[] datasetTypeKeyVals = new KeyVal[] { val1, val2, val3 };
        EmfDataset dataset = new EmfDataset();
        DatasetType datasetType = new DatasetType();
        datasetType.setKeyVals(datasetTypeKeyVals);
        dataset.setKeyVals(keyVals);
        dataset.setDatasetType(datasetType);
        data = new EditableKeyValueTableData(dataset.getKeyVals(), datasetTypeKeyVals, new Keywords(keywords));
    }

    public void testShouldHaveThreeColumns() {
        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Keyword", columns[1]);
        assertEquals("Value", columns[2]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectColumnAndStringForAllOtherColumns() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be editable", data.isEditable(0));
        assertTrue("All cells should be editable", data.isEditable(1));
        assertTrue("All cells should be editable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 3 rows", rows);
        assertEquals(3, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals("key1", row.getValueAt(1));
        assertEquals("val1", row.getValueAt(2));
    }

    public void testShouldReturnARowRepresentingAKeyValEntry() {
        assertEquals(val1, data.element(0));
        assertEquals(val2, data.element(1));
    }

    public void testShouldRemoveRowOnRemove() {
        data.remove(val1);
        assertEquals(2, data.rows().size());

        data.remove(new KeyVal());
        assertEquals(2, data.rows().size());
    }

    public void testShouldRemoveSelectedOnRemove() {
        assertEquals(3, data.rows().size());

        data.setValueAt(Boolean.TRUE, 0, 0);
        data.removeSelected();

        assertEquals(2, data.rows().size());
    }

    public void testShouldAddEntryOnAdd() {
        data.addBlankRow();

        assertEquals(4, data.rows().size());
    }

    public void testShouldAddBlankEntry() {
        data.addBlankRow();

        List rows = data.rows();
        assertEquals(4, rows.size());
        Row blankRow = (Row) rows.get(3);
        KeyVal blankSource = ((KeyVal) blankRow.source());
        assertEquals(new Keyword(""), blankSource.getKeyword());
        assertEquals("", blankSource.getValue());
    }

    public void testShouldReturnCurrentlyHeldKeyVal() throws EmfException {
        data.addBlankRow();

        String key = "key";
        String value = "value";
        data.setValueAt(new Boolean(true), 3, 0);
        data.setValueAt(key, 3, 1);
        data.setValueAt(value, 3, 2);

        KeyVal[] sources = data.sources();
        assertEquals(4, sources.length);
        assertEquals(val1, sources[0]);
        assertEquals(val2, sources[1]);
        assertEquals(new Keyword(key), sources[3].getKeyword());
    }

    public void testShouldGiveErrorForEmptyValues() {
        data.addBlankRow();
        try {
            data.sources();
        } catch (EmfException e) {
            assertEquals("On Keywords panel, empty keyword at row 4", e.getMessage());
            return;
        }
        assertFalse("blank key values are not allowed", true);
    }

    public void testShouldConfirmTrackChangesForAllExceptSelectColumn() {
        assertFalse(data.shouldTrackChange(0));
        assertTrue(data.shouldTrackChange(1));
        assertTrue(data.shouldTrackChange(2));
    }
}
