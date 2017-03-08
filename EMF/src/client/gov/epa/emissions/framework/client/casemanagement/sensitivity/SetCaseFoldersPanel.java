package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SetCaseFoldersPanel extends JPanel{

    private TextField inputDir;

    private TextField outputDir;
    
    private TextArea description; 
    
    private Case caseObj;
    
    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private EmfSession session; 
    
    private ManageChangeables changeablesList;

    //private Dimension preferredSize = new Dimension(380, 20);
    
    public SetCaseFoldersPanel(Case caseObj, MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parentConsole) {
        this.caseObj =caseObj;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.changeablesList = changeablesList;
        this.inputDir = new TextField("inputdir", 20);
        inputDir.setText(caseObj.getInputFileDir());
        changeablesList.addChangeable(inputDir);
        this.outputDir = new TextField("outputdir", 20);
        outputDir.setText(caseObj.getOutputFileDir());
        changeablesList.addChangeable(outputDir);
    }

    public void display(JComponent container, EmfSession session){
        this.session=session; 
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFolders(),BorderLayout.NORTH);
        container.add(panel);
    }
    
    private JPanel createFolders(){
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", getFolderChooserPanel(inputDir,
                "Select the base Input Folder for the Case"), panel);
        layoutGenerator.addLabelWidgetPair("Output Job Scripts Folder:", getFolderChooserPanel(outputDir,
        "Select the base Output Job Scripts Folder for the Case"), panel);
        layoutGenerator.addLabelWidgetPair("Sens. Case Description:", description(), panel);
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 50, // initialX, initialY
                5, 20);// xPad, yPad

        return panel;    
    }
    
    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }
    
    private ScrollableComponent description() {
        description = new TextArea("description", caseObj.getDescription(), 25, 5);
        changeablesList.addChangeable(description);
 
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        //descScrollableTextArea.setPreferredSize(new Dimension(255,80));
        return descScrollableTextArea;
    }

    private void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(parentConsole.getSession(), initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            if (title.toLowerCase().contains("output"))
                outputDir.setText(file.getAbsolutePath());
            else 
                inputDir.setText(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }    

    public void setFields() {
        caseObj.setInputFileDir(inputDir.getText().trim());
        caseObj.setOutputFileDir(outputDir.getText().trim());
        caseObj.setDescription(description.getText().trim());
    }
    
}
