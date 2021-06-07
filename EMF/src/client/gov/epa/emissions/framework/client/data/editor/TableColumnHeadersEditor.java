package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class TableColumnHeadersEditor {
    
    private JTable table;
    private TableMetadata tableMetadata;

    public TableColumnHeadersEditor(JTable table, TableMetadata tableMetadata){
        this.table = table;
        this.tableMetadata = tableMetadata;
    }
    
    public void  renderHeader() {
        JTableHeader tableHeader = new JTableHeader(table.getColumnModel());
        tableHeader.setBackground(UIManager.getDefaults().getColor("TableHeader.background"));
        tableHeader.setDefaultRenderer(new TableHeaderRenderer(tableHeader, tableMetadata));
        table.setTableHeader(tableHeader);
    }

    
    public class TableHeaderRenderer extends JPanel implements TableCellRenderer {

        private JTextPane textPane;

        private TableMetadata metadata;

        public TableHeaderRenderer(JTableHeader tableHeader, TableMetadata tableMetadata) {
            this.textPane = new JTextPane();
            this.metadata = tableMetadata;
            textPaneSettings(tableHeader);
            setLayout(new BorderLayout());
            add(textPane);
        }

        private void textPaneSettings(JTableHeader tableHeader) {
            textPane.setForeground(tableHeader.getForeground());
            textPane.setBackground(tableHeader.getBackground());
            textPane.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            StyledDocument doc = textPane.getStyledDocument();
            MutableAttributeSet standard = new SimpleAttributeSet();
            Font font = tableHeader.getFont();
            StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
            StyleConstants.setFontFamily(standard, font.getFamily());
            StyleConstants.setFontSize(standard,font.getSize());
            doc.setParagraphAttributes(0, 0, standard, true);
            
            textPane.setFont(font);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            textPane.setText(headerText(value));
            return this;
        }

        private String headerText(Object value) {
            String header = ((value == null) ? "" : value.toString());
            return header.toUpperCase() + type(header);
        }

        private String type(String header) {
            ColumnMetaData data = metadata.columnMetadata(header);
            if(data==null){
                return "\n";
            }
            String type = parse(data.getType());
            if(!type.equalsIgnoreCase("String")){
                return "\n"+type;
            }
            int length = data.getSize();
            String size = (length==-1 || length > 500)?"*":""+length;
            return "\n" + type + "(" + size + ")";
        }

        private String parse(String type) {
            int index = type.lastIndexOf('.');
            return type.substring(index + 1);
        }
    }


}
