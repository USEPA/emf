package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.AnalysisOption;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/**
 * A table to display, edit and save options, containing name value pairings. There will be columns for the name, value,
 * and information about the value.
 * 
 * The value editor would vary depending on the type of value defined in the corresponding optionInfo. This table can be
 * used for page options as well as plot options. The copy, paste and clear actions are implemented in this table, but
 * the buttons are located on the OptionsPanel toolbar.
 * 
 * @author Alison Eyth
 * @version $Id: OptionsTable.java,v 1.3 2006/12/15 15:32:10 parthee Exp $
 * 
 * @see gov.epa.mims.cse.parameter.SimpleTable
 */

public class OptionsTable extends JTable {
	/** analysis options to be shown */
	AnalysisOptions analysisOptions;

	/** all option keywords available */
	String[] allKeywords;

	/** default values for all option keywords */
	AnalysisOption[] defaultValues;

	/** current values for all options */
	AnalysisOption[] allValues;

	/** all option keywords available */
	String[] allNames;

	ArrayList renderers = new ArrayList(10);

	ArrayList editors = new ArrayList(10);

	ArrayList optionInfos = new ArrayList(10);

	/** columns of the table */
	String[] columns = { "Option Name", "Value Info", "Option Value" };

	/**
	 * The type of plot that this table is representing. Must be one of the plot type constants from
	 * AnalysisEngineConstants.
	 */
	private String plotTypeName = null;

	/**
	 * Constructor for options table.
	 * 
	 * @param options
	 *            AnalysisOptions to edit
	 * @param allKeywords
	 *            String [] the list of all possible keywords
	 * @param defaultValues
	 *            Object [] default values for all options
	 * @param plotType
	 *            String that is one of the plot constants in AnalysisEngineConstants.
	 */
	public OptionsTable(AnalysisOptions options, String[] allKeywords, AnalysisOption[] defaultValues, String plotType) {
		this.analysisOptions = options;
		allValues = new AnalysisOption[allKeywords.length];

		if (analysisOptions == null) {
			analysisOptions = new AnalysisOptions();
		}

		// fill the values with the items from analysis options
		for (int i = 0; i < allKeywords.length; i++) {
			Object obj = options.getOption(allKeywords[i]);
			if (obj != null) {
				allValues[i] = (AnalysisOption) obj;
			} else {
				allValues[i] = defaultValues[i];
				analysisOptions.addOption(allKeywords[i], defaultValues[i]);
			}
		}

		this.allKeywords = allKeywords;
		this.defaultValues = defaultValues;
		OptionsTableModel model = new OptionsTableModel();
		model.setupRows(); // initializes allValues;
		this.setModel(model);
		this.plotTypeName = plotType;
		// super(model);
		initialize();
	}// OptionsTable()

	private void initialize() {
		allNames = new String[allKeywords.length];
		for (int i = 0; i < allKeywords.length; i++) {
			try {
				Class editorClass = null;
				renderers.add(new OptionTableCellRenderer(allValues[i]));
				OptionInfo oi = OptionInfo.lookUp(allKeywords[i]);
				TableCellEditor editor = null;
				if (oi == null) {
					System.err.println("Could not find option info for " + allKeywords[i]);
				} else {
					allNames[i] = oi.getName();
					optionInfos.add(oi);
					editorClass = (Class) oi.getValueEditorType();
					if (editorClass == null) {
						// TBD: what to do if no editor found
						System.err.println("Could not find value editor type for " + allKeywords[i]);
					} else if (TableCellEditor.class.isAssignableFrom(editorClass)) {
						editor = (TableCellEditor) editorClass.newInstance();
					} else if (OptionDialog.class.isAssignableFrom(editorClass)) { /*
																					 * this is the main branch that
																					 * should be taken
																					 */
						editor = new OptionTableCellEditor();
						((OptionTableCellEditor) editor).initFromOptionInfo(oi, allValues[i], plotTypeName);
					}
				}
				editors.add(editor);
			} catch (Exception exc) {
				exc.printStackTrace();
				throw new IllegalArgumentException("Could not find editor for option " + allKeywords[i]);
			}
		}
	}

	/**
	 * @return Object [] the current values for all keywords
	 */
	protected Object[] getCurrentValues() {
		return allValues;
	}

	/**
	 * Return the AnalysisOptions
	 * 
	 */
	public AnalysisOptions getAnalysisOptions() {
		return analysisOptions;
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

	/**
	 * clear the values for the selected rows
	 */
	protected void clearValues() {
		int[] rows = this.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			int thisRow = rows[i];
			allValues[thisRow] = null;
			((OptionTableCellRenderer) renderers.get(thisRow)).setText("Add");
			analysisOptions.removeOption(allKeywords[thisRow]);
			try {
				((OptionTableCellEditor) editors.get(thisRow)).resetOption((OptionInfo) optionInfos.get(thisRow));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		this.repaint();
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

	protected void updateOption(int row, int col, OptionInfo optionInfo, Object objectToEdit) {
		analysisOptions.addOption(allKeywords[row], objectToEdit);
		((OptionTableCellRenderer) renderers.get(row)).setText("Edit");
		this.repaint();
		getModel().setValueAt(objectToEdit, row, col);
		// need to update renderer for option
	}

	/**
	 * a model for the options table
	 */
	private class OptionsTableModel extends AbstractTableModel {

		public OptionsTableModel() {
			super();
		}

		public void setupRows() {

		}

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return allKeywords.length;
		}

		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return allNames[row];
			case 1:
				if (allValues[row] != null) {
					return editors.get(row).toString();
				}
			case 2:
				return allValues[row];
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
				allValues[row] = (AnalysisOption) aValue;
				fireTableCellUpdated(row, 1);
				break;
			default:
				break;
			}
			fireTableCellUpdated(row, col);
		}

	}// OptionsTableModel

}
