package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.Status;

import java.util.Date;

import org.jmock.MockObjectTestCase;

public class StatusTableModelTest extends MockObjectTestCase {

    private StatusTableModel model;

    private Status status2;

    private Status status1;

    private Status[] statuses;

    protected void setUp() {
        Date status1Timestamp = new Date();
        status1 = new Status("user1", "type1", "message1", status1Timestamp);

        Date status2Timestamp = new Date(status1Timestamp.getTime() + 2000);
        status2 = new Status("user2", "type2", "message2", status2Timestamp);

        statuses = new Status[] { status1, status2 };

        model = new StatusTableModel();
        assertEquals(0, model.getRowCount());
        assertNull("No data on creation", model.getValueAt(0, 0));

        model.refresh(statuses);
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnRowsEqualingTheNumberOfStatusMessages() {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldHaveFourColumns() {
        assertEquals(3, model.getColumnCount());
    }

    public void testShouldReturnExpectedColumnsNames() {
        assertEquals("Message Type", model.getColumnName(0));
        assertEquals("Message", model.getColumnName(1));
        assertEquals("Timestamp", model.getColumnName(2));
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(status2.getType(), model.getValueAt(0, 0));
        assertEquals(status2.getMessage(), model.getValueAt(0, 1));
        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(status2.getTimestamp()), model.getValueAt(0, 2));

        assertEquals(status1.getType(), model.getValueAt(1, 0));
        assertEquals(status1.getMessage(), model.getValueAt(1, 1));
        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(status1.getTimestamp()), model.getValueAt(1, 2));
    }

    public void testShouldAppendStatusesOnRefresh() {
        Status status = new Status("user2", "type2", "message2", new Date());
        Status[] statuses = new Status[] { status };

        model.refresh(statuses);

        assertEquals(3, model.getRowCount());
    }

    public void testShouldSortStatusesOnRefresh() {
        long status2Time = statuses[1].getTimestamp().getTime();
        Status status3 = new Status("user3", "type2", "message3", new Date(status2Time + 2000));
        Status status4 = new Status("user4", "type2", "message4", new Date(status2Time + 4000));
        Status[] newList = new Status[] { status3, status4 };

        model.refresh(newList);

        assertEquals(4, model.getRowCount());
        assertEquals("message4", model.getValueAt(0, 1));
        assertEquals("message3", model.getValueAt(1, 1));
        assertEquals("message2", model.getValueAt(2, 1));
        assertEquals("message1", model.getValueAt(3, 1));
    }

    public void testShouldClearStatusesOnClear() {
        Status status = new Status("user2", "type2", "message2", new Date());
        Status[] statuses = new Status[] { status };

        model.refresh(statuses);

        model.clear();

        assertEquals(0, model.getRowCount());
    }
}
