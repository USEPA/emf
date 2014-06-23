package gov.epa.emissions.framework.client.preference;

import gov.epa.emissions.framework.services.EmfException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultUserPreferences implements UserPreference {

    private static final String DEFAULT_INPUT_FOLDER = "server.import.folder";

    private static final String DEFAULT_OUTPUT_FOLDER = "server.export.folder";

    private static final String DEFAULT_USER_NAME = "user.name";
    
    private static final String DEFAULT_USER_PASSWORD = "user.password";
    
    private static final String LOCAL_TEMP_DIR = "local.temp.dir";

    private static final String REMOTE_HOST = "remote.host";
    
    private static final String SORT_FILTER_PAGE_SIZE = "table.page.size";

    private static Log log = LogFactory.getLog(DefaultUserPreferences.class);

    public static final String EMF_PREFERENCE = "USER_PREFERENCES";

    private CommentedProperties props;

    public DefaultUserPreferences() throws EmfException {
        props = new CommentedProperties();
        try {
            FileInputStream inStream = new FileInputStream(getFile());
            props.load(inStream);
        } catch (Exception e) {
            log.error("Cannot load user preferences file " + getFile().getAbsolutePath(), e);
            throw new EmfException("Cannot load user preferences file "+getFile().getAbsolutePath());
        }
    }

    //For testign purpose
    public DefaultUserPreferences(String prefFile) throws EmfException {
        props = new CommentedProperties();
        File file = new File(prefFile);
        try {
            FileInputStream inStream = new FileInputStream(file);
            props.load(inStream);
        } catch (Exception e) {
            log.error("Cannot load user preferences file " + file.getAbsolutePath(), e);
            throw new EmfException("Cannot load user preferences file "+ file.getAbsolutePath());
        }
    }

    public DefaultUserPreferences(CommentedProperties props) {
        this.props = props;
    }

    public File getFile() {
        String property = System.getProperty(EMF_PREFERENCE);
   
        if (property != null && new File(property).exists())
            return new File(property);
 
        return new File(System.getProperty("user.home"),
                gov.epa.emissions.framework.install.installer.Constants.EMF_PREFERENCES_FILE);
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public String property(String name) {
        return props.getProperty(name);
    }

    public void setPreference(String preferenceKey, String preferenceValue) throws IOException {
        //if already exists, overwrite
        props.add(preferenceKey, preferenceValue);
//        outStream.
        //props.
//        outStream.write(preferenceKey + "=" + preferenceValue + "\n");
        FileOutputStream outStream = new FileOutputStream(getFile());

        props.store(outStream, null);
    }

    public String inputFolder() {
        return property(DEFAULT_INPUT_FOLDER);
    }

    public String outputFolder() {
        return property(DEFAULT_OUTPUT_FOLDER);
    }

    public String userName() {
        return property(DEFAULT_USER_NAME);
    }

    public String userPassword() {
        return property(DEFAULT_USER_PASSWORD);
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
    
 }
