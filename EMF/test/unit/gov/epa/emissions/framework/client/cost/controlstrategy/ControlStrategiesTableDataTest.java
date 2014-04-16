package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class ControlStrategiesTableDataTest extends TestCase {

    private ControlStrategiesTableData data;

    private ControlStrategy controlStrategy1;

    private ControlStrategy controlStrategy2;

    protected void setUp() {
        controlStrategy1 = new ControlStrategy();
        controlStrategy1.setName("name1");
        controlStrategy1.setRegion(new Region("region1"));
        controlStrategy1.setProject(new Project("project1"));
        controlStrategy1.setDiscountRate(0.5);
        controlStrategy1.setCostYear(2000);
        controlStrategy1.setInventoryYear(2001);
        controlStrategy1.setTargetPollutant(new Pollutant("NoX"));
        controlStrategy1.setLastModifiedDate(new Date());
        controlStrategy1.setCreator(new User("test user 1", "sss", "123-4567", "email@xxx.com", "xxx", "xxxxx123", false, false));

        controlStrategy2 = new ControlStrategy();
        controlStrategy2.setName("name2");
        controlStrategy2.setRegion(new Region("region2"));
        controlStrategy2.setProject(new Project("project2"));
        controlStrategy2.setDiscountRate(0.5);
        controlStrategy2.setCostYear(2000);
        controlStrategy2.setInventoryYear(2001);
        controlStrategy2.setTargetPollutant(new Pollutant("PM3"));
        controlStrategy2.setLastModifiedDate(new Date());
        controlStrategy2.setCreator(new User("test user 2", "sss", "123-4567", "email@xxx.com", "xxx", "xxxxx123", false, false));
        
        data = new ControlStrategiesTableData(new ControlStrategy[] { controlStrategy1, controlStrategy2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(16, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Last Modified", columns[1]);
        assertEquals("Region", columns[2]);
        assertEquals("Project", columns[3]);
        assertEquals("Strategy Type", columns[4]);
        assertEquals("Inv. Dataset", columns[5]);
        assertEquals("Version", columns[6]);
        assertEquals("Inventory Type", columns[7]);
        assertEquals("Target Pollutant", columns[8]);
        assertEquals("Cost Year", columns[9]);
        assertEquals("Inv. Year", columns[10]);
        assertEquals("Total Cost", columns[11]);
        assertEquals("Reduction", columns[12]);
        assertEquals("Run Status", columns[13]);
        assertEquals("Completion Date", columns[14]);
        assertEquals("Creator", columns[15]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));

    }

    public void testAllColumnsShouldBeUnEditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("name1", row.getValueAt(0));
        assertEquals(format(controlStrategy1.getLastModifiedDate()), row.getValueAt(1));
        assertEquals("region1", row.getValueAt(2));
        assertEquals("project1", row.getValueAt(3));
        assertEquals("", row.getValueAt(4));
        assertEquals("datasetType1", row.getValueAt(7));
        assertEquals("2000", row.getValueAt(9));

    }

    private String format(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(date);
    }

    public void testShouldReturnARowRepresentingAControlStrategyEntry() {
        assertEquals(controlStrategy1, data.element(0));
        assertEquals(controlStrategy2, data.element(1));
    }

}
