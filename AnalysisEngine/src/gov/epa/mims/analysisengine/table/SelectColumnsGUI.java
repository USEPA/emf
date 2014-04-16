package gov.epa.mims.analysisengine.table;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import gov.epa.mims.analysisengine.gui.*;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;

/**
 * <p>
 * Description: A GUI that allows the user to select a set of columns and show or hide them.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: SelectColumnsGUI.java,v 1.4 2006/11/01 15:33:36 parthee Exp $
 */
public class SelectColumnsGUI extends OptionDialog {
	/** The panel for selecting columns. */
	protected ColumnSelectionPanel selectionPanel = null;

	/** The FilterCriteria that this window displays and edits. */
	protected FilterCriteria filterCriteria = null;

	/**
	 * Constructor.
	 * 
	 * @param filterCriteria
	 *            FilterCriteria to use to set up the GUI.
	 * @param title
	 *            String that is the title to place on the window.
	 * @param checkString
	 *            String to place on the "check" button.
	 * @param uncheckString
	 *            String to place on the "uncheck" button.
	 */
	public SelectColumnsGUI(JFrame parent, FilterCriteria filterCriteria, String title, String checkString,
			String uncheckString) {
		super(parent);
		this.filterCriteria = filterCriteria;
		initialize(checkString, uncheckString);
		setDataSource(filterCriteria, "");
		setModal(true);
		setTitle(title);
	} // SelectColumnsGUI()

	/**
	 * Return the list of all column names. This will have the same length as the boolean array returned by
	 * getSelectedColumns().
	 * 
	 * @return String[] with all column names.
	 */
	public String[] getAllColumnNames() {
		return filterCriteria.getAllColumnNames();
	} // getAllColumnNames()

	/**
	 * Return the FilterCriteria being edited by this GUI.
	 */
	public FilterCriteria getFilterCriteria() {
		return filterCriteria;
	} // getFilterCriteria()

	/**
	 * Return the list of all column names. This will have the same length as the String array returned by
	 * getAllColumnNames().
	 * 
	 * @return boolean[] with true for each selected column.
	 */
	public boolean[] getSelectedColumns() {
		return filterCriteria.getColumntoShow();
	} // getAllColumnNames()

	/**
	 * Build the GUI
	 * 
	 * @param checkString
	 *            String that is the text to place on the check button.
	 * @param uncheckString
	 *            String that is the text to place on unthe check button.
	 */
	protected void initialize(String checkString, String uncheckString) {
		String[] colNames = filterCriteria.getAllColumnNames();

		// We have to transpose this right now since we aren't using column
		// headers yet.
		String[][] transpose = new String[colNames.length][1];
		for (int i = 0; i < colNames.length; i++) {
			transpose[i][0] = colNames[i];
		} // for(i)

		selectionPanel = new ColumnSelectionPanel(transpose, filterCriteria.getColumntoShow(), checkString,
				uncheckString);
		selectionPanel.setBorder(BorderFactory.createEtchedBorder());
		selectionPanel.setRequestFocusEnabled(false);
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(selectionPanel, BorderLayout.CENTER);
		JPanel buttonPanel = getButtonPanel();
		buttonPanel.requestFocus();
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	} // initialize()

	/**
	 * Write this method to initialize the GUI from the value of the data object.
	 */
	protected void initGUIFromModel() {
		//Empty
	} // initGUIFromModel()

	/**
	 * Write this method to store the info from the GUI in the data object.
	 * 
	 * @throws Exception
	 */
	protected void saveGUIValuesToModel() throws Exception {
		filterCriteria.setColumnsToShow(filterCriteria.getAllColumnNames(), selectionPanel.getCheckedColumns());
	} // saveGUIValuesToModel()
} // class SelectColumnsGUI

