package gov.epa.emissions.framework.client.download;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.io.File;

public class FileDownloadPresenter implements RefreshObserver {

    private DataCommonsService service;

    private FileDownloadView view;

    private User user;

    private FileDownloadMonitor monitor;

    private TaskRunner runner;

    private FileDownload[] fileDownloads = new FileDownload[] {};
                 
    public FileDownloadPresenter(User user, DataCommonsService servoce, TaskRunner runner) {
        this.user = user;
        this.service = servoce;
        this.runner = runner;

        this.monitor = new FileDownloadMonitor();
    }

    public void stop() {
        this.runner.stop();
    }

    public class FileDownloadMonitor implements Runnable {
        public void run() {
            doRefresh();
        }
    }

    public void display(FileDownloadView view) {
        this.view = view;
        view.observe(this);
        try {
            fileDownloads = service.getFileDownloads(user.getId());
            service.markFileDownloadsRead(buildFileDownloadIdList(fileDownloads));
            String downloadFolder = getDownloadFolder();
            //see if the file has already been downloaded
            for (FileDownload fileDownload : fileDownloads) {
                File file = new File(downloadFolder + "/" + fileDownload.getFileName());
                if (file.exists() && file.length() == fileDownload.getSize()) 
                    fileDownload.setProgress(100);
            }
            
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            view.notifyError(e.getMessage());
        }
        view.update(fileDownloads);
        view.display();

        runner.start(monitor);
    }

    private Integer[] buildFileDownloadIdList(FileDownload[] fileDownloads) {
        Integer[] fileDownloadIds = new Integer[fileDownloads.length];
        for (int i = 0; i < fileDownloads.length; i++) {
            fileDownloadIds[i] = fileDownloads[i].getId();
        }
        return fileDownloadIds;
    }
    
    public String getDownloadFolder() throws EmfException {
        String separator = File.separator; 
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();
        
        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDirFile.getAbsolutePath();
    }
    
    public void doRefresh() {
        try {
            fileDownloads = service.getUnreadFileDownloads(user.getId());
//            service.markFileDownloadsRead(buildFileDownloadIdList(fileDownloads));
            view.update(fileDownloads);
        } catch (EmfException e) {
            view.notifyError(e.getMessage());
        }
    }

    public void markFileDownloadRead(FileDownload fileDownload) throws EmfException {
        service.markFileDownloadsRead(new Integer[] {fileDownload.getId() });
    }

    public void doClear() {
        try {
            service.removeFileDownloads(user.getId());
            view.clear();
        } catch (EmfException e) {
            view.notifyError(e.getMessage());
        }
    }

    public void close() {
        runner.stop();
    }

}
