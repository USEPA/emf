package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

public class NewDatasetTypeWindow extends DisposableInteralFrame implements NewDatasetTypeView {
    private NewDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextField minFiles;

    private TextField maxFiles;

    private ComboBox derivedFrom;

    private TextField formatFile;

    private Button browse;
    
    private EmfConsole parentConsole;
    
    private EmfSession session;

    private TextArea description;

    private TextField fileFormatName;

    private TextArea fileFormatDesc;

    private ComboBox delimiter;

    private static int counter = 0;

    private static final String[] types = { DatasetType.FLEXIBLE, DatasetType.EXTERNAL, DatasetType.CSV, DatasetType.LINE_BASED, DatasetType.SMOKE };

    private static final String[] delimiters = {"          ,          ", "          ;", "          Tab", "          |"};
    
    public NewDatasetTypeWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session) {
        super("Create New Dataset Type", new Dimension(600, 500), desktopManager);
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(basicInputPanel());
        layout.add(formatDefPanel());
        layout.add(createButtonsPanel());
    }

    public void observe(NewDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        counter++;
        String name = "Create New Dataset Type" + counter;
        super.setTitle(name);
        super.setName("createNewDatasetType:" + counter);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel basicInputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", 40);
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);
        
        description = new TextArea("description", "", 40);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, panel);

        derivedFrom = new ComboBox("Choose one:", types);
        addChangeable(derivedFrom);
        layoutGenerator.addLabelWidgetPair("Derived From:", derivedFrom, panel);

        minFiles = new TextField("minfiles", 20);
        addChangeable(minFiles);
        layoutGenerator.addLabelWidgetPair("Min Files:", minFiles, panel);

        maxFiles = new TextField("maxfiles", 20);
        addChangeable(maxFiles);
        layoutGenerator.addLabelWidgetPair("Max Files:", maxFiles, panel);
        
        derivedFrom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                minFiles.setText("1");
                minFiles.setEditable(false);
                maxFiles.setText("1");
                maxFiles.setEditable(false);
                formatFile.setEnabled(false);
                browse.setEnabled(false);
                fileFormatName.setEditable(false);
                fileFormatName.setText("");
                fileFormatDesc.setEditable(false);
                fileFormatDesc.setText("");
                delimiter.setEnabled(false);
                
                if(((String)e.getItem()).equalsIgnoreCase(types[0])) {
                    formatFile.setEnabled(true);
                    browse.setEnabled(true);
                    fileFormatName.setEditable(true);
                    fileFormatName.setText(name.getText());
                    fileFormatDesc.setEditable(true);
                    fileFormatDesc.setText(description.getText());
                    delimiter.setEnabled(true);
                }
                
                if(((String)e.getItem()).equalsIgnoreCase(types[1])) {
                    maxFiles.setText("-1");
                    maxFiles.setEditable(true);
                    minFiles.setEditable(true);
                }
            }
        });
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel formatDefPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        fileFormatName = new TextField("fileformatname", name.getText(), 40);
        fileFormatName.setEditable(false);
        addChangeable(fileFormatName);
        layoutGenerator.addLabelWidgetPair("Name:", fileFormatName, panel);
        
        fileFormatDesc = new TextArea("fileformatdescription", name.getText(), 40);
        fileFormatDesc.setEditable(false);
        addChangeable(fileFormatDesc);
        ScrollableComponent formatDescScroll = new ScrollableComponent(fileFormatDesc);
        layoutGenerator.addLabelWidgetPair("Description:", formatDescScroll, panel);
        
        layoutGenerator.addLabelWidgetPair("Definition File:", formatFilePanel(), panel);
        
        delimiter = new ComboBox("Choose one:", delimiters);
        addChangeable(delimiter);
        layoutGenerator.addLabelWidgetPair("Delimiter:", delimiterPanel(), panel);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad
        
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "File Format",
                TitledBorder.CENTER, TitledBorder.TOP, getFont(), Color.BLUE));
        
        return panel;
    }
    
    private JPanel formatFilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        formatFile = new TextField("formatfile", 32);
        formatFile.setEnabled(false);
        addChangeable(formatFile);
        
        browse = new Button("Browse...", new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFile();
            }
        });
        browse.setMnemonic('B');
        browse.setEnabled(false);
        
        panel.add(formatFile);
        panel.add(new JLabel(" "));
        panel.add(browse);
        
        return panel;
    }
    
    private JPanel delimiterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        delimiter = new ComboBox("Choose one:", delimiters);
        delimiter.setSelectedIndex(1);
        delimiter.setEnabled(false);
        addChangeable(delimiter);
        panel.add(delimiter);
        
        return panel;
    }

    private void clear() {
        messagePanel.clear();
    }
    
    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(formatFile.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(parentConsole.getSession(), initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select Format File");
        chooser.setDirectoryAndFileMode();

        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles()[0] : null;

        if (file != null) { formatFile.setText(file.getAbsolutePath()); }
    }
    
    private boolean isDigit(String text) {
        if(text.length() == 0)
            return false;
        
        if (!Character.isDigit(text.charAt(0)) && text.charAt(0) != '-')
            return false;

        for (int n = 1; n < text.length(); n++) {
            if (!Character.isDigit(text.charAt(n))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkTextFields() {
        if (name.getText().equals(""))
            messagePanel.setError("Name field should be a non-empty string.");
        else if (!isDigit(minFiles.getText()))
            messagePanel.setError("Min Files field should only contain a number.");
        else if (!isDigit(maxFiles.getText()))
            messagePanel.setError("Max Files field should only contain a number.");
        else if (derivedFrom.getSelectedItem() == null)
            messagePanel.setError("Derived From field should have a value.");
        else if (((String) derivedFrom.getSelectedItem()).equalsIgnoreCase("Flexible File Format") 
                && delimiter.getSelectedItem() == null ){
            messagePanel.setError("Delimiter field should have a value.");
        }
        else{
            messagePanel.clear();
            return true;
        }

        return false;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    try {
                        resetChanges();
                        presenter.doSave(name.getText(), description.getText(), minFiles.getText(), maxFiles.getText(), 
                                (String) derivedFrom.getSelectedItem(), getFileFormat(), formatFile.getText());
                    } catch (EmfException e) {
                        messagePanel.setError(e.getMessage());
                    }
                }
            }
        };

        return action;
    }
    
    protected XFileFormat getFileFormat() {
        if (derivedFrom.getSelectedIndex() != 1)
            return null;
        
        XFileFormat fileFormat = new XFileFormat();
        fileFormat.setName(fileFormatName.getText());
        fileFormat.setDescription(fileFormatDesc.getText());
        fileFormat.setDelimiter(delimiter.getSelectedItem() == null ? "" : delimiter.getSelectedItem().toString());
        
        return fileFormat;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new SaveButton(saveAction());
        container.add(saveButton);
        container.add(new CloseButton("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

}
