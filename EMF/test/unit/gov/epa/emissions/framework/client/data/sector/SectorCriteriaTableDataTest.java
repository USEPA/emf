package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.framework.client.data.sector.SectorCriteriaTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class SectorCriteriaTableDataTest extends TestCase {

    private SectorCriteriaTableData data;

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

        data = new SectorCriteriaTableData(new SectorCriteria[] { criterion1, criterion2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Type", columns[1]);
        assertEquals("Criterion", columns[2]);
    }

    public void testShouldBooleanAsColumnClassForSelectAndStringForOtherTwoColumns() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
    }
    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be editable", data.isEditable(0));
        assertTrue("All cells should be editable", data.isEditable(1));
        assertTrue("All cells should be editable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals("type1", row.getValueAt(1));
        assertEquals("criterion1", row.getValueAt(2));
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

    public void testShouldRemoveCriterionOnRemove() {
        data.remove(criterion1);
        assertEquals(1, data.rows().size());

        data.remove(new SectorCriteria());
        assertEquals(1, data.rows().size());
    }

    public void testShouldRemoveSelectedOnRemove() {
        assertEquals(2, data.rows().size());

        data.setValueAt(Boolean.TRUE, 0, 0);
        data.removeSelected();

        assertEquals(1, data.rows().size());
    }

    public void testShouldAddCriterionOnAdd() {
        SectorCriteria criterion = new SectorCriteria();
        criterion.setId(3);

        data.add(criterion);

        assertEquals(3, data.rows().size());
    }

    public void testShouldAddBlankEntry() {
        data.addBlankRow();

        List rows = data.rows();
        assertEquals(3, rows.size());
        Row blankRow = (Row) rows.get(2);
        SectorCriteria blankSource = ((SectorCriteria) blankRow.source());
        assertEquals("", blankSource.getType());
        assertEquals("", blankSource.getCriteria());
    }

    public void testShouldReturnCurrentlyHeldSectorCriteria() {
        SectorCriteria criterion = new SectorCriteria();
        criterion.setId(3);

        data.add(criterion);

        SectorCriteria[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals(criterion1, sources[0]);
        assertEquals(criterion2, sources[1]);
        assertEquals(criterion, sources[2]);
    }

}
