package gov.epa.emissions.framework.services.basic;

import java.io.File;

public class EmfFile extends File {
    @SuppressWarnings("hiding")
    public static String pathSeparator = File.pathSeparator;
   
    @SuppressWarnings("hiding")
    public static char pathSeparatorChar = File.pathSeparatorChar;

    @SuppressWarnings("hiding")
    public static String separator = File.separator;

    @SuppressWarnings("hiding")
    public static char separatorChar = File.separatorChar; 

    private EmfFileInfo fileInfo;

    public EmfFile(EmfFileInfo fileInfo) {
        super(fileInfo.getAbsolutePath());
        this.fileInfo = fileInfo;
        this.pathSeparator = fileInfo.getPathSeparator();
        this.pathSeparatorChar = (char)fileInfo.getPathSeparatorChar();
        this.separator = fileInfo.getSeparator();
        this.separatorChar = (char)fileInfo.getSeparatorChar();
    }
    
    public EmfFile(String filePath, EmfFileInfo fileInfo) {
        super(fileInfo.getAbsolutePath());
        String newPath = filePath;
        if  ((filePath.lastIndexOf('\\')==filePath.length()-1) && (filePath.length()>3))
            newPath=filePath.substring(0,filePath.length()-1);
        
        this.fileInfo = fileInfo;
        this.fileInfo.setAbsolutePath(newPath);
        String parentString=filePath.substring(0,newPath.lastIndexOf('\\')+1);
        if (parentString.equals(newPath)) parentString=null;
        this.fileInfo.setParent(parentString);
        if (this.fileInfo.getParent() != null) 
        {
            if (this.fileInfo.getParent().equals(filePath))
               this.fileInfo.setParent(null);
        }
        this.pathSeparator = fileInfo.getPathSeparator();
        this.pathSeparatorChar = (char)fileInfo.getPathSeparatorChar();
        this.separator = fileInfo.getSeparator();
        this.separatorChar = (char)fileInfo.getSeparatorChar();
    }

    
    public String getName() {
        return fileInfo.getName();
    }
    
    public String getParent() {
        return fileInfo.getParent();
    }

    public File getParentFile() {
        if (fileInfo.getParent()== null)
            return null;
       return new EmfFile(fileInfo.getParent(), fileInfo);
    }
    
    public String getPath() {
        return fileInfo.getPath();
    }
    
    public String getAbsolutePath() {
        return fileInfo.getAbsolutePath();
    }
    
    public File getAbsoluteFile() {
        return new EmfFile(fileInfo.getAbsolutePath(), fileInfo);
    }
    
    public String getCanonicalPath() {
        return fileInfo.getCanonicalPath();
    }
    
    public File getCanonicalFile() {
        return new EmfFile(fileInfo.getCanonicalPath(), fileInfo);
    }
    
    public long getFreeSpace() {
        return fileInfo.getFreeSpace();
    }
    
    public long getTotalSpace() {
        return fileInfo.getTotalSpace();
    }
    
    public long lastModified() {
        return fileInfo.getLastModified();
    }
    
    public long length() {
        return fileInfo.getLength();
    }
    
    public long getUsableSpace() {
        return fileInfo.getUsableSpace();
    }
    
    public boolean isDirectory() {
        return fileInfo.isDirectory();
    }
    
    public boolean  isFile() {
        return fileInfo.isFile();
    }
    public boolean exists() {
        return fileInfo.isExists();
    }
    
    public boolean isAbsolute() {
        return fileInfo.isAbsolute();
    }
    
    public boolean isHidden() {
        return fileInfo.isHidden();
    }
    
    public boolean canExecute() {
        return fileInfo.canExecute();
    }
    
    public boolean canRead() {
        return fileInfo.canRead();
    }
    
    public boolean canWrite() {
        return fileInfo.canWrite();
    }
    
    public int hashCode() {
        return fileInfo.getHashCode();
    }
    
    public String toString() {
        return fileInfo.toString();
    }

}
