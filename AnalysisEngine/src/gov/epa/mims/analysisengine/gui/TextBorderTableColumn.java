package gov.epa.mims.analysisengine.gui;

/*
 * TextBorderTableColumn.java
 * A Table column which will be contain objects of type TextBorder and when some
 * clicks on one of the cell in the column TextBorderEditor will pop up
 * @see TextBorder.java
 */

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

public class TextBorderTableColumn extends SpecialTableColumn {
	private TextBorder defaultValue = new TextBorder();

	private TextBorder[] validChoices = { new TextBorder() };

	/** Creates a new instance of TextBorderTableColumn */
	public TextBorderTableColumn(int modelIndex, String name) {
		super(modelIndex, name);
		type = TextBorder.class;
		this.setupRenderer();
	}

	protected void copySelectedCell(Object obj) {
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Object nextChoice() {
		TextBorder txtBorder = new TextBorder();
		initValues(txtBorder);
		return txtBorder;
	}

	/** initialize values for the TextBorder with reasonable values */
	private void initValues(TextBorder txtBorder) {
		txtBorder.setPosition(TextBorder.REFERENCE_LINE, TextBorder.CENTER, txtBorder.getXJustification(), txtBorder
				.getYJustification());
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
					if (obj instanceof TextBorder) {
						label.setText(((TextBorder) obj).getTextString());
					} else {
						DefaultUserInteractor.get().notify(table, "Unexpected object type",
								"Expected a TextBorderEditor in TextBorderTableColumn.getTableCellRendererComponent()",
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
		this.defaultValue = (TextBorder) obj;
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
			return cellTextBorderEditor.getDataSource();
		}

		public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			Object obj = table.getValueAt(row, column);

			if (obj instanceof TextBorder) {
				cellTextBorderEditor = new TextBorderEditor((TextBorder) obj, TextEditor.EDITOR_REFLINE);
			} else
				DefaultUserInteractor.get().notify(table, "Unexpected object type",
						"Expected an TextBorderEditor in TextBorderTableColumn.getTableCellEditorComponent()",
						UserInteractor.ERROR);

			return label;

		}

		/**
		 * Bring up the JColorChooser and let the user select a color. This has to be done here because this is the last
		 * method that Java calls when editing. It does *NOT* work from getTableCellEditorComponent().
		 * 
		 * @author Daniel Gatti
		 * @return boolean that is what the super class would return.
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
