package gov.epa.mims.analysisengine.table;

import java.util.Date;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class AggregatingTableModel extends MultiRowHeaderTableModel implements TableModelListener {
	/** The underlying data model. */
	protected MultiRowHeaderTableModel underlyingModel = null;

	/** The extra rows. */
	protected Object[][] extraRows = null;

	/** The extra columns. */
	protected Object[][] extraColumns = null;

	/** The extra column names. */
	protected String[] extraColumnNames = null;

	/** Creates a new instance of AggregatingTableModel */
	public AggregatingTableModel(MultiRowHeaderTableModel model) {
		super(model);

		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");

		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
	} // AggregatingTableModel()

	public void setModel(MultiRowHeaderTableModel model) {
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");
		if ( underlyingModel != null )
			underlyingModel.removeTableModelListener(this);
		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
		//fireTableDataChanged();
	} // AggregatingTableModel()
	
	public void aggregateAll() {
		aggregateRows(true);
		aggregateColumns();
	} // aggregateAll()

	public void aggregateColumns() {
		int numRows = getRowCount();
		extraColumns = new Object[numRows][2];
		extraColumnNames = new String[2];
		extraColumnNames[0] = "Full Name";
		extraColumnNames[1] = "Quotient";

		for (int row = 0; row < numRows; row++) {
			extraColumns[row][0] = (String) getValueAt(row, 0) + " " + (String) getValueAt(row, 1);
			extraColumns[row][1] = Double.valueOf(((Double) getValueAt(row, 3)).doubleValue()
					/ ((Integer) getValueAt(row, 2)).doubleValue());
		} // for(row)

		aggregateRows(true);
		fireTableDataChanged();
	} // aggregateColumns()

	public void aggregateRows(boolean redo) {
		if (redo) {
			if (extraRows == null)
				return;
			extraRows = new Object[2][getColumnCount()];
		}

		int numColumns = getColumnCount();

		if (extraRows == null)
			extraRows = new Object[2][numColumns];

		for (int col = 0; col < numColumns; col++) {
			if (getValueAt(0, col) instanceof Number) {
				int numRows = underlyingModel.getRowCount();
				double sum = 0;
				for (int i = 0; i < numRows; i++) {
					sum += ((Number) getValueAt(i, col)).doubleValue();
				}

				if (getValueAt(0, col) instanceof Integer) {
					extraRows[0][col] = Integer.valueOf((int) sum);
					extraRows[1][col] = Integer.valueOf((int) sum / numRows);
				} else {
					extraRows[0][col] = Double.valueOf(sum);
					extraRows[1][col] = Double.valueOf(sum / numRows);
				}
			} else if (getValueAt(0, col) instanceof Date) {
				int numRows = underlyingModel.getRowCount();
				long sum = 0;
				for (int i = 0; i < numRows; i++) {
					sum += ((Date) getValueAt(i, col)).getTime();
				}
				extraRows[0][col] = new Date(sum);
				extraRows[1][col] = new Date(sum / numRows);
			} else {
				extraRows[0][col] = "n/a";
				extraRows[1][col] = "n/a";
			}
		} // for(col)

		fireTableDataChanged();
	} // aggregateRows()

	public Class getColumnClass(int col) {
		return underlyingModel.getColumnClass(col);
	} // getColumnClass()

	public int getColumnCount() {
		return underlyingModel.getColumnCount() + ((extraColumns == null) ? (0) : (extraColumns[0].length));
	} // getColumnCount()

	/**
	 * Return the list of headers for the given column. Pass through to the underlying model.
	 * 
	 * @param col
	 *            int that is the column index.
	 * @return String[] with the headers for one column. Could be "" if there are no column headers.
	 */
	public String[] getColumnHeaders(int col) {
		return underlyingModel.getColumnHeaders(col);
	} // getColumnHeaders()

	public String getColumnName(int col) {
		if (col > underlyingModel.getColumnCount())
			return extraColumnNames[col - underlyingModel.getColumnCount()];

		return underlyingModel.getColumnName(col);
	} // getColumnCount()

	public int getLastRealRow() {
		return underlyingModel.getRowCount();
	} // getLastRealRow()

	public int getRowCount() {
		return underlyingModel.getRowCount() + ((extraRows != null) ? (extraRows.length) : (0));
	} // getRowCount()

	public Object getValueAt(int rowIndex, int columnIndex) {
		int numUnderlyingRows = underlyingModel.getRowCount();
		int numUnderlyingColumns = underlyingModel.getColumnCount();
		if (rowIndex < numUnderlyingRows) {
			// The value is in the underlying model.
			if (columnIndex < numUnderlyingColumns) {
				return underlyingModel.getValueAt(rowIndex, columnIndex);
			}
			return extraColumns[rowIndex][columnIndex - numUnderlyingColumns];
		} // if (rowIndex < underlyingModel.getRowCount())
		// The value is in the extra rows area.
		if (columnIndex < numUnderlyingColumns) {
			return extraRows[rowIndex - numUnderlyingRows][columnIndex];
		}
		return extraRows[rowIndex - numUnderlyingRows][columnIndex - numUnderlyingColumns];
	} // getValueAt()

	/**
	 * WARNING: THIS METHOD IS NOT IMPLEMENTED PROPERLY: You have to correct this when we we start use this model: for
	 * implementation see the getValueAt()
	 */
	public int getBaseModelRowIndex(int rowIndex) {
		return underlyingModel.getBaseModelRowIndex(rowIndex);
	}

	/** Editing is allowed only if the column type is boolean */
	public void setValueAt(Object aValue, int row, int col) {
		underlyingModel.setValueAt(aValue, row, col);
	}

	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}

} // class AggregatingTableModel

