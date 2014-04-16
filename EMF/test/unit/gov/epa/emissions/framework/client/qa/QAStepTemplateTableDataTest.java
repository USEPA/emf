package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.qa.QAStepTemplateTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class QAStepTemplateTableDataTest extends TestCase {

    private QAStepTemplateTableData data;

    private QAStepTemplate template1;

    private QAStepTemplate template2;

    protected void setUp() {
        template1 = new QAStepTemplate();
        template1.setName("name1");
        template1.setProgram(program("program1"));
        template1.setProgramArguments("program-args1");
        template1.setRequired(true);
        template1.setOrder(1);

        template2 = new QAStepTemplate();
        template2.setName("name2");
        template2.setProgram(program("program2"));
        template2.setProgramArguments("program-args2");
        template2.setRequired(false);
        template2.setOrder(2);

        data = new QAStepTemplateTableData(new QAStepTemplate[] { template1, template2 });
    }
    
    private QAProgram program(String name) {
        return new QAProgram(name);
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();

        assertEquals(5, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Program", columns[1]);
        assertEquals("Arguments", columns[2]);
        assertEquals("Required", columns[3]);
        assertEquals("Order", columns[4]);
    }

    public void testShouldReturnStringAsColumnClassForAllColsExceptRequiredCol() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(Boolean.class, data.getColumnClass(3));
        assertEquals(Float.class, data.getColumnClass(4));
    }

    public void testAllColumnsShouldBeUneditable() {
        for (int i = 0; i < 4; i++) {
            assertFalse("All cells (except Select) should be uneditable", data.isEditable(i));
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
        assertEquals(template1.getName(), row.getValueAt(0));
        assertEquals(template1.getProgram(), row.getValueAt(1));
        assertEquals(template1.getProgramArguments(), row.getValueAt(2));
        assertEquals(template1.isRequired(), ((Boolean) row.getValueAt(3)).booleanValue());
        assertEquals(template1.getOrder(), 0.0, ((Float) row.getValueAt(4)).floatValue());
    }

    public void testShouldReturnARowRepresentingATemplateEntry() {
        assertEquals(template1, data.element(0));
        assertEquals(template2, data.element(1));
    }

}
