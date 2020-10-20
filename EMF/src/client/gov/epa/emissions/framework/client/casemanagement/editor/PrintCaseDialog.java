package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class PrintCaseDialog extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private JTextField serverfolder;
    
    private JTextField localfolder;

    private PrintCasePresenter presenter;

    private JButton okButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;
    
    private boolean serverFolderExist;
    
    private boolean localFolderExist;

    public PrintCaseDialog(String title, Component container, EmfConsole parentConsole, EmfSession session) {
        super(parentConsole);
        super.setTitle(title);
        super.setLocation(ScreenUtils.getCascadedLocation(container, container.getLocation(), 300, 300));
        super.setModal(true);

        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
        this.getContentPane().add(createLayout());
    }
    
    public void display() {
        this.pack();
        this.setVisible(true);
    }

    public void observe(PrintCasePresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        messagePanel.setMessage("Optionally select server folder, local folder or both. ");
        panel.add(createServerFolderPanel());
        panel.add(createLocalFolderPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createServerFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        serverfolder = new JTextField(40);
        serverfolder.setName("serverfolder");
        Button serverButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectFolder(serverfolder);
            }
        });
        Icon icon = new ImageResources().open("Open a folder");
        serverButton.setIcon(icon);

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(serverfolder, BorderLayout.LINE_START);
        folderPanel.add(serverButton, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Server Folder", folderPanel, panel);


        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    private JPanel createLocalFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        localfolder = new JTextField(40);
        localfolder.setName("localfolder");
        Button localButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectLocalFolder(localfolder);
            }
        });
        Icon icon = new ImageResources().open("Open a folder");
        localButton.setIcon(icon);

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(localfolder, BorderLayout.LINE_START);
        folderPanel.add(localButton, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Local Folder", folderPanel, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                10, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        okButton = new Button("OK", printCase());
        okButton.setMnemonic('O');
        container.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new CancelButton(cancelAction());
        container.add(cancelButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private Action printCase() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                clearMessagePanel();
                
                try {
                    checkFolderField();
                    if (serverFolderExist)
                        presenter.printCase(serverfolder.getText());
                    if (localFolderExist)
                        presenter.printLocalCase(localfolder.getText());                  
                    dispose();
                } catch (Exception e1) {
                    messagePanel.clear();
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){ 
                dispose();
            }
        };
    }
    
    private void clearMessagePanel() {
        messagePanel.clear();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null && serverfolder != null)
            serverfolder.setText(mostRecentUsedFolder);
    }

    private void selectFolder(JTextField folder) {
        
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(parentConsole.getSession(), initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder for exported case settings");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }
    
    private void selectLocalFolder(JTextField folder) {
        JFileChooser chooser;
        File file = new File(folder.getText());

        if (file.isDirectory()) {
            chooser = new JFileChooser(file);
        } else {
            chooser = new JFileChooser("C:\\");
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Please select a folder for local export.  ");

        int option = chooser.showDialog(this, "Select");
        if (option == JFileChooser.APPROVE_OPTION) {
            folder.setText("" + chooser.getSelectedFile());
        }
    }

    
    private void checkFolderField() throws EmfException {
        String serverSide = serverfolder.getText();
        String localSide = localfolder.getText();
        
        if ( (serverSide == null || serverSide.trim().isEmpty() || serverSide.trim().length() == 1)
              && ( localSide == null || localSide.trim().isEmpty() || localSide.trim().length() == 1 ))
            throw new EmfException("Please specify a valid server or local folder.");
        
        if (serverSide != null && !serverSide.trim().isEmpty() && serverSide.trim().length() > 1  ){
            if (serverSide.contains("/home/"))
                throw new EmfException("The EMF (tomcat user) cannot export data into a home directory.");
            
            if ( serverSide.charAt(0) != '/' && serverSide.charAt(1) != ':' )
                throw new EmfException("Specified server folder is not in a right format (ex. C:\\, /home, etc.).");

            if ( serverSide.charAt(0) != '/' && !Character.isLetter(serverSide.charAt(0)))
                throw new EmfException("Specified server folder is not in a right format (ex. C:\\).");
            serverFolderExist = true;
        }
        else 
            serverFolderExist = false;
        if (localSide != null && !localSide.trim().isEmpty() && localSide.trim().length() > 1 ){
            if (localSide.charAt(0) != '/' && localSide.charAt(1) != ':' )
                throw new EmfException("Specified local folder is not in a right format (ex. C:\\, /home, etc.).");

            if (localSide.charAt(0) != '/' && !Character.isLetter(localSide.charAt(0)))
                throw new EmfException("Specified local folder is not in a right format (ex. C:\\).");
            localFolderExist = true;
        }
        else
            localFolderExist = false;
    }
}
