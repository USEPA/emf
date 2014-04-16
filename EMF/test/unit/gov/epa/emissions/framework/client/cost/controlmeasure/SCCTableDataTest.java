package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import java.util.List;

import junit.framework.TestCase;

public class SCCTableDataTest extends TestCase {

    private SCCTableData data;

    private Scc[] sccs;

    protected void setUp() {
        sccs = new Scc[2];
        sccs[0] = new Scc("name1", "description1");
        sccs[0].setId(1);
        sccs[0].setControlMeasureId(1);
        sccs[1] = new Scc("name2", "description2");
        sccs[1].setId(2);
        sccs[1].setControlMeasureId(1);
        data = new SCCTableData(sccs);
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("SCC", columns[0]);
        assertEquals("Description", columns[1]);
    }

    public void testColumnClasses() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldBeEditable() {
        assertEquals(false, data.isEditable(0));
        assertEquals(false, data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldNotAddDuplicates() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);

        Scc scc3 = new Scc("name3", "description3");
        scc3.setId(3);
        scc3.setControlMeasureId(1);
        Scc[] newSccs = { sccs[0], scc3, sccs[1] };
        data.add(newSccs);
        rows = data.rows();
        assertEquals(3, rows.size());
    }

}
