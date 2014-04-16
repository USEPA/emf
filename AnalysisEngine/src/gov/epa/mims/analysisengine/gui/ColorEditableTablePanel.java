package gov.epa.mims.analysisengine.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * An EditableTablePanel that only accepts Colors for editing.
 * 
 * @author Daniel Gatti, CEP, UNC
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel.java
 * @version $Id: ColorEditableTablePanel.java,v 1.5 2007/05/31 14:29:33 qunhe Exp $
 */
public class ColorEditableTablePanel extends EditableTablePanel {
	/**
	 * The list of valid choices for the user to choose from. This will be null if the user can enter any value.
	 */
	protected Color[] validChoices = null;

	static final long serialVersionUID = 1;
	
	/** The default value that will be place in newly added rows. */
	protected Color defaultValue = Color.white;

	/**
	 * Constructor.
	 * 
	 * @author Daniel Gatti
	 * @param columnName
	 *            String that is the name of the single data column in this table.
	 */
	public ColorEditableTablePanel(String columnName) {
		super(columnName);
		tableModel.setColumnClass(Color.class);
		dataFlavor = ColorSelection.colorFlavor;
		setUpRenderer();
		setUpColorChooserEditor();
	}

	/**
	 * Copy the currently selected row. Note: This works only if one row is selected.
	 * 
	 * @author Daniel Gatti
	 */
	protected void copySelectedRow() {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		Object obj = table.getValueAt(table.getSelectedRow(), 1);
		if (obj instanceof Color) {
			Color c = (Color) obj;
			ColorSelection contents = new ColorSelection(c);
			cb.setContents(contents, null);
		} else
			DefaultUserInteractor.get().notify(
					this,
					"Unexpected object type",
					"Expected a Color value in ColorEditableTablePanel.copySelectedRow(). Found a "
							+ obj.getClass().toString() + " instead.", UserInteractor.ERROR);
	}

	/**
	 * Return the values in this table as an array of Colors.
	 * 
	 * @author Daniel Gatti
	 * @returns Color[] that is an array of the values in this table.
	 */
	public Object[] getValue() {
		Color[] retval = new Color[tableModel.getRowCount()];
		for (int r = tableModel.getRowCount() - 1; r >= 0; --r)
			retval[r] = (Color) tableModel.getValueAt(r, 1);

		return retval;
	}

	/**
	 * Insert the color white into the table.
	 * 
	 * @author Daniel Gatti
	 * @param above
	 *            boolean that is true if the value should be added above the currently selected row and false if below.
	 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#insertRow(boolean)
	 */
	protected void insertRow(boolean above) {
		insertRowInternal(above, defaultValue);
	}

	/**
	 * Set a default value that will be used when a new row is added.
	 * 
	 * @author Daniel Gatti
	 * @param value
	 *            Object that must be a Color and is the default value to place in a new row.
	 */
	public void setDefaultValue(Object value) {
		if (defaultValue instanceof Color)
			defaultValue = (Color) value;
		else
			DefaultUserInteractor.get().notify(this, "Unexpected object type",
					"Expected a Color value in ColorEditableTablePanel.setDefaultValue()", UserInteractor.ERROR);
	}

	/**
	 * Set a list of valid choices that will be the only ones that the user can enter into the table.
	 * 
	 * @author Daniel Gatti
	 * @param choices
	 *            Object[] that is a list of valid choices to be displayed in a combo box when the user is editing data
	 *            in the table.
	 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#setValidChoices(java.lang.Object[])
	 */
	public void setValidChoices(Object[] choices) {
		if (choices == null || choices.length == 0)
			return;

		if (!(choices[0] instanceof Color))
			return;

		validChoices = new Color[choices.length];
		System.arraycopy(choices, 0, validChoices, 0, choices.length);

		setUpComboBoxEditor();
	}

	/**
	 * Create a combo box with the valid choices passed in by the user and tell the table to use it for Integers. NOTE:
	 * I assume that you have checked that the validChoices array is not null.
	 * 
	 * @author Daniel Gatti
	 */
	protected void setUpComboBoxEditor() {
		// Create a JComboBox and place the colors in it.
		JComboBox comboBox = new JComboBox();
		for (int i = 0; i < validChoices.length; i++)
			comboBox.addItem(validChoices[i]);

		// Create a cell renderer (that has a different interface from the table
		// cell renderer below) to display colors in the cell.
		comboBox.setRenderer(new ListCellRenderer() {
			JPanel panel = new JPanel();

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Object obj = list.getModel().getElementAt(index);
				if (obj != null) {
					if (obj instanceof Color) {
						if (isSelected)
							panel.setBackground(((Color) obj).darker());
						else
							panel.setBackground((Color) obj);
					} else {
						DefaultUserInteractor.get().notify(
								ColorEditableTablePanel.this,
								"Unexpected object type",
								"Expected a Color value in ColorEditableTablePanel.getListCellRendererComponent()."
										+ "Found a " + obj.getClass() + " instead.", UserInteractor.ERROR);
					}
				}
				return panel;
			}
		});

		TableColumn tableColumn = table.getColumnModel().getColumn(1);
		DefaultCellEditor editor = new DefaultCellEditor(comboBox);
		editor.setClickCountToStart(2);
		tableColumn.setCellEditor(editor);
	} // setUpComboBoxEditor()

	/**
	 * Set up the JColorChooser to appear as the editor for cells in column 1.
	 * 
	 * @author Daniel Gatti
	 */
	private void setUpColorChooserEditor() {
		TableColumn tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setCellEditor(new ColorCellEditor());
	}

	/**
	 * Create a JPanel that will have a background color that is the value in the cell. Set this as the default renderer
	 * for column 1.
	 * 
	 * @author Daniel Gatti
	 */
	private void setUpRenderer() {
		TableColumn tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setCellRenderer(new DefaultTableCellRenderer() {
			JPanel panel = new JPanel();

			// Set the background color to be the value in the cell.
			// Use a slightly darker color when the cell is selected.
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Object obj = table.getValueAt(row, column);
				if (obj instanceof Color) {
					panel.setBackground((Color) obj);
				} else {
					DefaultUserInteractor.get().notify(ColorEditableTablePanel.this, "Unexpected object type",
							"Expected an Color value in ColorEditableTablePanel.getTableCellRendererComponent()",
							UserInteractor.ERROR);
				}
				return panel;
			}
		});
	} // setUpRenderer()

	/**
	 * An editor that will bring up a JColorChooser on a double-click and allow the user to select a color for the
	 * selected cell.
	 * 
	 * @author Daniel Gatti
	 * @see javax.swing.DefaultCellEditor
	 */
	class ColorCellEditor extends DefaultCellEditor {
		/** The original color in the cell. */
		private Color originalColor = null;

		/**
		 * The color that we will return. This will be the newly chosen value if editing was successful and the original
		 * value if editing was cancelled.
		 */
		private Color returnColor = null;

		/** The editing label to place in the cell while the user is choosing a color. */
		private JLabel label = new JLabel("Editing...", SwingConstants.CENTER);

		/**
		 * Constructor.
		 */
		public ColorCellEditor() {
			super(new JTextField());
		}

		/**
		 * Return either the colo that the user chose if editing was successful or the original color if editing was
		 * cancelled.
		 * 
		 * @return Object that is the Color held by this editor.
		 */
		public Object getCellEditorValue() {
			return returnColor;
		}

		/**
		 * Save the original color and return a JLabel that says "Editing..."
		 * 
		 * @return JLabel that says "Editing..." to fill the cell while the JColorChooser is displayed.
		 */
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Object obj = table.getValueAt(row, column);
			if (obj instanceof Color) {
				originalColor = (Color) obj;
				returnColor = originalColor;
			} else
				DefaultUserInteractor.get().notify(ColorEditableTablePanel.this, "Unexpected object type",
						"Expected an Color value in ColorCellEditor.getTableCellEditorComponent()",
						UserInteractor.ERROR);

			label.setBackground(originalColor);
			return label;
		}

		/**
		 * Set the return color back to the original color.
		 */
		public void cancelCellEditing() {
			returnColor = originalColor;
		}

		/**
		 * Bring up the JColorChooser and let the user select a color. This has to be done here because this is the last
		 * method that Java calls when editing. It does *NOT* work from getTableCellEditorComponent().
		 * 
		 * @author Daniel Gatti
		 * @return boolean that is what the super class would return.
		 */
		public boolean shouldSelectCell(EventObject e) {
			returnColor = JColorChooser.showDialog(ColorEditableTablePanel.this, "Choose Color", originalColor);
			if (returnColor == null)
				returnColor = originalColor;
			table.editingStopped(new ChangeEvent(table));
			return super.shouldSelectCell(e);
		}

		/**
		 * Always let the editor stop editing. The setting of the returnColor is handled in shouldSelectCell().
		 * 
		 * @return boolean that is always true.
		 */
		public boolean stopCellEditing() {
			return true;
		}
	} // class ColorCellEditor
} // class ColorEditableTablePanel

