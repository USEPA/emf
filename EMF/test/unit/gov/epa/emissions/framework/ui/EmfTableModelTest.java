package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class EmfTableModelTest extends MockObjectTestCase {

    private EmfTableModel model;

    private EmfDataset dataset1;

    private TableData tableData;

    private EmfDataset dataset2;

    protected void setUp() {
        List datasetList = new ArrayList();

        dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setDatasetType(new DatasetType("name1"));
        dataset1.setStatus("whatever-status");
        dataset1.setCreator("creator1");
        dataset1.setRegion(new Region("region1"));
        dataset1.setStartDateTime(new Date());
        dataset1.setModifiedDateTime(new Date());
        dataset1.setProject(new Project("p1"));
        dataset1.setIntendedUse(new IntendedUse("CEP"));

        datasetList.add(dataset1);

        dataset2 = new EmfDataset();
        dataset2.setName("name1");
        dataset2.setDatasetType(new DatasetType("name2"));
        dataset2.setStatus("whatever-status");
        dataset2.setCreator("creator1");
        dataset2.setRegion(new Region("region1"));
        dataset2.setStartDateTime(new Date());
        dataset2.setModifiedDateTime(new Date());
        dataset2.setProject(new Project("p2"));
        dataset1.setIntendedUse(new IntendedUse("EPA"));

        datasetList.add(dataset2);

        tableData = new EmfDatasetTableData(new EmfDataset[] { dataset1, dataset2 });

        model = new EmfTableModel(tableData);
    }

    public void testShouldReturnColumnsNames() {
        assertEquals(9, model.getColumnCount());

        assertEquals("Name", model.getColumnName(0));
        assertEquals("Last Modified Date", model.getColumnName(1));
        assertEquals("Type", model.getColumnName(2));
        assertEquals("Status", model.getColumnName(3));
        assertEquals("Creator", model.getColumnName(4));
        assertEquals("Intended Use", model.getColumnName(5));
        assertEquals("Project", model.getColumnName(6));
        assertEquals("Region", model.getColumnName(7));
        assertEquals("Start Date", model.getColumnName(8));

    }

    public void testShouldReturnRowsEqualingNumberOfDatasets() {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        assertEquals(dataset1.getName(), model.getValueAt(0, 0));
        assertEquals(dateFormat.format(dataset1.getModifiedDateTime()), model.getValueAt(0, 1));
        assertEquals(dataset1.getDatasetTypeName(), model.getValueAt(0, 2));
        assertEquals(dataset1.getStatus(), model.getValueAt(0, 3));
        assertEquals(dataset1.getCreator(), model.getValueAt(0, 4));
        assertEquals(dataset1.getIntendedUse(), model.getValueAt(0, 5));
        assertEquals(dataset1.getProject(), model.getValueAt(0, 6));
        assertEquals(dataset1.getRegion(), model.getValueAt(0, 7));
        assertEquals(dateFormat.format(dataset1.getStartDateTime()), model.getValueAt(0, 8));

    }

    public void testShouldMarkEmailColumnAsEditable() {
        assertFalse("All column should be uneditable", model.isCellEditable(0, 0));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 1));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 2));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 3));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 4));
    }

    public void testShouldReturnDatasetBasedOnIndex() {
        assertEquals(dataset1, model.element(0));
        assertEquals(dataset2, model.element(1));
    }

    public void testShouldGetColumnClassFromUnderlyingTableData() {
        Mock tableData = mock(TableData.class);
        tableData.stubs().method("getColumnClass").with(eq(new Integer(2))).will(returnValue(Integer.class));
        tableData.stubs().method("rows").withNoArguments().will(returnValue(Collections.EMPTY_LIST));
        tableData.stubs().method("columns").withNoArguments().will(returnValue(new String[0]));

        model = new EmfTableModel((TableData) tableData.proxy());
        assertEquals(Integer.class, model.getColumnClass(2));
    }

    public void testShouldDelegateSetValueToTableDataOnSetValueAt() {
        Object value = "val";
        int row = 2;
        int col = 3;

        Mock tableData = mock(TableData.class);
        tableData.stubs().method("rows").withNoArguments().will(returnValue(null));
        tableData.stubs().method("columns").withNoArguments().will(returnValue(null));
        tableData.expects(once()).method("setValueAt").with(eq(value), eq(new Integer(row)), eq(new Integer(col)));
        tableData.stubs().method("isEditable").with(eq(new Integer(col))).will(returnValue(Boolean.TRUE));

        model = new EmfTableModel((TableData) tableData.proxy());

        model.setValueAt(value, row, col);
    }

    public void testShouldNotSetValueOnTableDataWhenCellIsUneditableOnSetValueAt() {
        Object value = "val";
        int row = 2;
        int col = 3;

        Mock tableData = mock(TableData.class);
        tableData.stubs().method("rows").withNoArguments().will(returnValue(null));
        tableData.stubs().method("columns").withNoArguments().will(returnValue(null));
        tableData.stubs().method("isEditable").with(eq(new Integer(col))).will(returnValue(Boolean.FALSE));

        model = new EmfTableModel((TableData) tableData.proxy());

        model.setValueAt(value, row, col);
    }
}
