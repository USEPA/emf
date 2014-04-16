package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.sort.TableSorter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ColumnSelectionTable extends JTable {

	/** Constant for the column name column. */
	public static final int COLUMN_NAME = 0;

	/** Constant for the check box column. */
	public static final int CHECK_BOX = 1;

	/** True if the table is sorted, false if not. */
	protected boolean isTableSorted = false;

	/**
	 * The sorting table model. Note that I could not use the larger SortingTableModel because it extends
	 * MultiRowHeaderModel.
	 */
	protected TableSorter sortModel = null;

	/** The base table model with unsorted data. */
	protected ColumnSelectionTableModel model = null;
	
	private int selectRow = -1; 

	/** Constant for the column names in <b>this<b/> table. */
	public static final String[] LOCAL_COLUMN_NAMES = { "Column Name", "Selected?" };

	/**
	 * Constructor. This calls the constructor with 3 arguments.
	 * 
	 * @param columnHeaders
	 *            String[] with the headers of the columns in the application table.
	 * @param selected
	 *            boolean[] with true values for any selected columns. Must be the same length as columnNames;
	 */
	public ColumnSelectionTable(String[][] columnHeaders, boolean[] selected) {
		this(LOCAL_COLUMN_NAMES, columnHeaders, selected);
	} // ColumnSelectionTable()

	/**
	 * Constructor.
	 * 
	 * @param localColumn
	 *            String[] with the names of the columns in this table.
	 * @param columnHeaders
	 *            String[] with the headers of the columns in the application table.
	 * @param selected
	 *            boolean[] with true values for any selected columns. Must be the same length as columnNames;
	 */
	public ColumnSelectionTable(String[] localColumns, String[][] columnHeaders, boolean[] selected) {
		// We *require* these two arrays to be of the same length.
		if (columnHeaders.length != selected.length) {
			throw new IllegalArgumentException("columnNames and selected must be "
					+ "the same length in ColumnSelectionTable().");
		}

		Object[][] data = new Object[columnHeaders.length][localColumns.length];
		for (int r = 0; r < columnHeaders.length; r++) {
			for (int c = 0; c < localColumns.length - 1; c++) {
				data[r][c] = columnHeaders[r][c];
			}
            if (columnHeaders[r][0].equalsIgnoreCase("select"))
            	selectRow = r; 
			data[r][localColumns.length - 1] = new Boolean(selected[r]);
		} // for(r)

		model = new ColumnSelectionTableModel(data, localColumns);
		sortModel = new TableSorter(model);
		sortModel.setTableHeader(getTableHeader());
		setModel(sortModel);

		// Set the table size based on the number of rows. If we have less than
		// 10 make the height smaller. If we have more than 10 rows, then limit
		// the height to 10 rows.
		int width = 200;
		int height = getRowHeight();
		int rowCount = getRowCount();
		if (rowCount < 10) {
			height *= rowCount;
		} else {
			height *= 10;
		}

		Dimension dim = new Dimension(width, height);

		// Set the Renderer for the columns header to use a bold font.
		TableCellRenderer renderer = new BoldLabel();
		TableColumnModel tcm = this.getColumnModel();
		for (int i = 0; i < tcm.getColumnCount(); i++) {
			TableColumn col = tcm.getColumn(i);
			col.setHeaderRenderer(renderer);
		} // for(i)

		setPreferredScrollableViewportSize(dim);

	} // ColumnSelectionTable()

	public ColumnSelectionTableModel getTableModel() {
		return model;
	}

	/**
	 * Get the columns that are checked in the order that they were passed in. <b>NOTE: even if the user has sorted the
	 * column names, we will still return this array as though they had not sorted. </b>
	 * 
	 * @return boolean[] that is true if the column is checked.
	 */
	public boolean[] getCheckedColumns() {
		int lastCol = getColumnCount() - 1;
		int rowCount = getRowCount();
		boolean[] retval = new boolean[rowCount];

		for (int r = 0; r < rowCount; r++) {
			retval[r] = ((Boolean) model.getValueAt(r, lastCol)).booleanValue();
		} // for(r)

		return retval;
	} // getCheckedColumns()

	/**
	 * Return the String for all columns except the last one. Return Boolean for the last column.
	 * 
	 * @param col
	 *            int that is the column for which the class is requested.
	 * @return Class
	 */
	public Class getColumnClass(int col) {
		Class retval = String.class;

		if (col == getColumnCount() - 1) {
			retval = Boolean.class;
		}

		return retval;
	} // getColumnClass()

	/**
	 * Return false for all but the last column in the table.
	 * 
	 * @param row
	 *            int that is the row where the cell is.
	 * @param col
	 *            int that is the column where the cell is.
	 * @return true for the last column and false for all other columns.
	 */
	public boolean isCellEditable(int row, int col) {
		return (col == (getColumnCount() - 1) && row != selectRow);
	} // isCellEditable()

	/**
	 * A class to provide a bold header on the table.
	 */
	class BoldLabel extends JLabel implements TableCellRenderer {
		public BoldLabel() {
			setHorizontalAlignment(JLabel.CENTER);
			setBorder(BorderFactory.createRaisedBevelBorder());
			Font font = getFont();
			setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			setText(value.toString());
			return this;
		}
	} // class BoldLabel

} // class ColumnSelectionTabl

