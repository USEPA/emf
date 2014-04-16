package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.viewer.ViewablePage;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class NonEditablePageDataTest extends TestCase {

    public void testShouldReturnStringAsColumnClassForAllColumns() {
        String[] cols = { "col1", "col2" };
        ViewablePage data = new ViewablePage(tableMetadata(cols), new Page());

        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    private TableMetadata tableMetadata(String[] cols) {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.addColumnMetaData(new ColumnMetaData("record_id", "java.lang.Integer", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("dataset_id", "java.lang.Long", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("version", "java.lang.Long", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("delete_version", "java.lang.String", 10));
        for (int i = 0; i < cols.length; i++) {
            tableMetadata.addColumnMetaData(new ColumnMetaData(cols[i],"java.lang.String", 10));
        }
        return tableMetadata;
    }

    public void testShouldDisplayAllColumns() {
        String[] cols = new String[] { "col1", "col2", "col3" };

        ViewablePage data = new ViewablePage(tableMetadata(cols), new Page());

        String[] columns = data.columns();
        assertEquals(6, columns.length);
        assertEquals(cols[0], columns[0]);
        assertEquals(cols[1], columns[1]);
        assertEquals(cols[2], columns[2]);
        assertEquals("record_id", columns[3]);
        assertEquals("version", columns[4]);
        assertEquals("delete_version", columns[5]);
    }

    public void testShouldMarkAllColumnsAsNotEditable() {
        String[] cols = new String[] { "col1", "col2", "col3" };

        ViewablePage data = new ViewablePage(tableMetadata(cols), new Page());

        assertFalse("All columns should not be editable", data.isEditable(0));
        assertFalse("All columns should not be editable", data.isEditable(1));
        assertFalse("All columns should not be editable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToRecordsInSpecifiedPage() {
        String[] cols = new String[] { "col1", "col2", "col3", "col4", "col5", "col6", "col7" };

        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        record1.setDeleteVersions("1");
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        record1.setDeleteVersions("2");
        page.add(record2);

        ViewablePage data = new ViewablePage(tableMetadata(cols), page);

        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testRowsShouldContainDataValuesOfRecords() {
        String[] cols = new String[] { "col1", "col2", "col3", "col4", "col5", "col6", "col7" };

        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        ViewablePage data = new ViewablePage(tableMetadata(cols), page);

        List rows = data.rows();

        Row row1 = (Row) rows.get(0);
        assertEquals(record1.token(0), row1.getValueAt(0));
        assertEquals(record1.token(1), row1.getValueAt(1));

        Row row2 = (Row) rows.get(1);
        assertEquals(record2.token(0), row2.getValueAt(0));
        assertEquals(record2.token(1), row2.getValueAt(1));
    }
}
