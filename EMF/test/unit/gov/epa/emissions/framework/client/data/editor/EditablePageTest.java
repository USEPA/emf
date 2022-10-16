package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class EditablePageTest extends TestCase {

    private EditablePage data;

    private String[] cols;

    private VersionedRecord record1;

    private VersionedRecord record2;

    private Page page;

    private int datasetId;

    private Version version;

    private TableMetadata tableMetadata;

    protected void setUp() {
        page = new Page();

        record1 = new VersionedRecord();
        record1.setTokens(new Object[] { "1", Double.valueOf(2.0), Integer.valueOf(3) });
        page.add(record1);

        record2 = new VersionedRecord();
        record2.setTokens(new Object[] { "11", Double.valueOf(4.0), Integer.valueOf(6) });
        page.add(record2);

        cols = new String[] { "col1", "col2", "col3" };
        datasetId = 2;
        version = new Version();
        version.setVersion(34);
        tableMetadata = tableMetadata();
        data = new EditablePage(datasetId, version, page, tableMetadata);
    }

    private TableMetadata tableMetadata() {
        TableMetadata tableMetadata = new TableMetadata();

        tableMetadata.addColumnMetaData(new ColumnMetaData("record_id", "java.lang.Integer", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("dataset_id", "java.lang.Long", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("version", "java.lang.Long", 10));
        tableMetadata.addColumnMetaData(new ColumnMetaData("delete_versions", "java.lang.String", 10));

        ColumnMetaData col0 = new ColumnMetaData(cols[0], "java.lang.String", 10);
        ColumnMetaData col1 = new ColumnMetaData(cols[1], "java.lang.Double", 10);
        ColumnMetaData col2 = new ColumnMetaData(cols[2], "java.lang.Integer", 10);

        tableMetadata.addColumnMetaData(col0);
        tableMetadata.addColumnMetaData(col1);
        tableMetadata.addColumnMetaData(col2);

        return tableMetadata;
    }

    public void testShouldHaveThreeColumns() {
        String[] columns = data.columns();
        assertEquals(7, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("col1", columns[1]);
        assertEquals("col2", columns[2]);
        assertEquals("col3", columns[3]);
        assertEquals("record_id", columns[4]);
        assertEquals("version", columns[5]);
        assertEquals("delete_versions", columns[6]);
    }

    public void testShouldCheckColumnClasses() {
        String[] columns = data.columns();
        assertEquals(7, columns.length);
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Double.class, data.getColumnClass(2));
        assertEquals(Integer.class, data.getColumnClass(3));
        assertEquals(Integer.class, data.getColumnClass(4));
        assertEquals(Long.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
    }

    public void testShouldSelectAllColumnsWhenSelectAllCalled() {
        data.selectAll();
        List rows = data.rows();
        for (int i = 0; i < rows.size(); i++) {
            EditableRow row = (EditableRow) rows.get(i);
            assertEquals(Boolean.TRUE,row.getValueAt(0));
        }
    }
    
    public void testShouldClearAllColumnsWhenClearAllCalled() {
        data.clearAll();
        List rows = data.rows();
        for (int i = 0; i < rows.size(); i++) {
            EditableRow row = (EditableRow) rows.get(i);
            assertEquals(Boolean.FALSE,row.getValueAt(0));
        }
    }

    public void testAllColumnsShouldBeEditableExceptLastTwo() {
        int length = data.columns().length;
        for (int i = 0; i < length - 3; i++) {
            assertTrue("All cells should be editable", data.isEditable(i));
        }
        assertFalse("not editable", data.isEditable(length - 2));
        assertFalse("not editable", data.isEditable(length - 1));
    }

    public void testRowsShouldContainDataValuesOfRecord() {
        List rows = data.rows();

        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());

        Row row1 = (Row) rows.get(0);
        assertEquals(record1.token(0), row1.getValueAt(1));
        assertEquals(record1.token(1), row1.getValueAt(2));

        Row row2 = (Row) rows.get(1);
        assertEquals(record2.token(0), row2.getValueAt(1));
        assertEquals(record2.token(1), row2.getValueAt(2));
    }

    public void testShouldReturnARowRepresentingARecordEntry() {
        assertEquals(record1, data.element(0));
        assertEquals(record2, data.element(1));
    }

    public void testShouldRemoveRowOnRemove() {
        data.remove(record1);
        assertEquals(1, data.rows().size());

        data.remove(new VersionedRecord());
        assertEquals(1, data.rows().size());
    }

    public void testShouldRemoveSelectedOnRemove() {
        assertEquals(2, data.rows().size());

        data.setValueAt(Boolean.TRUE, 0, 0);
        data.removeSelected();

        assertEquals(1, data.rows().size());

        ChangeSet changeset = data.changeset();
        assertEquals(0, changeset.getNewRecords().length);
        assertEquals(1, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
        assertSame(record1, changeset.getDeletedRecords()[0]);
    }

    public void testShouldAddPreExistingRecordsThatAreModifiedToChangeSet() {
        data.setValueAt("modified-1", 0, 1);
        data.setValueAt("modified-2", 0, 2);
        data.setValueAt("modified-12", 1, 2);

        List rows = data.rows();
        assertEquals(2, rows.size());

        ChangeSet changeset = data.changeset();
        assertEquals(0, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(2, changeset.getUpdatedRecords().length);

        VersionedRecord updated1 = changeset.getUpdatedRecords()[0];
        assertEquals("modified-1", updated1.token(0));
        assertEquals("modified-2", updated1.token(1));

        VersionedRecord updated2 = changeset.getUpdatedRecords()[1];
        assertEquals("modified-12", updated2.token(1));
    }

    public void testModificationsToBlankRowShouldNotImpactChangeSet() {
        data.addBlankRow(2);
        assertEquals(3, data.rows().size());

        // modify newly added (blank) row
        data.setValueAt("31", 2, 1);
        data.setValueAt("32", 2, 2);

        ChangeSet changeset = data.changeset();
        assertEquals(1, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);

        VersionedRecord newRecord = changeset.getNewRecords()[0];
        assertEquals("31", newRecord.token(0));
        assertEquals("32", newRecord.token(1));
    }

    public void testChangeSetShouldIgnoreChangesToSelectCol() {
        data.addBlankRow(2);
        data.addBlankRow(3);

        data.setValueAt(Boolean.TRUE, 1, 0);
        data.setValueAt(Boolean.TRUE, 3, 0);
        data.setValueAt(Boolean.FALSE, 1, 0);

        assertEquals(4, data.rows().size());

        ChangeSet changeset = data.changeset();
        assertEquals(2, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
    }

    public void testShouldAddEntryOnAdd() {
        data.addBlankRow(2);

        assertEquals(3, data.rows().size());
    }

    public void testShouldAddBlankEntry() {
        data.addBlankRow(2);

        List rows = data.rows();
        assertEquals(3, rows.size());
        Row blankRow = (Row) rows.get(2);

        VersionedRecord blankRecord = (VersionedRecord) blankRow.source();
        assertEquals(datasetId, blankRecord.getDatasetId());
        assertEquals(version.getVersion(), blankRecord.getVersion());
        assertEquals("", blankRecord.getDeleteVersions());
        assertEquals(cols.length, blankRecord.tokens().size());
        for (int i = 0; i < cols.length - 1; i++)
            assertNull(blankRecord.token(i));
        assertNull(blankRecord.token(cols.length - 1));

        ChangeSet changeset = data.changeset();
        assertEquals(1, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
        assertSame(blankRecord, changeset.getNewRecords()[0]);
    }

    public void testShouldReturnCurrentlyHeldRecords() {
        data.addBlankRow(2);

        VersionedRecord[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals(record1, sources[0]);
        assertEquals(record2, sources[1]);
        assertEquals(datasetId, sources[2].getDatasetId());
    }

    public void testShouldConfirmTrackChangesForAllExceptSelectColumn() {
        assertFalse(data.shouldTrackChange(0));
        assertTrue(data.shouldTrackChange(1));
        assertTrue(data.shouldTrackChange(2));
        assertTrue(data.shouldTrackChange(3));
    }
}
