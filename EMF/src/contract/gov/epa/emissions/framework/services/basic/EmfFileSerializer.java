package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.io.IOException;

public class EmfFileSerializer {

    public EmfFileSerializer() {
        //
    }
    
    public static EmfFileInfo convert(File file) throws IOException {
        EmfFileInfo fileInfo = new EmfFileInfo();
        
        fileInfo.setPathSeparator(File.pathSeparator);
        fileInfo.setPathSeparatorChar(File.pathSeparatorChar);
        fileInfo.setSeparator(File.separator);
        fileInfo.setSeparatorChar(File.separatorChar);
        fileInfo.setAbsolute(file.isAbsolute());
        fileInfo.setAbsolutePath(file.getAbsolutePath());
        fileInfo.setCanExecute(file.canExecute());
        fileInfo.setCanonicalPath(file.getCanonicalPath());
        fileInfo.setCanRead(file.canRead());
        fileInfo.setCanWrite(file.canWrite());
        fileInfo.setDirectory(file.isDirectory());
        fileInfo.setExists(file.exists());
        fileInfo.setFile(file.isFile());
        fileInfo.setFreeSpace(file.getFreeSpace());
        fileInfo.setHashCode(file.hashCode());
        fileInfo.setHidden(file.isHidden());
        fileInfo.setInternalString(file.toString());
        fileInfo.setLastModified(file.lastModified());
        fileInfo.setLength(file.length());
        fileInfo.setName(file.getName());
        fileInfo.setParent(file.getParent());
        fileInfo.setPath(file.getPath());
        fileInfo.setTotalSpace(file.getTotalSpace());
        fileInfo.setUsableSpace(file.getUsableSpace());
        
        return fileInfo;
    }
    
    public static File convert(EmfFileInfo fileInfo) {
        return new EmfFile(fileInfo);
    }
}
