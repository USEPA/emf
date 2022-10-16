package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class DatasetTypesTableDataTest extends TestCase {

    private DatasetTypesTableData data;

    private DatasetType type1;

    private DatasetType type2;

    protected void setUp() {
        type1 = new DatasetType();
        type1.setName("name1");
        type1.setDescription("desc1");
        type1.setMinFiles(1);
        type1.setMaxFiles(3);

        type2 = new DatasetType();
        type2.setName("name2");
        type2.setDescription("desc2");
        type2.setMinFiles(3);
        type2.setMaxFiles(3);

        data = new DatasetTypesTableData(new DatasetType[] { type1, type2 });
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(6, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("# Keywords", columns[1]);
        assertEquals("# QA Step Templates", columns[2]);
        assertEquals("Min Files", columns[3]);
        assertEquals("Max Files", columns[4]);
        assertEquals("Description", columns[5]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(Integer.class, data.getColumnClass(1));
        assertEquals(Integer.class, data.getColumnClass(2));
        assertEquals(Integer.class, data.getColumnClass(3));
        assertEquals(Integer.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
    }

    public void testAllColumnsShouldBeNotEditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
        assertFalse("All cells should be uneditable", data.isEditable(3));
        assertFalse("All cells should be uneditable", data.isEditable(4));
        assertFalse("All cells should be uneditable", data.isEditable(5));
    }

    public void testShouldReturnTheRowsCorrespondingToDatasetTypesCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("name1", row.getValueAt(0));
        assertEquals("desc1", row.getValueAt(5));
        assertEquals(Integer.valueOf(1), row.getValueAt(3));
        assertEquals(Integer.valueOf(3), row.getValueAt(4));
    }

    public void testShouldReturnARowRepresentingADatasetTypeEntry() {
        assertEquals(type1, data.element(0));
        assertEquals(type2, data.element(1));
    }
}
