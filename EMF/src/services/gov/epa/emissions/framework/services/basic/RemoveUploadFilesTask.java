package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RemoveUploadFilesTask implements Runnable {

    private Log log = LogFactory.getLog(RemoveUploadFilesTask.class);

    private EntityManagerFactory entityManagerFactory;
    private int fileHoursToExpire = 12; //remove files older than 12 hours...

    public RemoveUploadFilesTask() {
        this.entityManagerFactory = JpaEntityManagerFactory.get();;
    }

    public void run() {
        log.info("RemoveUploadFilesTask.run() started");
        try {
            File tempDirectoryObj = new File(getTempDirectory() + File.separatorChar + "upload");

            //get user folders under main download folder
            String[] userFolders = tempDirectoryObj.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    //only get folders
                    return new File(dir, name).isDirectory();
                }
            });
            for (String userFolder : userFolders) {
                File userFolderObj = new File(tempDirectoryObj.getAbsolutePath() + File.separatorChar + userFolder);
                userFolderObj.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        //only get files, not directories (really which there shouldn't be any subdirs here)
                        if (name == null) return false;
                        File userFile = new File(dir, name);
                        if (userFile.isFile() && userFile.lastModified() <= (new Date().getTime() - fileHoursToExpire * 60 * 60 * 1000))
                            userFile.delete();
                        return true;
                    }
                });
            }


        } catch (Exception e) {
            //Important, suppress all errors so process doesn't kill anything...
            logError("Failed to remove file uploads", e);
        } finally {
            //
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private String getTempDirectory() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String tempDirectory = "";
        try {
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", entityManager);

            if (eximTempDir != null) {
                tempDirectory = eximTempDir.getValue();
            }
        } finally {
            entityManager.close();
        }
        return tempDirectory;
    }

}
