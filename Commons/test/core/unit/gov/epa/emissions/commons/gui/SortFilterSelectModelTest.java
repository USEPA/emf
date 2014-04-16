package gov.epa.emissions.commons.gui;


import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SortFilterSelectModelTest extends MockObjectTestCase {

    private Mock delegate;

    private SortFilterSelectModel model;

    protected void setUp() {
        delegate = mock(RefreshableTableModel.class);
        delegate.stubs().method("getRowCount").withNoArguments().will(returnValue(7));

        delegate.stubs().method("getColumnCount").withNoArguments().will(returnValue(3));

        delegate.stubs().method("getColumnName").with(eq(0)).will(returnValue("Name"));
        delegate.stubs().method("getColumnName").with(eq(1)).will(returnValue("Age"));
        delegate.stubs().method("getColumnName").with(eq(2)).will(returnValue("Country"));

        model = new SortFilterSelectModel((RefreshableTableModel) delegate.proxy());
    }

    public void testShouldReturnSelectAsFirstColumn() {
        assertEquals("Select", model.getColumnName(0));
    }

    public void testShouldReturnSelectAsSelectableColumnName() {
        assertEquals("Select", model.getSelectableColumnName());
    }
    
    public void testShouldReturnFirstColumnOfDelegateAsSecondColumn() {
        delegate.stubs().method("getColumnName").with(eq(0)).will(returnValue("Name"));

        assertEquals("Name", model.getColumnName(1));
    }

    public void testShouldReturnTwoPlusDelegateColumnCountAsColumnCount() {
        delegate.stubs().method("getColumnCount").withNoArguments().will(returnValue(3));

        assertEquals((3+1), model.getColumnCount());
    }

    public void testShouldReturnRowCountTheSameAsDelegateRowCount() {
        assertEquals(7, model.getRowCount());
    }

    public void testShouldReturnFalseAsInitialValueOfSelectColumnOfAllRows() {
        for (int i = 0; i < 7; i++) {
            assertEquals(Boolean.FALSE, model.getValueAt(i, 0));
        }
    }

    public void testShouldReturnDataFromDelegateOnGetValueAt() {
        delegate.stubs().method("getValueAt").with(eq(0), eq(0)).will(returnValue("Jimmy"));

        assertEquals("Jimmy", model.getValueAt(0, 1));
    }

    public void testShouldReturnTrueAsSelectColumnValueIfItsSelected() {
        model.setValueAt(Boolean.TRUE, 1, 0);

        assertEquals(Boolean.TRUE, model.getValueAt(1, 0));
        assertEquals(1, model.getSelectedIndexes()[0]);
    }

    public void testShouldReturnBaseModelRowIndexTheSameAsSpecifiedInTheArgument() {
        assertEquals(2, model.getBaseModelRowIndex(2));
        assertEquals(6, model.getBaseModelRowIndex(6));
    }

    public void testShouldMarkOnlySelectColumnAsEditable() {
        assertTrue(model.isCellEditable(0, 0));//Select col
        
        //assertFalse(model.isCellEditable(0, 1));
        //assertFalse(model.isCellEditable(0, 2));
    }
    
    public void testShouldReturnColumnNamesOfTheDelegateOnGetDelegateColumnNames() {        
        String[] columnNames = model.getDelegateColumnNames();
        
        assertEquals(3, columnNames.length);
        assertEquals("Name", columnNames[0]);
        assertEquals("Age", columnNames[1]);
        assertEquals("Country", columnNames[2]);
    }
        
    public void testShouldRefreshDelegateTableModelAndResetSelectionsOnRefresh() {
        delegate.expects(once()).method("refresh").withNoArguments();
        
        model.refresh();
        
        assertEquals(0, model.getSelectedCount());
    }
}
