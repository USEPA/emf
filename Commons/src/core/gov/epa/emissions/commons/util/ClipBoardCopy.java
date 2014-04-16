package gov.epa.emissions.commons.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

public class ClipBoardCopy implements ActionListener {

    private JTable table;

    private Clipboard systemClipboard;

    private String delimiter;

    public ClipBoardCopy(JTable table) {
        this.table = table;
        delimiter = "\t";
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public void registerCopyKeyStroke() {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        table.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo("Copy") == 0) {

            StringBuffer sb = new StringBuffer();
            int[] selectedColumns = selectedColumns(table);
            // if(table.getColumnSelectionAllowed())
            copyColumnHeaders(sb, selectedColumns, delimiter);
            copySelectedData(delimiter, sb, selectedColumns);

            // TODO: check with Alison whether to add a check to ensure we have
            // selected only a contiguous block of cells
            StringSelection stringSelection = new StringSelection(sb.toString());
            systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(stringSelection, stringSelection);
        }
    }

    private int[] selectedColumns(JTable table) {
        if (table.getColumnSelectionAllowed())
            return table.getSelectedColumns();

        int colCount = table.getColumnCount();
        int[] cols = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            cols[i] = i;
        }
        return cols;
    }

    private void copySelectedData(String delimiter, StringBuffer sb, int[] selectedColumns) {
        // table.setCellSelectionEnabled()
        int[] selectedRows = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            for (int j = 0; j < selectedColumns.length; j++) {
                Object valueAt = table.getValueAt(selectedRows[i], selectedColumns[j]);
                sb.append((valueAt == null) ? "" : valueAt.toString());
                if (j != selectedColumns.length - 1)
                    sb.append(delimiter);
            }
            if (i != selectedRows.length - 1)
                sb.append("\n");
        }
    }

    private void copyColumnHeaders(StringBuffer columnHeadersString, int[] selectedColumns, String delimiter) {
        if (selectedColumns.length == 0)
            return;

        String[] selectedColumnHeaders = new String[selectedColumns.length];
        for (int i = 0; i < selectedColumns.length; i++) {
            selectedColumnHeaders[i] = table.getColumnName(selectedColumns[i]);
        }

        for (int i = 0; i < selectedColumns.length; i++) {
            columnHeadersString.append(selectedColumnHeaders[i]);
            if (i != selectedColumns.length - 1)
                columnHeadersString.append(delimiter);
        }

        columnHeadersString.append("\n");
    }

}
