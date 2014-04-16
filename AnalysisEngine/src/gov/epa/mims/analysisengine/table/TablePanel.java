package gov.epa.mims.analysisengine.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * <p>
 * Title:ATablePanel
 * </p>
 * <p>
 * Description: A panel to hold the SortFilterTablePanel and header and footer panel
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: TablePanel.java,v 1.8 2008/05/02 19:49:39 eyth Exp $
 */
public class TablePanel extends JPanel {

	/** header panell which displays the header information */
	private DescriptionPanel tableHeaderPanel;

	/** sort fileter table */
	protected SUSortFilterTablePanel tablePanel;

	/** header panell which displays the footer information */
	private DescriptionPanel footerPanel;

	/** name of the file opened in the tab pane (absolute file name) */
	private String fileName;

	private TablePanelModel tablePanelModel;

	private String tabName;

	private JTabbedPane maintTabbedPane;

	public TablePanel(Component parent, SpecialTableModel model, String fileName, String tabName, String fileType,
			JTabbedPane mainTabbedPane) {
		this.fileName = fileName;
		this.tabName = tabName;
		this.maintTabbedPane = mainTabbedPane;
		tablePanelModel = new TablePanelModel(fileName, fileType);
		String name = "";
		if (fileName != null) {
			name = new File(fileName).getName();
		}
		initialize(parent, name, model);
	}

	public String getFileName() {
		return fileName;
	}

	/** initialize the gui */
	private void initialize(Component parent, String name, SpecialTableModel model) {
		setLayout(new BorderLayout());
		// header panel
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		JPanel fileNamePanel = new javax.swing.JPanel();
		fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.X_AXIS));
		JLabel label = new javax.swing.JLabel("File Name");
		JTextField fileNameTextField = new javax.swing.JTextField();
		fileNameTextField.setPreferredSize(new java.awt.Dimension(300, 25));
		fileNamePanel.add(label);
		fileNamePanel.add(fileNameTextField);
		headerPanel.add(fileNamePanel);// ,BorderLayout.NORTH);
		headerPanel.add(Box.createVerticalStrut(10));
		tableHeaderPanel = new DescriptionPanel(model.getTableDataHeader(), "Header ", name);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		headerPanel.add(tableHeaderPanel, BorderLayout.SOUTH);
		add(headerPanel, BorderLayout.NORTH);

		// main tab panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());

		// footer panel
		String footMsg = model.getTableDataFooter();
		if (footMsg != null)
		{
			String ret = System.getProperty("line.separator");
			int retIndex = footMsg.indexOf(ret);
			// String index out of range exceptions were generated here - doesn't seem necessary 
			// to remove the line separators...
//			if (retIndex >= 0)
//				footMsg = ret.substring(0, retIndex) + ret.substring(retIndex + ret.length());
		}

		if (footMsg != null && !footMsg.trim().equals("")) {
			footerPanel = new DescriptionPanel(footMsg, "Footer ", name);
			footerPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		}

		tablePanel = new SUSortFilterTablePanel(parent, fileName, tabName, maintTabbedPane, model);
		fileNameTextField.setText(fileName);
		mainPanel.add(tablePanel, BorderLayout.CENTER);

		add(mainPanel, BorderLayout.CENTER);
		if (footerPanel != null)
			add(footerPanel, BorderLayout.SOUTH);
	}

	/** get the data in a tabbed pane */
	public TablePanelModel getTablePanelModel() {

		tablePanelModel.setTableDataHeader(tableHeaderPanel.getDescription());
		int noOfColumn = tablePanel.getColumnCount();

		// System.out.println("noOfColumn="+noOfColumn);
		int noOfRows = tablePanel.getRowCount();
		// System.out.println("noOfRows="+noOfRows);
		// noOfColumn starts from 1 since 0 is the first column which have row labels
		String[][] tempColumnHeaders = new String[noOfColumn - 1][];
		ArrayList tableData = new ArrayList();
		Class[] columntypes = new Class[noOfColumn];

		// column headers
		for (int j = 0; j < tempColumnHeaders.length; j++) {
			tempColumnHeaders[j] = tablePanel.getColumnHeader(j);
		}// for(j)

		for (int i = 1; i < noOfColumn; i++) {
			columntypes[i - 1] = tablePanel.getColumnClass(i);
		}
		// row data
		for (int i = 0; i < noOfRows; i++) {
			ArrayList rowData = new ArrayList();
			for (int j = 1; j < noOfColumn; j++) {
				rowData.add(tablePanel.getFormattedValueAt(i, j));
			}// for(j)
			tableData.add(rowData);
		}// for(i)
		// have to transpose it since in table model column header is arraged in column fashion
		tablePanelModel.setColumnHeaders(transposeArray(tempColumnHeaders));
		tablePanelModel.setTableData(tableData);
		if (footerPanel != null)
			tablePanelModel.setTableDataFooter(footerPanel.getDescription());
		tablePanelModel.setColumnTypes(columntypes);
		return tablePanelModel;
	}

	/**
	 * Transpose the given array.
	 * 
	 * @param original
	 *            String[][] that is the array to transpose.
	 * @return String[][] that isthe transpose of the array passed in or null if the original array was null
	 */
	private String[][] transposeArray(String[][] original) {
		if (original == null || original.length == 0) {
			return null;
		}

		String[][] retval = new String[original[0].length][original.length];
		for (int r = 0; r < original.length; r++) {
			for (int c = 0; c < original[0].length; c++) {
				retval[c][r] = original[r][c];
			} // for(c)
		} // for(r)

		return retval;
	}// transposeArrays()

}// ATabPanel

