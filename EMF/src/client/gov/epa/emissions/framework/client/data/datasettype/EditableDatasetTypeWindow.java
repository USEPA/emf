package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.client.qa.QAStepTemplatesPanelPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class EditableDatasetTypeWindow extends DisposableInteralFrame implements EditableDatasetTypeView {

    private EditableDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextField sortOrder;

    private TextArea description;

    private EditableKeyValueTableData keywordsTableData;

    private DatasetTypeKeywordsPanel keywordsPanel;

    private DesktopManager desktopManager;

    private EditQAStepTemplatesPanel qaStepTemplatesPanel;

    private EmfConsole parent;

    private EmfSession session;

    private JTable fileFormat;

    public EditableDatasetTypeWindow(EmfSession session, EmfConsole parent, DesktopManager desktopManager) {
        super("Edit Dataset Type", new Dimension(620, 580), desktopManager);

        this.desktopManager = desktopManager;
        this.parent = parent;
        this.session = session;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(EditableDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type, QAProgram[] programs, Keyword[] keywords) {
        super.setTitle("Edit Dataset Type: " + type.getName());
        super.setName("datasetTypeEditor:" + type.getId());
        
        layout.removeAll();
        doLayout(layout, type, keywords,programs);

        super.display();
    }

    private void doLayout(JPanel layout, DatasetType type, Keyword[] keywords, QAProgram[] programs) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel(type));
        layout.add(createKeywordsPanel(type, keywords));
        layout.add(createQAStepTemplatesPanel(type,programs));
        layout.add(createButtonsPanel());
    }

    private JPanel createInputPanel(DatasetType type) {
        JPanel uPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", type.getName(), 40);
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, uPanel);

        description = new TextArea("description", type.getDescription(), 40, 2);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(new Dimension(80, 50));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, uPanel);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(uPanel, 2, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad
        
        User user = type.getCreator();
        Date cDate = type.getCreationDate();
        Date mDate = type.getLastModifiedDate();
        String spaces = "       ";
        JPanel creator = new JPanel();
        creator.setLayout(new BoxLayout(creator, BoxLayout.X_AXIS));
        creator.add(new JLabel("Creator: " + (user == null ? " " : user.getName() + spaces)));
        creator.add(new JLabel("Creation Date: " + CustomDateFormat.format_YYYY_MM_DD_HH_MM(cDate) + spaces));
        creator.add(new JLabel("Last Modified Date: " + CustomDateFormat.format_YYYY_MM_DD_HH_MM(mDate)));
        //layoutGenerator.addLabelWidgetPair("", creator, uPanel);
        
        JPanel lPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator2 = new SpringLayoutGenerator();

        fileFormat = getFileFormat(type);
        if (fileFormat == null){
            TextField fileFomatTextArea =new TextField(""," No file format for view.  ",40);
            fileFomatTextArea.setEditable(false);
            layoutGenerator2.addLabelWidgetPair("File Format:", fileFomatTextArea, lPanel);
        }else{   
            ScrollableComponent fileFomatTextArea = new ScrollableComponent(fileFormat);
            fileFomatTextArea.setMinimumSize(new Dimension(80, 50));
            layoutGenerator2.addLabelWidgetPair("File Format:", fileFomatTextArea, lPanel);
        }
        sortOrder = new TextField("sortOrder", type.getDefaultSortOrder(), 40);
        addChangeable(sortOrder);
        layoutGenerator2.addLabelWidgetPair("Default Sort Order:", sortOrder, lPanel);

        // Lay out the panel.
        layoutGenerator2.makeCompactGrid(lPanel, 2, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(uPanel);
        panel.add(creator);
        panel.add(new JLabel("  "));
        panel.add(lPanel);
        
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
            return new JTable(10,10);
        
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

    private JPanel createKeywordsPanel(DatasetType type, Keyword[] keywords) {
        keywordsTableData = new EditableKeyValueTableData(type.getKeyVals(), new Keywords(keywords));
        keywordsPanel = new DatasetTypeKeywordsPanel(keywordsTableData, keywords, this, parent);
        keywordsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        keywordsPanel.setMinimumSize(new Dimension(80, 100));
        return keywordsPanel;
    }

    private JPanel createQAStepTemplatesPanel(DatasetType type, QAProgram[] programs) {
        qaStepTemplatesPanel = new EditQAStepTemplatesPanel(session, type, programs, this, desktopManager, parent,messagePanel);
        QAStepTemplatesPanelPresenter presenter = new QAStepTemplatesPanelPresenter(session,type, qaStepTemplatesPanel);
        presenter.display();
        qaStepTemplatesPanel.setMinimumSize(new Dimension(80, 100));
        return qaStepTemplatesPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
        container.setLayout(layout);

        Button saveButton = new SaveButton(saveAction());
        container.add(saveButton);
        CloseButton closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        };

        return action;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private void doSave() {
        if (name.getText().equals("")) {
            messagePanel.setError("Name should be specified.");
            return;
        }

        resetChanges();
        try {
            keywordsPanel.commit();
            qaStepTemplatesPanel.commit();
            //DatasetType type;
            presenter.doSave(name.getText(), description.getText(), keywordsTableData.sources(), sortOrder.getText());
        } catch (EmfException e) {
            messagePanel.setError("Could not save: " + e.getMessage());
        }
    }

}
