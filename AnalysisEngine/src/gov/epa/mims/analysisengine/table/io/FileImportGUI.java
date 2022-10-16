package gov.epa.mims.analysisengine.table.io;

import gov.epa.mims.analysisengine.UserPreferences;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.IntegerValuePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.CurrentDirectory;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.TableApp;
import gov.epa.mims.analysisengine.table.TextDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class FileImportGUI extends JDialog {

	/** file type */
	private String selectedFileType;

	// /**delimiter string
	// */
	// private String delimiter;
	/** model for the table */
	private DefaultTableModel tableModel;

	/** table to list the imported file Names */
	private JTable table;

	/** to denote the first column */
	private final int FIRST_COLUMN = 0;

	/* to denote the second column */
	private final int SECOND_COLUMN = 1;

	/** to check whether every thing is set */
	private boolean allSet = false;

	/** a textField for delimiter */
	private JTextField customDelimitField;

	/** Delimiter label */
	private JLabel delimiterLabel;

	/** integer value panel to specify no of column header rows */
	private IntegerValuePanel noOfcolumnHeaderPanel;

	/** integer value panel to specify start index for tab name */
	private IntegerValuePanel startTabIndexPanel;

	/** integer value panel to specify end index for tab name */
	private IntegerValuePanel endTabIndexPanel;

	/**
	 * whether to consider multiple occurences of the delimiter character as one or not
	 */
	private static boolean multipleOccurences = false;

	/** The file dialog to use or loading and saving file. */
	private static final JFileChooser fileChooser = new JFileChooser();

	/** storage for log from the parser */
	private static String log = null;

	JButton importButton = new JButton("Import");

	JButton cancelButton = new JButton("Cancel");

	static {
		String userDir = System.getProperty("user.dir");
		if (userDir != null)
			fileChooser.setCurrentDirectory(new File(userDir));
	}

	// have the file type variables
	/*
	 * public static final String TRIM_RESULTS_FILE = "TRIM Results File"; public static final String
	 * TRIM_SENSITIVITY_FILE = "TRIM Sensitivity File"; public static final String GENERIC_FILE = "Generic File"; public
	 * static final String MONTE_CARLO_FILE = "Monte Carlo File"; public static final String COSU_FILE = "COSU File";
	 * public static final String DAVE_OUTPUT_FILE = "DAVE Output File"; public static final String SMOKE_REPORT_FILE =
	 * "SMOKE Report File";
	 */
	/** starting tab name index */
	public static final int START_TAB_NAME_INDEX = 1;

	/** ending tab name index */
	public static final int END_TAB_NAME_INDEX = 40;

	public static final String TRIM_RESULTS_FILE = "TRIM.FaTE Results File";

	public static final String TRIM_SENSITIVITY_FILE = "TRIM.FaTE Sensitivity File";

	public static final String GENERIC_FILE = "Custom Delimited File";

	public static final String FIXED_WIDTH_FILE = "Fixed Column Width Format";

	public static final String MONTE_CARLO_FILE = "TRIM.FaTE Monte Carlo Inputs";

	public static final String COSU_FILE = "COSU File";

	public static final String DAVE_OUTPUT_FILE = "DAVE Output File";

	public static final String SMOKE_REPORT_FILE = "SMOKE Report File";

	public static final String TAB_DELIMITED_FILE = "Tab Delimited File";

	public static final String SPACE_DELIMITED_FILE = "Space Delimited File";

	public static final String COMMA_DELIMITED_FILE = "Comma Separated File";

	public static final String ARFF_FILE = "Attribute-Relation File Format";

	private final String[] ALL_FILE_TYPES = { COMMA_DELIMITED_FILE, COSU_FILE, GENERIC_FILE, DAVE_OUTPUT_FILE,
			FIXED_WIDTH_FILE, SMOKE_REPORT_FILE, SPACE_DELIMITED_FILE, TAB_DELIMITED_FILE, ARFF_FILE,
			TRIM_RESULTS_FILE, MONTE_CARLO_FILE, TRIM_SENSITIVITY_FILE };

	private JFrame parent;

	private CurrentDirectory currentDirectory;

	/**
	 * Creates a new instance of FileImport
	 * 
	 * @param directory
	 */
	public FileImportGUI(JFrame parent, CurrentDirectory directory) {
		super(parent);
		this.parent = parent;
		this.currentDirectory = directory;
		setupTableModel(null);
		initialize();
		pack();
		Point point = ScreenUtils.getPointToCenter(this);
		setLocation(point);
		setVisible(true);
	}

	/**
	 * Creates a new instance of FileImport
	 * 
	 * @param fileNames
	 *            String []
	 * @param fileType
	 *            Selected File Type fileType can be FileImportGUI.TRIM_RESULTS_FILE FileImportGUI.TRIM_SENSITIVITY_FILE
	 *            FileImportGUI.DAVE_OUTPUT_FILE FileImportGUI.MONTE_CARLO_FILE FileImportGUI.COSU_FILE
	 *            FileImportGUI.COMMA_DELIMITED_FILE FileImportGUI.SMOKE_REPORT_FILE FileImportGUI.GENERIC_FILE
	 * @param currentDirectory
	 */
	public FileImportGUI(JFrame parent, String[] fileNames, String fileType, CurrentDirectory currentDirectory) {
		super(parent);
		selectedFileType = fileType;
		this.currentDirectory = currentDirectory;
		setupTableModel(fileNames);
		initialize();
		pack();
		Point point = ScreenUtils.getPointToCenter(this);
		setLocation(point);
		setVisible(true);
	}

	/**
	 * Helper method to setup a default table model
	 */
	private void setupTableModel(String[] fileNames) {
		Vector columnNames = new Vector();
		columnNames.add("File Names");
		columnNames.add("Import?");
		Vector tableData = new Vector();
		if (fileNames != null) {
			for (int i = 0; i < fileNames.length; i++) {
				Vector rowData = new Vector();
				rowData.add(fileNames[i]);
				rowData.add(Boolean.FALSE);
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
				return table.getValueAt(0, colNo).getClass();
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

	// helper method to initialize the gui
	private void initialize() {
		DefaultUserInteractor.set(new GUIUserInteractor());
		this.setModal(true);
		this.setTitle("Import Files");
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createLoweredBevelBorder()));

		JPanel fileTypePanel = new JPanel();
		JLabel label = new JLabel("File Type ");
		final JComboBox fileTypeComboBox = new JComboBox(ALL_FILE_TYPES);
		fileTypePanel.add(label);
		fileTypePanel.add(fileTypeComboBox);
		mainPanel.add(fileTypePanel);
		table = new JTable();
		// {
		// public TableCellRenderer getCellRenderer(int row, int column)
		// {
		// if ((column == 0))
		// {
		// DefaultTableCellRenderer tableCellRenderer =
		// new DefaultTableCellRenderer()
		// {
		// String storeFileName = null;
		// public Component getTableCellRendererComponent(
		// JTable table, Object fileName,
		// boolean isSelected, boolean hasFocus,
		// int row, int column)
		// {
		// JLabel label = new JLabel();
		// storeFileName = (String)fileName;
		// File file = new File(storeFileName);
		// label.setText(file.getName());
		// return label;
		// }
		// public void setToolTipText(String tip)
		// {
		// super.setToolTipText(storeFileName);
		// }
		// };
		//
		//
		// return tableCellRenderer;
		// }
		// // else...
		// return super.getCellRenderer(row, column);
		// }
		// };
		table.setModel(tableModel);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(FIRST_COLUMN).setPreferredWidth(400);
		columnModel.getColumn(SECOND_COLUMN).setPreferredWidth(75);

		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(tablePanel);
		JPanel cbPanel = new JPanel();

		JCheckBox cbIgnoreMultipleDelimiters = new JCheckBox("Ignore multiple " +

		"occurences of the delimiter", false);

		cbIgnoreMultipleDelimiters.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent it) {
				if (multipleOccurences)
					multipleOccurences = false;
				else
					multipleOccurences = true;
			}
		});
		// cbPanel.add(cbIgnoreMultipleDelimiters);
		mainPanel.add(cbPanel);
		// tabNameConfigPanel.setBorder(BorderFactory.createTitledBorder(
		// BorderFactory.createLineBorder(Color.black),"Tab Name"));

		JPanel tabNameConfigPanel = new JPanel();
		// tabNameConfigPanel.setLayout(new BoxLayout(
		// tabNameConfigPanel,BoxLayout.X_AXIS));
		startTabIndexPanel = new IntegerValuePanel("Name the tab based on characters at positions ", false, 1,
				Integer.MAX_VALUE);
		tabNameConfigPanel.add(startTabIndexPanel);
		endTabIndexPanel = new IntegerValuePanel("through", false, 1, Integer.MAX_VALUE);
		tabNameConfigPanel.add(endTabIndexPanel);
		JButton previewButton = new JButton("Preview");
		previewButton.setToolTipText("See a preview of the tab name");
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getRowCount() > 0) {
					String fileName = (String) table.getValueAt(0, FIRST_COLUMN);
					File file = new File(fileName);
					String fileNameWithExt = file.getName();
					int wholeLength = fileNameWithExt.length();
					String fileNameWOExt = fileNameWithExt.substring(0, wholeLength - 4);
					int endIndex = -1;
					int startIndex = -1;
					startIndex = startTabIndexPanel.getValue();
					if (startTabIndexPanel.getValue() > fileNameWOExt.length()) {
						startIndex = FileImportGUI.START_TAB_NAME_INDEX;
					}// if(startTabIndexPanel.getValue()>fileNameWOExt.length())
					endIndex = endTabIndexPanel.getValue();
					if (endIndex > fileNameWOExt.length()) {
						endIndex = fileNameWOExt.length();
					}// if(endTabIndexPanel>fileNameWOExt.length())
					String message = "The truncated tab name of the first file in" + " the table = "
							+ fileNameWOExt.substring(startIndex - 1, endIndex);
					JOptionPane.showMessageDialog(FileImportGUI.this, message, "Example Tab Name",
							JOptionPane.INFORMATION_MESSAGE);
				}// if(table.getRowCount() >0)
			}
		});
		tabNameConfigPanel.add(previewButton);
		startTabIndexPanel.setValue(START_TAB_NAME_INDEX);
		endTabIndexPanel.setValue(END_TAB_NAME_INDEX);

		JPanel addFileInfoPanel = new JPanel();
		delimiterLabel = new JLabel("Delimiter ");
		// use a document so that the user is able to only enter one character
		PlainDocument plainDoc = new PlainDocument() {
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (offs != 0)
					throw new BadLocationException("You should only enter " + "one character", offs);
				else if (str.length() > 1)
					throw new BadLocationException("You should " + "only enter one character", str.length());
				else
					super.insertString(offs, str, a);
			}
		};
		customDelimitField = new JTextField(plainDoc, ";", 3);
		// set the textfield to be center justified
		customDelimitField.setHorizontalAlignment(JTextField.CENTER);
		addFileInfoPanel.add(delimiterLabel);
		addFileInfoPanel.add(customDelimitField);
		addFileInfoPanel.add(cbIgnoreMultipleDelimiters);
		/*
		 * noOfcolumnHeaderPanel = new IntegerValuePanel("Number of column header"+ " rows ", false,0,
		 * Integer.MAX_VALUE); noOfcolumnHeaderPanel.setValue(1); addFileInfoPanel.add(noOfcolumnHeaderPanel);
		 */

		mainPanel.add(addFileInfoPanel);
		mainPanel.add(tabNameConfigPanel);
		// JPanel fileNamePanel = new JPanel();
		// JLabel fileNameLabel = new JLabel("File Names");
		// fileNameTextField = new JTextField();
		// fileNameTextField.setPreferredSize(new Dimension(200,25));
		//
		//
		// fileNamePanel.add(fileNameLabel);
		// fileNamePanel.add(fileNameTextField);
		// mainPanel.add(fileNamePanel);

		container.add(mainPanel, BorderLayout.CENTER);
		JPanel editPanel = createEditButtonPanel();

		JPanel importCancelPanel = createImportCancelButtonPanel();

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(editPanel, BorderLayout.NORTH);
		buttonPanel.add(importCancelPanel, BorderLayout.SOUTH);
		container.add(buttonPanel, BorderLayout.SOUTH);
		fileTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedFileType = (String) fileTypeComboBox.getSelectedItem();
				/*
				 * if(table.getRowCount() > 0) { int no = DefaultUserInteractor.get().selectOption( "Warning","You will
				 * loose the all the rows, if you change"+ " the file type.",UserInteractor.OK_CANCEL,
				 * UserInteractor.CANCEL); if(no == UserInteractor.YES) { removeAllRows(); } }// if(table.getRowCount() >
				 * 0)
				 */
				if (selectedFileType.equals(GENERIC_FILE)) {
					customDelimitField.setText(";");
					delimiterLabel.setEnabled(true);
					customDelimitField.setEnabled(true);
					// noOfcolumnHeaderPanel.setValue(1);
					// noOfcolumnHeaderPanel.setEnabled(true);
				}// if(selectedFileType.equals(GENERIC_FILE))
				else if (selectedFileType.equals(COMMA_DELIMITED_FILE)) {
					customDelimitField.setText(",");
					// noOfcolumnHeaderPanel.setValue(1);
					delimiterLabel.setEnabled(false);
					customDelimitField.setEnabled(false);
					// noOfcolumnHeaderPanel.setEnabled(true);
				} else if (selectedFileType.equals(SPACE_DELIMITED_FILE)) {
					customDelimitField.setText(" ");
					// noOfcolumnHeaderPanel.setValue(1);
					delimiterLabel.setEnabled(false);
					customDelimitField.setEnabled(false);
					// noOfcolumnHeaderPanel.setEnabled(true);
				} else if (selectedFileType.equals(TAB_DELIMITED_FILE)) {
					customDelimitField.setText("\t");
					// noOfcolumnHeaderPanel.setValue(1);
					delimiterLabel.setEnabled(false);
					customDelimitField.setEnabled(false);

					// noOfcolumnHeaderPanel.setEnabled(true);

				}

				else if (selectedFileType.equals(DAVE_OUTPUT_FILE))

				{

					customDelimitField.setText(",");

					delimiterLabel.setEnabled(true);

					customDelimitField.setEnabled(true);

					// noOfcolumnHeaderPanel.setValue(1);

					// noOfcolumnHeaderPanel.setEnabled(false);

				}

				else if (selectedFileType.equals(SMOKE_REPORT_FILE))

				{

					delimiterLabel.setEnabled(true);

					customDelimitField.setEnabled(true);

					customDelimitField.setText(";");

					// noOfcolumnHeaderPanel.setValue(2);

					// noOfcolumnHeaderPanel.setEnabled(false);

				}

				else if (selectedFileType.equals(FIXED_WIDTH_FILE))

				{

					delimiterLabel.setEnabled(false);

					customDelimitField.setEnabled(false);

					// noOfcolumnHeaderPanel.setValue(1);

					// noOfcolumnHeaderPanel.setEnabled(false);

				}

				else

				{

					if (selectedFileType.equals(COSU_FILE))

					{

						customDelimitField.setText(",");

						// noOfcolumnHeaderPanel.setValue(1);

					}

					else if (selectedFileType.equals(ARFF_FILE))

					{

						customDelimitField.setText(",");

						// noOfcolumnHeaderPanel.setValue(1);

					}

					else if (selectedFileType.equals(TRIM_RESULTS_FILE))

					{

						customDelimitField.setText(";");

						// noOfcolumnHeaderPanel.setValue(2);

					}

					else if (selectedFileType.equals(TRIM_SENSITIVITY_FILE))

					{

						customDelimitField.setText(";");

						// noOfcolumnHeaderPanel.setValue(1);

					}

					else

					{

						customDelimitField.setText(",");

						// noOfcolumnHeaderPanel.setValue(4);

					}

					delimiterLabel.setEnabled(false);

					customDelimitField.setEnabled(false);

					// noOfcolumnHeaderPanel.setEnabled(false);

				}// else

			}

		});

		if (selectedFileType == null)

		{

			selectedFileType = ALL_FILE_TYPES[0];

		}

		fileTypeComboBox.setSelectedItem(selectedFileType);

	}

	// helper method to create the edit panel

	private JPanel createEditButtonPanel()

	{

		JPanel editPanel = new JPanel();

		/*
		 * JButton previewButton = new JButton("Preview");
		 * 
		 * previewButton.setToolTipText("Preview the file selected in the file"+ " listing panel");
		 * 
		 * previewButton.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent e) { }
		 * 
		 * });
		 * 
		 * editPanel.add(previewButton);
		 * 
		 */// editPanel.setLayout(new BoxLayout(editPanel,BoxLayout.X_AXIS));
		JButton addButton = new JButton("Add Files");

		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				// disable buttons so the user doesn't think they can click them
				// when the file dialog is open
				importButton.setEnabled(false);
				cancelButton.setEnabled(false);
				File[] selectedFiles = getFilesFromUser(JFileChooser.OPEN_DIALOG, currentDirectory);
				addFilesAfterCheck(selectedFiles);
				importButton.setEnabled(true);
				cancelButton.setEnabled(true);
			}
		});
		addButton.setToolTipText("Add a file for import");
		editPanel.add(addButton);
		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if (rows.length == 0 && table.getRowCount() > 0)
					new GUIUserInteractor().notify(FileImportGUI.this, "Preview",
							"Select a file in the above table to preview the contents", UserInteractor.NOTE);
				else if (rows.length > 1)
					new GUIUserInteractor().notify(FileImportGUI.this, "Preview",
							"Select only one file in the above table " + "to preview the file contents",
							UserInteractor.NOTE);
				else if (rows.length == 0)
					new GUIUserInteractor().notify(FileImportGUI.this, "Preview",
							"No files are available in the table to preview. \n"
									+ "Please add then select a file in the table", UserInteractor.NOTE);
				else {
					String fileName = (String) table.getValueAt(rows[0], 0);
					try {
						TextDialog dialog = new TextDialog(parent, "Preview " + fileName, "", false);
						ArrayList list = new ArrayList();
						list.add(TableApp.get50Lines(fileName));
						dialog.setTextFromList("", list);
						dialog.setSize(900, 400);
						dialog.setModal(true);
						dialog.setVisible(true);
					} catch (Exception ee) {
						System.out.println("Error bringing up the file preview " + ee.getMessage());
					}
				}

			}

		});

		previewButton.setToolTipText("Preview the first 50 lines of the file " +

		"selected from the above table");

		editPanel.add(previewButton);

		JButton removeButton = new JButton("Remove");

		removeButton.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e)

			{

				deleteSelectedRows();

				// int [] selectedRows = table.getSelectedRows();

				// if(selectedRows != null)

				// {

				// for(int i=0; i< selectedRows.length; i++)

				// {

				// int index = selectedRows[i];

				// table.return

				// tableModel.removeRow(index);

				// }//for(i)

				// }//if(selectedRows != null)

			}

		});

		removeButton.setToolTipText("Unselect the above highlighted file for" +

		" import");

		editPanel.add(removeButton);

		JButton removeAllButton = new JButton("Remove All");

		removeAllButton.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e)

			{

				removeAllRows();

			}

		});

		removeAllButton.setToolTipText("Remove all the files in the above table");

		editPanel.add(removeAllButton);

		JButton toggleButton = new JButton("Toggle");

		toggleButton.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e)

			{

				int[] selectedRows = table.getSelectedRows();

				for (int i = 0; i < selectedRows.length; i++)

				{

					int index = selectedRows[i];

					Boolean value = (Boolean) tableModel.getValueAt(index,

					SECOND_COLUMN);

					boolean aValue = value.booleanValue();

					Boolean newValue;

					// toggling

					if (aValue == true)

					{

						newValue = Boolean.valueOf("false");

					}

					else

					{

						newValue = Boolean.valueOf("true");

					}

					tableModel.setValueAt(newValue, index, SECOND_COLUMN);

				}// for(i)

			}// actionPerformed()

		});

		toggleButton.setToolTipText("Toggle the selection of the highlighted files " +

		"in the above table");

		editPanel.add(toggleButton);

		return editPanel;

	}

	/**
	 * helper method to verify whether selected are files and wbether we have
	 * 
	 * already have the file in the table model then pop up the message
	 * 
	 */

	private void addFilesAfterCheck(File[] selectedFiles)

	{

		if (selectedFiles != null)

		{

			int length = selectedFiles.length;

			int noOfRows = table.getRowCount();

			boolean isFileExist = false;

			for (int i = 0; i < length; i++)

			{

				if (selectedFiles[i].isFile())

				{

					String fileName = selectedFiles[i].getAbsolutePath();

					// checking whether file is already selected

					for (int j = 0; j < noOfRows; j++)

					{

						String fileNameInTable = (String) table.getValueAt(j,

						FIRST_COLUMN);

						if (fileName.equals(fileNameInTable))

						{

							isFileExist = true;

							break;

						}// if(fileName.equals(fileNameInTable))

					}// for(j)

					if (isFileExist)

					{

						DefaultUserInteractor.get().notify(this, "Note", "The file " +

						fileName + " is already selected!", UserInteractor.NOTE);

					}

					else

					{

						Vector rowData = new Vector();

						rowData.add(fileName);

						rowData.add(Boolean.TRUE);

						tableModel.addRow(rowData);

					}

				}// if(selectedFiles[i].isFile())

			}// for(i)

		}// if(selectedFiles != null)

	}// addFilesAfterCheck()

	/** a helper method to create Button Panel */

	private JPanel createImportCancelButtonPanel()

	{

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(importButton);

		buttonPanel.add(cancelButton);

		importButton.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e)

			{

				allSet = true;

				dispose();

			}

		});

		importButton.setToolTipText("Import the above selected files");

		cancelButton.addActionListener(new ActionListener()

		{

			public void actionPerformed(ActionEvent e)

			{

				allSet = false;

				dispose();

			}

		});

		cancelButton.setToolTipText(

		"Cancel importing files and return to the main window");

		return buttonPanel;

	}// createOKCancelPanel()

	/**
	 * a helper method to remove all rows in the table
	 * 
	 */

	public void removeAllRows()

	{

		int rowCount = tableModel.getRowCount();

		for (int i = 0; i < rowCount; i++)

		{

			tableModel.removeRow(0);

		}

	}// removeAllRows()

	/**
	 * create a model
	 * 
	 * @param fileName
	 *            String
	 * 
	 * @param selectedType
	 *            String selected file type
	 * 
	 * @param delimiter
	 *            for the file
	 * 
	 * @parma noOfColNameRows no of rows for column names
	 * 
	 * @return Model
	 * 
	 */

	public static SpecialTableModel createAModel(String fileName, String selectedFileType, String delimiter,
			int noOfColNameRows) throws Exception {
		if (selectedFileType.equals(GENERIC_FILE)) {
			int length = fileName.length();
			String extension = fileName.substring(length - 3, length);
			if (extension.equalsIgnoreCase("csv")) {
				delimiter = ",";
			}
			FileParser reader = new FileParser(fileName, delimiter, noOfColNameRows, multipleOccurences);
			return createSpecialTableModel(reader);
		}// if(selectedFileType.equals(GENERIC_FILE))

		else if (selectedFileType.equals(COMMA_DELIMITED_FILE) || selectedFileType.equals(COSU_FILE)) {
			if (selectedFileType.equals(COSU_FILE))
				noOfColNameRows = 1;
			FileParser reader = new FileParser(fileName, ",", noOfColNameRows, multipleOccurences);
			return createSpecialTableModel(reader);
		} else if (selectedFileType.equals(SPACE_DELIMITED_FILE)) {
			FileParser reader = new FileParser(fileName, " ", noOfColNameRows, multipleOccurences);
			return createSpecialTableModel(reader);
		} else if (selectedFileType.equals(TAB_DELIMITED_FILE)) {
			FileParser reader = new FileParser(fileName, "\t", noOfColNameRows, multipleOccurences);
			return createSpecialTableModel(reader);
		} else if (selectedFileType.equals(FIXED_WIDTH_FILE)) {
			FixedWidthReader reader = new FixedWidthReader(fileName);
			return createSpecialTableModel(reader);
		}
		/*
		 * kthanga - commented out because COSUAdapter is not working properly. Currently using FileParser for
		 * processing this file. else if (selectedFileType.equals(COSU_FILE)) { //cosu assumes a "," delimiter
		 * COSUAdapter adapter = new COSUAdapter(fileName); String [][] columnHeader = adapter.getColumnHeader();
		 * ArrayList tableData = adapter.getFileData(); return new SpecialTableModel(null,columnHeader,tableData,
		 * adapter.getColumnClass()); }//else if (selectedFileType.equals(this.COSU_FILE))
		 */
		else if (selectedFileType.equals(MONTE_CARLO_FILE)) {
			MonteCarloFileReader reader = new MonteCarloFileReader(fileName, multipleOccurences);
			return createSpecialTableModel(reader);
		}// else if(selectedFileType.equals(this.MONTE_CARLO_FILE))
		else if (selectedFileType.equals(TRIM_RESULTS_FILE)) {
			TRIMResultFileReader reader = new TRIMResultFileReader(fileName, multipleOccurences);
			return createSpecialTableModel(reader);
		}// else if(selectedFileType.equals(TRIM_RESULTS_FILE))
		else if (selectedFileType.equals(TRIM_SENSITIVITY_FILE)) {
			TRIMSensitivityFileReader reader = new TRIMSensitivityFileReader(fileName, multipleOccurences);
			return createSpecialTableModel(reader);
		}// else if(selectedFileType.equals(TRIM_SENSITIVITY_FILE))
		else if (selectedFileType.equals(DAVE_OUTPUT_FILE)) {
			DAVEFileReader reader = new DAVEFileReader(fileName, delimiter, multipleOccurences);
			return createSpecialTableModel(reader);
		}// else if(selectedFileType.equals(TRIM_SENSITIVITY_FILE))
		else if (selectedFileType.equals(SMOKE_REPORT_FILE)) {
			SMKReportFileReader reader = new SMKReportFileReader(fileName, delimiter, multipleOccurences);
			return createSpecialTableModel(reader);
		}// else if(selectedFileType.equals(SMOKE_REPORT_FILE))
		else if (selectedFileType.equals(ARFF_FILE)) {
			ARFFReader reader = new ARFFReader(fileName);
			return createSpecialTableModel(reader);
		}
		return null;
	}

	public static String getLogMessages() {
		if (log == null) {
			return null;
		}
		return log;
	}

	/**
	 * a helper method to point the data from the reader to table model
	 * 
	 * @return SpecialTableModel
	 */
	private static SpecialTableModel createSpecialTableModel(FileParser reader) {
		ArrayList tableData = reader.getFileData();
		if (tableData == null) {
			tableData = new ArrayList();
		}
		ArrayList columnHeader = reader.getColumnHeaderData();
		if (columnHeader == null) {
			columnHeader = new ArrayList();
		}
		String[][] colHeader = arrayListToArray(columnHeader);
		ArrayList rowHeader = reader.getRowHeaderData();
		String[] rowHeaderData = null;
		if (rowHeader != null) {
			String[] a = {};
			rowHeaderData = (String[]) rowHeader.toArray(a);
		}
		String fileFooter = reader.getFileFooter();
		String fileHeader = reader.getFileHeader();
		Class[] columnTypes = reader.getColumnClass();

		if (reader.getLogMessages() == null) {
			log = null;
		} else {
			log = new String("Log for " + reader.getFileName() + ":\n" + reader.getLogMessages());
		}

		return new SpecialTableModel(fileHeader, rowHeaderData, colHeader, tableData, fileFooter, columnTypes);
	}

	/**
	 * a helper method to convert a array within a ArrayList to a two dimensional array.
	 */
	public static String[][] arrayListToArray(ArrayList a) {
		if (a == null)
			return null;
		String[][] array = new String[a.size()][];
		for (int i = 0; i < a.size(); i++) {
			array[i] = (String[]) a.get(i);
		} // for(i)
		return array;
	}// arrayListToArray()

	/** get the the delimiter */
	public String getDelimiter() {
		return customDelimitField.getText().trim();
	}

	/**
	 * get the no of rows for the column headers
	 */
	public int getNoOfColumnHeaders()

	{

		return noOfcolumnHeaderPanel.getValue();

	}

	/**
	 * getter for the file name
	 * 
	 * @return String[]
	 */
	public String[] getSelectedFiles() {
		int rowCount = table.getRowCount();
		if (rowCount > 0) {
			Vector selectedFiles = new Vector();
			for (int i = 0; i < rowCount; i++) {
				boolean isSelected = ((Boolean) table.getValueAt(i, 1)).booleanValue();
				if (isSelected) {
					selectedFiles.add(table.getValueAt(i, 0));
				}// if(isSelected)
			}// for(i)
			String[] a = {};
			return (String[]) selectedFiles.toArray(a);
		}
		return null;
	}// getFileName

	/**
	 * Delete the currently selected row(s) from the table. If none are selected, do nothing.
	 * 
	 * @author Daniel Gatti
	 */
	private void deleteSelectedRows() {
		int singleSelectedIndex = -1;
		int rowCountAfterDeletion = -1;
		int[] sel = table.getSelectedRows();
		if (sel == null || sel.length == 0)
			return;

		// If there is only one row selected, then save the row so that we
		// can reselect a row once the selected row is gone.
		if (sel.length == 1)
			singleSelectedIndex = sel[0];
		for (int i = sel.length - 1; i >= 0; --i)
			tableModel.removeRow(sel[i]);

		// If the table isn't empty, then select the row at the deleted row's
		// index.
		rowCountAfterDeletion = table.getModel().getRowCount();
		if (rowCountAfterDeletion != 0) {
			if (singleSelectedIndex >= 0) {
				if (singleSelectedIndex >= rowCountAfterDeletion)
					table.getSelectionModel()
							.setSelectionInterval(rowCountAfterDeletion - 1, rowCountAfterDeletion - 1);
				else
					table.getSelectionModel().setSelectionInterval(singleSelectedIndex, singleSelectedIndex);
			}
		}
	} // deleeSelectedRows()

	/** getter for the delimiter */
	public String getSelectedFileType() {
		return selectedFileType;
	}

	// public String getDelimiter()
	// {
	// // return customDelimitField.getText();
	// }

	/**
	 * 
	 * Get array of filenames from the user with either an Open or Save
	 * 
	 * FileChooser.
	 * 
	 * @param fileChooserType
	 *            String that is either JFileChooser.OPEN_DIALOG or
	 * 
	 * JFileChooser.SAVE_DIALOG
	 * @param currentDirectory2
	 * 
	 * @return file File that the user selected. Will be null if the user
	 * 
	 * selected 'Cancel'.
	 * 
	 */

	public static File[] getFilesFromUser(int fileChooserType, CurrentDirectory currentDirectory) {
		File[] retval = null;
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setCurrentDirectory(currentDirectory.getCurrentDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		switch (fileChooserType) {
		case JFileChooser.OPEN_DIALOG:
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFiles();
			}
			break;
		case JFileChooser.SAVE_DIALOG:
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFiles();
			}
			break;
		default:
			if (fileChooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFiles();
			}
			break;
		}
		if (retval != null && retval.length > 0)
			currentDirectory.setCurrentDirectory(retval[0].getParentFile());
		return retval;
	}

	/**
	 * Get a dir from the user
	 * 
	 * @return File directory
	 */
	public static File getDirFromUser(CurrentDirectory currentDirectory) {
		File retval = null;
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(currentDirectory.getCurrentDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			retval = fileChooser.getSelectedFile();
			if (retval == null) {
				retval = fileChooser.getCurrentDirectory();
			}
			currentDirectory.setCurrentDirectory(retval);
		}
		return retval;
	}

	/**
	 * Get a initial specified current dir from the user
	 * 
	 * @return File directory
	 */

	public static File getInitialCurrentDir() {
		String userDir = System.getProperty("user.dir");
		if (userDir != null) {
			return new File(userDir);
		}
		return null;
	}

	/**
	 * getter for allset
	 * 
	 * @return boolean allSet
	 */
	public boolean isAllSet() {
		return allSet;
	}

	public int getStartTabNameIndex() {
		int value = startTabIndexPanel.getValue();
		return (value == Integer.MIN_VALUE) ? START_TAB_NAME_INDEX : value;
	}

	/**
	 * getter for start tab name index
	 * 
	 * @return int
	 */
	public int getEndTabNameIndex() {
		int value = this.endTabIndexPanel.getValue();
		return (value == Integer.MIN_VALUE) ? END_TAB_NAME_INDEX : value;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		FileImportGUI fileImportGui = new FileImportGUI(new JFrame(), CurrentDirectory
				.get(UserPreferences.USER_PREFERENCES));
		fileImportGui.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}// main()

}
