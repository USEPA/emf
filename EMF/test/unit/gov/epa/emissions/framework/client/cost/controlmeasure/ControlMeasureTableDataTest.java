package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import junit.framework.TestCase;

public class ControlMeasureTableDataTest extends TestCase {

    public void testShouldAppropriateColumnClassDefinedForAllColumns() throws EmfException {
        ControlMeasureTableData data = new ControlMeasureTableData(new ControlMeasure[0], null, new Pollutant("major"), "2006");
        for (int i = 0; i < data.columns().length; i++) {
            if ((i >= 3 && i <= 10) || i == 13)
                assertEquals(Double.class, data.getColumnClass(i));
            else
                assertEquals(String.class, data.getColumnClass(i));
        }
    }
}
