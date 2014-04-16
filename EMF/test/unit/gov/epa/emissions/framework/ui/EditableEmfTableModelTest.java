package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.EmfMockObjectTestCase;

import org.jmock.Mock;

public class EditableEmfTableModelTest extends EmfMockObjectTestCase {

    public void testShouldDelegateDecisionToTrackChangesToUnderlyingTableData() {
        Mock tableData = mock(EditableTableData.class);
        stub(tableData, "rows", null);
        stub(tableData, "columns", null);
        tableData.expects(once()).method("shouldTrackChange").with(eq(new Integer(0))).will(returnValue(Boolean.TRUE));
        tableData.expects(once()).method("shouldTrackChange").with(eq(new Integer(1))).will(returnValue(Boolean.FALSE));

        EditableEmfTableModel model = new EditableEmfTableModel((EditableTableData) tableData.proxy());

        assertTrue(model.shouldTrackChange(0));
        assertFalse(model.shouldTrackChange(1));
    }

}
