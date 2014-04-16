package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.io.Serializable;

public class EmfFileSystemView implements Serializable {

    private DataCommonsService service;

    private EmfFileInfo[] emfRoots;

    public EmfFileSystemView(DataCommonsService service) {
        this.service = service;
    }

    public EmfFileInfo[] getFiles(EmfFileInfo dir, String pattern) {
        try {
            return service.getEmfFileInfos(dir, pattern);
        } catch (Exception e) {
            e.printStackTrace();
            return new EmfFileInfo[0];
        }
    }

    public EmfFileInfo createNewFolder(String folder, String subfolder) throws Exception {
        return service.createNewFolder(folder, subfolder);
    }

    public EmfFileInfo getDefaultDir() {
        try {
            EmfFileInfo defaultDir = service.getDefaultDir();
            return defaultDir;
        } catch (EmfException e) {
            e.printStackTrace();
            return null;
        }
    }

    public EmfFileInfo getHomeDir() {
        try {
            EmfFileInfo homeDir = service.getHomeDir();
            return homeDir;
        } catch (EmfException e) {
            e.printStackTrace();
            return null;
        }
    }

    public EmfFileInfo[] getEmfRoots() {
        try {
            if (emfRoots == null)
                this.emfRoots = service.getRoots();

            return emfRoots;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isRoot(EmfFileInfo file) {
        try {
            boolean retVal;
            if (emfRoots == null)
                retVal = service.isRoot(file);

            retVal = contains(file, emfRoots);

            if (file.getAbsolutePath().equals("/"))
                retVal = true;

            return retVal;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFileSystemRoot(EmfFileInfo file) {
        try {
            boolean retVal;
            retVal = file.getAbsolutePath().equals("/")
                    || ((file.getAbsolutePath().length() == 3) && (file.getAbsolutePath().charAt(1) == ':'));
            return retVal;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDrive(EmfFileInfo file) {
        return isFileSystemRoot(file);
    }

    private boolean contains(EmfFileInfo file, EmfFileInfo[] files) {
        boolean result = false;
        String filepath = file.getAbsolutePath();

        for (int i = 0; i < files.length; i++)
            if (filepath.equals(files[i].getAbsolutePath()))
                result = true;

        return result;
    }

    public EmfFileInfo getChild(EmfFileInfo file, String child) throws EmfException {
        try {
            EmfFileInfo fileInfo = service.getChild(file, child);
            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    public EmfFileInfo[] getSubdirs(EmfFileInfo file) throws EmfException {
        try {
            return service.getSubdirs(file);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    public EmfFileInfo getParentDirectory(EmfFileInfo file) throws EmfException {
        try {
            return service.getParentDirectory(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

}
