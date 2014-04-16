/*
 * PopupTableColumn.java
 *
 * Created on December 10, 2003, 9:56 AM
 */
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBorder;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;

public class PopupTableColumn extends SpecialTableColumn {

	private TextBorderEditor defaultValue = new TextBorderEditor(new TextBorder());

	private TextBorderEditor[] validChoices = { new TextBorderEditor(new TextBorder()) };

	/** Creates a new instance of PopupTableColumn */
	public PopupTableColumn(int modelIndex, String name) {
		super(modelIndex, name);
		type = String.class;
		this.setupRenderer();
	}

	protected void copySelectedCell(Object obj) {
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Object nextChoice() {
		return new TextBorderEditor(new TextBorder());
	}

	private void setupRenderer() {
		this.setCellRenderer(new DefaultTableCellRenderer() {
			JLabel label = new JLabel();

			// Set the background color to be the value in the cell.
			// Use a slightly darker color when the cell is selected.
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Object obj = table.getValueAt(row, column);
				if (obj != null) {
					if (obj instanceof TextBorderEditor) {
						label.setText(((TextBorderEditor) obj).getInfoString());
					} else {
						DefaultUserInteractor.get().notify(table, "Unexpected object type",
								"Expected a TextBorderEditor in PopupTableColumn.getTableCellRendererComponent()",
								UserInteractor.ERROR);
					}
				} else {
					label.setText("");
				}
				return label;
			}
		});
	} // setUpRenderer()

	public void setDefaultValue(Object obj) {
		// this.defaultValue = (Strng)obj;
	}

	protected void setupCellEditor() {
		cellEditor = new TextCellEditor();
	}

	public class TextCellEditor extends DefaultCellEditor {

		/** The original color in the cell. */
		private String originalString = null;

		/**
		 * The color that we will return. This will be the newly chosen value if editing was successful and the original
		 * value if editing was cancelled.
		 */

		private String returnString = null;

		/** The editing label to place in the cell while the user is choosing a color. */

		private JLabel label = new JLabel("Editing...", SwingConstants.CENTER);

		private TextBorderEditor cellTextBorderEditor = null;

		public TextCellEditor() {
			super(new JTextField());
		}

		public Object getCellEditorValue() {
			return cellTextBorderEditor;

		}

		public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			Object obj = table.getValueAt(row, column);
			if (obj instanceof TextBorderEditor) {
				cellTextBorderEditor = (TextBorderEditor) obj;
			} else
				DefaultUserInteractor.get().notify(table, "Unexpected object type",
						"Expected an TextBorderEditor in PopupTableColumn.getTableCellEditorComponent()",
						UserInteractor.ERROR);
			// label.setText(cellTextBorderEditor.getInfoString());
			return label;
		}

		/**
		 * 
		 * Bring up the JColorChooser and let the user select a color. This has to be done here because this is the last
		 * method that Java calls when editing. It does *NOT* work from getTableCellEditorComponent().
		 * 
		 * @author Daniel Gatti
		 * 
		 * @return boolean that is what the super class would return.
		 * 
		 */

		public boolean shouldSelectCell(EventObject e) {
			JTable table = (JTable) e.getSource();
			cellTextBorderEditor.setVisible(true);
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

		/**
		 * Set the return color back to the original color.
		 */
		public void cancelCellEditing() {
			returnString = originalString;
		}
	}

}
