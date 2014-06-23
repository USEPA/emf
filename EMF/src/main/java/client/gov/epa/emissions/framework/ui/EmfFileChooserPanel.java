package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class EmfFileChooserPanel extends JPanel implements Runnable {

    private volatile Thread getFilesThread;

    private EmfFileSystemView fsv;

    private EmfFileInfo currentDir;

    // private EmfFileInfo[] selectedFile;

    private JPanel filePanel;

    private JTextField folder;

    private JTextField subfolder;

    private EditableComboBox filePattern;

    private JList subdirsList;

    private boolean dirOnly;

    private JPanel subdirsPanel;

    private Window window;

    private SingleLineMessagePanel messagePanel;

    private Component parent;

    private EmfFileTableData tableData;

    private EmfTableModel model;

    private SortFilterSelectModel selectModel;

    private String lastFilter = "*";

    private List<String> curFilterList;

    private final String[] filters = { "*", "*.*", "*.txt", "*.ncf" };

    public EmfFileChooserPanel(Component parent, EmfFileSystemView fsv, EmfFileInfo initialFile, boolean dirOnly,
            Window window) {
        this.fsv = fsv;
        this.currentDir = initialFile;
        this.dirOnly = dirOnly;
        this.messagePanel = new SingleLineMessagePanel();
        this.parent = parent;
        this.window = window;
        this.curFilterList = new ArrayList<String>();
        this.curFilterList.addAll(Arrays.asList(filters));
        if (!dirOnly)
            this.curFilterList.add(EmptyStrings.create(40));

        this.getFilesThread = new Thread(this);

        display(new EmfFileInfo[0], new EmfFileInfo[0]);
        getFilesThread.start();
    }

    public void display(EmfFileInfo[] subdirs, EmfFileInfo[] files) {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(this.messagePanel);
        add(upperPanel());
        add(fileListPanels(subdirs, files));

        // Make sub directory list get the focus whenever frame is activated.
        window.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                subdirsList.requestFocusInWindow();
            }
        });

        if (dirOnly){
            setPreferredSize(new Dimension(600, 350));
            setMinimumSize(new Dimension(600, 350));
        }
        else{
            setPreferredSize(new Dimension(800, 440));
            setMinimumSize(new Dimension(800, 440));
        }

        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    }

    private JPanel fileListPanels(EmfFileInfo[] subdirs, EmfFileInfo[] files) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        subdirsPanel = subdirPanel(subdirs);

        if (dirOnly) {
            container.add(subdirsPanel);
        } else {
            container.add(subdirsPanel);
            container.add(new JLabel("  ")); // to fill out some space in between
            container.add(filesPanel(files));
        }

        return container;
    }

    public JPanel upperPanel() {
        JPanel panel = new JPanel();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

        createDirField();
        JPanel chooser = new JPanel(new BorderLayout(10, 10));
        chooser.add(new JLabel("Folder     "),BorderLayout.WEST);
        chooser.add(folder);
        mainPanel.add(chooser);
        mainPanel.add(Box.createVerticalStrut(10));
        
        if (!dirOnly) {
            createPatternBox();
            JPanel apply = new JPanel(new BorderLayout(10,10));
            apply.add(new JLabel("Pattern   "),BorderLayout.WEST);
            apply.add(filePattern);
            mainPanel.add(apply);
            mainPanel.add(Box.createVerticalStrut(10));
        }

        if (dirOnly)  {
            mainPanel.add(createNewSubfolderField());
            mainPanel.add(Box.createVerticalStrut(10));
        }

        panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,20));
        panel.setLayout(new BorderLayout(10,10));
        panel.add(mainPanel,BorderLayout.NORTH);
        return panel;
    }

    private void createDirField() {
        if (dirOnly)
            folder = new JTextField(currentDir.getAbsolutePath(), 40);
        else
            folder = new JTextField(currentDir.getAbsolutePath(), 60);

        folder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String newvalue = ((JTextField) evt.getSource()).getText();
                EmfFileInfo fileInfo = new EmfFileInfo(newvalue, true, true);
                updateDirSelections(fileInfo);
            }

        });
    }

    private JPanel createNewSubfolderField() {
        JPanel panel = new JPanel(new BorderLayout(10,10));

        subfolder = new JTextField("", 40);
        panel.add(new JLabel("New Subfolder:"), BorderLayout.WEST);
        subfolder.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    subfolder.setText("");
            }

            public void mouseEntered(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e) {
                // NOTE Auto-generated method stub

            }
        });

        Button create = new Button("Create", createSubdir());
        create.setMnemonic('C');
        panel.add(subfolder);
        panel.add(create, BorderLayout.EAST);
        
        return panel;
    }

    private Action createSubdir() {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                try {
                    EmfFileInfo newFolder = fsv.createNewFolder(currentDir.getAbsolutePath(), subfolder.getText());
                    subfolder.setText("");
                    updateDirSelections(newFolder);
                } catch (Exception exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }

        };
    }

    private void createPatternBox() {
        filePattern = new EditableComboBox(curFilterList.toArray());
        filePattern.setSelectedIndex(getFilterIndex(lastFilter));
        filePattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object pat = ((EditableComboBox) e.getSource()).getSelectedItem();
                String cur = pat.toString();
                if (!dirOnly && !cur.equalsIgnoreCase(lastFilter)) {
                    lastFilter = cur;

                    if (!contains(pat))
                        curFilterList.add(0, pat.toString());

                    updateDirSelections(currentDir);
                }
            }

        });
    }

    private int getFilterIndex(String pat) {
        return curFilterList.indexOf(pat);
    }

    private boolean contains(Object pat) {
        return curFilterList.contains(pat);
    }

    private JPanel subdirPanel(EmfFileInfo[] dirs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Subfolders:"), BorderLayout.NORTH);
        panel.add(subdirListWidgit(dirs));
        panel.setPreferredSize(new Dimension(150, 250));

        return panel;
    }

    private JPanel filesPanel(EmfFileInfo[] files) {
        filePanel = new JPanel();

        filePanel.setLayout(new BorderLayout());
        filePanel.add(new JLabel("Files:"), BorderLayout.NORTH);

        // Button getFiles = new Button("Get Files", getFilesAction());
        // getFiles.setMnemonic('G');
        // JPanel getFilesPanel = new JPanel(new FlowLayout());
        // getFilesPanel.add(getFiles);
        JPanel middlePanel = new JPanel(new BorderLayout(2, 5));
        middlePanel.add(createFilePanel(files), BorderLayout.CENTER);
        // middlePanel.add(getFilesPanel, BorderLayout.SOUTH);
        middlePanel.setBorder(BorderFactory.createEtchedBorder());

        filePanel.add(middlePanel, BorderLayout.CENTER);

        return filePanel;
    }

    private JPanel createFilePanel(EmfFileInfo[] files) {
        EmfFileInfo[] infos = (files != null) ? files : fsv.getFiles(currentDir, lastFilter);
        tableData = new EmfFileTableData(infos);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
        return createSortFilterPanel(parent);
        // return new SortFilterSelectionPanel(parent, selectModel);
    }

    private JPanel createSortFilterPanel(Component parent) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parent, selectModel);
        sortFilterPanel.sort(sortCriteria());
        return sortFilterPanel;
        // JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        // sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        // return scrollPane;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { false });
    }

    private JScrollPane subdirListWidgit(EmfFileInfo[] files) {
        List<EmfFileInfo> filesList = new ArrayList<EmfFileInfo>();
        filesList.addAll(Arrays.asList(files));
        Collections.sort(filesList);
        DefaultComboBoxModel model = new DefaultComboBoxModel(filesList.toArray());

        subdirsList = new JList();
        subdirsList.setModel(model);
        subdirsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subdirsList.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        subdirsList.setSelectedIndex(0);
        subdirsList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelectedDir(e);
                }
            }

            public void keyReleased(KeyEvent e) {
                //
            }

            public void keyTyped(KeyEvent e) {
                //
            }
        });

        subdirsList.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedDir(e);
                }

            }

            public void mouseEntered(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e) {
                // NOTE Auto-generated method stub

            }

        });

        return new JScrollPane(subdirsList);
    }

    private void openSelectedDir(InputEvent e) {
        Object source = e.getSource();
        EmfFileInfo fileInfo = (EmfFileInfo) ((JList) source).getSelectedValue();

        if (fileInfo == null || fileInfo.getName() == null || fileInfo.getName().equals("."))
            return;

        updateDirSelections(fileInfo);
    }
    
    public void setMsg( String msg) {
        messagePanel.setMessage(msg);
    }
    public void setError( String err) {
        messagePanel.setError(err);
    }

    protected void updateDirSelections(EmfFileInfo fileInfo) {
        clearMsg();
        currentDir = fileInfo;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            if (dirOnly) {
                messagePanel.setMessage("Please wait while retrieving all subfolders ...");
                refreshFiles(getAllDirs(currentDir), new EmfFileInfo[0]);
                messagePanel.setMessage("Finished retrieving subfolders.");
            } else {
                messagePanel.setMessage("Please wait while retrieving all subfolders and files ...");
                if (lastFilter.trim().isEmpty())
                    refreshFiles(getAllDirs(currentDir), new EmfFileInfo[0]);
                else
                    refreshFiles(getAllDirs(currentDir), fsv.getFiles(currentDir, lastFilter));
                messagePanel.setMessage("Finished retrieving subfolders and files.");
            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            setFocusOnDirList();
        }
    }

    private EmfFileInfo[] getAllDirs(EmfFileInfo dir) throws EmfException {
        if ((dir.getAbsolutePath().length() == 2) && (dir.getAbsolutePath().charAt(1) == ':'))
            throw new EmfException("Please enter a '\\' after the drive letter (e.g., 'D:\\')");

        EmfFileInfo[] dirs = fsv.getSubdirs(dir);

        folder.setText(dirs[0].getAbsolutePath());
        if (!dirs[0].getAbsolutePath().equals(currentDir.getAbsolutePath()))
            currentDir = dirs[0];

        return dirs;
    }

    // private Action getFilesAction() {
    // return new AbstractAction() {
    // public void actionPerformed(ActionEvent arg0) {
    // getFiles();
    // }
    // };
    // }

    // private void getFiles() {
    // this.selectedFile = fsv.getFiles(this.currentDir, true);
    // refreshFiles(selectedFile);
    // }

    private void refreshFiles(EmfFileInfo[] subdirs, EmfFileInfo[] files) {
        display(subdirs, files);
        this.validate();
    }

    protected void clearMsg() {
        this.messagePanel.clear();
    }

    public EmfFileInfo[] selectedFiles() {
        List selected = selectModel.selected();

        return (EmfFileInfo[]) selected.toArray(new EmfFileInfo[0]);
    }

    public EmfFileInfo selectedDirectory() {
        return this.currentDir;
    }
    
    public void setFocusOnDirList() {
        subdirsList.requestFocusInWindow();
    }
    
    public void setSelectionCurrentDir() {
        EmfFileInfo selected = (EmfFileInfo)subdirsList.getSelectedValue();
        
        if (selected == null)
            return;
        
        String dir = selected.getName();
        
        if (!dir.equals(".") && !dir.equals("..")) {
            this.folder.setText(dir);
            this.currentDir = selected;
        }
    }

    public void run() {
        try {
            updateDirSelections(this.currentDir);
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            messagePanel
                    .setError("Connection to server timed out: " + ((e.getMessage() == null) ? "" : e.getMessage()));
        }
    }
    
    public void refresh()
    {
        //EmfFileInfo fi = this.currentDir;
        //EmfFileInfo fi2 = new EmfFileInfo("C:\\", true);
        updateDirSelections( this.currentDir);
        //updateDirSelections( fi2); // TODO: seems like the service do not update the dir content if currentDir not changed
        //updateDirSelections( fi );
    }
    

}
