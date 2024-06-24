package gov.epa.emissions.framework.install.installer;


public class Constants {
    public static final String VERSION = "6/24/2024";
    public static final String SEPARATOR = System.getProperty("line.separator");
    public static final String WORK_DIR = System.getProperty("user.dir");
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    public static final String TIME_FORMAT = "MM/dd/yyyy hh:mmaaa";
    public static final String EMF_URL = "https://www.cmascenter.org/emf/install/";
    public static final String SERVER_ADDRESS = "https://sage.hesc.epa.gov:8443/emf/services";
    public static final String CMD_ARGUMENTS = "-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT";
    
    public static final String INSTALLER_PREFERENCES_FILE = "EMFPrefsTemplate.txt";
    public static final String EMF_PREFERENCES_FILE = "EMFPrefs.txt";
    public static final String FILE_LIST = "files.txt";
    public static final String UPDATE_FILE = "update.dat";
    public static final String EMF_BATCH_FILE = "EMFClient.bat";
    public static final String CLIENT_JAR_FILE = WORK_DIR + "/deploy/client/emf-client.jar";
    public static final String REFERENCE_PATH = WORK_DIR + "/config/ref/delimited";
    public static final String PREFERENCE_PATH = WORK_DIR + "/config/preferences";
    public static final String EMF_ICON = "/config/preferences/logo.ico";
    
    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String UNINSTALL_DESKTOP = USER_HOME + "/Desktop/EMF Client Installer.lnk";
    public static final String UNINSTALL_START = USER_HOME + "/Start Menu/Programs/EMF Client Installer";
    
    public static final String EMF_INSTALL_MESSAGE = "<html> <br><br><br><br>" +
            "Installing the Emissions Modeling Framework...";  
    
    public static final String EMF_REINSTALL_MESSAGE = "<html> <br><br><br><br>" +
    "Re-installing the Emissions Modeling Framework...";  
    
    public static final String EMF_UPDATE_MESSAGE = "<html> <br><br><br><br>" +
    "Updating the Emissions Modeling Framework...";  
    
    public static final String INSTALL_CLOSE_MESSAGE = "<html><br><br><br><br>" +
            "Installation complete.";
    
    public static final String REINSTALL_CLOSE_MESSAGE = "<html><br><br><br><br>" +
    "Re-installation complete.";
    
    public static final String UPDATE_CLOSE_MESSAGE = "<html><br><br><br><br>" +
    "Update complete.";
    
}
