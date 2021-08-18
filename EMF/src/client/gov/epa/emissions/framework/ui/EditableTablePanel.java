package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class EditableTablePanel extends JPanel implements Editor {

    protected EditableEmfTableModel tableModel;

    protected EditableTable table;

    protected ManageChangeables changeablesList;

    private EmfConsole parent;

    public EditableTablePanel(String label, InlineEditableTableData tableData, ManageChangeables changeablesList, EmfConsole parent) {
        this.changeablesList = changeablesList;
        this.parent = parent;
        super.setLayout(new BorderLayout());
        super.add(doLayout(label, tableData), BorderLayout.CENTER);
    }

    private JPanel doLayout(String label, InlineEditableTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        if (label != null)
            container.add(labelPanel(label), BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);
        container.add(buttonsPanel(tableData), BorderLayout.PAGE_END);

        return container;
    }

    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    protected JScrollPane table(InlineEditableTableData tableData) {
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        changeablesList.addChangeable(table);
        table.setRowHeight(16);
        
        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
    }

    private JPanel buttonsPanel(final InlineEditableTableData tableData) {
        JPanel container = new JPanel();

        JButton add = new JButton("Add");
        add.setMargin(new Insets(2, 2, 2, 2));
        add.setToolTipText("Add");
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tableData.addBlankRow();
                refresh();
            }
        });
        add.setMnemonic(KeyEvent.VK_A);
        container.add(add);

        JButton remove = new JButton("Remove");
        remove.setMargin(new Insets(2, 2, 2, 2));
        remove.setToolTipText("Remove");
        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doRemove(tableData);
            }
        });
        remove.setMnemonic(KeyEvent.VK_O);
        container.add(remove);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private void doRemove(final InlineEditableTableData tableData) {
        int rowCount = tableData.getSelectedCount();

        if (rowCount == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the "+rowCount+" selected items?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.removeSelected();
            refresh();
        }
    }
    
    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    public void setColumnEditor(TableCellEditor editor, int columnIndex, String toolTip) {
        TableColumnModel colModel = table.getColumnModel();
        TableColumn col = colModel.getColumn(columnIndex);
        col.setCellEditor(editor);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTip);
        col.setCellRenderer(renderer);
    }

    public void invalidate() {
        setColumnWidths(table.getColumnModel());
        super.invalidate();
    }

    public void commit() {
        table.commit();
    }

    public void addListener(KeyListener keyListener) {
        table.addKeyListener(keyListener);
    }

}
