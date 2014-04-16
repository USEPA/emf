package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ExternalSourceUpdateWindow extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private TextField folder;
    
    private ManageChangeables changeablesList;

    private ExternalSourceUpdatePresenter presenter;

    private JCheckBox massLoc;
    
    private JButton updateButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public ExternalSourceUpdateWindow(String title, EmfConsole parentConsole, ManageChangeables changeablesList, EmfSession session) {
        super(parentConsole);
        setTitle(title);
        setLocation(ScreenUtils.getCascadedLocation(parentConsole, this.getLocation(), 200, 200));
        setModal(true);

        this.changeablesList = changeablesList;
        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
        this.getContentPane().add(createLayout());
    }
    
    public void display() {
        this.pack();
        this.setVisible(true);
    }

    public void observe(ExternalSourceUpdatePresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createFolderPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        folder = new TextField("updateSoureFolder", 40);
        changeablesList.addChangeable(folder);
        
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        Icon icon = new ImageResources().open("Open a folder");
        button.setIcon(icon);

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(folder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("New Folder", folderPanel, panel);

        JPanel massLocPanel = new JPanel(new BorderLayout());
        massLoc = new JCheckBox("Is the new location in mass storage?", false);
        massLoc.setEnabled(true);
        massLoc.setName("masslocation");
        massLocPanel.add(massLoc, BorderLayout.LINE_START);

        panel.add(new JPanel());// filler
        panel.add(massLocPanel);


        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
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

        updateButton = new Button("Update", update());
        container.add(updateButton);
        getRootPane().setDefaultButton(updateButton);

        JButton cancelButton = new CancelButton(cancelAction());
        container.add(cancelButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private Action update() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                clearMessagePanel();
                
                try {
                    presenter.update(folder.getText(), massLoc.isSelected());
                } catch (Exception e1) {
                    e1.printStackTrace();
                    messagePanel.setError(e1.getMessage());
                }
                
                dispose();
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
        if (mostRecentUsedFolder != null && folder != null)
            folder.setText(mostRecentUsedFolder);
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder containing the external files for the dataset");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }

}
