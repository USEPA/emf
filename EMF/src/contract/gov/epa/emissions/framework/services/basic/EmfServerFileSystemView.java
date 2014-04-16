package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

public class EmfServerFileSystemView extends FileSystemView {
    
    public EmfServerFileSystemView () {
        super();
    }

    public File createNewFolder(File folder) throws IOException  {
        if (!folder.isDirectory())
            return null;
        
        try {
            File newFolder = new File(folder.getAbsolutePath() + File.separatorChar + "newfolder");
            
            if (newFolder.mkdir()) {
                newFolder.setWritable(true, false);
                return newFolder.getCanonicalFile();
            }
            
            return null;
        } catch (Exception e) {
            throw new IOException("User is not allowed to create a new folder. " + e.getMessage());
        }
    }

}
