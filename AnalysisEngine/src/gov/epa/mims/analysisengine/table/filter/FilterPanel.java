package gov.epa.mims.analysisengine.table.filter;

import gov.epa.mims.analysisengine.gui.ChildHasChangedListener;
import gov.epa.mims.analysisengine.gui.HasChangedListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * <p>
 * Description: A generic panel that displays and edits filtering information. There is a table with 3 columns. The
 * first column is the "what to sort on" column. This might be a column name for row filtering. The second column is the
 * "what operation to use when filtering" column. This might contain "greater-than" or "contains". The last column is
 * the "what to look for" column. So, to filter rows with emissions greater than 1000, the user enters "Emission", ">",
 * "1000". At the top of the panel are two radio buttons for "AND" and "OR" comparison of all of the filtering criteria
 * entered.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: FilterPanel.java,v 1.2 2006/12/18 16:38:00 parthee Exp $
 */
public class FilterPanel extends JPanel implements ChildHasChangedListener, ActionListener {
	/** The table in which the filter information is entered. */
	protected JTable table = null;

	/** The editor for the first column. */
	protected JComboBox firstColEditor = null;

	/** The editor for the second (operation) column */
	protected JComboBox operationColEditor = null;

	/** The table model for the filtering information. */
	protected DefaultTableModel localModel = null;

	/** The choices that are offered to the user in the first column. */
	protected String[] filteringChoices = null;

	/** The operations that are offered to the user in the first column. */
	protected String[] operationChoices = null;

	/**
	 * The index of the default filtering choice to show when a new row is added to the table.
	 */
	protected int defaultFilteringChoice = 0;

	/**
	 * The index of the default operation choice to show when a new row is added to the table.
	 */
	protected int defaultOperationChoice = 0;

	/** Constant for adding a row. */
	protected static final String ADD_ROW_STR = "Add Criteria";

	/** Constant for deleting a row. */
	protected static final String DELETE_ROW_STR = "Delete Criteria";

	/** The add row button. */
	protected JButton addBtn = new JButton(ADD_ROW_STR);

	/** The delete row button. */
	protected JButton deleteBtn = new JButton(DELETE_ROW_STR);

	/** The tool bar for the window. */
	protected JToolBar toolBar = new JToolBar();

	/** A check box for applying the filter or not */
	protected JCheckBox filterCheckBox;

	/** The radio buttom for applying "AND" to all criteria in the table. */
	protected JRadioButton andRdo = new JRadioButton("ALL criteria");

	/** The radio buttom for applying "OR" to all criteria in the table. */
	protected JRadioButton orRdo = new JRadioButton("ANY criteria");

	/** a label */
	protected JLabel matchUsing = new JLabel("Match using: ");

	/** Constant for the first column in the table. */
	public static final int NAME_COLUMN = 0;

	/** Constant for the second column in the table. */
	public static final int OPERATION_COLUMN = 1;

	/** Constant for the third column in the table. */
	public static final int VALUE_COLUMN = 2;

	private Component parentComponent = null;

	private boolean hasChanged = false;

	/**
	 * Constructor.
	 */
	public FilterPanel() {
		initialize();
	} // FilterPanel()

	/**
	 * Constructor.
	 * 
	 * @param useAnd
	 *            boolean that is true if the "AND" button should be checked.
	 * @param newData
	 *            String[][] that is all of the data to place in the table.
	 * @param filteringChoices
	 *            String[] with all of the choices for the first column.
	 * @param operationChoices
	 *            String[] with all of the choices for the operation column.
	 * @param defaultFilteingChoice
	 *            int that is the index of the default filtering choice to display when a new row is added.
	 * @param defaultOperationChoice
	 *            int that is the index of the default operation choice to display when a new row is added.
	 */
	public FilterPanel(boolean useAnd, String[][] newData, String[] filteringChoices, String[] operationChoices,
			int defaultFilteingChoice, int defaultOperationChoice) {
		initialize();
		setFilteringChoices(filteringChoices, operationChoices, defaultFilteingChoice, defaultOperationChoice);
		setUseAnd(useAnd);
		setTableData(newData);
	} // FilterPanel()

	/**
	 * Add a new empty row to the filter table.
	 */
	protected void addRow() {
		localModel.addRow(new String[] { filteringChoices[defaultFilteringChoice],
				operationChoices[defaultOperationChoice], "" });
	} // addRow()

	/**
	 * Delete the selected rows from the filter table.
	 */
	protected void deleteSelectedRows() {
		int[] selectedRows = table.getSelectedRows();
		for (int i = selectedRows.length - 1; i >= 0; --i)
			localModel.removeRow(selectedRows[i]);
	} // deleteSelectedRows()

	public void removeFilterColumns(java.util.List colNames) {
		for (int i = 0, rowCount = table.getRowCount(); i < rowCount; i++) {
			String name = (String) table.getValueAt(i, NAME_COLUMN);
			if (colNames.contains(name)) {
				localModel.removeRow(i);
			}// if(colNames.contains(name))
		}// for(i)
	}// removeFilterColumn

	// public void addFilterPanelData(java.util.Map data)
	// {
	// Iterator iterator = data.keySet().iterator();
	// while(iterator.hasNext())
	// {
	// Object name = iterator.next();
	// Object[] rowData = (Object[])data.get(name);
	// localModel.insertRow(table.getRowCount(),rowData);
	// }//while
	// }

	public List getFilterColumnNames() {
		List list = new ArrayList();
		for (int i = 0, rowCount = table.getRowCount(); i < rowCount; i++) {
			list.add(table.getValueAt(i, NAME_COLUMN));
		}
		return list;
	}

	/**
	 * Return true if the "AND" button is checked.
	 * 
	 * @return boolean that is true if filtering by "AND" and false if filtering with "OR".
	 */
	public boolean getUseAnd() {
		return andRdo.isSelected();
	} // getUseAnd()

	/**
	 * Build the GUI.
	 */
	protected void initialize() {
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory
				.createEtchedBorder()));

		localModel = new DefaultTableModel(new Object[0][0], new String[] { "Column Name", "Operation", "Value" });
		table = new JTable(localModel);
		table.getAccessibleContext().setAccessibleName("List of current filter criteria");
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setPreferredScrollableViewportSize(new Dimension(450, 100));
		localModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE) // insert and delete handled by the insert & delete button
				{
					update();
				}
			}
		});
		toolBar.add(addBtn);
		toolBar.add(deleteBtn);

		addBtn.addActionListener(this);
		addBtn.setMnemonic(KeyEvent.VK_A);
		deleteBtn.addActionListener(this);
		deleteBtn.setMnemonic(KeyEvent.VK_D);

		filterCheckBox = new JCheckBox("Apply Filter?");
		filterCheckBox.addActionListener(this);
		JPanel compPanel = new JPanel();
		compPanel.add(filterCheckBox);
		compPanel.add(Box.createHorizontalStrut(10));
		compPanel.add(matchUsing);
		andRdo.setToolTipText("Only rows that match all criteria will be displayed");
		compPanel.add(andRdo);
		orRdo.setToolTipText("Rows that match any criteria will be displayed");
		compPanel.add(orRdo);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(andRdo);
		buttonGroup.add(orRdo);
		andRdo.setSelected(true);
		andRdo.addActionListener(this);
		orRdo.addActionListener(this);
		JPanel subPanel = new JPanel(new BorderLayout());
		subPanel.add(toolBar, BorderLayout.NORTH);
		subPanel.add(scrollPane, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(compPanel, BorderLayout.NORTH);
		add(subPanel, BorderLayout.CENTER);
	} // initialize();

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addBtn) {
			stopTableEditing();
			addRow();
			update();
		} else if (e.getSource() == deleteBtn) {
			stopTableEditing();
			deleteSelectedRows();
			update();
		} else if (e.getSource() == filterCheckBox) {
			boolean enabled = filterCheckBox.isSelected();
			update();
			setEnabled(enabled, true);
		} else if (e.getSource() == andRdo) {
			update();
		} else if (e.getSource() == orRdo) {
			update();
		}
	}

	// This handles the case where the user has entered a value in the cell
	// but has not pressed "Enter" to stop editing and record the value.
	private void stopTableEditing() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}

	/**
	 * Return the Strings in the table as a String[][].
	 * 
	 * @return String[][] that is all of the data in the table.
	 */
	public String[][] getTableData() {
		// This handles the case where the user has entered a value in the cell
		// but has not pressed "Enter" to stop editing and record the value.
		stopTableEditing();

		int numRows = table.getRowCount();
		int numCols = table.getColumnCount();
		String[][] retval = new String[numRows][numCols];
		for (int r = 0; r < numRows; r++) {
			for (int c = 0; c < numCols; c++) {
				retval[r][c] = (String) table.getValueAt(r, c);
			} // for(c)
		} // for(r)

		return retval;
	} // getTableData()

	/**
	 * Set the choices that the user is offered in the first and operation columns.
	 * 
	 * @param filteringChoices
	 *            String[] that are the choices to place in the first column.
	 * @param operationChoices
	 *            String[] that are the choices to place in the operation column.
	 * @param int
	 *            defaultFilteringChoice that is the index of the default filtering choice to display when a new row is
	 *            added.
	 * @param int
	 *            defaultOperationChoice that is the index of the default operation choice to display when a new row is
	 *            added.
	 */
	public void setFilteringChoices(String[] filteringChoices, String[] operationChoices, int defaultFilteringChoice,
			int defaultOperationChoice) {
		this.filteringChoices = filteringChoices;
		this.operationChoices = operationChoices;
		this.defaultFilteringChoice = defaultFilteringChoice;
		this.defaultOperationChoice = defaultOperationChoice;

		if (table != null) {
			TableColumnModel columnModel = table.getColumnModel();
			if (filteringChoices != null) {
				// If we have only one choice, then set the value and don't
				// allow editing.
				if (filteringChoices.length == 1) {
					// FIXME:??
				}
				// Otherwise make the editor a combo box.
				else {
					JComboBox columnBox = new JComboBox(filteringChoices);
					columnModel.getColumn(NAME_COLUMN).setCellEditor(new DefaultCellEditor(columnBox));
				}
			} // if (filteringChoices != null)

			if (operationChoices != null) {
				JComboBox operationBox = new JComboBox(operationChoices);
				columnModel.getColumn(OPERATION_COLUMN).setCellEditor(new DefaultCellEditor(operationBox));
			} // if (operationChoices != null)
		} // if (table != null)
	} // setFilteringChoices()

	/**
	 * 
	 * Set the Strings in the table as a String[][]. If the data contains columns names not exist in the filterChoices
	 * then that row won't be added If the rows already exist then it will be removed first before adding the data
	 * 
	 * @param String[][]
	 *            that is the new data in the table.
	 */
	public void setTableData(String[][] newData) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		// Clear the old data first.
		int numRows = model.getRowCount();
		for (int r = 0; r < numRows; r++) {
			model.removeRow(0);
		}

		// If newData is null, then we have cleared the table and we're done.
		if (newData == null || newData.length == 0) {
			return;
		}

		// Fill the table with new data.
		numRows = newData.length;
		List filterChoiceList = Collections.EMPTY_LIST;
		if (filteringChoices != null) {
			filterChoiceList = Arrays.asList(filteringChoices);
		}

		for (int r = 0; r < numRows; r++) {
			//if (filterChoiceList.contains(newData[r][NAME_COLUMN])) {
				model.addRow(newData[r]);
			//}
		} // for(r)

		model.fireTableDataChanged();
	} // setTableData()

	/**
	 * Return true if the "AND" button is checked.
	 * 
	 * @param boolean
	 *            that is true if filtering by "AND" and false if filtering with "OR".
	 */
	public void setUseAnd(boolean useAnd) {
		andRdo.setSelected(useAnd);
		orRdo.setSelected(!useAnd);
	} // setUseAnd()

	public void setApplyFilter(boolean applyFilters) {
		filterCheckBox.setSelected(applyFilters);
		setEnabled(applyFilters, true);
	}

	public boolean isApplyFilters() {
		return filterCheckBox.isSelected();
	}

	public void setEnabled(boolean enabled, boolean applyFilterEnabled) {
		matchUsing.setEnabled(enabled);
		addBtn.setEnabled(enabled);
		andRdo.setEnabled(enabled);
		deleteBtn.setEnabled(enabled);
		orRdo.setEnabled(enabled);
		table.setEnabled(enabled);
		filterCheckBox.setEnabled(applyFilterEnabled);

	}

	public void setParentComponent(Component parent) {
		parentComponent = parent;
	}

	/**
	 * IF hasChanged is false then update calls the update method in the parent component otherwise it changes the
	 * hasChanged value to true
	 */
	public void update() {
		if (parentComponent != null && parentComponent instanceof HasChangedListener) {
			if (!hasChanged) {
				((HasChangedListener) parentComponent).update();
			}
			hasChanged = true;
		}
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

} // class FilterPanel

