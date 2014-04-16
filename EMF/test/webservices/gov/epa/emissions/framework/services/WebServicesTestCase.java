package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public abstract class WebServicesTestCase extends ServiceTestCase {

    protected Properties config() throws Exception {
        File conf = configFile();

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + conf.getParent() + "), name it " + conf.getName() + ", configure "
                    + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        return properties;
    }

    protected File configFile() {
        String folder = "test";
        return new File(folder, "webservices.conf");
    }

    abstract protected void doTearDown() throws Exception;

    protected ServiceLocator serviceLocator() throws Exception {
        Properties config = config();
        String baseUrl = config.getProperty("emf.services.url");
        return new RemoteServiceLocator(baseUrl);
    }

}
