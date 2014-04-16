package gov.epa.emissions.framework.services.basic;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RemoveDownloadFilesTask implements Runnable {

    private Log log = LogFactory.getLog(RemoveDownloadFilesTask.class);
    private String downloadExportFolder;
    private int fileHoursToExpire;

    public RemoveDownloadFilesTask(String downloadExportFolder, int fileHoursToExpire) {
        this.downloadExportFolder = downloadExportFolder;
        this.fileHoursToExpire = fileHoursToExpire;
    }

   public void run() {
        
        try {
            //TODO:  make it work...
            File downloadExportFolderObj = new File(downloadExportFolder);
            
            //get user folders under main download folder
            String[] userFolders = downloadExportFolderObj.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    //only get folders
                    return new File(dir, name).isDirectory();
                }
            });
            for (String userFolder : userFolders) {
                File userFolderObj = new File(downloadExportFolderObj.getAbsolutePath() + File.separatorChar + userFolder);
                /*String[] userFiles = */userFolderObj.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        //only get files, not directories (really which there shouldn't be any subdirs here)
                        File userFile = new File(dir, name);
                        if (userFile.isFile() && userFile.lastModified() <= (new Date().getTime() - fileHoursToExpire * 60 * 60 * 1000))
                            userFile.delete();
                       return true;
                    }
                });
            }

            
        } catch (Exception e) {
            //Important, suppress all errors so process doesn't kill anything...
            logError("Failed to remvoe file downloads", e);
        } finally {
            //
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }
    
    public static void main(String[] args) {
        File file = new File("C:/Apache_Software_Foundation/Tomcat_7.0/webapps");
            //TODO:  make it work...
            File downloadExportFolderObj = new File("C:/Apache_Software_Foundation/Tomcat_7.0/webapps");
            
            String[] userFolders = downloadExportFolderObj.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
            System.out.println(Arrays.toString(userFolders));
            for (String userFolder : userFolders) {
                File userFolderObj = new File(downloadExportFolderObj.getAbsolutePath() + File.separatorChar + userFolder);
                String[] userFiles = userFolderObj.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return !new File(dir, name).isDirectory();
                    }
                });
                System.out.println(Arrays.toString(userFiles));
            }
    }
    
}
