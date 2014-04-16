package gov.epa.mims.analysisengine.table;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * A table model that add a row number in the left hand column. All other rows are shifted to the right by one row. This
 * class also supports optional units.
 * 
 * @author Daniel Gatti
 * @version $Id: RowHeaderTableModel.java,v 1.5 2009/03/26 14:22:23 dyang02 Exp $
 */
public class RowHeaderTableModel extends MultiRowHeaderTableModel implements TableModelListener {
	private MultiRowHeaderTableModel underlyingModel;

	/** Creates a new instance of RowHeaderTableModel */
	public RowHeaderTableModel(MultiRowHeaderTableModel model) {
		super(model);

		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");

		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
	} // RowHeaderTableModel()
	
	public void setModel(MultiRowHeaderTableModel model) {
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");
		if ( underlyingModel != null )
			underlyingModel.removeTableModelListener(this);
		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
		fireTableDataChanged();
	} // RowHeaderTableModel()

	public Class getColumnClass(int col) {
		if (col <= 0) {
			return String.class;
		}
		return underlyingModel.getColumnClass(col - 1);
	}

	/**
	 * Return the number of columns in the underlying model plus one.
	 */
	public int getColumnCount() {
		return underlyingModel.getColumnCount() + 1;
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
		if (col == 0) {
			return "Row";
		}
		return underlyingModel.getColumnName(col - 1);
	}

	public int getRowCount() {
		return underlyingModel.getRowCount();
	} // getRowCount()

	public Object getValueAt(int row, int col) {
		// Return the 1-based row for column 1.
		if (col == 0) {
			if (row < 0) {
				return "Row";
			}
			row++;
			return Integer.toString(row);
		}
		return underlyingModel.getValueAt(row, col - 1);
	} // getValueAt()

	/** Editing is allowed only if the column type is boolean */
	public void setValueAt(Object aValue, int row, int col) {
		underlyingModel.setValueAt(aValue, row, col - 1);
	}

	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}

	public int getBaseModelRowIndex(int rowIndex) {
		return underlyingModel.getBaseModelRowIndex(rowIndex);
	} // getValueAt()
} // class RowHeaderTableModel

