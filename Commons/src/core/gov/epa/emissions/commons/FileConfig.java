package gov.epa.emissions.commons;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

public abstract class FileConfig implements Config {

    protected PropertiesConfiguration config;

    private File file;

    public FileConfig(String file) throws ConfigurationException {
        this.file = new File(file);
//        config = new CompositeConfiguration();
//        config.addConfiguration(new SystemConfiguration());

        try {
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
            	    new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
            	    .configure(new Parameters().properties()
            	        .setFile(this.file)
            	        .setThrowExceptionOnMissing(true)
//            	        .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
            	        .setIncludesAllowed(false));
            	this.config = builder.getConfiguration();
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
