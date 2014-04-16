package gov.epa.emissions.commons;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public abstract class FileConfig implements Config {

    protected CompositeConfiguration config;

    private File file;

    public FileConfig(String file) throws ConfigurationException {
        this.file = new File(file);
        config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());

        try {
            PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(file);
            config.addConfiguration(propertiesConfig);
        } catch (ConfigurationException e) {
            System.err.println("Please ensure that your configuration is defined in " + file
                    + ". If not present, copy the TEMPLATE-" + file + " as " + file
                    + ", customize (as needed), and rerun the test");
            throw e;
        }
    }

    public String value(String name) {
        return (String) config.getProperty(name);
    }

    public Properties properties() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException("could not load from file: " + file.getAbsolutePath());
        }

        return props;
    }
}
