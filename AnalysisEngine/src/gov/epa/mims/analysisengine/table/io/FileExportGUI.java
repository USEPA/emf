package gov.epa.mims.analysisengine.table.io;

import gov.epa.mims.analysisengine.UserPreferences;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.CurrentDirectory;
import gov.epa.mims.analysisengine.table.TableApp;
import gov.epa.mims.analysisengine.table.TablePanelModel;
import gov.epa.mims.analysisengine.table.TextDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * FileExportGUI.java GUIInterface to the exportGUI Created on April 5, 2004, 2:46 PM
 * 
 * @author Parthee Partheepan, CEP, UNC CHAPEL HILL.
 * @version $Id: FileExportGUI.java,v 1.3 2006/12/21 16:29:54 parthee Exp $
 */
public class FileExportGUI extends JDialog {
	/** storing the data for each tab */
	// private TablePanelModel [] allTablePanelModels;
	/** store the current tab that is selected */
	private int selectedTabIndex = -1;

	/** store the tab names */
	private String[] tabNames;

	/** JTable to list the files */
	private JTable table;

	/** model for the table */
	private DefaultTableModel tableModel;

	/** to denote the first column */
	private final int FIRST_COLUMN = 0;

	/* to denote the second column */
	private final int SECOND_COLUMN = 1;

	/** csv file extension */
	private final String CSV = "csv";

	/** txt file extension */
	private final String TXT = "txt";

	/** a radio button specify whether it's TAB file export */
	private JRadioButton tabRB;

	/** a radio button specify whether it's csv file export */
	private JRadioButton csvRB;

	/** a radio button specify custom delimiter */
	private JRadioButton customDelimiterRB;

	/** a radio button specify whether it's ARFF file export */
	private JRadioButton arffRB;

	/** to specify a custom delimiter */
	private JTextField customDelimitField;

	/** txt field to specify the file names */
	/** to specify the dir */
	private JTextField dirTextField;

	/** prefix of the file name */
	private StringValuePanel prefixValuePanel;

	/** file name without ext */
	private StringValuePanel fileNameValuePanel;

	/** suffix of the file name */
	private StringValuePanel suffixValuePanel;

	/** extension of the file name */
	private StringValuePanel extValuePanel;

	/** a check box to specify whether to overwrite the file */
	private JCheckBox overwriteCheckBox;

	/**
	 * a boolean value to close the dialog
	 */
	private boolean closeDialog = true;

	/**
	 * TableApp
	 */
	private TableApp tableApp;

	private CurrentDirectory currentDirectory;

	/**
	 * Creates a new instance of FileExportGUI
	 * 
	 * @param currentDirectory
	 */
	public FileExportGUI(TableApp parent, String[] tabNames, int selectedTabIndex, CurrentDirectory currentDirectory) {
		super(parent);
		tableApp = parent;
		this.selectedTabIndex = selectedTabIndex;
		this.tabNames = tabNames;

		if (tabNames == null) {
			new GUIUserInteractor().notify(this, "File Export", "No Tab is Open to " + "Export", UserInteractor.NOTE);
			return;
		}
		this.currentDirectory = currentDirectory;
		setupTableModel(tabNames);
		initialize();
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
		setVisible(true);
	}

	/**
	 * Helper method to setup a default table model
	 */
	private void setupTableModel(String[] tabNames) {
		Vector columnNames = new Vector();
		columnNames.add("Tab Names");
		columnNames.add("Export?");
		Vector tableData = new Vector();
		if (tabNames != null) {
			for (int i = 0; i < tabNames.length; i++) {
				Vector rowData = new Vector();
				rowData.add(tabNames[i]);
				if (i != selectedTabIndex) {
					rowData.add(new Boolean(false));
				}// if(i!= selectedTabIndex)
				else {
					rowData.add(new Boolean(true));
				}// else
				tableData.add(rowData);
			}// for(i)
		}// if()

		tableModel = new DefaultTableModel(tableData, columnNames) {
			public boolean isCellEditable(int row, int column) {
				if (column == SECOND_COLUMN) {
					return true;
				}
				return false;
			}

			public Class getColumnClass(int colNo) {
				return table.getValueAt(FIRST_COLUMN, colNo).getClass();
			}

			/** to remove row */
			public void removeRow(int row) {
				if (row < 0 || row >= dataVector.size())
					return;

				dataVector.remove(row);
				fireTableRowsDeleted(row, row);
			}
		};
	}// setupTableModel

	/** helper method to initialize the gui */
	private void initialize() {
		this.setModal(true);
		this.setTitle("Export Files");
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		table = new JTable();
		table.setModel(tableModel);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(FIRST_COLUMN).setPreferredWidth(225);
		columnModel.getColumn(SECOND_COLUMN).setPreferredWidth(75);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(350, 200));
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		JPanel exportButtonPanel = new JPanel();
		JButton exportAllButton = new JButton("Select All");
		exportAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int noOfRows = table.getRowCount();
				for (int i = 0; i < noOfRows; i++) {
					tableModel.setValueAt(new Boolean("true"), i, SECOND_COLUMN);
				}// for(i)
			}
		});
		exportButtonPanel.add(exportAllButton);
		tablePanel.add(exportButtonPanel, BorderLayout.SOUTH);

		mainPanel.add(tablePanel, BorderLayout.CENTER);

		JPanel formatAndNamePanel = new JPanel();
		formatAndNamePanel.setLayout(new BoxLayout(formatAndNamePanel, BoxLayout.Y_AXIS));
		formatAndNamePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel formatPanel = new JPanel();
		JRadioButton originalFormatRB = new JRadioButton("Original Format", false);
		originalFormatRB.setEnabled(false);
		csvRB = new JRadioButton("CSV", true);
		csvRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extValuePanel.setValue(CSV);
				extValuePanel.setEnabled(false);
				customDelimitField.setEnabled(false);
			}
		});
		tabRB = new JRadioButton("TAB", false);
		tabRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				customDelimitField.setEnabled(false);
				extValuePanel.setEnabled(true);
			}
		});

		arffRB = new JRadioButton("ARFF", false);
		arffRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				customDelimitField.setEnabled(false);
				extValuePanel.setValue("arff");
				extValuePanel.setEnabled(false);
			}
		});

		customDelimiterRB = new JRadioButton("Custom Delimiter", false);
		customDelimiterRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extValuePanel.setValue(TXT);
				extValuePanel.setEnabled(true);
				customDelimitField.setEnabled(true);
				customDelimitField.setText(";");
			}
		});
		ButtonGroup formatBG = new ButtonGroup();
		formatBG.add(originalFormatRB);
		formatBG.add(tabRB);
		formatBG.add(csvRB);
		formatBG.add(customDelimiterRB);
		formatBG.add(arffRB);
		// use a document so that the user is able to only enter one character
		PlainDocument plainDoc = new PlainDocument() {
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (offs != 0)
					throw new BadLocationException("Can only enter one character", offs);
				else if (str.length() > 1)
					throw new BadLocationException("Can only enter one character", str.length());
				else
					super.insertString(offs, str, a);
			}
		};
		customDelimitField = new JTextField(plainDoc, ";", 3);
		// set the textfield to be center justified
		customDelimitField.setHorizontalAlignment(JTextField.CENTER);
		formatPanel.add(csvRB);
		formatPanel.add(tabRB);
		formatPanel.add(customDelimiterRB);
		formatPanel.add(customDelimitField);
		formatPanel.add(Box.createHorizontalStrut(15));
		formatPanel.add(arffRB);
		// formatPanel.add(originalFormatRB);
		formatAndNamePanel.add(Box.createVerticalStrut(5));
		formatAndNamePanel.add(formatPanel);
		// formatAndNamePanel.add(Box.createVerticalStrut(3));

		// fileName specifier and dir specifier
		// JPanel fileNamePanel = new JPanel(new BorderLayout());
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.Y_AXIS));
		fileNamePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "File Name")));

		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
		JLabel dirLabel = new JLabel("Output Dir. ");
		dirTextField = new JTextField();
		File currentDir = FileImportGUI.getInitialCurrentDir();
		if (currentDir != null) {
			dirTextField.setText(currentDir.getAbsolutePath());
		}
		// dirTextField.setPreferredSize(new Dimension(100, 25));
		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = FileImportGUI.getDirFromUser(currentDirectory);
				if (dir != null) {
					dirTextField.setText(dir.getAbsolutePath());
				}
			}
		});
		dirPanel.add(dirLabel);
		dirPanel.add(dirTextField);
		dirPanel.add(browseButton);
		fileNamePanel.add(dirPanel);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		prefixValuePanel = new StringValuePanel("Prefix", true);
		prefixValuePanel.setFieldColumnSize(5);
		fileNameValuePanel = new StringValuePanel("File Name", true);
		fileNameValuePanel.setFieldColumnSize(20);
		extValuePanel = new StringValuePanel("Ext", true);
		extValuePanel.setFieldColumnSize(3);
		if (table.getRowCount() > 0) {
			fileNameValuePanel.setValue(tabNames[selectedTabIndex]);
		}// if()
		suffixValuePanel = new StringValuePanel("Suffix", true);
		suffixValuePanel.setFieldColumnSize(5);
		namePanel.add(prefixValuePanel);
		namePanel.add(fileNameValuePanel);
		namePanel.add(suffixValuePanel);
		namePanel.add(extValuePanel);

		fileNamePanel.add(Box.createVerticalStrut(3));
		fileNamePanel.add(namePanel);
		fileNamePanel.add(Box.createVerticalStrut(3));
		JPanel exampleNamePanel = new JPanel();
		// exampleNamePanel.setLayout(new BoxLayout(exampleNamePanel,BoxLayout.X_AXIS));
		JLabel exampleLabel = new JLabel("Output fileName=");
		JLabel egNameLabel = new JLabel();
		egNameLabel.setText("OutputDir" + File.separator + "PrefixFilenameSuffix.Ext");
		JButton previewButton = new JButton("Preview");
		if (table.getRowCount() == 0) {
			previewButton.setEnabled(false);
		}
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String prefix = prefixValuePanel.getValue();
				String name = fileNameValuePanel.getValue();
				if (name == null) {
					name = "";
				}
				String suffix = suffixValuePanel.getValue();
				String ext = extValuePanel.getValue();
				if (prefix == null)
					prefix = "";
				if (suffix == null)
					suffix = "";
				if (ext == null)
					ext = "";
				String dir = dirTextField.getText();
				String seperator = File.separator;
				if (!dir.endsWith(seperator)) {
					dir += seperator;
				}
				String completeFileName = dir + prefix + name + suffix + "." + ext;
				JOptionPane.showMessageDialog(FileExportGUI.this, completeFileName, "Example File Name",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		exampleNamePanel.add(exampleLabel);
		exampleNamePanel.add(egNameLabel);
		exampleNamePanel.add(Box.createHorizontalStrut(10));
		exampleNamePanel.add(previewButton);
		// fileNamePanel.add(exampleNamePanel,BorderLayout.SOUTH);
		fileNamePanel.add(exampleNamePanel);
		formatAndNamePanel.add(fileNamePanel);

		JPanel configureButtonPanel = new JPanel();
		overwriteCheckBox = new JCheckBox("Overwrite the file if it exists?", false);
		configureButtonPanel.add(overwriteCheckBox);
		formatAndNamePanel.add(configureButtonPanel);

		mainPanel.add(formatAndNamePanel, BorderLayout.SOUTH);
		container.add(mainPanel);
		container.add(createExportCancelPanel(), BorderLayout.SOUTH);

		if (csvRB.isSelected() == true) // initialization
		{
			customDelimitField.setEnabled(false);
			extValuePanel.setValue(CSV);
			extValuePanel.setEnabled(false);
		}
		if (table.getRowCount() == 0) {
			previewButton.setEnabled(false);
		}
		// buttonPanel;
	}

	/** create a buttonPanel */
	private JPanel createExportCancelPanel() {
		JPanel buttonPanel = new JPanel();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog = true;
				exportFiles();
				if (closeDialog) {
					dispose();
				}
			}
		});
		buttonPanel.add(exportButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}

	/**
	 * a helper method to export files
	 */
	private void exportFiles() {
		String completeFileName;
		String nameWOExt;
		Vector selectedIndices = getSelectedFilesIndices();
		String delimiter;

		ArrayList statusLog = new ArrayList();
		if (csvRB.isSelected()) {
			delimiter = ",";
		} else if (tabRB.isSelected()) {
			delimiter = "\t";
		} else if (arffRB.isSelected()) {
			delimiter = ",";
		} else {
			delimiter = customDelimitField.getText();
		}

		if (selectedIndices.size() >= 1) {
			if (selectedIndices.size() == 1) {
				nameWOExt = fileNameValuePanel.getValue();
				completeFileName = createCompleteFileName(nameWOExt);
				if (!checkForOverwrite(completeFileName)) {
					return;
				}
				int index = ((Integer) selectedIndices.get(0)).intValue();
				TablePanelModel tablePanelModel = tableApp.getTablePanelModel(tabNames[index]);
				if (arffRB.isSelected())
					exportARFF(completeFileName, tablePanelModel);
				else
					exportFile(completeFileName, delimiter, tablePanelModel);
				statusLog.add(completeFileName);

			}// if(selectedIndices.size()==1)
			else {
				for (int i = 0; i < selectedIndices.size(); i++) {
					int index = ((Integer) selectedIndices.get(i)).intValue();
					nameWOExt = tabNames[index];
					completeFileName = createCompleteFileName(nameWOExt);
					if (!checkForOverwrite(completeFileName)) {
						return;
					}// if(!checkForOverwrite(completeFileName))
					statusLog.add(completeFileName);
					TablePanelModel tablePanelModel = tableApp.getTablePanelModel(tabNames[index]);
					if (arffRB.isSelected())
						exportARFF(completeFileName, tablePanelModel);
					else
						exportFile(completeFileName, delimiter, tablePanelModel);
				}//
			}// else
			try {
				TextDialog dialog = new TextDialog(this, "Export File Status", "", false);
				dialog.setTextFromList("The following files were exported:", statusLog);
				dialog.setVisible(true);
			} catch (Exception e) {
				DefaultUserInteractor.get().notifyOfException(this, "Fail to show export status dialog", e,
						UserInteractor.ERROR);
			}
		}// if(selectedIndices.size()>=1)
	}// exportFiles()

	/**
	 * helper method to check whether file is exist and give a message if overwrite check box is unchecked
	 */
	private boolean checkForOverwrite(String completerFileName) {
		File file = new File(completerFileName);
		if (file.exists() && !overwriteCheckBox.isSelected()) {
			DefaultUserInteractor.get().notify(this, "WARNING",
					"The file " + completerFileName + " is already exists please enter a different name. ",
					UserInteractor.WARNING);
			closeDialog = false;
			return false;
		}
		return true;
	}

	/**
	 * helper method to return vector of selected indices from the table
	 */
	private Vector getSelectedFilesIndices() {
		int noOfRows = table.getRowCount();
		Vector selectedIndices = new Vector();
		for (int i = 0; i < noOfRows; i++) {
			Boolean value = (Boolean) table.getValueAt(i, SECOND_COLUMN);
			if (value.booleanValue() == true) {
				selectedIndices.add(new Integer(i));
			}// if( value.booleanValue() == true)
		}// for(i)
		return selectedIndices;
	}// getSelectedFilesIndices()

	/**
	 * helper method to prepare the file name
	 */
	private String createCompleteFileName(String fileNameWithoutExt) {
		String dir = dirTextField.getText();
		String seperator = File.separator;
		if (!dir.endsWith(seperator)) {
			dir = dir + seperator;
		}// if
		String prefix = prefixValuePanel.getValue();
		String suffix = suffixValuePanel.getValue();
		String ext = extValuePanel.getValue();
		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";
		if (ext == null)
			ext = TXT;
		String completeFileName = dir + prefix + fileNameWithoutExt + suffix + "." + ext;

		return completeFileName;
	}// createCompleteFileName()

	/**
	 * a helper method to export a fileName
	 * 
	 * @param fileName
	 *            should be absolute fileName
	 * @param delimiter
	 *            String
	 * @param data
	 *            TablePanelModel
	 */
	private void exportFile(String fileName, String delimiter, TablePanelModel data) {
		FileExport export = new FileExport(fileName, delimiter);
		String fileHeader = data.getTableDataHeader();
		String[][] columnHeaders = data.getColumnHeaders();
		ArrayList fileData = data.getTableData();
		String fileFooter = data.getTableDataFooter();
		export.writeFile(fileHeader, columnHeaders, fileData, fileFooter);
	}

	/**
	 * a helper method to export a fileName
	 * 
	 * @param fileName
	 *            should be absolute fileName
	 * @param data
	 *            TablePanelModel
	 */
	private void exportARFF(String fileName, TablePanelModel data) {
		FileExport export = new FileExport(fileName, ",");
		String fileHeader = data.getTableDataHeader();
		String[][] columnHeaders = data.getColumnHeaders();
		StringBuffer[] columnNames = new StringBuffer[columnHeaders[0].length];
		System.out.println(columnHeaders);
		System.out.println(columnHeaders.length);
		if (columnHeaders != null && columnHeaders.length > 0) {
			for (int j = 0; j < columnHeaders[0].length; j++)
				columnNames[j] = new StringBuffer("\"");
			for (int i = 0; i < columnHeaders.length; i++) {
				String[] columns = columnHeaders[i];
				for (int j = 0; j < columns.length; j++)
					columnNames[j].append(columns[j] + " ");
			}
			columnHeaders = new String[1][columnNames.length];
			for (int j = 0; j < columnHeaders[0].length; j++)
				columnHeaders[0][j] = columnNames[j].toString().trim() + "\"";
		}

		StringBuffer header = new StringBuffer(fileHeader);
		if (fileHeader.length() > 0) {
			int startIndex = 1;
			header.insert(0, "%");
			while (header.substring(startIndex).indexOf("\n") != -1) {
				startIndex += (header.substring(startIndex)).indexOf("\n") + 1;
				header.insert(startIndex, "%");
			}
			if (header.lastIndexOf("%") == (header.length() - 1))
				header.deleteCharAt(header.length() - 1);
		}

		String fileFooter = data.getTableDataFooter();
		StringBuffer footer = new StringBuffer(fileFooter);
		// process File Footer
		if (fileFooter != null && fileFooter.length() != 0) {
			int startIndex = 1;
			footer.insert(0, "%");
			while (footer.substring(startIndex).indexOf("\n") != -1) {
				startIndex += (footer.substring(startIndex)).indexOf("\n") + 1;
				footer.insert(startIndex, "%");
			}
			if (footer.lastIndexOf("%") == (footer.length() - 1))
				footer.deleteCharAt(footer.length() - 1);
			fileFooter = footer.toString();
		}

		header.append("@relation Analysis_Engine_Table_Application \n");

		for (int i = 0; i < columnHeaders[0].length; i++) {
			header.append("@attribute " + (columnHeaders[0][i]).trim());
			if (data.getColumnClass(i).equals(Double.class))
				header.append(" real\n");
			else if (data.getColumnClass(i).equals(Integer.class))
				header.append(" real\n");
			else
				header.append(" string\n");
		}

		header.append("\n@data");

		ArrayList fileData = data.getTableData();
		export.writeFile(header.toString(), null, fileData, fileFooter);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String[] names = { "Orange", "Apple", "Grapes" };
		FileExportGUI fileExportGUI = new FileExportGUI(null, names, 0, CurrentDirectory
				.get(UserPreferences.USER_PREFERENCES));

		fileExportGUI.pack();
		fileExportGUI.setVisible(true);
	}

}
