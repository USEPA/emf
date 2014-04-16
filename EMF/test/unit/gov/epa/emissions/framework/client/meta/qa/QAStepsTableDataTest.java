package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.Row;

import java.util.Date;
import java.util.List;

public class QAStepsTableDataTest extends EmfMockObjectTestCase {

    private QAStepsTableData data;

    private QAStep step1;

    private QAStep step2;

    protected void setUp() {
        step1 = new QAStep();
        step1.setVersion(2);
        step1.setName("step1");
        step1.setWho("username1");
        step1.setDate(new Date());
        step1.setProgram(program("program1"));
        step1.setRequired(true);
        step1.setOrder(1);
        step1.setComments("result1");
        step1.setStatus("status1");
        step1.setConfiguration("dataset one");

        step2 = new QAStep();
        step2.setVersion(2);
        step2.setName("step2");
        step2.setWho("username2");
        step2.setDate(new Date());
        step2.setProgram(program("program2"));
        step2.setRequired(false);
        step2.setOrder(2);
        step2.setComments("result2");
        step2.setStatus("status2");
        step2.setConfiguration("dataset two");

        data = new QAStepsTableData(new QAStep[] { step1, step2 }, new QAStepResult[] {null, null});
    }

    private QAProgram program(String name) {
        return new QAProgram(name);
    }

    public void testShouldHaveNineColumns() {
        String[] columns = data.columns();
        assertEquals(11, columns.length);
        assertEquals("Version", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Required", columns[2]);
        assertEquals("Order", columns[3]);
        assertEquals("Status", columns[4]);
        assertEquals("When", columns[5]);
        assertEquals("Who", columns[6]);
        assertEquals("Comment", columns[7]);
        assertEquals("Program", columns[8]);
        assertEquals("Arguments", columns[9]);
        assertEquals("Configuration", columns[10]);
    }

    public void testShouldReturnAppropriateColumnClassForEachCol() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Boolean.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(User.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
    }

    public void testAllColumnsShouldBeUneditable() {
        for (int i = 0; i < 8; i++)
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
        assertEquals(new Integer(step1.getVersion()), row.getValueAt(0));
        assertEquals(step1.getName(), row.getValueAt(1));
        assertEquals(step1.isRequired(), ((Boolean) row.getValueAt(2)).booleanValue());
        assertEquals(step1.getOrder() + "", row.getValueAt(3) + "");
        assertEquals(step1.getStatus(), row.getValueAt(4));

        assertEquals(CustomDateFormat.format_YYYY_MM_DD_HH_MM(step1.getDate()), row.getValueAt(5));

        assertEquals(step1.getWho(), row.getValueAt(6));
        assertEquals(step1.getComments(), row.getValueAt(7));
        assertEquals(step1.getProgram().getName(), row.getValueAt(8));
        assertEquals(step1.getProgramArguments(), row.getValueAt(9));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(step1, data.element(0));
        assertEquals(step2, data.element(1));
    }

}
