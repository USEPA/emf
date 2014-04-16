package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class UsersTableDataTest extends TestCase {

    private UsersTableData data;

    private User user0;

    private User user1;

    protected void setUp() {
        user0 = new User();
        user0.setUsername("username0");
        user0.setName("name0");
        user0.setEmail("user0@test.org");
        user0.setAdmin(true);

        user1 = new User();
        user1.setUsername("username0");
        user1.setName("name0");
        user1.setEmail("user0@test.org");

        data = new UsersTableData(new User[] { user0, user1 });
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(4, columns.length);
        assertEquals("Username", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Email", columns[2]);
        assertEquals("Is Admin ?", columns[3]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectAndIsAdminColsAndStringForAllOtherCols() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(Boolean.class, data.getColumnClass(3));
    }

    public void testExceptForSelectAllOtherColumnsShouldBeUneditable() {
        assertTrue("Select column should be editable", data.isEditable(0));
        for (int i = 1; i < 4; i++) {
            assertFalse("All cells (except Select) should be uneditable", data.isEditable(1));
        }
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(user0.getUsername(), row.getValueAt(0));
        assertEquals(user0.getName(), row.getValueAt(1));
        assertEquals(user0.getEmail(), row.getValueAt(2));
        assertEquals(user0.isAdmin(), ((Boolean) row.getValueAt(3)).booleanValue());
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(user0, data.element(0));
        assertEquals(user1, data.element(1));
    }

}
