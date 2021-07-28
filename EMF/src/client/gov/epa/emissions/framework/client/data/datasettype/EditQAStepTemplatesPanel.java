package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.qa.EditQAStepTemplateWindow;
import gov.epa.emissions.framework.client.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.client.qa.NewQAStepTemplatePresenter;
import gov.epa.emissions.framework.client.qa.NewQAStepTemplateView;
import gov.epa.emissions.framework.client.qa.NewQAStepTemplateWindow;
import gov.epa.emissions.framework.client.qa.QAStepTemplatesPanelPresenter;
import gov.epa.emissions.framework.client.qa.QAStepTemplatesPanelView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class EditQAStepTemplatesPanel extends JPanel implements QAStepTemplatesPanelView, Editor {

    protected EditableEmfTableModel tableModel;

    protected EditableTable table;

    protected ManageChangeables changeablesList;

    protected DatasetType type;

    protected QAStepTemplatesPanelPresenter presenter;

    private EditableQAStepTemplateTableData tableData;

    private DesktopManager desktopManager;

    private EmfConsole parent;

    private QAProgram[] programs;
    
    private EmfSession session;

    private SingleLineMessagePanel messagePanel;

    public EditQAStepTemplatesPanel(EmfSession session, DatasetType type, QAProgram[] programs, ManageChangeables changeablesList,
            DesktopManager desktopManager, EmfConsole parent, SingleLineMessagePanel messagePanel) {
        this.changeablesList = changeablesList;
        this.type = type;
        this.session = session;
        this.parent = parent;
        this.programs = programs;
        this.messagePanel = messagePanel;
        tableData = new EditableQAStepTemplateTableData(type.getQaStepTemplates());
        this.desktopManager = desktopManager;

        createLayout();
    }

    private void createLayout() {
        setBorder(new Border("QA Step Templates"));
        super.setLayout(new BorderLayout());
        super.add(centerPanel(), BorderLayout.CENTER);
    }

    private JPanel centerPanel() {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(), BorderLayout.CENTER);
        container.add(buttonsPanel(), BorderLayout.PAGE_END);

        return container;
    }

    protected JScrollPane table() {
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel) {
            public String getToolTipText(MouseEvent e) {
                return getCellTip(e, this);
            }
        };
        changeablesList.addChangeable(table);
        table.setRowHeight(16);

        return new JScrollPane(table);
    }
    
    public QAStepTemplate[] getQAStepTemps(){
        return tableData.sources();
    }

    private String getCellTip(MouseEvent e, EditableTable table) {
        Point p = e.getPoint();
        int rowIndex = table.rowAtPoint(p);
        int colIndex = table.columnAtPoint(p);

        Object obj = table.getValueAt(rowIndex, colIndex);
        return (obj == null) ? "" : obj.toString();
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
    }

    private JPanel buttonsPanel() {
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        });
        add.setToolTipText("Add QA step template");
        add.setMnemonic(KeyEvent.VK_D);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doRemove();
            }
        });
        remove.setToolTipText("Remove QA step template");
        remove.setMnemonic(KeyEvent.VK_R);
        container.add(remove);

        Button update = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doEdit();
            }
        });
        update.setToolTipText("Edit QA step template");
        container.add(update);

        Button copy = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doCopy();
            }
        });
        copy.setToolTipText("Copy QA step template");
        container.add(copy);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doEdit() {
        QAStepTemplate[] selected = tableData.getSelected();
        for (int i = 0; i < selected.length; i++) {
            EditQAStepTemplateWindow view = new EditQAStepTemplateWindow(
                    selected[i].getName() + " - " + type.getName(), desktopManager);
            try {
                presenter.doEdit(view, selected[i]);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    protected void doCopy() {
        QAStepTemplate[] templates = tableData.getSelected();
        if (templates.length == 0) {
            messagePanel.setMessage("Please select QAStepTemplate(s) to copy.");
            return;
        }
        
        CopyQAStepTemplateToDatasetTypeSelectionView view = new CopyQAStepTemplateToDatasetTypeSelectionDialog(parent);
        CopyQAStepTemplateToDatasetTypeSelectionPresenter presenter = new CopyQAStepTemplateToDatasetTypeSelectionPresenter(view, session);
        try {
            presenter.display(new DatasetType[] {this.type});
            DatasetType[] datasetTypes = presenter.getSelectedDatasetTypes();
            boolean copyToExistingDatasetType = false;
            if (datasetTypes.length > 0) {
                int[] datasetTypeIds = new int[datasetTypes.length];
                for (int i = 0; i < datasetTypes.length; i++) {
                    datasetTypeIds[i] = datasetTypes[i].getId();
                    if (datasetTypes[i].getId() == type.getId()) copyToExistingDatasetType = true;
                }
                this.presenter.doCopyQAStepTemplates(templates, datasetTypeIds, presenter.shouldReplace());
                //if copied to self, then a lock would have been lose, and needs to be reclaimed
                //also, refresh the templates table
                if (copyToExistingDatasetType) {
                    this.type = this.presenter.obtainLockedDatasetType(session.user(), this.type);
                    tableData.removeAll();
                    for (QAStepTemplate template : this.type.getQaStepTemplates())
                        tableData.add(template);

//                    tableData = new EditableQAStepTemplateTableData(this.type.getQaStepTemplates());
//                    tableModel.refresh(tableData);
                    
                    refresh();
                }
            }
            messagePanel.setMessage("Copied " + templates.length + " QA Step Templates to " + datasetTypes.length + " Dataset Types.");
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    public void refresh() {
        tableData.sortByOrder();
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
        type.setQaStepTemplates(tableData.sources());
    }

    public void addListener(KeyListener keyListener) {
        table.addKeyListener(keyListener);
    }

    public void observe(QAStepTemplatesPanelPresenter presenter) {
        this.presenter = presenter;
    }

    private void doRemove() {
        QAStepTemplate[] templates = tableData.getSelected();

        if (templates.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected template(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.removeSelected();
            refresh();
        }
    }

    private void doAdd() {
        NewQAStepTemplateView view = new NewQAStepTemplateWindow(desktopManager);
        NewQAStepTemplatePresenter newTemplatePresenter = new NewQAStepTemplatePresenter(this, view);
        newTemplatePresenter.display(type, programs, session);
    }

    public void add(QAStepTemplate template) {
        tableData.add(template);
        refresh();
    }

}