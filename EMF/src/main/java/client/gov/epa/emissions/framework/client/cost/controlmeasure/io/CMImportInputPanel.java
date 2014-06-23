package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class CMImportInputPanel extends JPanel {

    private MessagePanel messagePanel;

    private CMImportPresenter presenter = null;

    private TextField folder;
    
    private TextField pattern;

    private TextArea filenames;

    private TextArea importStatusTextArea;
    
    private EmfSession session;
    
    private EmfConsole parentConsole;

    private static String lastFolder = null;
    
    private boolean superUser = false;
    private JCheckBox chkPurge;
    private JList sectorListBox; 
    private Sector[] sectors;
    private Sector[] allSectors;

    public CMImportInputPanel(EmfConsole parentConsole, MessagePanel messagePanel, EmfSession session) {
        this.messagePanel = messagePanel;
        this.session = session;
        this.parentConsole = parentConsole;

        //initialize();
    }

    private void initialize() {
        
        int width = 40;
        
        JPanel mainPanel = new JPanel();
        //mainPanel.setLayout(new SpringLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        
        //SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JPanel chooser = new JPanel(new BorderLayout(10,10));
        folder = new TextField("folder", width);
        chooser.add(new JLabel("Folder     "),BorderLayout.WEST);
        chooser.add(folder);
        chooser.add(browseFileButton(), BorderLayout.EAST);
        
        //layoutGenerator.addLabelWidgetPair("Folder   ", chooser, mainPanel);

        JPanel apply = new JPanel(new BorderLayout(10,10));
        pattern = new TextField("pattern", width);
        apply.add(new JLabel("Pattern   "),BorderLayout.WEST);
        apply.add(pattern);
        apply.add(applyPatternButton(), BorderLayout.EAST);
        //layoutGenerator.addLabelWidgetPair("Pattern", apply, mainPanel);

        JPanel fileNamesPanel = new JPanel(new BorderLayout(4,10));
        filenames = new TextArea("filenames", "", width, 6);
        JScrollPane fileTextAreaPane = new JScrollPane(filenames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileNamesPanel.add(new JLabel("Filenames"),BorderLayout.WEST);
        fileNamesPanel.add(fileTextAreaPane);
        //layoutGenerator.addLabelWidgetPair("Filenames", fileTextAreaPane, mainPanel);
        
        // JIZHEN detect if this is super user
        this.superUser = this.checkIfSuperUser();        
        JPanel sectorPanel = this.createSectorPanel();

        JPanel statusPanel = new JPanel(new BorderLayout(10,10));
        importStatusTextArea = new TextArea("Import Status", "", width);
        JScrollPane statusTextAreaPane = new JScrollPane(importStatusTextArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        statusPanel.add(new JLabel("Status    "),BorderLayout.WEST);
        statusPanel.add(statusTextAreaPane);
        
        //layoutGenerator.addLabelWidgetPair("Status", statusTextAreaPane, mainPanel);

        // Lay out the panel.
        //layoutGenerator.makeCompactGrid(mainPanel, 4, 2, // rows, cols
        //        10, 10, // initialX, initialY
        //        10, 10);// xPad, yPad
        
        mainPanel.add(chooser);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(apply);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(fileNamesPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        if ( this.superUser) {
            mainPanel.add(sectorPanel);
            mainPanel.add(Box.createVerticalStrut(10));
        }
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        this.setLayout(new BorderLayout(10,10));
        this.add(mainPanel,BorderLayout.NORTH);
        this.add(statusPanel);
     }
    
    private boolean checkIfSuperUser() {
        try {
            User currentUser = session.user();
            String costSUs = presenter.getCoSTSUs();
            //if this is found, then every one is considered an SU (really used for State Installations....)
            if (costSUs.equals("ALL_USERS")) return true;
            StringTokenizer st = new StringTokenizer(costSUs,"|");
            while ( st.hasMoreTokens()) {
                String token = st.nextToken();
                if ( token.equals( currentUser.getUsername())) {
                    return true;
                }
            }
            return false;

        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
            messagePanel.setMessage(e1.getMessage());
            return false;
        }        
    }
    
    private JPanel createSectorPanel() {
        JPanel sectorPanel = null;
        if ( this.superUser) {
            sectorPanel = new JPanel();
            sectorPanel.setLayout(new BoxLayout(sectorPanel, BoxLayout.Y_AXIS));
            sectorPanel.setBorder(BorderFactory.createTitledBorder("Purge Existing Measures By Sectors"));
            
            JPanel chkPanel = new JPanel();
            chkPanel.setLayout(new BorderLayout(10,10));
            JLabel emptyLabel = new JLabel("               ");
            chkPanel.add( emptyLabel, BorderLayout.WEST);
            this.chkPurge = new JCheckBox( "Purge     ");
            chkPanel.add( this.chkPurge);
            
            JPanel secPanel = new JPanel();
            secPanel.setLayout(new BorderLayout(10,10));
            JLabel secLabel = new JLabel("Sector    ");
            secPanel.add( secLabel, BorderLayout.WEST);
            
            if ( this.presenter != null) {
                try {
                    this.sectors = presenter.getDistinctControlMeasureSectors();
                    List<Sector> sectorList = new ArrayList<Sector>();
                    sectorList.add(new Sector("All", "All"));
                    sectorList.addAll( Arrays.asList( sectors));
                    allSectors = sectorList.toArray(new Sector[0]);
                    sectorListBox = new JList( allSectors);
                    sectorListBox.setVisibleRowCount(5);
                    JScrollPane scrollPane = new JScrollPane( sectorListBox);
                    secPanel.add(scrollPane);                     
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                    messagePanel.setMessage(e.getMessage());
                }                
            }
            
            sectorPanel.add(chkPanel);
            sectorPanel.add(Box.createVerticalStrut(10));
            sectorPanel.add(secPanel);
        }
        return sectorPanel;
    }

    private JButton browseFileButton() {
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFile();
            }
        });

        Icon icon = new ImageResources().open("Import a File");
        button.setIcon(icon);

        return button;
    }

    private JButton applyPatternButton() {
        Button button = new Button("Apply Pattern", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFilesFromPattern();
            }
        });

        return button;
    }

    public void register(CMImportPresenter presenter) {
        this.presenter = presenter;
        this.initialize();
    }

    private void selectFilesFromPattern() {
        try {
            populateFilenamesFiled(presenter.getFilesFromPatten(folder.getText(), pattern.getText()));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void populateFilenamesFiled(String[] files) {
        String text = "";
        for (int i = 0; i < files.length; i++) {
            text += files[i] + System.getProperty("line.separator");
        }
        filenames.setText(text);
    }

    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select files containing control measures to import");
        chooser.setDirectoryAndFileMode();
        
        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        EmfFileInfo dir = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        
        if (dir != null && !dir.getAbsolutePath().equals(lastFolder)) {
            folder.setText(dir.getAbsolutePath());
            lastFolder = dir.getAbsolutePath();
            clearfilenames();
        }
        
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            String[] fileNames = new String[files.length];
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = files[i].getName();
            }
            populateFilenamesFiled(fileNames);
        } else {
            singleFile(files[0]);
        }
    }

    private void clearfilenames() {
        filenames.setText("");
    }
    
    private void singleFile(EmfFileInfo file) {
        filenames.setText(file.getName());
    }

    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder);
    }

    public String folder() {
        return folder.getText();
    }

    public String[] files() {
        List names = new ArrayList();
        int lines = filenames.getLineCount();
        try {
            for (int i = 0; i < lines; i++) {
                int start = filenames.getLineStartOffset(i);
                int end = filenames.getLineEndOffset(i);
                names.add(filenames.getText(start, end - start));
            }
            names = removeEmptyFileNams(names);

        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        return (String[]) names.toArray(new String[0]);
    }

    private List removeEmptyFileNams(List files) {
        List nonEmptyList = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.get(i);
            if (file.trim().length() != 0) {
                nonEmptyList.add(file.trim());
            }
        }
        return nonEmptyList;
    }

    public void setStartImportMessage(String message) {
        importStatusTextArea.clear();
        messagePanel.setMessage(message);
    }

    private void clear() {
        messagePanel.clear();
    }

    public void addStatusMessage(String messages) {
        importStatusTextArea.append(messages);
    }
    
    // will be used by the CMImportWindow
    
    public boolean isSuperUser() {
        return this.superUser;
    }
    
    public boolean toPurge() {
        return this.superUser && this.chkPurge.isSelected();
    }
    
    public int[] getSectorIDs(){
        int [] IDs = null;
        if ( this.toPurge()) {
            int [] inx = sectorListBox.getSelectedIndices();
            if ( inx.length == 0){
                messagePanel.setError("Please select Sector(s).");
            } else {
                IDs = new int[ inx.length];
                for ( int i = 0; i < inx.length; i++) {
                    //this means the all item was selected...
                    if (inx[i] == 0)
                        return new int[] {};
                    IDs[i] = this.sectors[inx[i] - 1].getId();
//                    IDs[i] = this.sectors[inx[i]].getId();
                }
            }            
        } else {
            messagePanel.setError("Did not choose to purge Control Measures.");
        }
        return IDs;
    }

}
