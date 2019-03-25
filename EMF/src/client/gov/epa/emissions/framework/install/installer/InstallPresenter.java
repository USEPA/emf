package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class InstallPresenter {
    private InstallView view;

    private Download model;

    private CheckUpdatedFiles checkUpdates;

    public InstallPresenter() {
        this.model = new Download();
        model.addObserver(this);
        this.checkUpdates = new CheckUpdatedFiles();
        checkUpdates.addObserver(this);
    }

    public void doCancel() {
        view.close();
    }

    public void display(InstallView view) {
        this.view = view;
        this.view.observe(this);

        this.view.display();
    }

    public void startDownload() {
        model.start();
    }

    public void initModels(String url, String filelist, String installhome) throws InstallException {
        model.initialize(url, filelist, installhome);
        checkUpdates.initialize(installhome, model);
    }

    public String[] checkUpdates() {
        return checkUpdates.getNewFilesName();
    }
    
    public int getNumOfOutDateFiles() {
        return checkUpdates.getNumOfOutDateFiles();
    }
    
    public List<String> getOutDateFiles() {
        return checkUpdates.getOutDateFiles();
    }    

    public void downloadUpdates() {
        checkUpdates.download();
        //checkUpdates.deleteOutOfDateFiles();
    }
    
    public void deleteOutOfDateFiles() {
        checkUpdates.deleteOutOfDateFiles();
    }

    public void stopDownload() {
        model.stopDownload();
    }

    public void writePreference(String website, String input, String output, String javahome, String rhome, String emfhome,
            String tmpDir, String server) {
        try {
            Tools.writePreference(website, input, output, javahome, rhome, emfhome, tmpDir, server);
        } catch (Exception e) {
            view.displayErr("Creating EMF client preference file failed.");
        }
    }

    public void setStatus(String status) {
        view.setStatus(status);
    }

    public void setCursor(Cursor cursor) {
        view.setCursor(cursor);
    }

    public void displayErr(String err) {
        view.displayErr(err);
    }

    public void setFinish() {
        view.setFinish();
    }

    public void createBatchFile(String filename, String preference, String javahome, String rhome, String server) {
        try {
            new ClientBatchFile(filename).create(preference, javahome, rhome, server);
        } catch (Exception e) {
            view.displayErr("Creating EMF client batch file failed.");
        }
    }

    public void createShortcut() {
        model.createShortcut();
    }
    
    public InstallPreferences getUserPreference() throws Exception {
        File userPreference = new File(Constants.USER_HOME, Constants.EMF_PREFERENCES_FILE);
        
        if (userPreference.exists())
            return new InstallPreferences(userPreference);
        
        InputStream templateInputStream = this.getClass().getResource("/" + Constants.INSTALLER_PREFERENCES_FILE).openStream();
        
        return new InstallPreferences(templateInputStream);
    }
    
    public boolean windowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
