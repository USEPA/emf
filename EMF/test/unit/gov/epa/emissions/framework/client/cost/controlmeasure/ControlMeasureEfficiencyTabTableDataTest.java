package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import junit.framework.TestCase;

public class ControlMeasureEfficiencyTabTableDataTest extends TestCase {

    public void testShouldAppropriateColumnClassDefinedForAllColumns() {
        EfficiencyRecord[] records = {};
        ControlMeasureEfficiencyTableData data = new ControlMeasureEfficiencyTableData(records);

      for (int i = 0; i < data.columns().length; i++) {
            if (i == 20)
                assertEquals(Integer.class, data.getColumnClass(i));
            else if (i == 4 || i == 5 ||i == 6 || i == 7 || i == 8 || i == 9 || i == 10 || i == 12 || i == 13||i==14 ||i==15)
                assertEquals(Double.class, data.getColumnClass(i));
            else
                assertEquals(String.class, data.getColumnClass(i));
        }
    }
}
