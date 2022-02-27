package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

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

    
    public class TableHeaderRenderer extends DefaultTableCellRenderer {

        private TableMetadata metadata;

        public TableHeaderRenderer(JTableHeader tableHeader, TableMetadata tableMetadata) {
            this.metadata = tableMetadata;
            
            setForeground(tableHeader.getForeground());
            setBackground(tableHeader.getBackground());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            
            setFont(tableHeader.getFont());
            
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText("<html><center>" + headerText(value) + "</center></html>");
            return this;
        }

        private String headerText(Object value) {
            String header = ((value == null) ? "" : value.toString());
            return header.toUpperCase() + type(header);
        }

        private String type(String header) {
            ColumnMetaData data = metadata.columnMetadata(header);
            if(data==null){
                return "<br>&nbsp;";
            }
            String type = parse(data.getType());
            if(!type.equalsIgnoreCase("String")){
                return "<br>"+type;
            }
            int length = data.getSize();
            String size = (length==-1 || length > 500)?"*":""+length;
            return "<br>" + type + "(" + size + ")";
        }

        private String parse(String type) {
            int index = type.lastIndexOf('.');
            return type.substring(index + 1);
        }
    }


}
