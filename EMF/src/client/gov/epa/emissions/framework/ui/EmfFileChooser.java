package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.download.FileDownloadTableCellRenderer;
import gov.epa.emissions.framework.client.download.FileDownloadTableModel;
import gov.epa.emissions.framework.client.upload.UploadTask;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FileDownload;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

public class EmfFileChooser extends JComponent {

    public static final int OPEN_DIALOG = 0;

    public static final int SAVE_DIALOG = 1;

    public static final int CUSTOM_DIALOG = 2;

    public static final int CANCEL_OPTION = 1;

    public static final int APPROVE_OPTION = 0;

    public static final int ERROR_OPTION = -1;

    public static final int FILES_ONLY = 0;

    public static final int DIRECTORIES_ONLY = 1;

    public static final int FILES_AND_DIRECTORIES = 2;

    public static final String CANCEL_SELECTION = "CancelSelection";

    public static final String APPROVE_SELECTION = "ApproveSelection";
    
    private static EmfFileInfo LAST_SELECTED_DIR = null;
    private EmfSession session;

    private EmfFileInfo current;

    private String approveButtonText = "Select";

    private String title = "EMF Folder Chooser";

    private int returnValue = ERROR_OPTION;

    private int dialogType = OPEN_DIALOG;
    
    private JDialog dialog = null;

    private EmfFileChooserPanel chooserPanel;

    private boolean dirOnly = true;

    private EmfFileSystemView fsv;

    private SingleLineMessagePanel messagePanel;
    private FileDownloadTableModel fileDownloadTableModel;
    private JTable table;
    private UploadTask task;
    private JFileChooser fileChooser;
    private JTabbedPane tabbedPane;
    private int maxFileUploadSize = 0;

//    public EmfFileChooser(EmfFileInfo dir, EmfFileSystemView fsv) {
//        this.fsv = fsv;
//        this.current = dir;
//        if ((dir.getAbsolutePath().trim().equalsIgnoreCase("")) && (LAST_SELECTED_DIR != null))
//            this.current = LAST_SELECTED_DIR;
//    }
//
//    public EmfFileChooser(EmfFileSystemView fsv) {
//        this(fsv.getDefaultDir(), fsv);
//    }
//
    public EmfFileChooser(EmfSession session, EmfFileInfo dir, EmfFileSystemView fsv) {
        this.session = session;
        this.fsv = fsv;
        this.current = dir;
        if ((dir.getAbsolutePath().trim().equalsIgnoreCase("")) && (LAST_SELECTED_DIR != null))
            this.current = LAST_SELECTED_DIR;
        this.maxFileUploadSize = getUploadFileMaxSize();
    }

    public void setDirectoryOnlyMode() {
        this.dirOnly = true;
    }

    public void setDirectoryAndFileMode() {
        this.dirOnly = false;
    }

    public EmfFileInfo getSelectedDir() {
        if (this.chooserPanel == null)
            return null;

        if (tabbedPane.getSelectedIndex() == 0) {        //SERVER FILES
            return this.chooserPanel.selectedDirectory();
        } else if (tabbedPane.getSelectedIndex() == 1) { //LOCAL FILES
            EmfFileInfo fileInfo = new EmfFileInfo();
            String tempDirectory = getTempDirectory();//"/upload"
            if (tempDirectory.lastIndexOf("/") > 0 ) {   //assume linux server
                tempDirectory += "/upload/" + session.user().getUsername();
            } else {
                tempDirectory += (tempDirectory.lastIndexOf("/") != tempDirectory.length() - 1 ? File.separator : "")
                        + "upload" + File.separator + session.user().getUsername();
            }
            fileInfo = new EmfFileInfo(tempDirectory, true);
            fileInfo.setAbsolutePath(tempDirectory);
            return fileInfo;
        }
        return null;
    }

    public EmfFileInfo[] getSelectedFiles() {
        if (tabbedPane.getSelectedIndex() == 0) {        //SERVER FILES
            return this.chooserPanel.selectedFiles();
        } else if (tabbedPane.getSelectedIndex() == 1) { //LOCAL FILES
            //translate to EmfFileInfo model
            EmfFileInfo[] files = new EmfFileInfo[fileDownloadTableModel.getFileDownloads().size()];
            int i = 0;
            String tempDirectory = getTempDirectory() + File.separator + "upload" + File.separator + session.user().getUsername();
            for (FileDownload fileDownload : fileDownloadTableModel.getFileDownloads()) {
                EmfFileInfo fileInfo = files[i];
                fileInfo = new EmfFileInfo(fileDownload.getAbsolutePath(), true);
                String fileName = fileDownload.getFileName();
                if (fileName.lastIndexOf("/") > 0 ) {   //assume linux server
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
                    fileInfo.setName(fileName);
                    fileInfo.setAbsolutePath(tempDirectory + "/" + fileName);
                } else {
                    fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.length());
                    fileInfo.setName(fileName);
                    fileInfo.setAbsolutePath(tempDirectory + File.separator + fileName);
                }
                files[i] = fileInfo;
                i++;
            }

            return files;
        }
        return new EmfFileInfo[0];
    }

    public void setApproveButtonText(String approveButtonText) {
        if (this.approveButtonText == approveButtonText) {
            return;
        }
        String oldValue = this.approveButtonText;
        this.approveButtonText = approveButtonText;
        firePropertyChange("ApproveButtonTextChangedProperty", oldValue, approveButtonText);
    }

    public int showDialog(Component parent, String approveButtonText) {
        try {
            if (approveButtonText != null) {
                setApproveButtonText(approveButtonText);
                setDialogType(CUSTOM_DIALOG);
            }

            dialog = createDialog(parent);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    returnValue = CANCEL_OPTION;
                }
            });
            returnValue = ERROR_OPTION;

            dialog.setVisible(true);
            firePropertyChange("EmfFileChooserDialogIsClosingProperty", dialog, null);

            //return returnValue;
        } catch ( ClassCastException e) {
            chooserPanel.setError( "ClassCastException when choosing file: " + e.getMessage());
        } catch ( Exception e) {
            chooserPanel.setError( "Exception when choosing file: " + e.getMessage());
        }
        return returnValue;
    }

    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = new JDialog((Frame) parent, title, true);
        dialog.setComponentOrientation(this.getComponentOrientation());

        this.messagePanel = new SingleLineMessagePanel();
        this.chooserPanel = new EmfFileChooserPanel(parent, fsv, current, dirOnly, dialog, this.messagePanel);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.messagePanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        JPanel serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));

        serverPanel.add(this.chooserPanel);

        tabbedPane.addTab("Server", serverPanel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JPanel localPanel = new JPanel();
        localPanel.setLayout(new BoxLayout(localPanel, BoxLayout.Y_AXIS));
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setApproveButtonText("Upload");
        fileChooser.setControlButtonsAreShown(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                if (action.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
                {
                    System.out.printf("CancelSelection\n");
//                    this.setVisible(false);
//                    this.dispose();
                }
                if (action.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
                {
                    for (File file : fileChooser.getSelectedFiles()) {
                        System.out.printf("Selected file:" + file.getAbsolutePath() + " \n");
                    }
//                    this.setVisible(false);
//                    this.dispose();
                }
            }
        });
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        InputMap map = fileChooser.getInputMap(JFileChooser.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        map.put(enter, "approveSelection");

        localPanel.add(fileChooser);
        JButton buttonUpload = new JButton("Upload");
        buttonUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                buttonUploadActionPerformed(event);
            }
        });
        buttonUpload.setMnemonic(KeyEvent.VK_U);
        JPanel uploadFileButtonPanel = new JPanel(new BorderLayout());
        uploadFileButtonPanel.add(buttonUpload, BorderLayout.EAST);
        localPanel.add(uploadFileButtonPanel);
        JPanel uploadFilesPanel = new JPanel(new BorderLayout());
        uploadFilesPanel.add(new JLabel("Uploaded Files:"), BorderLayout.NORTH);
        uploadFilesPanel.add(uploadFilesListWidgit());
        uploadFilesPanel.setPreferredSize(new Dimension(150, 250));
        localPanel.add(uploadFilesPanel);

        tabbedPane.addTab("Local", localPanel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        contentPane.add((dirOnly ? this.chooserPanel : tabbedPane), BorderLayout.CENTER);
        contentPane.add(buttonsPanel(), BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }


    private JScrollPane uploadFilesListWidgit() {

        fileDownloadTableModel = new FileDownloadTableModel();
        fileDownloadTableModel.setHeader(new String[] { "Uploaded Files" });
        table = new JTable(fileDownloadTableModel);
//            new MultiLineTable(fileDownloadTableModel);
        table.setName("fileDownloads");
//        table.setRowHeight(50);
        //table.setDefaultRenderer(Object.class, new TextAreaTableCellRenderer());

        table.setCellSelectionEnabled(true);
//        MultiLineCellRenderer multiLineCR = new MultiLineCellRenderer();
        FileDownloadTableCellRenderer progressBarTableCellRenderer = new FileDownloadTableCellRenderer();
//        table.getColumnModel().getColumn(0).setCellRenderer(multiLineCR);
//        table.getColumnModel().getColumn(1).setCellRenderer(multiLineCR);
//        table.getColumnModel().getColumn(2).setCellRenderer(multiLineCR);
        table.getColumnModel().getColumn(0).setCellRenderer(progressBarTableCellRenderer);

//        setColumnWidths(table.getColumnModel());
//        table.setPreferredScrollableViewportSize(this.getSize());

        return new JScrollPane(table);
    }

    /**
     * handle click event of the Upload button
     */
    private void buttonUploadActionPerformed(ActionEvent event) {
        String uploadURL = session.serviceLocator().getBaseUrl().replaceFirst("/services","/uploadFile");
        //final FileDownload[] fileDownloads = new FileDownload[fileChooser.getSelectedFiles().length];
        List<FileDownload> fileDownloads = new ArrayList<FileDownload>();
        for (File uploadFile : fileChooser.getSelectedFiles()) {

            if (uploadFile.length() >= maxFileUploadSize) {
                JOptionPane.showMessageDialog(null, "File to large (max size is " + (maxFileUploadSize / (1024 * 1024))
                                + " MB) to upload: " + uploadFile.getName(),
                                "File Upload Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
            
            if (uploadFile.getName().contains(" ")) {
                JOptionPane.showMessageDialog(null, "Filenames can't contain spaces: " + uploadFile.getName(),
                        "File Upload Error", JOptionPane.ERROR_MESSAGE);
                break;
            }

            FileDownload fileDownload = new FileDownload();
            fileDownload.setUrl(uploadFile.getPath());
            fileDownload.setAbsolutePath(uploadFile.getPath());

            Path path = Paths.get(uploadFile.getPath()); // the path to the file
            BasicFileAttributes attributes =
                    null;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
                FileTime creationTime = attributes.lastModifiedTime();
                fileDownload.setTimestamp(new Date(creationTime.toMillis()));
            } catch (IOException e) {
                e.printStackTrace();
                fileDownload.setTimestamp(new Date());
            }
            fileDownload.setSize(uploadFile.length());
            fileDownloads.add(fileDownload);
        }

        fileDownloadTableModel.refresh(fileDownloads.toArray(new FileDownload[0]));
        //make sure row height is consistent
        for (int j = 0; j < fileDownloadTableModel.getRowCount(); j++) {
            table.setRowHeight(j, 50);
        }

        for (final FileDownload fileDownload : fileDownloads) {
            try {
                fileDownload.setProgress(0);

                File uploadFile = new File(fileDownload.getAbsolutePath());
                UploadTask task = new UploadTask(session.user().getUsername(), uploadURL, uploadFile);
                task.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress" == evt.getPropertyName()) {
                            int progress = (Integer) evt.getNewValue();
                            fileDownload.setProgress(progress);
                            table.repaint();
                        }
                    }
                });
                task.execute();
                uploadFile = null;
                task = null;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error executing upload task: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /**
     * Update the progress bar's state whenever the progress of upload changes.
     */
    public void propertyChange(JProgressBar progressBar, PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }


    private void setDialogType(int dialogType) {
        if (this.dialogType == dialogType) {
            return;
        }
        if (!(dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG || dialogType == CUSTOM_DIALOG)) {
            throw new IllegalArgumentException("Incorrect Dialog Type: " + dialogType);
        }
        int oldValue = this.dialogType;
        this.dialogType = dialogType;
        if (dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG) {
            setApproveButtonText(null);
        }
        firePropertyChange("DialogTypeChangedProperty", oldValue, dialogType);
    }

    public void setTitle(String newTitle) {
        if (this.title.equals(newTitle))
            return;

        String oldValue = this.title;
        this.title = newTitle;
        
        if (dialog != null)
            dialog.setTitle(newTitle);
        
        firePropertyChange("DialogTitleChangedProperty", oldValue, newTitle);
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        // this.approveButton = new Button("OK", selectAction());
        // panel.add(approveButton);
        panel.add(new OKButton(selectAction()));
        panel.add(new CancelButton(cancelAction()));
        
        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    //chooserPanel.setSelectionCurrentDir();
                    //LAST_SELECTED_DIR = chooserPanel.selectedDirectory();
                    chooserPanel.refresh();
                } catch (Exception e) {
                    //showError(e.getMessage());
                }
            }
        });
        refresh.setToolTipText("Refresh the content of the dir");
        refresh.setMnemonic(KeyEvent.VK_R);
        panel.add(refresh); 

        JPanel container = new JPanel(new BorderLayout(0, 20));
        container.add(panel, BorderLayout.CENTER);

        return container;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
                returnValue = CANCEL_OPTION;
            }
        };
    }
    
    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                chooserPanel.setSelectionCurrentDir();
                LAST_SELECTED_DIR = chooserPanel.selectedDirectory();
                closeDialog();
                returnValue = APPROVE_OPTION;
            }
        };
    }
    
    private Action refreshAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                chooserPanel.setSelectionCurrentDir();
                LAST_SELECTED_DIR = chooserPanel.selectedDirectory();
                // TODO: refresh related action
            }
        };
    }    

    private void closeDialog() {
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
    }

    private String getTempDirectory() {
        try {
            return session.userService().getPropertyValue(EmfProperty.IMPORT_EXPORT_TEMP_DIR);
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return null;
    }


    private int getUploadFileMaxSize() {
        try {
            return 1024 * 1024 * Integer.parseInt(session.userService().getPropertyValue(EmfProperty.MAX_FILE_UPLOAD_SIZE));
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
