package gov.epa.emissions.framework.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class FileChooser {

    private JFileChooser chooser;

    private String action;

    private Component parent;
    
    public FileChooser(String action, Component parent) {
        this(action, new File(System.getProperty("user.dir")), parent);
    }

    public FileChooser(String action, File folder, Component parent) {
        this.action = action;
        this.parent = parent;

        chooser = new JFileChooser(folder);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    public FileChooser(String action, FileSystemView fsv, Component parent) {
        this.action = action;
        this.parent = parent;
        
        chooser = new JFileChooser(fsv);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    public void setTitle(String title) {
        chooser.setDialogTitle(title);
    }

    public File[] choose() {
        int result = chooser.showDialog(parent, action);
        return (result == JFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : new File[0];
    }
    
    public void setCurrentDir(String dir) {
        chooser.setCurrentDirectory(new File(dir));
    }
    
    public void resetSelectionMode(int mode) {
        chooser.setFileSelectionMode(mode);
    }
}
