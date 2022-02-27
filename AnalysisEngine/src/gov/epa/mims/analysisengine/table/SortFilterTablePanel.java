package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.ChildHasChangedListener;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.HasChangedListener;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.filter.FilterRowGUI;
import gov.epa.mims.analysisengine.table.format.ColumnFormatGUI;
import gov.epa.mims.analysisengine.table.format.ColumnFormatInfo;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;
import gov.epa.mims.analysisengine.table.format.HasFormatter;
import gov.epa.mims.analysisengine.table.persist.AnalysisConfiguration;
import gov.epa.mims.analysisengine.table.persist.ConfigFileHistory;
import gov.epa.mims.analysisengine.table.persist.LoadConfigurationGUI;
import gov.epa.mims.analysisengine.table.persist.SaveConfigModel;
import gov.epa.mims.analysisengine.table.persist.SaveConfigView;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.table.sort.SortGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.Format;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * <p>
 * Title: Sorting Filter Table
 * </p>
 * <p>
 * Description: This table can both sort and filter data based on criteria entred by the user.
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: SortFilterTablePanel.java,v 1.30 2009/03/26 14:22:23 dyang02 Exp $
 */
public class SortFilterTablePanel extends JPanel implements TableModelListener, ChildHasChangedListener {

	/** The table that will display the data. */
	protected RowHeaderTable table = null;

	/**
	 * The status bar at the bottom of the table that contains the number of rows and columns.
	 */
	protected JLabel statusLabel = new JLabel();

	/** The text for the start of the status label. */
	public static final String ROWS_STR = " rows : ";

	/** The text for the start of the status label. */
	public static final String COLUMNS_STR = " columns";

	/** The popup that displays the filterand forting menus. */
	protected JPopupMenu popupMenu = null;

	/** The toolbar with buttons for the items on the popup. */
	protected JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

	/** The top most model that controls the other models. */
	protected OverallTableModel overallModel = null;

	protected AnalysisConfiguration aconfig = null;

	/**
	 * A list of all of the Formatters for the columns. This is used to pass to the filter mechanism.
	 */
	// protected Format[] allColumnFormats = null;
	/**
	 * The current X value for the last mouse click. Used to bring up the popup menu.
	 */
	protected int currentX = 0;

	/** Parent component that holds this panel. */
	protected Component parent;

	/** column selection gui for stats */
	protected SelectColumnsGUI filterGUI;

	/** a variable to save the selected columns in the ColumnFormatGUI */
	protected String[] selFormatColName = null;

	/** to keep track of the columnformatinfo for multiple columns * */
	protected ColumnFormatInfo formatInfo = null;

	// //Icons for the tool bar ////////
	static protected ImageIcon sortIcon = null;

	static protected ImageIcon filterIcon = null;

	static protected ImageIcon showHideIcon = null;

	static protected ImageIcon resetIcon = null;

	static protected ImageIcon formatIcon = null;

	static protected ImageIcon plotIcon = null;

	static protected ImageIcon statsIcon = null;

	static protected ImageIcon topNRowsIcon = null;

	static protected ImageIcon bottomNRowsIcon = null;

	static protected ImageIcon configIcon = null;

	static protected ImageIcon clearAllIcon = null;

	static protected ImageIcon selAllIcon = null;

	/** scrollPane JScrollPane on which the table resides. */

	protected JScrollPane scrollPane;

	private int defaultRowHeight = -1;

	protected boolean popupMenuWanted = true;

	protected boolean sort = true;

	protected boolean filter = true;

	protected boolean format = true;

	protected boolean showHide = true;

	protected boolean reset = true;

	public final static int FIRST_COLUMN_ROW_HEADER_WIDTH = 30;

	protected boolean hasChanged = false;

	/**
	 * Constructor.
	 * 
	 * @param model
	 *            MultiRowHeaderTableModel that is the base model for this table.
	 */
	public SortFilterTablePanel(Component parent, MultiRowHeaderTableModel model) {
		this.parent = parent;
		createTable(model);
		scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		createIcons();
		createPopupMenuAndToolBar();
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		statusLabel.setFocusable(true);
		statusLabel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                statusLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            public void focusLost(FocusEvent e) {
                statusLabel.setBorder(null);
            }
        });
		add(statusLabel, BorderLayout.SOUTH);
		DefaultUserInteractor.set(new GUIUserInteractor());
		for (int i = 0; i < table.getColumnCount(); i++) {
			ColumnFormatInfo info = new ColumnFormatInfo(table.getColumnModel().getColumn(i));
			// System.out.println("table.getColumnName(i)="+table.getColumnName(i));
			overallModel.setColumnFormatInfo(table.getColumnName(i), info);
		}
		defaultRowHeight = table.getRowHeight();
		table.getColumnModel().getColumn(0).setPreferredWidth(FIRST_COLUMN_ROW_HEADER_WIDTH);
	} // SortFilterTablePanel()

	/**
	 * Adding/Removing different actions are implemented in createPopupMenuAndToolBar This is only implemented in the
	 * gov.epa.emissions.emisview.gui.SortFilterSelectionPanel So if you want to use this constructor from some other
	 * class please refactor the createPopupMenuAndToolBar()
	 * 
	 */
	public SortFilterTablePanel(Component parent, MultiRowHeaderTableModel model, boolean popupMenu, boolean sort,
			boolean filter, boolean format, boolean showHide, boolean reset) {
		this.sort = sort;
		this.filter = filter;
		this.format = format;
		this.showHide = showHide;
		this.reset = reset;
		this.parent = parent;
		this.popupMenuWanted = popupMenu;
		createTable(model);
		scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		createIcons();
		createPopupMenuAndToolBar();
		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		statusLabel.setFocusable(true);
		statusLabel.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
            	statusLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            public void focusLost(FocusEvent e) {
            	statusLabel.setBorder(null);
            }
        });
		add(statusLabel, BorderLayout.SOUTH);
		DefaultUserInteractor.set(new GUIUserInteractor());
		for (int i = 0; i < table.getColumnCount(); i++) {
			ColumnFormatInfo info = new ColumnFormatInfo(table.getColumnModel().getColumn(i));
			overallModel.setColumnFormatInfo(table.getColumnName(i), info);
		}
		defaultRowHeight = table.getRowHeight();
		table.getColumnModel().getColumn(0).setPreferredWidth(FIRST_COLUMN_ROW_HEADER_WIDTH);
	}

	/**
	 * A helper method to create icons subclasses will override to add additional icons
	 */
	protected void createIcons() {
		// //Icons for the tool bar ////////
		sortIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/sort.jpeg");
		filterIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/filter.jpeg");
		showHideIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/show.jpeg");
		resetIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/reset.jpeg");
		formatIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/format.jpeg");
		plotIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/plot.jpeg");
		statsIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/histogram.jpeg");
		topNRowsIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/topNRows.jpeg");
		bottomNRowsIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/bottomNRows.jpeg");
		configIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/config1.jpeg");
		clearAllIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/clearAll.jpeg");
		selAllIcon = createImageIcon("/gov/epa/mims/analysisengine/table/icons/selectAll.jpeg");
	}

	/**
	 * Aggregate the columns and add new columns.
	 */
	public void aggregateColumns() {
		overallModel.aggregateColumns();
	} // aggregateColumns()

	/**
	 * 
	 * Aggregate rows by adding sum and average row.
	 * 
	 */

	public void aggregateRows() {
		overallModel.aggregateRows(false);
	} // aggregateRows()

	/**
	 * get Column Class
	 */

	public Class getColumnClass(int i) {
		return overallModel.getColumnClass(i);
	}

	/**
	 * A helper method to load the icons
	 * 
	 * @param path
	 *            location of the image icon.
	 * @return ImageIcon
	 */
	public ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		}
		System.err.println("Could not find file: " + path + " in classpath.");
		return null;
	} // createImageIcon()

	/**
	 * Create the popup menu for the table. Also add any items that are not column specific to the toolbar.
	 */
	protected void createPopupMenuAndToolBar() {
		Action action = null;
		popupMenu = new JPopupMenu("Table Operations");
		action = new SortMultipleAction(this);
		popupMenu.add(action);

		JButton sortButton = toolBar.add(action);
		sortButton.setToolTipText("Sort(Ascending/Descending)");
		popupMenu.addSeparator();
		action = new FilterAction(this);
		popupMenu.add(action);

		JButton filterButton = toolBar.add(action);
		filterButton.setToolTipText("Filter Rows");
		action = new ShowHideColumnsAction(this);
		popupMenu.add(action);

		JButton showHideButton = toolBar.add(action);
		showHideButton.setToolTipText("Show/Hide Columns");
		popupMenu.addSeparator();
		action = new SingleFormatAction(this);
		popupMenu.add(action);
		action = new MultipleFormatAction(this);
		popupMenu.add(action);

		JButton formatButton = toolBar.add(action);
		formatButton.setToolTipText("Format columns");

		// popupMenu.add(new AggregateRowsAction(this));
		// popupMenu.add(new AggregateColumnsAction(this));
		popupMenu.addSeparator();
		action = new ResetAction(this);
		popupMenu.add(action);

		JButton resetButton = toolBar.add(action);
		resetButton.setToolTipText("Reset");

		TableMouseAdapter tableMouseAdapter = new TableMouseAdapter();
		PopupMouseAdapter popupMouseAdapter = new PopupMouseAdapter(scrollPane);
		table.getTableHeader().addMouseListener(tableMouseAdapter);
		table.addMouseListener(popupMouseAdapter);

	}
	
	protected void setTableModel(MultiRowHeaderTableModel baseModel){
		overallModel.setBaseModel(baseModel);
		table.setModel(overallModel);
		table.setFormat(overallModel);	
		defaultRowHeight = table.getRowHeight();
		table.getColumnModel().getColumn(0).setPreferredWidth(FIRST_COLUMN_ROW_HEADER_WIDTH);
	    table.revalidate();
	}

	protected void createTable(MultiRowHeaderTableModel baseModel) {
		overallModel = new OverallTableModel(baseModel);
		//final String headerName =overallModel.getColumnName(2).trim().toLowerCase();
		table = new RowHeaderTable(overallModel){
	    final String column1 =overallModel.getColumnName(1);
		 // This table displays a tool tip text based on the string
	    // representation of the corresponding name column
	        public Component prepareRenderer(TableCellRenderer renderer,
	                                         int rowIndex, int vColIndex) {
	            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
	            if ( column1.trim().equalsIgnoreCase("select")){
	            	if (c instanceof JComponent && vColIndex == 1) {
	            		JComponent jc = (JComponent)c;
	            		jc.setToolTipText("Use checkbox to select this row");
	            		jc.getAccessibleContext().setAccessibleDescription("Use checkbox to select this row");
	            	}
	            	if (c instanceof JComponent && vColIndex>2 ) {
	            		JComponent jc = (JComponent)c;
	            		jc.setToolTipText(getValueAt(rowIndex, 2) != null? getValueAt(rowIndex, 2).toString():"");
	            		jc.getAccessibleContext().setAccessibleDescription("");
	            	}
	            	return c;
	            }
				if (c instanceof JComponent && vColIndex>1 ) {
					JComponent jc = (JComponent)c;
					if (jc != null)
					{
						Object obj = getValueAt(rowIndex, 1);
						if (obj != null) {
						   jc.setToolTipText(getValueAt(rowIndex, 1).toString());
						   jc.getAccessibleContext().setAccessibleDescription("");
						}
					}
				}
				return c;
	        }
	    };

		overallModel.addTableModelListener(this);
		updateStatusLabel();

	} // createTable()

	/**
	 * Return the number of columns in the top most model
	 * 
	 * @return int
	 */
	public int getColumnCount() {
		return overallModel.getColumnCount();
	} // getColumnCount()

	/**
	 * Return the column header for the requested column.
	 * 
	 * @return String that is a list of the headers on *one* column.
	 */
	public String[] getColumnHeader(int col) {
		return overallModel.getColumnHeaders(col);
	} // getColumnHeader()

	/**
	 * Return the column row headers.
	 * 
	 * @return String[]
	 */
	public String[] getColumnRowHeaders() {
		return overallModel.getColumnRowHeaders();
	} // getColumnHeader()

	/**
	 * Return the number of rows in the top most model
	 * 
	 * @return int
	 */
	public int getRowCount() {
		return overallModel.getRowCount();
	} // getRowCount()

	/**
	 * WARNING:When you try to set the data values it should be done through the overallTable mode Return the table
	 * model.
	 * 
	 * @return TableModel
	 */
	public OverallTableModel getOverallTableModel() {
		return overallModel;
	} // getTableModel()

	/**
	 * Return the formatted Object at the given row and column.
	 * 
	 * @return String that is the formatted
	 */
	public String getFormattedValueAt(int row, int col) {
		TableColumn column = table.getColumnModel().getColumn(col);
		Format format = ((FormattedCellRenderer) column.getCellRenderer()).getFormat();
		return format.format(overallModel.getValueAt(row, col));
	} // getValueAt()

	public JTable getTable() {
		return table;
	}

	/**
	 * Reset the columns to the unaltered data.
	 */
	public void reset() {
		overallModel.reset();
		TableColumnModel model = table.getColumnModel();
		for (int i = 1; i < model.getColumnCount(); i++) {
			Class colClass = overallModel.getColumnClass(i);
			TableColumn column = model.getColumn(i);
			if (!colClass.equals(Boolean.class)) {
				column.setCellRenderer(FormattedCellRenderer.getDefaultFormattedCellRenderer(colClass));
			}
			overallModel.setColumnFormatInfo(table.getColumnName(i), new ColumnFormatInfo(column));
		}
		table.setRowHeight(defaultRowHeight);
		table.getColumnModel().getColumn(0).setPreferredWidth(FIRST_COLUMN_ROW_HEADER_WIDTH);
	    table.revalidate();	    
	} // reset()

	/**
	 * Show the GUI for formatting the column text and update the selected column. Also reset the row hieghts in the
	 * table to match any new font heights.
	 */
	public void showColumnFormatGUI() {
		int selectedColumn = table.getColumnModel().getColumnIndexAtX(currentX);
		// Don't format the row header column.
		if (selectedColumn > 0) {
			Class colClass = getColumnClass(selectedColumn);
			if (colClass.equals(Boolean.class)) {
				DefaultUserInteractor.get()
						.notify(this, "Error", "You cannot format this column", UserInteractor.ERROR);
				return;
			}
			// Get the selected TableColumn and extract it's formatting information.
			TableColumnModel columnModel = table.getColumnModel();
			TableColumn column = columnModel.getColumn(selectedColumn);
			ColumnFormatInfo singleFormatInfo = new ColumnFormatInfo(column);
			ColumnFormatGUI formatGUI = new ColumnFormatGUI((JFrame) parent, table.getColumnName(selectedColumn),
					singleFormatInfo);
			formatGUI.setLocationRelativeTo(this);
			formatGUI.setVisible(true);
			// if the user has hit Cancel then do nothing
			int result = formatGUI.getResult();
			if (result == ColumnFormatGUI.CANCEL_RESULT) {
				return;
			}
			// Get the edited formatting object from the user.
			try {
				singleFormatInfo = formatGUI.getColumnFormatInfo();
				// Set the new format values in the TableColumn.
				column.setPreferredWidth(singleFormatInfo.width);
				FormattedCellRenderer formattedRenderer = new FormattedCellRenderer(singleFormatInfo.getFormat(),
						singleFormatInfo.alignment);
				formattedRenderer.setFont(singleFormatInfo.font);
				formattedRenderer.setForeground(singleFormatInfo.foreground);
				formattedRenderer.setBackground(singleFormatInfo.background);
				// Now set the new renderer to be used in the column.
				column.setCellRenderer(formattedRenderer);
				// update the format information in OverallTableModel
				overallModel.setColumnFormatInfo(table.getColumnName(selectedColumn), singleFormatInfo);
				// Check the size of all of the fonts. Set the row height to be the
				// height of the largest font in the table.
				int numCols = columnModel.getColumnCount();
				int largestRowHeight = 0;
				FontMetrics fm = null;
				// Remember not to check the row header column. It's fixed and doesn't
				// use a FormattedCellRenderer.
				TableCellRenderer rend = null;
				for (int c = 1; c < numCols; c++) {
					if (table.getColumnClass(c).equals(Boolean.class)) {
						continue;
					}
					rend = columnModel.getColumn(c).getCellRenderer();
					formattedRenderer = (FormattedCellRenderer) rend;
					Font font = null;
					if (formattedRenderer != null && (font = formattedRenderer.getFont()) != null) {
						fm = formattedRenderer.getFontMetrics(font);
						int height = fm.getHeight() + 1;
						if (height > largestRowHeight) {
							largestRowHeight = height;
						}
					}
				} // for(c)
				// Only make a change if the current row height is not correct.
				// (too small or too large)
				if (table.getRowHeight() != largestRowHeight) {
					table.setRowHeight(largestRowHeight);
				}
				table.revalidate();
			} // try
			catch (Exception e) {
				DefaultUserInteractor.get().notifyOfException(this, "Unable to get formatting information", e,
						UserInteractor.ERROR);
			} // catch
		} // if (selectedColumn > 0)
	} // showColumnFormatGUI()

	/**
	 * Show the row filtering GUI and filter the table based on what is returned.
	 */
	public void showFilterRowGUI() {
		int numCols = overallModel.getColumnCount();
		String[] cols = new String[numCols - 1];
		Class[] classes = new Class[numCols - 1];
		// Format[] formats = new Format[numCols - 1];
		// HasFormatter renderer = null;
		// Note that we're getting colunmns 1 through n here.
		int c = 0;

		for (int i = 1; i < numCols; i++) {
			c = i - 1;
			cols[c] = table.getColumnName(i);
			classes[c] = table.getColumnClass(i);
			// renderer = (HasFormatter) tableColumn.getCellRenderer();
			// formats[c] = renderer.getFormat();
		} // for (i)

		FilterRowGUI filterGUI = new FilterRowGUI((JFrame) parent, cols, classes, overallModel.getFilterCriteria());
		filterGUI.setLocationRelativeTo(this);
		filterGUI.setVisible(true);

		if (filterGUI.getResult() == OptionDialog.OK_RESULT) {
			FilterCriteria fCriteria = filterGUI.getFilterCriteria();
			fCriteria.setTableModel(overallModel);
			overallModel.filterRows(fCriteria);
		}
		table.revalidate();
	} // showFilterGUI()

	/**
	 * Present a GUI that allows the user to select a subset of columns and allows them to format the selected columns.
	 * 
	 */

	public void showMultipleColumnFormatGUI() {
		TableColumnModel columnModel = table.getColumnModel();
		// here the columnformatinfo is being arbitrarily picked
		TableColumn column = columnModel.getColumn(1);
		// if there was an existing columnformatinfo use that..
		if (formatInfo == null) {
			formatInfo = new ColumnFormatInfo(column);
		}
		int numCols = overallModel.getDataColumnCount();

		String[][] columnHeaders = new String[numCols][];
		Format[] formats = new Format[numCols];
		for (int c = 0; c < numCols; c++) {
			columnHeaders[c] = overallModel.getColumnHeaders(c);
			column = columnModel.getColumn(c + 1);
			TableCellRenderer rend = column.getCellRenderer();
			if (rend instanceof HasFormatter) {
				HasFormatter hf = (HasFormatter) rend;
				// formatsList.add(hf.getFormat());
				formats[c] = hf.getFormat();
			} else {
				formats[c] = FormattedCellRenderer.nullFormatter;
			}
		} // for(c)
		ColumnFormatGUI formatGUI = null;
		if (selFormatColName == null) {
			formatGUI = new ColumnFormatGUI((JFrame) parent, columnHeaders, formats, formatInfo);
		} else {
			boolean[] selected = new boolean[numCols]; // deduting one for the first column
			for (int i = 0; i < selFormatColName.length; i++) {
				int index = overallModel.getColumnHeaderIndex(selFormatColName[i]);
				if (index != -1) {
					selected[--index] = true; // decucting one to discount the first col
				}// if
			}// for(i)
			formatGUI = new ColumnFormatGUI((JFrame) parent, columnHeaders, selected, formats, formatInfo);
		}// else

		formatGUI.setLocationRelativeTo(this);
		formatGUI.setVisible(true);
		// If the user pressed 'Cancel', then return without making any changes.
		if (formatGUI.getResult() != OptionDialog.OK_RESULT) {
			return;
		}

		try {
			// Get the formatting information and the columns to modify from the GUI.
			selFormatColName = formatGUI.getSelectedColumnNames();
			formatInfo = formatGUI.getColumnFormatInfo();
			boolean[] selectedColumns = formatGUI.getSelectedColumns();
			// Change the format for each column that was selected by the user.
			for (int c = 0; c < selectedColumns.length; c++) {
				if (selectedColumns[c]) {
					// Don't forget to add one to skip the row header.
					column = columnModel.getColumn(c + 1);
					if (table.getColumnClass(c + 1).equals(Boolean.class)) {
						continue;
					}
					// Set the new format values in the TableColumn.
					column.setPreferredWidth(formatInfo.width);
					FormattedCellRenderer formattedRenderer = (FormattedCellRenderer) column.getCellRenderer();

					formattedRenderer.setFont(formatInfo.font);
					formattedRenderer.setHorizontalAlignment(formatInfo.alignment);
					formattedRenderer.setForeground(formatInfo.foreground);
					formattedRenderer.setBackground(formatInfo.background);
					if (formatInfo.getFormat() != null) {
						formattedRenderer.setFormat(formatInfo.getFormat());
					} // if (formatInfo.format != null)

					// Now set the new renderer to be used in the column.
					column.setCellRenderer(formattedRenderer);
					overallModel.setColumnFormatInfo(table.getColumnName(c + 1), new ColumnFormatInfo(column));
				} // if (selectedColumns[c])
			} // for(c)
			// Check the size of all of the fonts. Set the row height to be the
			// height of the largest font in the table.
			numCols = columnModel.getColumnCount();
			int largestRowHeight = 0;
			FontMetrics fm = null;
			// Remember not to check the row header column. It's fixed and doesn't
			// use a FormattedCellRenderer.
			TableCellRenderer rend = null;
			for (int c = 1; c < numCols; c++) {
				if (table.getColumnClass(c).equals(Boolean.class)) {
					continue;
				}
				rend = columnModel.getColumn(c).getCellRenderer();
				FormattedCellRenderer formattedRenderer = (FormattedCellRenderer) rend;
				// if(formattedRenderer == null)
				// {
				// continue;
				// }
				Font font = formattedRenderer.getFont();
				if (font != null) {
					fm = formattedRenderer.getFontMetrics(font);
					int height = fm.getHeight() + 1;
					if (height > largestRowHeight) {
						largestRowHeight = height;
					}
				}
			} // for(c)
			// Only make a change if the current row height is not correct.
			// (too small or too large)
			if (table.getRowHeight() != largestRowHeight) {
				table.setRowHeight(largestRowHeight);
			}
			formatGUI.dispose();
			table.revalidate();
		} // try
		catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Unable to get formatting information", e,
					UserInteractor.ERROR);
		} // catch
	} // showMultipleColumnFormatGUI()

	/**
	 * Show the show/hide columns GUI. Filter the table based on the user's selections once the dialog closes.
	 */
	public void showFilterColumnsGUI() {
		SelectColumnsGUI filterGUI = null;
		FilterCriteria filterCriteria = overallModel.getFilterCriteria();
		// If we already have a filter criteria, then use it to build the GUI.
		if (filterCriteria != null) {
			filterGUI = new SelectColumnsGUI((JFrame) parent, filterCriteria, "Show/Hide Columns", "Show", "Hide");
		} // Otherwise, pass in the list of all columns names.
		else {
			int numcols = overallModel.getBaseColumnCount();
			boolean[] selected = new boolean[numcols];
			Arrays.fill(selected, true);

			filterCriteria = new FilterCriteria(overallModel.getBaseColumnNames(),
			/* allColumnFormats, */selected);
			filterCriteria.setTableModel(overallModel);
			filterGUI = new SelectColumnsGUI((JFrame) parent, filterCriteria, "Show/Hide Columns", "Show", "Hide");
		} // else
		// Show the filteer column GUI.
		filterGUI.setLocationRelativeTo(this);
		filterGUI.setVisible(true);

		// Apply the filter if the user pressed OK.
		if (filterGUI.getResult() == OptionDialog.OK_RESULT) {
			FilterCriteria fCriteria = filterGUI.getFilterCriteria();
			fCriteria.setTableModel(overallModel);
			overallModel.filterColumns(fCriteria);
			updateFormat();
		}
	} // showFilterColumnsGUI()

	/**
	 * Show the sorting GUI.
	 */
	public void showSortGUI() {
		String[] cols = new String[overallModel.getColumnCount() - 1];

		for (int c = 0; c < cols.length; c++) {
			cols[c] = overallModel.getColumnName(c + 1);
		} // for(c)
		SortGUI sortGUI = new SortGUI((JFrame) parent, cols, overallModel.getSortCriteria());
		sortGUI.setLocationRelativeTo(this);
		sortGUI.setVisible(true);
		SortCriteria sortCriteria = null;
		if (sortGUI.getResult() == OptionDialog.OK_RESULT) {
			sortCriteria = sortGUI.getSortCriteria();
			/*
			 * // First we have to make sure that the columns we are sorting by are // visible. String[] columnNames =
			 * sortCriteria.getColumnNames(); int[] columnIndices = new int[columnNames.length]; for (int i = 0; i <
			 * columnIndices.length; i++) { Integer intObj = } if (filterCriteria != null) { boolean[] colsToShow =
			 * filterCriteria.getColumntoShow(); for (int i = 0; i < columnIndices.length; i++) {
			 * if(colsToShow[columnIndices[i]] == false) { DefaultUserInteractor.get().notify("Invalid Column for
			 * Sorting", "You cannot apply a sort to a column that is not being displayed.\n" + "Please show the column
			 * and then sort on it.", UserInteractor.ERROR); return; } } // for(i) } // if (currentFilterCriteria !=
			 * null)
			 */
			overallModel.sort(sortCriteria);
			table.revalidate();
		} // if (sortGUI.getResult() == OptionDialog.OK_RESULT)

	} // showSortGUI()

	/**
	 * Find the currently selected column and sort it. This is what is called when the user clicks on the header of the
	 * table.
	 * 
	 * @param ascending
	 *            boolean that is true if we should sort in ascending order.
	 */
	private void sortAtCurrentX(boolean ascending) {
		int selectedColumn = table.getColumnModel().getColumnIndexAtX(currentX);
		String[] columnNames = { table.getColumnName(selectedColumn) };
		if (selectedColumn > 0) {
			boolean[] caseSensitive = { true };
			// Do not sort case sensitively for Strings only.
			if (table.getColumnClass(selectedColumn).equals(String.class)) {
				caseSensitive[0] = false;
			}
			SortCriteria sortCriteria = new SortCriteria(columnNames, new boolean[] { ascending }, caseSensitive);
			sort(sortCriteria);
			table.revalidate();
		}
	} // sortAtCurrentX()

	/**
	 * Sort the given columns.
	 * 
	 * @param sortCriteria
	 *            SortCriteria to use to sort the data.
	 */
	public void sort(SortCriteria sortCriteria) {
		if (sortCriteria!=null){
			
			overallModel.sort(sortCriteria);
		}
	} // sort()

	/**
	 * Update the status label when the underlying data changes.
	 * 
	 * @param e
	 */
	
	public void filter(FilterCriteria filterCriteria) {
		if ( filterCriteria != null){
			overallModel.filterRows(filterCriteria);
//			overallModel.filterColumns(filterCriteria);
//			updateFormat();
		}
	} // filter()

	public void tableChanged(TableModelEvent e) {
		updateStatusLabel();
		update();
	} // tableChanged()

	protected void updateFormat() {
//		TableColumnModel model = table.getColumnModel();
//		int count = model.getColumnCount();
//		for (int i = 1; i < count; i++) {
//			TableColumn tcol = model.getColumn(i);
//			String colName = table.getColumnName(i);
//			Class colClass = table.getColumnClass(i);
//			ColumnFormatInfo info = overallModel.getColumnFormatInfo(colName);
//			if (info != null && !colClass.equals(Boolean.class)) {
//				FormattedCellRenderer renderer = new FormattedCellRenderer(info.getFormat(), info.alignment);
//				tcol.setPreferredWidth(info.width);
//				renderer.setFont(info.font);
//				renderer.setForeground(info.foreground);
//				renderer.setBackground(info.background);
//				tcol.setCellRenderer(renderer);
//			}
//		}
		table.setModel(overallModel);
		table.setFormat(overallModel);
		table.setRowHeight(defaultRowHeight);
		table.getColumnModel().getColumn(0).setPreferredWidth(FIRST_COLUMN_ROW_HEADER_WIDTH);
		table.revalidate();
	}

	/**
	 * After an operation that changes the number of rows or columns, update the number of rows and columns in the
	 * status label.
	 */
	public void updateStatusLabel() {
// Parthee's version:
//		String info = overallModel.getRowCount() + ROWS_STR + overallModel.getDataColumnCount() + COLUMNS_STR
//						+ "[ " + overallModel.filterSortInfoString() + " ]";
	    // Alison's version:
		String info = " " + overallModel.getRowCount() + ROWS_STR + overallModel.getDataColumnCount() +
                        COLUMNS_STR + " [" + overallModel.filterSortInfoString() + "]";
		statusLabel.setText(info);
		statusLabel.setToolTipText(info);
		statusLabel.getAccessibleContext().setAccessibleName("Table status " + info);
	} // updateStatusLabel()

	/**
	 * This will apply the filter criteria to the TableModel WARNING: Please note that we are not checking whether
	 * filterCriter has the same column names as overall table model
	 * 
	 * @param FilterCriteria
	 *            filterCriteria
	 * 
	 */
	public void filterRows(FilterCriteria filterCriteria) {
		if (filterCriteria != null) {
			filterCriteria.setTableModel(overallModel);
			this.overallModel.filterRows(filterCriteria);
		}
	}

	public void setConfiguration (AnalysisConfiguration config) {
		this.aconfig = config;
	}
	
	public void showSaveConfigGUI() {
		try {
			SaveConfigModel saveConfigModel = new SaveConfigModel(aconfig);
			SaveConfigView.showGUI((JFrame) this.parent, saveConfigModel);
		} catch (Exception e) {
			new GUIUserInteractor().notify(this, "Saving Configuration?", e.getMessage(), UserInteractor.NOTE);
		}
	}

	public void showLoadConfigGUI(File file, boolean binaryFormat, ConfigFileHistory configFileshistory,
			CurrentDirectory currentDirectory) {
		try {
			AnalysisConfiguration config = new AnalysisConfiguration(overallModel);
			config.loadConfiguration(file, false, binaryFormat, parent);
			LoadConfigurationGUI gui = new LoadConfigurationGUI(config, aconfig, table, (JFrame) this.parent,
					currentDirectory);
			gui.setVisible(true);
			configFileshistory.addToHistory(file.getAbsolutePath(), binaryFormat);
		} catch (Exception e) {
			new GUIUserInteractor().notify(this, "Load Configuration", e.getMessage(), UserInteractor.ERROR);
		}
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	public void setParentComponent(Component parent) {
		this.parent = parent;
	}

	public void update() {
		if (parent != null && parent instanceof HasChangedListener) {
			if (!hasChanged) {
				((HasChangedListener) parent).update();
			}
			hasChanged = true;
		}
	}

	// ////////////////// CLASSES ////////////////////////////

	/**
	 * Ascending Sort Action.
	 */
	class SortAscendingAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public SortAscendingAction(SortFilterTablePanel parent) {
			super("Sort Ascending", sortIcon);
			this.parent = parent;
		} // SortAscendingAction()

		public void actionPerformed(ActionEvent e) {
			parent.sortAtCurrentX(true);
		} // actionPerformed()
	} // class SortAscendingAction

	/**
	 * Descending Sort Action.
	 */
	class SortDescendingAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public SortDescendingAction(SortFilterTablePanel parent) {
			super("Sort Descending", sortIcon);
			this.parent = parent;
		} // SortDescendingAction()

		public void actionPerformed(ActionEvent e) {
			parent.sortAtCurrentX(false);
		} // actionPerformed()
	} // class SortDescendingAction

	/**
	 * Sort By Multiple Columns Action.
	 */
	protected class SortMultipleAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public SortMultipleAction(SortFilterTablePanel parent) {
			super("Sort By Columns...", sortIcon);
			this.parent = parent;
		} // SortMultipleAction()

		public void actionPerformed(ActionEvent e) {
			parent.showSortGUI();
		} // actionPerformed()
	} // class SortMultipleAction

	/**
	 * Filter the table based on the given criteria
	 */
	protected class FilterAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public FilterAction(SortFilterTablePanel parent) {
			super("Filter...", filterIcon);
			this.parent = parent;
		} // FilterAction()

		public void actionPerformed(ActionEvent e) {
			parent.showFilterRowGUI();
		} // actionPerformed()
	} // class FilterAction

	/**
	 * Action for formatting multiple columns.
	 */
	protected class MultipleFormatAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public MultipleFormatAction(SortFilterTablePanel parent) {
			super("Format columns...", formatIcon);
			this.parent = parent;
		} // SingleFormatAction()

		public void actionPerformed(ActionEvent e) {
			parent.showMultipleColumnFormatGUI();
		} // actionPerformed()
	} // class MultipleFormatAction

	protected class AggregateRowsAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public AggregateRowsAction(SortFilterTablePanel parent) {
			super("Aggregate Rows");
			this.parent = parent;
		} // FilterAction()

		public void actionPerformed(ActionEvent e) {
			parent.aggregateRows();
		}
	}

	/**
	 * An action that adds 2 columns to the table.
	 */

	protected class AggregateColumnsAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public AggregateColumnsAction(SortFilterTablePanel parent) {
			super("Aggregate Columns");
			this.parent = parent;
		} // FilterAction()

		public void actionPerformed(ActionEvent e) {
			parent.aggregateColumns();
		} // actionPerformed()
	} // class AggregateColumnsAction

	/**
	 * An action to reset the data in the table to the original data.
	 */
	protected class ResetAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public ResetAction(SortFilterTablePanel parent) {
			super("Reset", resetIcon);
			this.parent = parent;
		} // FilterAction()

		public void actionPerformed(ActionEvent e) {
			parent.reset();
		} // actionPerformed()
	} // class ResetAction

	/**
	 * Action for showing/hiding columns.
	 */
	protected class ShowHideColumnsAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public ShowHideColumnsAction(SortFilterTablePanel parent) {
			super("Show/Hide columns...", showHideIcon);
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			parent.showFilterColumnsGUI();
		}
	}

	/**
	 * Action for formatting one column.
	 */
	protected class SingleFormatAction extends AbstractAction {
		SortFilterTablePanel parent = null;

		public SingleFormatAction(SortFilterTablePanel parent) {
			super("Format column...", formatIcon);
			this.parent = parent;
		} // SingleFormatAction()

		public void actionPerformed(ActionEvent e) {
			parent.showColumnFormatGUI();
		} // actionPerformed()
	} // class SingleFormatAction

	/**
	 * Mouse Listener to bring up the popup and sort columns.
	 */
	protected class PopupMouseAdapter extends MouseAdapter {
		private JScrollPane scrollPane = null;

		public PopupMouseAdapter(JScrollPane scrollPane) {
			this.scrollPane = scrollPane;
		}

		public void mousePressed(MouseEvent e) {
			Point point = e.getPoint();
			SortFilterTablePanel.this.currentX = point.x;
			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
				popupMenu.show(SortFilterTablePanel.this, point.x - scrollPane.getViewport().getViewPosition().x,
						point.y - scrollPane.getViewport().getViewPosition().y);
			}
		}

	}

	protected class TableMouseAdapter extends MouseAdapter {
		boolean sortToggle = false;

		public TableMouseAdapter() {
			// Empty
		}

		public void mouseClicked(MouseEvent e) {
			Point point = e.getPoint();
			SortFilterTablePanel.this.currentX = point.x;
			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
				SortFilterTablePanel.this.sortAtCurrentX(sortToggle);
				sortToggle = !sortToggle;
			}
		}

	}
}
