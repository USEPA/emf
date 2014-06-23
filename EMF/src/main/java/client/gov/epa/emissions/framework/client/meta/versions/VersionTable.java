package gov.epa.emissions.framework.client.meta.versions;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class VersionTable extends JTable {

    private JComponent descriptionComponent;

    public VersionTable(TableModel tableModel) {

        super(tableModel);

        if (tableModel.getRowCount() == 1) {
            if ((tableModel.getColumnClass(0).isInstance(Boolean.TRUE))
                    && tableModel.getColumnName(0).equalsIgnoreCase("Select")) {
                tableModel.setValueAt(Boolean.TRUE, 0, 0);
            }
        }

        this.setRowHeight(18);

        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int columnIndex) {

        Component c = super.prepareRenderer(renderer, rowIndex, columnIndex);
        int descriptionIndex = this.getColumnIndex("Description");

        if (columnIndex == descriptionIndex) {
            if (c instanceof JComponent) {

                this.descriptionComponent = (JComponent) c;
                String valueAt = (String) getValueAt(rowIndex, columnIndex);

                if (valueAt == null || valueAt.isEmpty()) {
                    this.descriptionComponent.setToolTipText(null);
                } else {
                    this.descriptionComponent.setToolTipText(Utils
                            .convertTextToHTML(Utils.addBreaks(valueAt, 80, '\n')));
                }
            }
        } else {

            if (this.descriptionComponent != null) {

                this.descriptionComponent.setToolTipText(null);
                this.descriptionComponent = null;
            }
        }

        return c;
    }

    public int getColumnIndex(String colName) {

        TableColumnModel model = this.getColumnModel();

        int index = -1;
        for (int i = 0; i < model.getColumnCount(); i++) {

            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();
            if (headerValue.equals(colName)) {

                index = i;
                break;
            }
        }

        return index;
    }
}
