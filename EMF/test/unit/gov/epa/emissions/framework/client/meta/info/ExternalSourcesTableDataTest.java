package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.framework.client.meta.info.ExternalSourcesTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import org.jmock.MockObjectTestCase;

public class ExternalSourcesTableDataTest extends MockObjectTestCase {

    private ExternalSourcesTableData data;

    private ExternalSource source1;

    private ExternalSource source2;

    protected void setUp() {
        source1 = new ExternalSource();
        source1.setDatasource("ds1");

        source2 = new ExternalSource();
        source2.setDatasource("ds2");

        data = new ExternalSourcesTableData(new ExternalSource[] { source1, source2 });
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(1, columns.length);
        assertEquals("Source", columns[0]);
    }

    public void testShouldStringAsColumnClassForSourceColumn() {
        assertEquals(String.class, data.getColumnClass(0));
    }

    public void testAllColumnsShouldBeUneditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
    }

    public void testShouldReturnTheRowsCorrespondingToInternalSourcesCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("ds1", row.getValueAt(0));
    }

    public void testShouldReturnARowRepresentingAnExternalSourceEntry() {
        assertEquals(source1, data.element(0));
        assertEquals(source2, data.element(1));
    }
}
