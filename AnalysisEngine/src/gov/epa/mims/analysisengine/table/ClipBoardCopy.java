package gov.epa.mims.analysisengine.table;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class ClipBoardCopy implements ActionListener {

	private RowHeaderTable rowHeaderTable;

	private Clipboard systemClipboard;

	private String delimiter;

	public ClipBoardCopy(RowHeaderTable rowHeaderTable) {
		this.rowHeaderTable = rowHeaderTable;
		delimiter = "\t";
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	public void registerCopyKeyStroke() {
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		rowHeaderTable.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().compareTo("Copy") == 0) {

			StringBuffer sb = new StringBuffer();
			int[] selectedColumns = rowHeaderTable.getSelectedColumns();
			copyColumnHeaders(sb, selectedColumns, delimiter);
			copySelectedData(delimiter, sb, selectedColumns);

			// TODO: check with Alison whether to add a check to ensure we have
			// selected only a contiguous block of cells
			StringSelection stringSelection = new StringSelection(sb.toString());
			systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			systemClipboard.setContents(stringSelection, stringSelection);
		}
	}

	private void copySelectedData(String delimiter, StringBuffer sb, int[] selectedColumns) {
		int[] selectedRows = rowHeaderTable.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			for (int j = 0; j < selectedColumns.length; j++) {
				Object valueAt = rowHeaderTable.getValueAt(selectedRows[i], selectedColumns[j]);
				if (valueAt != null)
				    sb.append(valueAt.toString());
				if (j != selectedColumns.length - 1)
					sb.append(delimiter);
			}
			if (i != selectedRows.length - 1)
				sb.append("\n");
		}
	}

	private void copyColumnHeaders(StringBuffer columnHeadersString, int[] selectedColumns, String delimiter) {
		int colummHeaderLength = 0;
		if (selectedColumns.length == 0)
			return;

		colummHeaderLength = rowHeaderTable.underlyingModel.getColumnHeaders(0).length;

		String[][] selectedColumnHeaders = new String[selectedColumns.length][colummHeaderLength];
		for (int i = 0; i < selectedColumns.length; i++) {
			int index = selectedColumns[i] - 1;
			if (index < 0)// row header column
				selectedColumnHeaders[i] = rowHeaderArray(colummHeaderLength);
			else
				selectedColumnHeaders[i] = rowHeaderTable.underlyingModel.getColumnHeaders(index);
		}

		for (int j = 0; j < colummHeaderLength; j++) {
			for (int i = 0; i < selectedColumns.length; i++) {
				columnHeadersString.append(selectedColumnHeaders[i][j]);
				if (i != selectedColumns.length - 1)
					columnHeadersString.append(delimiter);
			}
			columnHeadersString.append("\n");
		}
	}

	private String[] rowHeaderArray(int colummHeaderLength) {
		String[] rowHeader = new String[colummHeaderLength];
		for (int i = 0; i < colummHeaderLength; i++) {
			rowHeader[i] = "";

			if (i == 0)
				rowHeader[i] = "Row";
		}
		return rowHeader;
	}

}
