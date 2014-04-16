package gov.epa.emissions.framework.ui;

import junit.framework.TestCase;

public class EditableRowTest extends TestCase {

    public void testShouldReturnSourceObjectAsRecord() {
        Object[] values = new Object[0];
        RowSource source = new ReadableRowSource(values);
        EditableRow row = new EditableRow(source);

        assertEquals(values, row.source());
    }

    public void testShouldReturnValueAtCorrectCol() {
        RowSource source = new ReadableRowSource(new Object[] { "1", "2" });
        EditableRow row = new EditableRow(source);

        assertEquals("1", row.getValueAt(0));
        assertEquals("2", row.getValueAt(1));
    }

}
