package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * <p>
 * Title:TabNameGUI.java
 * </p>
 * <p>
 * Description: A gui for editing tab names
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: TabNameGUI.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */

public class TabNameGUI extends JDialog {
	private JTable table;

	private String[] allTabNames;

	private int result = JOptionPane.CANCEL_OPTION;

	/** Creates a new instance of TabNameGUI */
	public TabNameGUI(JFrame parent, String[] tabNames) {
		super(parent);
		allTabNames = tabNames;
		initialize();
		setModal(true);
		setTitle("Edit Tab Names");
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
		setVisible(true);
	}

	private void initialize() {

		String[] colNames = { "No", "Tab Name" };
		// String [] names = filesInTabbedPane.getAllUniqueNames();
		Object[][] data = new Object[allTabNames.length][colNames.length];
		for (int i = 0; i < allTabNames.length; i++) {
			data[i][0] = Integer.valueOf(i + 1);
			data[i][1] = allTabNames[i];
		}// for(i)
		DefaultTableModel tableModel = new DefaultTableModel(data, colNames) {
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				if (columnIndex == 0)
					return false;
				return true;
			}
		};

		table = new JTable(tableModel);
		setUpTextFieldEditor();
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(0).setMaxWidth(60);
		colModel.getColumn(0).setPreferredWidth(30);

		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(300, 300));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		mainPanel.add(scrollPane);

		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(mainPanel);
		container.add(getButtonPanel(), BorderLayout.SOUTH);
	}// initialize()

	/**
	 * Create a validating text field to act as the editor and tell the table whether the new tab name entered is an
	 * unique name
	 */
	protected void setUpTextFieldEditor() {
		// Set up the cell editor.
		final JTextField txtField = new JTextField();
		DefaultCellEditor cellEditor = new DefaultCellEditor(txtField) {
			// Save the original value.
			Object originalObjectValue = null;

			// Override getTableCellEditorComponent() to save the original value
			// in case the user enters an invalid value.
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				originalObjectValue = table.getValueAt(row, column);
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
					if (enteredValue.equalsIgnoreCase((String) originalObjectValue)) {
						return enteredValue;
					}
					checkForUniqueNames(enteredValue);
					return enteredValue;
				} catch (Exception e) {
					DefaultUserInteractor.get().notify(TabNameGUI.this, "Error", e.getMessage(), UserInteractor.ERROR);
					return originalObjectValue;
				}
			}// getCellEditorValue

		};

		TableColumn tableColumn = table.getColumnModel().getColumn(1);
		cellEditor.setClickCountToStart(2);
		tableColumn.setCellEditor(cellEditor);
	} // setUpTextFieldEditor()

	/**
	 * A helper method for checking whether the newName matches any names of the tab
	 */
	private void checkForUniqueNames(String newName) throws Exception {
		for (int i = 0; i < allTabNames.length; i++) {
			if (newName.equalsIgnoreCase(allTabNames[i])) {
				throw new Exception("The name \"" + newName + "\" is already exist in the" + " list of tab names");
			}
		}
	}

	private JPanel getButtonPanel() {
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.OK_OPTION;
				TabNameGUI.this.dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;
				TabNameGUI.this.dispose();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}

	public int getResult() {
		return result;
	}

	public String[] getAllTabNames() {
		int rowCount = table.getRowCount();
		String[] newTabNames = new String[rowCount];
		for (int i = 0; i < rowCount; i++) {
			newTabNames[i] = (String) table.getValueAt(i, 1);
		}
		return newTabNames;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		String[] tabNames = { "tabName1", "tabName2" };
		new TabNameGUI(frame, tabNames);
		System.exit(0);
	}

}
