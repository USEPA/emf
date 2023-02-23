package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.client.meta.info.InternalSourcesTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import org.jmock.MockObjectTestCase;

public class InternalSourcesTableDataTest extends MockObjectTestCase {

    private InternalSourcesTableData data;

    protected void setUp() {
        InternalSource source1 = new InternalSource();
        source1.setCols(new String[0]);

        InternalSource source2 = new InternalSource();
        source2.setCols(new String[0]);

        data = new InternalSourcesTableData(new InternalSource[] { source1, source2 });
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(4, columns.length);
        assertEquals("Table", columns[0]);
        assertEquals("Type", columns[1]);
        assertEquals("Source", columns[2]);
        assertEquals("Size", columns[3]);
    }

    public void testShouldReturnStringAsColumnClassForAllOtherColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
    }

    public void testAllColumnsShouldBeUneditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
        assertFalse("All cells should be uneditable", data.isEditable(3));
    }

    public void testShouldReturnTheRowsCorrespondingToInternalSourcesCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        InternalSource source = new InternalSource();
        source.setTable("table");
        source.setType("type");
        source.setSource("source");
        source.setSourceSize(2800);
        source.setCols(new String[] { "1", "2" });

        InternalSourcesTableData data = new InternalSourcesTableData(new InternalSource[] { source });

        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("table", row.getValueAt(0));
        assertEquals("type", row.getValueAt(1));
        assertEquals("source", row.getValueAt(2));
        assertEquals(Long.valueOf(2800), row.getValueAt(3));
    }

    public void testShouldReturnARowRepresentingAnInternalSourceEntry() {
        InternalSource source1 = new InternalSource();
        source1.setCols(new String[0]);
        InternalSource source2 = new InternalSource();
        source2.setCols(new String[0]);

        data = new InternalSourcesTableData(new InternalSource[] { source1, source2 });

        assertEquals(source1, data.element(0));
        assertEquals(source2, data.element(1));
    }
}
