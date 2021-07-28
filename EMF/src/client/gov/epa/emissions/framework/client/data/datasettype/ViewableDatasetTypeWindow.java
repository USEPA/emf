package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.*;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.*;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

public class ViewableDatasetTypeWindow extends DisposableInteralFrame implements ViewableDatasetTypeView {

    private ViewableDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;
    
    private DesktopManager desktopManager;
    
    private DatasetType type;

    private JTable fileFormat;

    public ViewableDatasetTypeWindow(DesktopManager desktopManager) {
        super("View Dataset Type", new Dimension(600, 520), desktopManager);

        this.desktopManager = desktopManager;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(ViewableDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type) {
        super.setTitle("View Dataset Type: " + type.getName());
        super.setName("datasetTypeView:" + type.getId());
        this.type = type;
        layout.removeAll();
        doLayout(layout, type);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, DatasetType type) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createBasicDataPanel(type));
        layout.add(createKeywordsPanel(type.getKeyVals()));
        layout.add(createQAStepTemplatesPanel(type));
        layout.add(createButtonsPanel());

        messagePanel.setMessage(lockStatus(type));
    }

    private String lockStatus(DatasetType type) {
        if (!type.isLocked())
            return "";

        return "Locked by User: " + type.getLockOwner() + " at " + CustomDateFormat.format_YYYY_MM_DD_HH_MM(type.getLockDate());
    }

    private JPanel createBasicDataPanel(DatasetType type) {
        JPanel uPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        TextField name = new TextField("name", type.getName(), 40);
        name.setEditable(false);
        name.setToolTipText("Dataset type name");
        layoutGenerator.addLabelWidgetPair("Name:", name, uPanel);

        TextArea description = new TextArea("description", type.getDescription(), 40, "Dataset type description");
        description.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(new Dimension(80, 80));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, uPanel);

        TextField sortOrder = new TextField("sortOrder", type.getDefaultSortOrder(), 40);
        sortOrder.setEditable(false);
        sortOrder.setToolTipText("Dataset type sort order columns");
        layoutGenerator.addLabelWidgetPair("Default Sort Order:", sortOrder, uPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(uPanel, 3, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad
        
        User user = type.getCreator();
        Date cDate = type.getCreationDate();
        Date mDate = type.getLastModifiedDate();
        String spaces = "       ";
        JPanel creator = new JPanel(new SpringLayout());
        creator.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator subLayoutGenerator = new SpringLayoutGenerator();

        // creator
        gov.epa.emissions.framework.client.Label creatorLabel = new gov.epa.emissions.framework.client.Label("Creator", (user == null ? " " : user.getName() + spaces));
        creatorLabel.setToolTipText("Creator of dataset type");
        subLayoutGenerator.addLabelWidgetPair("Creator:", creatorLabel, creator);
        // creation date
        gov.epa.emissions.framework.client.Label creationDateLabel = new gov.epa.emissions.framework.client.Label("Creation Date", CustomDateFormat.format_YYYY_MM_DD_HH_MM(cDate) + spaces);
        creationDateLabel.setToolTipText("Creation date of dataset type");
        subLayoutGenerator.addLabelWidgetPair("Creation Date:", creationDateLabel, creator);
        // creation date
        gov.epa.emissions.framework.client.Label lastModDtLabel = new gov.epa.emissions.framework.client.Label("Last Modified Date", CustomDateFormat.format_YYYY_MM_DD_HH_MM(mDate) + spaces);
        lastModDtLabel.setToolTipText("Last modified date of dataset typpe");
        subLayoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModDtLabel, creator);

        // Lay out the panel.
        subLayoutGenerator.makeCompactGrid(creator, 1, 6, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad


        fileFormat = getFileFormat(type);

        JPanel fileFormatPanel = new JPanel(new BorderLayout());
        fileFormatPanel.setBorder(BorderFactory.createTitledBorder("File Format"));

        //add file format table, if applicable
        if (fileFormat !=null) {
            fileFormat.setRowHeight(16);
            fileFormatPanel.add(new JScrollPane(fileFormat), BorderLayout.CENTER);
        }else {
            TextField fileFomatTextArea =new TextField(""," No file format for view.  ",40);
            fileFomatTextArea.setEditable(false);
            fileFomatTextArea.setToolTipText("File format for dataset type");
            fileFormatPanel.add(new JScrollPane(fileFomatTextArea), BorderLayout.CENTER);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(uPanel);
        panel.add(creator);
        panel.add(new JLabel("  "));
        panel.add(fileFormatPanel);
        
        return panel;
    }
    
    private JTable getFileFormat(DatasetType type) {
        XFileFormat fileFormat = type.getFileFormat();
        String importer = type.getImporterClassName();
        
        if (importer == null || !importer.equalsIgnoreCase(DatasetType.FLEXIBLE_IMPORTER))
            return null;
        
        if (fileFormat == null )
            return null;
        
        Column[] cols = fileFormat.getColumns();

        if (cols == null || cols.length == 0)
            return new JTable(10, 10);
        
        String[] titles = new String[]{"Column", "Type", "Default Value", "Mandatory", "Description"};
        String[][] values = new String[cols.length][5];
        
        for (int i = 0; i < cols.length; i++) {
            Column col = cols[i];
            values[i][0] = col.getName();
            values[i][1] = col.getSqlType();
            values[i][2] = col.getDefaultValue();
            values[i][3] = col.isMandatory()+"";
            values[i][4] = col.getDescription();
        }
        
        return new JTable(values, titles);
    }

    private JPanel createKeywordsPanel(KeyVal[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        TableData tableData = new DatasetTypeKeyValueTableData(vals);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createQAStepTemplatesPanel(DatasetType type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("QA Step Templates"));

        EditableQAStepTemplateTableData tableData = new EditableQAStepTemplateTableData(type.getQaStepTemplates());
        JTable table = new JTable(new EmfTableModel(tableData)){
            public String getToolTipText(MouseEvent e) { return getCellTip(e, this); }
        };
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createViewButton(tableData), BorderLayout.SOUTH);

        return panel;
    }
    
    private JPanel createViewButton(final EditableQAStepTemplateTableData data) {
        JPanel panel = new JPanel(new BorderLayout());
        
        Button view = new ViewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showTemplateWindows(data);
            }
        });
        view.setToolTipText("View dataset type");
        panel.add(view, BorderLayout.LINE_START);
        
        return panel;
    }
    
    private String getCellTip(MouseEvent e, JTable table) {
        Point p = e.getPoint();
        int rowIndex = table.rowAtPoint(p);
        int colIndex = table.columnAtPoint(p);
        
        return table.getValueAt(rowIndex, colIndex).toString();
    }

    private void showTemplateWindows(EditableQAStepTemplateTableData data) {
        QAStepTemplate[] selected = data.getSelected();
        
        for(int i = 0; i < selected.length; i++) {
            ViewableQAStepTemplateView view = new ViewableQAStepTemplateWindow(selected[i].getName()+
                    " - "+type.getName(), desktopManager);
            ViewableQAStepTemplatePresenter presenter = new ViewableQAStepTemplatePresenter(view, selected[i]);
            presenter.display();
        }
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Button closeButton = new CloseButton(closeAction());
        panel.add(closeButton, BorderLayout.LINE_END);
        getRootPane().setDefaultButton(closeButton);

        return panel;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        };

        return action;
    }

}
