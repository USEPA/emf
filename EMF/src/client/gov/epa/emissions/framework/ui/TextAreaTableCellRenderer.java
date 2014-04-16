package gov.epa.emissions.framework.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;


public class TextAreaTableCellRenderer extends JTextArea implements TableCellRenderer {
    
    
    public TextAreaTableCellRenderer(){
        setLineWrap(true);
        setWrapStyleWord(true);
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setText(value.toString());
        return this;
    }

}
