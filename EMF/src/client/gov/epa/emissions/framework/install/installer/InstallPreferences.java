package gov.epa.emissions.framework.install.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import gov.epa.emissions.framework.client.preference.CommentedProperties;

public class InstallPreferences {

    private static final String DEFAULT_INPUT_FOLDER = "server.import.folder";

    private static final String DEFAULT_OUTPUT_FOLDER = "server.export.folder";

    private static final String EMF_INSTALL_FOLDER = "emf.install.folder";

    private static final String R_HOME = "r.home";

    private static final String EMF_SERVER_ADDRESS = "server.address";

    private static final String WEB_SITE = "web.site";
    
    private static final String LOCAL_TEMP_DIR = "local.temp.dir";

    private static final String REMOTE_HOST = "remote.host";
    
    private static final String SORT_FILTER_PAGE_SIZE = "table.page.size";
    
    private static final String SIGNIFICANT_DIGITS = "format.double.significant_digits";
    
    private static final String DECIMAL_PLACES = "format.double.decimal_places";
    
    private static final String DOUBLE_OPTION = "format.double.option";

    public static final String EMF_PREFERENCE = "USER_PREFERENCES";

    private CommentedProperties props;

    public InstallPreferences() throws Exception {
        props = new CommentedProperties();
        FileInputStream inStream = null;
        
        try {
            inStream = new FileInputStream(getFile());
            props.load(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot load user preferences file");
        } finally {
            if (inStream != null)
                inStream.close();
        }
    }

    public InstallPreferences(File prefFile) throws Exception {
        props = new CommentedProperties();
        FileInputStream inStream = null;
        
        try {
            inStream = new FileInputStream(prefFile);
            props.load(inStream);
        } catch (FileNotFoundException e) {
            throw new Exception("Cannot find the user preference file: " + prefFile.getAbsolutePath());
        } catch (Exception e) {
            throw new Exception("User preferences file has invalid value(s):\n" + e.getMessage());
        } finally {
            if (inStream != null)
                inStream.close();
        }
    }
    
    public InstallPreferences(InputStream inStream) throws Exception {
        props = new CommentedProperties();
        try {
            props.load(inStream);
        } catch (FileNotFoundException e) {
            throw new Exception("Cannot find the user preference file.");
        } catch (Exception e) {
            throw new Exception("User preferences file has invalid value(s):\n" + e.getMessage());
        } finally {
            if (inStream != null)
                inStream.close();
        }
    }

    private File getFile() {
        String property = System.getProperty(EMF_PREFERENCE);
        if (property != null && new File(property).exists())
            return new File(property);

        return new File(System.getProperty("user.home"), Constants.EMF_PREFERENCES_FILE);
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    private String property(String name) {
        return props.getProperty(name);
    }

    public String inputFolder() {
        return property(DEFAULT_INPUT_FOLDER);
    }

    public String outputFolder() {
        return property(DEFAULT_OUTPUT_FOLDER);
    }

    public String rHome() {
        return property(R_HOME);
    }

    public String emfInstallFolder() {
        return property(EMF_INSTALL_FOLDER);
    }

    public String emfWebSite() {
        return property(WEB_SITE);
    }

    public String emfServer() {
        return property(EMF_SERVER_ADDRESS);
    }
    
    public String localTempDir() {
        return property(LOCAL_TEMP_DIR);
    }
    
    public String remoteHost() {
        return property(REMOTE_HOST);
    }
    
    public String sortFilterPageSize() {
        return property(SORT_FILTER_PAGE_SIZE);
    }
    
    public String significantDigits() {
        return property(SIGNIFICANT_DIGITS);
    }
    
    public String decimalPlaces() {
        return property(DECIMAL_PLACES);
    }
    
    public String doubleOption() {
        return property(DOUBLE_OPTION);
    }

    public CommentedProperties props() {
        return props;
    }

    public void setPreference(String preferenceKey, String preferenceValue) {
        props.add(preferenceKey, preferenceValue);
    }

}
