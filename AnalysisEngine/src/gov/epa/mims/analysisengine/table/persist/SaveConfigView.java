package gov.epa.mims.analysisengine.table.persist;

import gov.epa.mims.analysisengine.UserPreferences;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.CurrentDirectory;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class SaveConfigView extends JDialog {

	/** OK button to close the panel and save configurations */
	private JButton okButton;

	/** Cancel button to not proceed with saving configuration */
	private JButton cancelButton;

	/** Panel containing OK and Cancel */
	private JPanel okCancelPanel;

	/** Browse button for a poping up a FileChooser Dialog */
	private JButton bBrowse;

	/**
	 * Textfield displaying the selected output file or for inputting the filename
	 */
	private JTextField tFileName;

	/** Save As label */
	private JLabel lSaveAs;

	/** Panel showing the Saving to file option */
	private JPanel ConfigFilePanel;

	/** Panel showing Config Table Modifiers */
	private JPanel ConfigTableModifierPanel;

	/** Delete button */
	private JButton bDelete;

	/** View the plot button */
	private JButton bView;

	/** Edit the plot configuration button */
	private JButton bEdit;

	/** Check the selected configurations */
	private JButton bSelect;

	/** Clear the selected configurations */
	private JButton bClear;

	/** Table displaying the configurations */
	private JTable chooseConfigTable;

	/** Panel dispalying the configurations */
	private JScrollPane chooseConfigSPanel;

	/** Overall panel containing components */
	private JPanel OverallPanel;

	/** Model for the Save Config Model */
	private SaveConfigModel model;

	private JRadioButton xmlConfig;

	private JRadioButton binaryConfig;

	public SaveConfigView(Frame parent, SaveConfigModel model) {
		super(parent);
		this.model = model;
		initGUI();
		setLocation(ScreenUtils.getPointToCenter(this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
	}

	public void initGUI() {
		try {

			chooseConfigSPanel = new JScrollPane();
			chooseConfigTable = new JTable();
			ConfigTableModifierPanel = new JPanel();
			bDelete = new JButton();
			bView = new JButton();
			bEdit = new JButton();
			bSelect = new JButton();
			bClear = new JButton();
			JLabel lHelp = new JLabel();
			ConfigFilePanel = new JPanel();
			lSaveAs = new JLabel();
			tFileName = new JTextField();
			bBrowse = new JButton();
			okCancelPanel = new JPanel();
			cancelButton = new JButton();
			okButton = new JButton();
			OverallPanel = new JPanel();

			BoxLayout thisLayout = new BoxLayout(OverallPanel, 1);

			OverallPanel.setLayout(thisLayout);
			this.setResizable(false);
			this.setTitle("Analysis Configuration");
			this.setSize(new java.awt.Dimension(465, 320));
			this.setMinimumSize(new java.awt.Dimension(465, 320));
			this.getContentPane().add(OverallPanel);
			OverallPanel.add(chooseConfigSPanel);
			OverallPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			chooseConfigTable.setRowSelectionAllowed(true);
			chooseConfigSPanel.add(chooseConfigTable);
			chooseConfigSPanel.setViewportView(chooseConfigTable);
			chooseConfigTable.setModel(model);

			final JTextField txtField = new JTextField();
			DefaultCellEditor cellEditor = new DefaultCellEditor(txtField) {
				// Save the original value.
				Object originalObjectValue = null;

				int selectedRow = -1;

				// Override getTableCellEditorComponent() to save the original value
				// in case the user enters an invalid value.
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
						int column) {
					originalObjectValue = table.getValueAt(row, column);
					selectedRow = row;
					String newValue = (String) value;
					txtField.setText(newValue);
					return txtField;
				}// getTableCellEditorComponent

				// Override DefaultCellEditor's getCellEditorValue method
				// to return an Double, not a String:
				public Object getCellEditorValue() {
					String enteredValue;
					try {
						enteredValue = txtField.getText();
						if (enteredValue == null || enteredValue.trim().equals("")) {
							throw new Exception("Please enter a valid name");
						}
						enteredValue = enteredValue.trim();
						if (enteredValue.equalsIgnoreCase((String) originalObjectValue)) {
							return enteredValue;
						}
						model.renameConfig(selectedRow, enteredValue);
						return enteredValue;
					} catch (Exception e) {
						DefaultUserInteractor.get().notify(SaveConfigView.this, "Error", e.getMessage(),
								UserInteractor.ERROR);
						return originalObjectValue;
					}
				}// getCellEditorValue

			};

			TableColumn tableColumn = chooseConfigTable.getColumnModel().getColumn(0);
			cellEditor.setClickCountToStart(2);
			tableColumn.setCellEditor(cellEditor);

			FlowLayout ConfigTableModifierLayout = new FlowLayout(FlowLayout.CENTER, 3, 4);

			ConfigTableModifierPanel.setLayout(ConfigTableModifierLayout);
			ConfigTableModifierPanel.setPreferredSize(new java.awt.Dimension(400, 40));
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
			lHelp.setPreferredSize(new java.awt.Dimension(380, 25));
			lHelp.setText("Hint: Double click on a plot name to begin renaming it");
			lHelp.setHorizontalTextPosition(SwingConstants.LEFT);
			lHelp.setVisible(true);
			panel.add(lHelp);
			OverallPanel.add(panel);
			OverallPanel.add(ConfigTableModifierPanel);

			bView.setText("View");
			bView.setToolTipText("View all or selected Configuration");
			bView.setPreferredSize(new java.awt.Dimension(75, 25));
			bView.setVisible(true);
			ConfigTableModifierPanel.add(bView);
			bView.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bViewMouseClicked();
				}
			});

			bEdit.setText("Edit");
			bEdit.setToolTipText("Edit Selected Configuration");
			bEdit.setPreferredSize(new java.awt.Dimension(75, 25));
			bEdit.setVisible(true);
			ConfigTableModifierPanel.add(bEdit);
			bEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					editConfiguration();
				}
			});

			bDelete.setText("Delete");
			bDelete.setPreferredSize(new java.awt.Dimension(75, 25));
			bDelete.setToolTipText("Delete Selected Configurations");
			bDelete.setVisible(true);
			ConfigTableModifierPanel.add(bDelete);
			bDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
						deleteConfigurations();
				}
			});

			bSelect.setText("Select");
			bSelect.setPreferredSize(new java.awt.Dimension(75, 25));
			bSelect.setToolTipText("Check Selected Configurations");
			bSelect.setVisible(true);
			ConfigTableModifierPanel.add(bSelect);
			bSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					doSelect();
				}
			});

			bClear.setText("Clear");
			bClear.setPreferredSize(new java.awt.Dimension(75, 25));
			bClear.setToolTipText("Uncheck Selected Configurations");
			bClear.setVisible(true);
			ConfigTableModifierPanel.add(bClear);
			bClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					doClear();
				}
			});

			JLabel xmlBinaryConfigLabel = new JLabel("File Format: ");
			binaryConfig = new JRadioButton("Binary", true);
			xmlConfig = new JRadioButton("XML", false);
			ButtonGroup group = new ButtonGroup();
			group.add(binaryConfig);
			group.add(xmlConfig);
			JPanel configFormatPanel = new JPanel();
			configFormatPanel.add(xmlBinaryConfigLabel);
			configFormatPanel.add(binaryConfig);
			configFormatPanel.add(xmlConfig);
			OverallPanel.add(configFormatPanel);

			FlowLayout ConfigFilePanelLayout = new FlowLayout();

			ConfigFilePanel.setLayout(ConfigFilePanelLayout);
			ConfigFilePanel.setPreferredSize(new java.awt.Dimension(400, 35));
			ConfigFilePanelLayout.setAlignment(FlowLayout.CENTER);
			ConfigFilePanelLayout.setHgap(8);
			ConfigFilePanelLayout.setVgap(5);
			OverallPanel.add(ConfigFilePanel);

			lSaveAs.setText("Save As");
			//lSaveAs.setPreferredSize(new java.awt.Dimension(50, 16));
			lSaveAs.setVisible(true);
			ConfigFilePanel.add(lSaveAs);

			tFileName.setPreferredSize(new java.awt.Dimension(210, 20));
			ConfigFilePanel.add(tFileName);

			bBrowse.setText("Browse");
			//bBrowse.setPreferredSize(new java.awt.Dimension(95, 25));
			ConfigFilePanel.add(bBrowse);
			bBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					browse();
				}
			});

			okButton.setText("Save");
			//okButton.setPreferredSize(new java.awt.Dimension(88, 25));
			ConfigFilePanel.add(okButton);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						saveConfiguration();
					} catch (RuntimeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			FlowLayout OKCancelPanelLayout = new FlowLayout();

			okCancelPanel.setLayout(OKCancelPanelLayout);
			okCancelPanel.setPreferredSize(new java.awt.Dimension(400, 35));
			OKCancelPanelLayout.setAlignment(FlowLayout.CENTER);
			OKCancelPanelLayout.setHgap(5);
			OKCancelPanelLayout.setVgap(5);
			OverallPanel.add(okCancelPanel);

			cancelButton.setText("Close");
			cancelButton.setPreferredSize(new java.awt.Dimension(88, 25));
			okCancelPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					doCancel();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showGUI(Frame parent, SaveConfigModel model) {
		try {
			SaveConfigView inst = new SaveConfigView(parent, model);
			inst.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void bViewMouseClicked() {
		int[] indexs = chooseConfigTable.getSelectedRows();

		if (indexs.length < 1) {
			if (chooseConfigTable.getRowCount() == 0)
				return;
			indexs = new int[chooseConfigTable.getRowCount()];
			for (int i = 0; i < indexs.length; i++)
				indexs[i] = i;
		}

		if (indexs.length == 0) {
			int rows = chooseConfigTable.getRowCount();
			indexs = new int[rows];
			for (int i = 0; i < rows; i++)
				indexs[i] = i;
		}

		for (int i = 0; i < indexs.length; i++) {
			try {
				String configName = (String) model.getValueAt(indexs[i], 0);
				if (configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_SORT_CRITERIA)
						|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FILTER_CRITERIA)
						|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FORMAT)) {
					new GUIUserInteractor().notify(this, "View " + configName, configName
							+ " reflects the current state of the table", UserInteractor.NOTE);
					continue;
				}
				model.showPlot(indexs[i], this);
			} catch (Exception e) {
				GUIUserInteractor gui = new GUIUserInteractor();
				gui.notify(this, "There is an error with " + model.getValueAt(indexs[i], 0), e.getMessage(), UserInteractor.ERROR);

				//Don't need to delete the faulty config here because one can delete them through
				//delete button. Qun He 6/1/2007
//				int result = gui.selectOption(this, "Delete Faulty Configuration?", "Delete FaultyConfiguration?",
//						GUIUserInteractor.YES_NO, GUIUserInteractor.NO);

//				if (result == GUIUserInteractor.YES) {
//					model.remove(indexs[i]);
//					model.removeRow(indexs[i]);
//				} else {
//					return;
//				}
			}
		}
	}

	protected void editConfiguration() {
		int[] indexs = chooseConfigTable.getSelectedRows();
		if (indexs.length > 1) {
			new GUIUserInteractor().notify(this, "Note", "Choose only one configuration " + "at a time to edit",
					UserInteractor.ERROR);
			return;
		}

		if (indexs.length == 0) {
			new GUIUserInteractor().notify(this, "Note", "No selected configuration to edit", UserInteractor.NOTE);
			return;
		}

		String configName = (String) model.getValueAt(indexs[0], 0);

		if (configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_SORT_CRITERIA)
				|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FILTER_CRITERIA)
				|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FORMAT)) {
			new GUIUserInteractor().notify(this, "Note", "Table Configuration"
					+ " is not editable. It reflects the current state of the table.", UserInteractor.NOTE);
			return;
		}

		try {
			model.showTree(configName);
		} catch (Exception e) {
			e.printStackTrace();
			new GUIUserInteractor().notify(this, "Exception Occurred", e.getMessage(), UserInteractor.ERROR);
		}
	}

	protected void deleteConfigurations() {
		SaveConfigModel model = (SaveConfigModel) chooseConfigTable.getModel();
		int[] indexs = chooseConfigTable.getSelectedRows();

		if (indexs.length == 0) {
			return;
		}
		int k = indexs.length;

		while (k-- > 0) {
			String configName = (String) model.getValueAt(indexs[k], 0);
			if (configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_SORT_CRITERIA)
					|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FILTER_CRITERIA)
					|| configName.equalsIgnoreCase(AnalysisConfiguration.TABLE_FORMAT)) {
				int res = new GUIUserInteractor().selectOption(this, "Deleting " + configName,
						" Do you really want to delete the Table Configuration?.\n Once "
								+ " deleted, you cannot save the " + configName + " of \n"
								+ " this table later in this session.", UserInteractor.YES_NO, UserInteractor.NO);
				if (res == UserInteractor.NO)
					continue;
			}
			//model.remove(indexs[k]);
			model.remove(configName);
			model.removeRow(indexs[k]);
		}
	}

	protected void doSelect() {
		int[] indexs = chooseConfigTable.getSelectedRows();

		if (indexs.length > 0) {
			TableModel model = chooseConfigTable.getModel();

			for (int i = 0; i < indexs.length; i++) {
				model.setValueAt(Boolean.TRUE, indexs[i], 2);
			}
		}
		return;
	}

	protected void doClear() {
		int[] indexs = chooseConfigTable.getSelectedRows();

		if (indexs.length > 0) {
			TableModel model = chooseConfigTable.getModel();

			for (int i = 0; i < indexs.length; i++) {
				model.setValueAt(Boolean.FALSE, indexs[i], 2);
			}
		}
		return;
	}

	protected void browse() {
		CurrentDirectory currentDirectory = CurrentDirectory.get(UserPreferences.USER_PREFERENCES);
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(currentDirectory.getCurrentDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = chooser.showSaveDialog(this);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			currentDirectory.setCurrentDirectory(file.getParentFile());
			tFileName.setText(file.getAbsolutePath());
		}
		return;
	}

	protected void doCancel() {
		this.dispose();
		return;
	}

	protected void saveConfiguration() {
		String fileName = tFileName.getText();
		if (fileName == null || fileName.length() == 0) {
			new GUIUserInteractor().notify(this, "Error Saving Configuration", "No Configuration File specified",
					UserInteractor.ERROR);
			return;
		}
		try {
			model.saveConfiguration(binaryConfig.isSelected(), new File(tFileName.getText()));
			dispose();
		} catch (Exception e) {
			e.printStackTrace();
			new GUIUserInteractor().notify(this, "Error Saving Configuration", e.getMessage(), UserInteractor.ERROR);
			return;
		}
	}

	/**
	 * MyCellEditorListener - to propogate changes to the ConfigTable to the AnalysisConfiguration object in the table
	 */

	class MyCellEditorListener implements CellEditorListener {

		public void editingStopped(ChangeEvent e) {
			int row = chooseConfigTable.getSelectedRow();

			try {
				model.renameConfig(row, (String) ((TableCellEditor) (e.getSource())).getCellEditorValue());
			} catch (Exception ex) {
				new GUIUserInteractor().notify(SaveConfigView.this, "Error", ex.getMessage(), UserInteractor.ERROR);
				chooseConfigTable.getModel().setValueAt(model.getConfigName(row), row, 0);
			}
		}

		public void editingCanceled(ChangeEvent e) {
			//
		}

	}

}
