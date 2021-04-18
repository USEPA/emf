package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.filter.FilterPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

public class ColumnSelectionPanel extends JPanel implements ListSelectionListener {
	/** The main filtering panel for the selection of columns. */
	protected FilterPanel filterPanel = null;

	/** Table with the columns on it. */
	protected ColumnSelectionTable table = null;

	/**
	 * FilterCriteria for choosing columns. Note that this is *NOT* the same as the FilterCriteria held by the main
	 * table. This one is only for selecting columns.
	 */
	protected FilterCriteria localCriteria = null;

	/** Button to apply the filter criteria to select the rows in the table. */
	protected JButton filterBtn = new JButton("Select");

	/** Button that reverses the current selection. */
	protected JButton reverseBtn = new JButton("Invert");

	/** Button to check the selected rows in the table. */
	protected JButton checkBtn = new JButton("Check");

	/** Button to un-check the selected rows in the table. */
	protected JButton uncheckBtn = new JButton("Uncheck");

	/** The label that shows the number of rows selected. */
	protected JLabel selectionLabel = new JLabel("");

	/**
	 * Constructor. In here since
	 * 
	 * @param columnHeaders
	 *            String[][] that is the list of headers for each column.
	 */
	public ColumnSelectionPanel(String[][] columnHeaders) {
		this(columnHeaders, new boolean[columnHeaders.length]);
	} // ColumnSelectionPanel()

	/**
	 * Constructor.
	 * 
	 * @param columnHeaders
	 *            String[][] that is the list of headers for each column.
	 * @param selected
	 *            boolean[] where an element is true if the column is selected.
	 */
	public ColumnSelectionPanel(String[][] columnHeaders, boolean[] selected) {
		initialize(columnHeaders, selected, "Check", "Uncheck");
	} // ColumnSelectionPanel()

	/**
	 * Constructor.
	 * 
	 * @param columnHeaders
	 *            String[][] that is the list of headers for each column.
	 * @param selected
	 *            boolean[] where an element is true if the column is selected.
	 * @param checkString
	 *            String to place on the "check" button.
	 * @param uncheckString
	 *            String to place on the "uncheck" button.
	 */
	public ColumnSelectionPanel(String[][] columnHeaders, boolean[] selected, String checkString, String uncheckString) {
		initialize(columnHeaders, selected, checkString, uncheckString);
	} // ColumnSelectionPanel()

	/**
	 * Allow another class to listen when the table values are checked or unchecked.
	 */
	public void addTableModelListener(TableModelListener listener) {
		table.getModel().addTableModelListener(listener);
	} // addTableModelListener()

	/**
	 * Take the filter that is shown and select rows in the column name table based on the filter.
	 * 
	 * @param check
	 */
	public void applyFilterToSelection() {
		Object[][] tableData = filterPanel.getTableData();
		if (tableData == null || tableData.length == 0) {
			return;
		}

		// Extract the information from the filter panel and create a
		// FilterCriteria.
		String[] columnNames = new String[tableData.length];
		int[] operations = new int[columnNames.length];
		Comparable[] values = new Comparable[columnNames.length];
		// Format[] formats = new Format[columnNames.length];
		String[] allColumnNames = { "Column Name" };

		for (int i = 0; i < tableData.length; i++) {
			columnNames[i] = (String) tableData[i][0];
			Integer intObj = (Integer) FilterCriteria.symbolToConstantHash.get(tableData[i][1]);
			operations[i] = intObj.intValue();
			values[i] = (Comparable) tableData[i][2];
			// formats[i] = FormattedCellRenderer.nullFormatter;
		} // for(i)

		boolean useAnd = filterPanel.getUseAnd();

		FilterCriteria fc = new FilterCriteria(columnNames, operations, values,
		/* formats, */allColumnNames, useAnd);
		fc.setTableModel(table.getTableModel());
		// Now apply the filter to decide which rows to select.
		int numRows = table.getRowCount();
		Comparable[] comp = new Comparable[1];
		table.clearSelection();
		for (int r = 0; r < numRows; r++) {
			comp[0] = (Comparable) table.getValueAt(r, 0);
			if (fc.accept(comp)) {
				table.addRowSelectionInterval(r, r);
			}
		} // for(r)
	} // applyFilterToSelection()

	/**
	 * Check or uncheck the selected rows in the table.
	 * 
	 * @param check
	 *            boolean that is true if we should check the selected values and false if we should uncheck them.
	 */
	public void checkSelectedRows(boolean check) {
		int[] selectedRows = table.getSelectedRows();
		int lastCol = table.getColumnCount() - 1;
		Boolean value = new Boolean(check);

		for (int i = 0; i < selectedRows.length; i++) {
			table.setValueAt(value, selectedRows[i], lastCol);
		} // for(i)
	} // checkSelectedRows()

	/**
	 * Build the GUI.
	 * 
	 * @param columnHeaders
	 *            String[][] that is the list of headers for each column.
	 * @param selected
	 *            boolean[] where an element is true if the column is selected.
	 */
	protected void initialize(String[][] columnHeaders, boolean[] selected, String checkString, String uncheckString) {
		// Table
		String[] tableHeaders = { "Column Name", checkString + "?" };

		table = new ColumnSelectionTable(tableHeaders, columnHeaders, selected);
		table.getAccessibleContext().setAccessibleName("List of columns to show or hide");
		table.getSelectionModel().addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		selectionLabel.setFocusable(true);
		selectionLabel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                selectionLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            public void focusLost(FocusEvent e) {
                selectionLabel.setBorder(null);
            }
        });
		selectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		valueChanged(new ListSelectionEvent(table, 0, 0, false));

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
				BorderFactory.createEtchedBorder()));
		topPanel.add(scrollPane, BorderLayout.CENTER);
		topPanel.add(selectionLabel, BorderLayout.SOUTH);

		// Button panel
		checkBtn.setText(checkString);
		uncheckBtn.setText(uncheckString);
		JPanel buttonPanel = new JPanel();
		filterBtn.setToolTipText("Select columns matching the filter criteria");
		buttonPanel.add(filterBtn);
		reverseBtn.setToolTipText("Invert column selections");
		buttonPanel.add(reverseBtn);
		checkBtn.setToolTipText("Mark selected columns as shown");
		buttonPanel.add(checkBtn);
		uncheckBtn.setToolTipText("Mark selected columns as hidden");
		buttonPanel.add(uncheckBtn);

		filterBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyFilterToSelection();
			}
		});
		filterBtn.setMnemonic('S');

		reverseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reverseSelection();
			}
		});
		reverseBtn.setMnemonic('I');

		checkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkSelectedRows(true);
			}
		});
		checkBtn.setMnemonic('w');

		uncheckBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkSelectedRows(false);
			}
		});
		uncheckBtn.setMnemonic('H');

		// Filter Panel
		filterPanel = new FilterPanel(true, null, new String[] { "Column Name" }, FilterCriteria.OPERATION_STRINGS, 0,
				FilterCriteria.CONTAINS);

		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new BorderLayout());
		JPanel largerTopPanel = new JPanel(new BorderLayout());
		largerTopPanel.add(topPanel, BorderLayout.CENTER);
		largerTopPanel.add(buttonPanel, BorderLayout.SOUTH);
		add(largerTopPanel, BorderLayout.CENTER);
		add(filterPanel, BorderLayout.SOUTH);
	} // initialize()

	/**
	 * Return the checked columns. <b>NOTE: even if the user has sorted the column names, we will still return this
	 * array as though they had not sorted. </b>
	 * 
	 * @return boolean[] with true for each column that was checked in the GUI.
	 */
	public boolean[] getCheckedColumns() {
		return table.getCheckedColumns();
	} // getFilterCriteria()

	/**
	 * Reverse the current selection of columns in the table. If nothing is selected, select everything.
	 */
	public void reverseSelection() {
		int numRows = table.getRowCount();
		for (int r = 0; r < numRows; r++) {
			if (table.isRowSelected(r)) {
				table.removeRowSelectionInterval(r, r);
			} else {
				table.addRowSelectionInterval(r, r);
			}
		} // for(r)
	} // reverseSelection()

	/**
	 * Save the filter values to the FilterCriteria.
	 */
	public void saveGUIValuesToModel() {
		// Get the value for filtering from the filter panel.
		String[][] tableData = filterPanel.getTableData();
		String[] cols = new String[tableData.length];
		int[] ops = new int[tableData.length];
		String[] values = new String[tableData.length];
		for (int r = 0; r < tableData.length; r++) {
			cols[r] = "Column Name";
			Integer intObj = (Integer) FilterCriteria.symbolToConstantHash.get(tableData[r][1]);
			ops[r] = intObj.intValue();
			values[r] = tableData[r][2];
		}

		if (localCriteria == null) {
			localCriteria = new FilterCriteria(cols, ops, values,/* formats, */cols, filterPanel.getUseAnd());
		} else {
			localCriteria.setRowCriteria(cols, ops, values, /* formats, */filterPanel.getUseAnd());
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			selectionLabel.setText(table.getSelectedRowCount() + " columns selected");
		}
	}

}
