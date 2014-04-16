package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSetInfo;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.PlotInfo;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * A table to display, edit and save options, containing name value pairings. There will be columns for the name, value,
 * and information about the value.
 * 
 * The value editor would vary depending on the type of value defined in the corresponding optionInfo. This table can be
 * used for page options as well as plot options. The copy, paste and clear actions are implemented in this table, but
 * the buttons are located on the OptionsPanel toolbar.
 * 
 * @author Alison Eyth
 * @version $Id: DataSetsTable.java,v 1.5 2007/05/31 14:29:33 qunhe Exp $
 * 
 * @see gov.epa.mims.cse.parameter.Table
 */

public class DataSetsTable extends JTable {

	ArrayList renderers = new ArrayList(10);

	ArrayList editors = new ArrayList(10);

	/** list of DataSets */
	ArrayList dataSets = new ArrayList(10);

	DataSetInfo[] dataSetInfos = null;

	DataSetsAdapter dataSetsAdapter = null;

	/** columns of the table */
	String[] columns = { "Data Set Name", "Data Set Info", "Set" };

	public DataSetsTable(DataSetsAdapter dataSetsAdapter, PlotInfo plotInfo, Plot plot) {
		dataSetInfos = plotInfo.getDataSetInfo();
		this.dataSetsAdapter = dataSetsAdapter;
		DataSetsTableModel model = new DataSetsTableModel();

		for (int i = 0; i < dataSetInfos.length; i++) {
			String[] keys = null;
			
			if (plot.keysInitialized())
				keys = plot.getKeys(i);

			if (keys != null && keys.length > 0)
				model.setupRow(getTableRowVector(dataSetsAdapter, keys));
			else
				model.setupRow(new Vector());
		}

		this.setModel(model);
		initialize();
	}// DataSetsTable()

	private Vector getTableRowVector(DataSetsAdapter dataSetsAdapter, String[] keys) {
		Vector rowVector = new Vector(keys.length);

		for (int i = 0; i < keys.length; i++) {
			DataSetIfc dataSet = dataSetsAdapter.getDataSet(keys[i]);
			DataSetWithKey dswk = new DataSetWithKey(keys[i], dataSet);
			rowVector.add(dswk);
		}

		return rowVector;
	}

	private void initialize() {
		for (int i = 0; i < dataSetInfos.length; i++) {
			Class editorClass = null;
			renderers.add(new ButtonTableCellRenderer("Set"));
			TableCellEditor editor = new DataSetTableCellEditor("Set", i);
			editors.add(editor);
		}
	}

	/**
	 * copy the values in the selected rows of the table to a clipboard
	 */
	protected void copyValues() {
	}

	/**
	 * paste the values in the clipboard to the selected rows of the table
	 */
	protected void pasteValues() {
		// if one value is on the clipboard, copy it to all selected rows
		// if multiple values are on the clipboard, make sure the same number
		// of values are selected with the same data types and copy them
		// to the selected rows
	}

	protected void setDataSetsForPlot(Plot plot) throws Exception {
		// TBD: come back and make the auto selection work again
		/*
		 * dataSets.toArray(datasetArray); for (int i = 0; i < dataSets.size(); i++) { // don't fill in all the data
		 * sets if the max is not N int max = dataSetInfos[i].getMaxNumber(); if ((datasetArray[i].getKeys().size() ==
		 * 0) && (max == -1)) { // if nothing is selected, automatically select all the data sets in // the data sets
		 * adapter DataSets ds = (DataSets)dataSets.get(i); Vector keyVect = dataSetsAdapter.getDataSetKeys( null,
		 * this.getParent()); // TBD: if only one data set is required, don't add them all for (int j = 0; j <
		 * keyVect.size(); j++) { Object key = keyVect.get(j); ds.add(dataSetsAdapter.getDataSet(key), key); }
		 * this.repaint(); } } dataSets.toArray(datasetArray);
		 */

		plot.createDataSetKeys(dataSets);
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column < 2) {
			return super.getCellRenderer(row, column);
		} else {
			return (TableCellRenderer) renderers.get(row);
		}
	}

	/**
	 * return the cell editor
	 */
	public TableCellEditor getCellEditor(int row, int column) {
		TableCellEditor result = null;
		if (column == 0) {
			return super.getCellEditor(row, column);
		} else {
			return (TableCellEditor) (editors.get(row));
		}
	}

	public class DataSetsTableModel extends AbstractTableModel {

		public DataSetsTableModel() {
			super();
		}

		public void setupRows() {
			for (int i = 0; i < dataSetInfos.length; i++) {
				dataSets.add(new Vector());
			}
		}

		public void setupRow(Vector v) {
			dataSets.add(v);
		}

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataSetInfos.length;
		}

		public Object getValueAt(int row, int col) {
			Vector sets;
			switch (col) {
			case 0:
				return dataSetInfos[row].getName();
			case 1:
				sets = ((Vector) dataSets.get(row));
				// Set keys = sets.getKeys();
				if (sets.size() == 0) {
					return dataSetInfos[row].getInfo();
				} else if (sets.size() == 1) {
					return sets.iterator().next().toString();
					// return sets.getDataSet(keys.iterator().next()).getName();
				} else {
					return Integer.toString(sets.size()) + " selected";
				}
			case 2:
				return dataSetInfos[row];
			}
			return "";
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Class getColumnClass(int col) {
			return Object.class;
		}

		public boolean isCellEditable(int row, int col) {
			return (col == 2);
		}

		public void setValueAt(Object aValue, int row, int col) {
			switch (col) {
			case 2:
				dataSets.remove(row);
				dataSets.add(row, aValue);
				fireTableCellUpdated(row, 1);
				break;
			default:
				break;
			}
			fireTableCellUpdated(row, col);
		}

	}// DataSetsTableModel

	public class DataSetTableCellEditor extends JButton implements TableCellEditor {
		protected Vector listeners;

		protected JTable table;

		protected int row, column;

		/** dialog to show when edit button is pressed */

		/** the index of the data set list being edited */
		int index;

		/** the text to display on the button */
		String labelText;

		public DataSetTableCellEditor(String text, int index) {
			super(text);
			labelText = text;
			this.index = index;
			addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent A) {
					edit();
				}
			});
			listeners = new Vector();

			// add focus listener to stop editing when focus is lost
			this.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent evt) {
				}

				public void focusLost(FocusEvent evt) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.table = table;
			this.row = row;
			this.column = column;

			// originalValue = (Value) this.table.getParameter(row);
			// newValue = originalValue;
			return this;
		}

		// CellEditor methods
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		public Object getCellEditorValue() {
			return dataSets.get(index);
		}

		public boolean isCellEditable(EventObject eo) {
			return true;
		}

		public boolean shouldSelectCell(EventObject eo) {
			return true;
		}

		public boolean stopCellEditing() {
			fireEditingStopped();
			return true;
		}

		public void addCellEditorListener(CellEditorListener cel) {
			listeners.addElement(cel);
		}

		public void removeCellEditorListener(CellEditorListener cel) {
			listeners.removeElement(cel);
		}

		protected void fireEditingCanceled() {
			ChangeEvent ce = new ChangeEvent(this);
			for (int i = listeners.size() - 1; i >= 0; i--) {
				((CellEditorListener) listeners.elementAt(i)).editingCanceled(ce);
			}
		}

		protected void fireEditingStopped() {
			ChangeEvent ce = new ChangeEvent(this);
			for (int i = listeners.size() - 1; i >= 0; i--) {
				((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
			}
		}

		void edit() {
			Vector currentSelection = (Vector) dataSets.get(index);
			Vector selected = DataSetSelector.pickDataSets(dataSetsAdapter, currentSelection, dataSetInfos[index]);
			if ((selected != null) && (selected != currentSelection)) {
				dataSets.remove(index);
				dataSets.add(index, selected);
				table.setValueAt(dataSets.get(index), row, column);
			}
		}

		public String toString() {
			return super.toString();
		}
	} // DataSetTableCellEditor

}
