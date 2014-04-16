package gov.epa.emissions.framework.ui;

import junit.framework.TestCase;

public class SelectableRowTest extends TestCase {

    public void testShouldReturnSourceObjectAsRecord() {
        Object[] values = new Object[0];
        RowSource source = new ReadableRowSource(values);
        SelectableRow row = new SelectableRow(source);

        assertEquals(values, row.source());
    }

    public void testShouldReturnValueAtCorrectCol() {
        RowSource source = new ReadableRowSource(new Object[] { "1", "2" });
        SelectableRow row = new SelectableRow(source);

        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals("1", row.getValueAt(1));
        assertEquals("2", row.getValueAt(2));
    }

}
