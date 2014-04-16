package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.framework.client.data.sector.SectorCriteriaTableData;
import gov.epa.emissions.framework.client.data.sector.ViewableSectorCriteriaTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class ViewableSectorCriteriaTableDataTest extends TestCase {

    private ViewableSectorCriteriaTableData data;

    private SectorCriteria criterion1;

    private SectorCriteria criterion2;

    protected void setUp() {
        criterion1 = new SectorCriteria();
        criterion1.setId(1);
        criterion1.setType("type1");
        criterion1.setCriteria("criterion1");

        criterion2 = new SectorCriteria();
        criterion2.setId(2);
        criterion2.setType("type2");
        criterion2.setCriteria("criterion2");

        data = new ViewableSectorCriteriaTableData(new SectorCriteria[] { criterion1, criterion2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("Type", columns[0]);
        assertEquals("Criterion", columns[1]);
    }

    public void testShouldHaveStringAsColumnClassForBothColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldBeNonEditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("type1", row.getValueAt(0));
        assertEquals("criterion1", row.getValueAt(1));
    }

    public void testShouldReturnARowRepresentingASectorCriteriaEntry() {
        SectorCriteria element1 = new SectorCriteria();
        element1.setType("type1");
        element1.setCriteria("criterion1");

        SectorCriteria element2 = new SectorCriteria();
        element2.setType("type2");
        element2.setCriteria("criterion2");

        SectorCriteriaTableData data = new SectorCriteriaTableData(new SectorCriteria[] { element1, element2 });

        assertEquals(element1, data.element(0));
        assertEquals(element2, data.element(1));
    }

}
