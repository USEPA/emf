package gov.epa.mims.analysisengine.table.format;

import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.table.ColumnSelectionPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.text.Format;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * <p>
 * Description: A GUI that can edit the format for either a single column or multiple columns.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: ColumnFormatGUI.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public class ColumnFormatGUI extends OptionDialog implements TableModelListener {
	/** The underlying data model for this panel. */
	protected ColumnFormatInfo formatInfo = null;

	/**
	 * The column headers. This in only used (and hence non-null) in MULTIPLE mode.
	 */
	protected String[][] columnHeaders = null;

	/** The format for each column. This is used only in MULTIPLE mode. */
	protected Format[] formats = null;

	/** The main panel for editing the format. */
	protected ColumnFormatPanel formatPanel = null;

	/**
	 * The column selection GUI for the case where we are editing multiple columns. This in only used (and hence
	 * non-null) in MULTIPLE mode.
	 */
	protected ColumnSelectionPanel selectionPanel = null;

	/**
	 * An array of booleans to indicate whether the columns are selected or not
	 */
	private boolean[] selColumns;

	/** The mode that we are operating in. Either SINGLE or MULTIPLE. */
	protected byte mode = SINGLE;

	/** The constant for single column editing mode. */
	public static final byte SINGLE = 0;

	/** The constant for multiple column editing mode. */
	public static final byte MULTIPLE = 1;

	/**
	 * Constructor for <b>SINGLE</b> column editing mode.
	 * 
	 * @param columnName
	 *            String that is the name of the column being edited.
	 * @param columnFormatInfo
	 *            ColumnFormatInfo that contains the formatting information for the column being edited.
	 */
	public ColumnFormatGUI(JFrame parent, String columnName, ColumnFormatInfo columnFormatInfo) {
		super(parent);
		formatInfo = columnFormatInfo;
		mode = SINGLE;
		initialize();
		setDataSource(formatInfo, "");
		setTitle("Format Column : " + columnName);
		setModal(true);
		pack();
	} // ColumnFormatGUI()

	/**
	 * Constructor for <b>MULTIPLE</b> column editing mode.
	 * 
	 * @param columnHeaders
	 *            String[][] that contains the column headers for all columns.
	 * @param formats
	 *            Format[] that contains the Format for each column in the order passed in the columnHeaders array.
	 * @param columnFormatInfo
	 *            ColumnFormatInfo that contains the formatting information for the columns being edited.
	 */
	public ColumnFormatGUI(JFrame parent, String[][] columnHeaders, Format[] formats, ColumnFormatInfo columnFormatInfo) {
		super(parent);
		if (columnHeaders.length != formats.length) {
			throw new IllegalArgumentException("columnHeaders.length and "
					+ " formats.length must be equal in ColumnFormatGUI()!");
		}

		this.columnHeaders = columnHeaders;
		this.formats = formats;
		formatInfo = columnFormatInfo;
		mode = MULTIPLE;
		initialize();
		setDataSource(formatInfo, "");
		setTitle("Format Columns");
		setModal(true);
		pack();
	} // ColumnFormatGUI()

	/**
	 * Constructor for <b>MULTIPLE</b> column editing mode.
	 * 
	 * @param columnHeaders
	 *            String[][] that contains the column headers for all columns.
	 * @param selColumns
	 *            boolean [] true for selected and false for unselected
	 * @param formats
	 *            Format[] that contains the Format for each column in the order passed in the columnHeaders array.
	 * @param columnFormatInfo
	 *            ColumnFormatInfo that contains the formatting information for the columns being edited.
	 */
	public ColumnFormatGUI(JFrame parent, String[][] columnHeaders, boolean[] selColumns, Format[] formats,
			ColumnFormatInfo columnFormatInfo) {
		super(parent);
		if (columnHeaders.length != formats.length || columnHeaders.length != selColumns.length) {
			throw new IllegalArgumentException("columnHeaders.length and "
					+ " formats.length must be equal in ColumnFormatGUI()!");
		}

		this.columnHeaders = columnHeaders;
		this.selColumns = selColumns;
		this.formats = formats;
		formatInfo = columnFormatInfo;
		mode = MULTIPLE;
		initialize();
		setDataSource(formatInfo, "");
		setTitle("Format Columns");
		setModal(true);
		pack();
	} // ColumnFormatGUI()

	protected void initGUIFromModel() {
		//
	}

	/**
	 * Build the GUI.
	 */
	protected void initialize() {
		formatPanel = new ColumnFormatPanel(formatInfo);

		if (mode == SINGLE) {
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(formatPanel, BorderLayout.CENTER);
			contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		} // if (mode == SINGLE)
		else // MULTIPLE
		{

			// Select none by default.
			if (selColumns == null) {
				selColumns = new boolean[columnHeaders.length];
			}
			// set the format to nulls
			selectionPanel = new ColumnSelectionPanel(columnHeaders, selColumns, "Show", "Hide");
			selectionPanel.addTableModelListener(this);
			// to initialize the panel correctly with appropriate format RP
			tableChanged(null);

			JPanel mainPanel = new JPanel();
			// mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(selectionPanel, BorderLayout.CENTER);
			mainPanel.add(formatPanel, BorderLayout.EAST);

			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(mainPanel, BorderLayout.CENTER);
			contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		} // else

	} // initialize()

	/**
	 * Return the edited ColumnFormatInfo.
	 * 
	 * @throws java.lang.Exception
	 */
	public ColumnFormatInfo getColumnFormatInfo() throws Exception {
		saveGUIValuesToModel();
		return formatInfo;
	} // getColumnFormatInfo()

	/**
	 * Return the selected columns if we are in multiple mode.
	 * 
	 * @return getSelectedColumns() boolean[] with a true for each selected column. This is in the same order as the
	 *         column names passed in.
	 * 
	 * throws IllegalArgumentException if we are <b>not</b> in MULTIPLE mode.
	 */
	public boolean[] getSelectedColumns() {
		return selectionPanel.getCheckedColumns();
	} // getSelectedColumns()

	/**
	 * Return the String array that consist of selected column names in muliple mode
	 * 
	 * @returnn String[] selectedcolumn Names
	 */

	public String[] getSelectedColumnNames() {
		boolean[] selCols = selectionPanel.getCheckedColumns();
		Vector selColsNames = new Vector();
		for (int i = 0; i < selCols.length; i++) {
			if (selCols[i]) {
				selColsNames.add(columnHeaders[i][0]); // first line is displayed on the col sel table
				// so this name is used to track the whether column is previously selected or not
			}
		}
		if (selColsNames.size() == 0) {
			return null;
		}
		String[] a = {};
		return (String[]) selColsNames.toArray(a);
	}

	/**
	 * Save the values in the GUI to the data model.
	 * 
	 * @throws java.lang.Exception
	 */
	protected void saveGUIValuesToModel() throws java.lang.Exception {
		formatPanel.saveGUIValuesToModel();
		formatInfo = formatPanel.getColumnFormatInfo();
	} // saveGUIValuesToModel()

	/**
	 * Respond to new columns that have been checked or unchecked in the column selection panel. When this happens,
	 * check the formats for the checked columns. If they are all of the same type (Number or Date), then show a
	 * formatting panel with
	 */
	public void tableChanged(TableModelEvent e) {
		boolean[] checkedColumns = selectionPanel.getCheckedColumns();
		Format lastFormat = null;

		// Loop through all of the columns and see which ones were checked.
		// For one that were checked, see if they use the same type of format.
		for (int c = 0; c < checkedColumns.length; c++) {
			if (checkedColumns[c]) {
				if (lastFormat != null) {
					if (!lastFormat.getClass().equals(formats[c].getClass())) {
						lastFormat = null;
						break;
					}
				} // if (lastFormat != null)

				lastFormat = formats[c];
			} // if (checkedColumns[c])
		} // for(c)

		// If only one column was checked, then we

		// If the lastFormat is *not* null, then we have a group of columns
		// checked that have a common number or date format.
		if (lastFormat != null) {
			// If the common format was the NullFormatter, then pass null to the
			// formatPanel to tell it to remove the text formatting panel.
			if (lastFormat instanceof NullFormatter) {
				lastFormat = null;
			}
		} // if (lastFormat != null)

		formatPanel.setFormat(lastFormat);
	} // tableChanged()

} // class ColumnFormatGUI

